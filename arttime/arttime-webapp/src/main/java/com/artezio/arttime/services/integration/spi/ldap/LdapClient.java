package com.artezio.arttime.services.integration.spi.ldap;

import com.artezio.arttime.config.ApplicationSettings;
import com.artezio.arttime.config.Settings;
import com.artezio.arttime.services.integration.spi.UserInfo;
import com.artezio.arttime.utils.StringUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;

import javax.inject.Inject;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import java.util.*;

public class LdapClient {

    static final String LDAP_CONTEXT_FACTORY_CLASS_NAME = "com.sun.jndi.ldap.LdapCtxFactory";

    private int searchScope = SearchControls.SUBTREE_SCOPE;
    private int searchTimeLimit = 10000;
    private Map<String, String> attributeMapping;
    private Settings settings;

    class Filter {
        private String expression;
        private Object[] args;

        private Filter() {
            super();
        }

        private Filter(String expression, Object[] filterArgs) {
            super();
            this.expression = expression;
            this.args = filterArgs;
        }

        public String getExpression() {
            return expression;
        }

        public void setExpression(String expression) {
            this.expression = expression;
        }

        public Object[] getArgs() {
            return args;
        }

        public void setArgs(Object[] filterArgs) {
            this.args = filterArgs;
        }
    }

    @Inject
    public LdapClient(@ApplicationSettings Settings settings) {
        this.settings = settings;
        this.attributeMapping = settings.getEmployeeMappingParams();
    }

    public List<UserInfo> listUsers() {
        Filter filter = new Filter(settings.getLdapEmployeeFilter(), new Object[]{});
        return listUsers(filter);
    }

    public List<UserInfo> listUsers(String groupCode) {
        Filter filter = new Filter(settings.getLdapGroupMemberFilter(), new Object[]{groupCode});
        return listUsers(filter);
    }

    public UserInfo findUser(String userName) {
        if (StringUtils.isNotBlank(settings.getLdapUserFilter())) {
            Filter filter = new Filter(settings.getLdapUserFilter(), new Object[]{userName});
            List<UserInfo> users = listUsers(filter);
            return (users.isEmpty())
                    ? null
                    : users.get(0);
        }
        return null;
    }

    public Set<String> listDepartments() {
        InitialLdapContext ctx = null;
        Filter filter = new Filter(settings.getLdapDepartmentFilter(), new Object[]{});

        Set<String> departments = new HashSet<>();
        try {
            ctx = initializeContext();
            SearchControls controls = makeSearchControls();
            controls.setReturningAttributes(new String[]{settings.getLdapDepartmentFilterDepartmentAttribute()});
            NamingEnumeration<SearchResult> answer = ctx.search(
                    settings.getLdapUserContextDN(), filter.getExpression(), filter.getArgs(),
                    controls);

            while (answer.hasMore()) {
                SearchResult sr = answer.next();
                Attributes attrs = sr.getAttributes();
                String department = parseAttribute(attrs.get(settings.getLdapDepartmentFilterDepartmentAttribute()));
                departments.add(WordUtils.capitalizeFully(department, new char[]{'-', ' '}));
            }

            answer.close();
        } catch (NamingException ex) {
            throw new RuntimeException("Error getting Departments ", ex);
        } finally {
            closeContext(ctx);
        }
        return departments;
    }

    protected List<UserInfo> listUsers(Filter filter) {
        List<UserInfo> users = new ArrayList<>();
        InitialLdapContext ctx = null;

        try {
            ctx = initializeContext();
            SearchControls controls = makeSearchControls();
            NamingEnumeration<SearchResult> answer = ctx.search(
                    settings.getLdapUserContextDN(), filter.getExpression(), filter.getArgs(),
                    controls);

            while (answer.hasMore()) {
                SearchResult sr = answer.next();
                Attributes attrs = sr.getAttributes();
                UserInfo user = createUserInfo(attrs);
                users.add(user);
            }
            answer.close();
        } catch (NamingException ex) {
            throw new RuntimeException("Error listing users ", ex);
        } finally {
            closeContext(ctx);
        }
        return users;
    }

    protected UserInfo createUserInfo(Attributes attrs) throws NamingException {
        String userName = parseAttribute(attrs.get(settings.getLdapUserNameAttribute()));
        String firstName = parseAttribute(attrs.get(settings.getLdapFirstNameAttribute()));
        String lastName = parseAttribute(attrs.get(settings.getLdapLastNameAttribute()));
        String email = parseAttribute(attrs.get(settings.getLdapMailAttribute()));
        String department = parseAttribute(attrs.get(settings.getLdapDepartmentAttribute()));
        return new UserInfo(userName, firstName, lastName, email, department);
    }

    protected String parseAttribute(Attribute attribute) throws NamingException {
        StringBuilder attrValue = new StringBuilder();
        if (attribute != null) {
            for (int i = 0; i < attribute.size(); i++) {
                attrValue.append(attribute.get(i).toString());
            }
        }
        return attrValue.toString();
    }

    private void closeContext(InitialLdapContext ctx) {
        if (ctx != null) {
            try {
                ctx.close();
            } catch (NamingException e) {
                e.printStackTrace();
            }
        }
    }

    protected SearchControls makeSearchControls() {
        SearchControls controls = new SearchControls();
        controls.setSearchScope(searchScope);
        controls.setReturningAttributes(makeReturningAttr());
        controls.setTimeLimit(getSearchTimeLimit());
        return controls;
    }

    private String[] makeReturningAttr() {
        Set<String> keySet = attributeMapping.keySet();
        return keySet.toArray(new String[keySet.size()]);
    }

    protected InitialLdapContext initializeContext() throws NamingException {
        return initializeContext(getLdapAccessPrincipal(), settings.getLdapBindCredentials());
    }

    protected InitialLdapContext initializeContext(String principal, String credentials) throws NamingException {
        Properties env = new Properties();

        env.setProperty(Context.INITIAL_CONTEXT_FACTORY, LDAP_CONTEXT_FACTORY_CLASS_NAME);
        env.setProperty(Context.SECURITY_AUTHENTICATION, "simple");

        String providerUrl = String.format("ldap://%s:%d", settings.getLdapServerHost(), settings.getLdapServerPort());
        env.setProperty(Context.PROVIDER_URL, providerUrl);

        env.setProperty(Context.SECURITY_PRINCIPAL, principal);
        env.setProperty(Context.SECURITY_CREDENTIALS, credentials);

        InitialLdapContext ctx = new InitialLdapContext(env, null);
        return ctx;
    }

    int getSearchTimeLimit() {
        return searchTimeLimit;
    }

    String getLdapAccessPrincipal() {
        String suffix = settings.getLdapPrincipalSuffix();
        return settings.getLdapPrincipalUsername() + (suffix != null ? suffix : "");
    }

}

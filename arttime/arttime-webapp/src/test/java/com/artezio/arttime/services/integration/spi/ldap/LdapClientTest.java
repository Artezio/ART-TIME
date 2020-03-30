package com.artezio.arttime.services.integration.spi.ldap;

import static junitx.util.PrivateAccessor.setField;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.times;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.replay;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;

import org.apache.commons.lang.WordUtils;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.artezio.arttime.config.Setting;
import com.artezio.arttime.config.Settings;
import com.artezio.arttime.datamodel.Employee;
import com.artezio.arttime.services.integration.spi.UserInfo;

@RunWith(PowerMockRunner.class)
@PrepareForTest({InitialLdapContext.class, LdapClient.class})
public class LdapClientTest {

    private final static String USERNAME_ATTRIBUTE = "unameAttrib";
    private final static String LASTNAME_ATTRIBUTE = "lastnameAttrib";
    private final static String FIRSTNAME_ATTRIBUTE = "firstnameAttrib";
    private final static String MAIL_ATTRIBUTE = "mailAttrib";
    private final static String DEPARTMENT_ATTRIBUTE = "deptAttribute";

    private LdapClient ldapClient = new LdapClient(new Settings(new EnumMap<>(Setting.Name.class)));
    private Settings settings;

    @Before
    public void setUp() throws NoSuchFieldException {
        settings = new Settings(new HashMap<>());
        setField(ldapClient, "settings", settings);
    }

    @Test
    public void testListUsers() throws Exception {
        final String userContextDN = "user-cdn";
        final String filterExpression = "filter-expr";
        final Object[] filterArgs = new Object[]{};
        LdapClient.Filter mockFilter = createMock(LdapClient.Filter.class);
        expect(mockFilter.getExpression()).andReturn(filterExpression).anyTimes();
        expect(mockFilter.getArgs()).andReturn(filterArgs).anyTimes();
        SearchControls mockControls = createMock(SearchControls.class);
        InitialLdapContext mockContext = createMock(InitialLdapContext.class);
        ldapClient = createMockBuilder(LdapClient.class)
                .addMockedMethod("initializeContext", new Class[]{})
                .addMockedMethod("makeSearchControls")
                .createMock();
        settings.setLdapUserContextDN(userContextDN);
        setField(ldapClient, "settings", settings);
        setAttributeMapping();
        NamingEnumeration enumeration = createMock(NamingEnumeration.class);
        SearchResult result1 = createMock(SearchResult.class);
        SearchResult result2 = createMock(SearchResult.class);
        expect(enumeration.hasMore()).andReturn(true).times(2);
        expect(enumeration.hasMore()).andReturn(false).once();
        expect(enumeration.next()).andReturn(result1).once();
        expect(enumeration.next()).andReturn(result2).once();
        enumeration.close();
        expectLastCall().andVoid();
        Employee expectedEmployee1 = new Employee("uname1", "fname1", "lname1", "email1", "dep1");
        Employee expectedEmployee2 = new Employee("uname2", "fname2", "lname2", "email2", "dep2");
        expect(result1.getAttributes()).andReturn(getAttributesFor(expectedEmployee1));
        expect(result2.getAttributes()).andReturn(getAttributesFor(expectedEmployee2));
        expect(ldapClient.makeSearchControls()).andReturn(mockControls);
        expect(ldapClient.initializeContext()).andReturn(mockContext);
        mockContext.close();
        expectLastCall().once().andVoid();
        expect(mockContext.search(eq(userContextDN), eq(filterExpression), eq(filterArgs), eq(mockControls)))
                .andReturn(enumeration);
        replay(ldapClient, mockContext, mockControls, mockFilter, enumeration, result1, result2);

        List<UserInfo> actualList = ldapClient.listUsers(mockFilter);

        assertEquals(2, actualList.size());
        UserInfo actualUser1 = actualList.stream().filter(e -> e.getUsername().equals(expectedEmployee1.getUserName())).findFirst().get();
        UserInfo actualUser2 = actualList.stream().filter(e -> e.getUsername().equals(expectedEmployee2.getUserName())).findFirst().get();
        assertTrue(isEmployeeFullyEqualToUserInfo(expectedEmployee1, actualUser1));
        assertTrue(isEmployeeFullyEqualToUserInfo(expectedEmployee2, actualUser2));
    }

    @Test
    public void testGetDepartments() throws Exception {
        final String userContextDN = "user-cdn";
        final String filterExpression = "filter-expr";
        final Object[] filterArgs = new Object[]{};
        LdapClient.Filter mockFilter = createMock(LdapClient.Filter.class);
        PowerMock.expectNew(LdapClient.Filter.class, settings.getLdapDepartmentFilter(), new Object[]{}).andReturn(mockFilter);
        expect(mockFilter.getExpression()).andReturn(filterExpression).anyTimes();
        expect(mockFilter.getArgs()).andReturn(filterArgs).anyTimes();
        SearchControls mockControls = createMock(SearchControls.class);
        InitialLdapContext mockContext = createMock(InitialLdapContext.class);
        ldapClient = createMockBuilder(LdapClient.class)
                .addMockedMethod("initializeContext", new Class[]{})
                .addMockedMethod("makeSearchControls")
                .createMock();
        setField(ldapClient, "settings", settings);
        settings.setLdapUserContextDN(userContextDN);
        setAttributeMapping();
        settings.setLdapDepartmentFilterDepartmentAttribute(DEPARTMENT_ATTRIBUTE);
        NamingEnumeration enumeration = createMock(NamingEnumeration.class);
        SearchResult result1 = createMock(SearchResult.class);
        SearchResult result2 = createMock(SearchResult.class);
        SearchResult result3 = createMock(SearchResult.class);
        expect(enumeration.hasMore()).andReturn(true).times(3);
        expect(enumeration.hasMore()).andReturn(false).once();
        expect(enumeration.next()).andReturn(result1).once();
        expect(enumeration.next()).andReturn(result2).once();
        expect(enumeration.next()).andReturn(result3).once();
        enumeration.close();
        expectLastCall().andVoid();
        String expectedDepartment1 = "dep1";
        String expectedDepartment2 = "dep2";
        expect(result1.getAttributes()).andReturn(getAttributesFor(expectedDepartment1));
        expect(result2.getAttributes()).andReturn(getAttributesFor(expectedDepartment2));
        expect(result3.getAttributes()).andReturn(getAttributesFor(expectedDepartment2));
        expect(ldapClient.makeSearchControls()).andReturn(mockControls);
        mockControls.setReturningAttributes(aryEq(new String[]{DEPARTMENT_ATTRIBUTE}));
        expectLastCall().andVoid();
        expect(ldapClient.initializeContext()).andReturn(mockContext);
        mockContext.close();
        expectLastCall().once().andVoid();
        expect(mockContext.search(eq(userContextDN), eq(filterExpression), eq(filterArgs), eq(mockControls)))
                .andReturn(enumeration);
        replay(LdapClient.Filter.class, ldapClient, mockContext, mockControls, mockFilter, enumeration, result1, result2, result3);

        Set<String> actualSet = ldapClient.listDepartments();

        assertEquals(2, actualSet.size());
        assertTrue(actualSet.contains(WordUtils.capitalizeFully(expectedDepartment1, new char[]{'-', ' '})));
        assertTrue(actualSet.contains(WordUtils.capitalizeFully(expectedDepartment2, new char[]{'-', ' '})));
    }

    private boolean isEmployeesFullyEqual(Employee employee1, Employee employee2) {
        return employee1.getUserName().equals(employee2.getUserName())
                && employee1.getDepartment().equalsIgnoreCase(employee2.getDepartment())
                && employee1.getEmail().equals(employee2.getEmail())
                && employee1.getFirstName().equals(employee2.getFirstName())
                && employee1.getLastName().equals(employee2.getLastName());
    }

    private boolean isEmployeeFullyEqualToUserInfo(Employee employee, UserInfo userInfo) {
        return employee.getUserName().equals(userInfo.getUsername())
                && employee.getDepartment().equalsIgnoreCase(userInfo.getDepartment())
                && employee.getEmail().equals(userInfo.getEmail())
                && employee.getFirstName().equals(userInfo.getFirstName())
                && employee.getLastName().equals(userInfo.getLastName());
    }

    private Attributes getAttributesFor(Employee e) {
        Attributes attributes = new BasicAttributes();
        attributes.put(USERNAME_ATTRIBUTE, e.getUserName());
        attributes.put(FIRSTNAME_ATTRIBUTE, e.getFirstName());
        attributes.put(LASTNAME_ATTRIBUTE, e.getLastName());
        attributes.put(MAIL_ATTRIBUTE, e.getEmail());
        attributes.put(DEPARTMENT_ATTRIBUTE, e.getDepartment());
        attributes.put("RandomAttribute", "Random Value");
        attributes.put("RandomAttribute2", "Random Value 2");
        return attributes;
    }

    private Attributes getAttributesFor(String department) {
        Attributes attributes = new BasicAttributes();
        attributes.put(DEPARTMENT_ATTRIBUTE, department);
        attributes.put("RandomAttribute", "Random Value");
        attributes.put("RandomAttribute2", "Random Value 2");
        return attributes;
    }

    private void setAttributeMapping() {
        settings.setLdapUserNameAttribute(USERNAME_ATTRIBUTE);
        settings.setLdapFirstNameAttribute(FIRSTNAME_ATTRIBUTE);
        settings.setLdapLastNameAttribute(LASTNAME_ATTRIBUTE);
        settings.setLdapMailAttribute(MAIL_ATTRIBUTE);
        settings.setLdapDepartmentAttribute(DEPARTMENT_ATTRIBUTE);
    }

    @Test
    public void testGetLdapAccessPrincipal_principalSuffixNull() {
        String principalUsername = "uname";
        settings.setLdapPrincipalSuffix(null);
        settings.setLdapPrincipalUsername(principalUsername);

        String actual = ldapClient.getLdapAccessPrincipal();

        assertEquals(principalUsername, actual);
    }

    @Test
    public void testGetLdapAccessPrincipal_principalSuffixNotNull() {
        String principalUsername = "uname";
        String principalSuffix = "suffix";
        String expected = principalUsername + principalSuffix;
        settings.setLdapPrincipalSuffix(principalSuffix);
        settings.setLdapPrincipalUsername(principalUsername);

        String actual = ldapClient.getLdapAccessPrincipal();

        assertEquals(expected, actual);
    }

    @Test
    public void testInitializeContext() throws Exception {
        final String expectedInitialFactoryClassName = LdapClient.LDAP_CONTEXT_FACTORY_CLASS_NAME;
        final String securityAuthentication = "simple";
        final String serverHost = "some_host";
        final int serverPort = 2534;
        final String principal = "princ";
        final String credentials = "cred";
        final String expectedProviderUrl = String.format("ldap://%s:%d", serverHost, serverPort);
        settings.setLdapServerHost(serverHost);
        settings.setLdapServerPort(serverPort);
        InitialLdapContext initialContextMock = PowerMockito.mock(InitialLdapContext.class);
    
        Capture<Hashtable> capture = Capture.newInstance();
    
        PowerMockito.whenNew(InitialLdapContext.class)
                .withAnyArguments()
                .thenAnswer(invocation -> {
                    capture.setValue(invocation.getArgument(0));
                    return initialContextMock;
                });
        
        ldapClient.initializeContext(principal, credentials);
    
        Hashtable properties = capture.getValue();
        PowerMockito.verifyNew(InitialLdapContext.class, times(1)).withArguments(properties, null);
    
        assertTrue(properties.containsKey(Context.SECURITY_AUTHENTICATION));
        assertTrue(properties.containsKey(Context.INITIAL_CONTEXT_FACTORY));
        assertTrue(properties.containsKey(Context.PROVIDER_URL));
        assertTrue(properties.containsKey(Context.SECURITY_PRINCIPAL));
        assertTrue(properties.containsKey(Context.SECURITY_CREDENTIALS));
        String actualProviderUrl = (String) properties.get(Context.PROVIDER_URL);
        String actualContextFactoryClassName = (String) properties.get(Context.INITIAL_CONTEXT_FACTORY);
        String actualPrincipal = (String) properties.get(Context.SECURITY_PRINCIPAL);
        String actualCredentials = (String) properties.get(Context.SECURITY_CREDENTIALS);
        String actualAuthentication = (String) properties.get(Context.SECURITY_AUTHENTICATION);
        assertEquals(expectedProviderUrl, actualProviderUrl);
        assertEquals(expectedInitialFactoryClassName, actualContextFactoryClassName);
        assertEquals(principal, actualPrincipal);
        assertEquals(credentials, actualCredentials);
        assertEquals(securityAuthentication, actualAuthentication);
    }


    @Test
    public void testFindEmployee() throws NoSuchFieldException {
        ldapClient = createMockBuilder(LdapClient.class)
                .addMockedMethod("listUsers", LdapClient.Filter.class)
                .createMock();
        settings.setLdapUserFilter("LDAP-FILTER");
        setField(ldapClient, "settings", settings);
        List<UserInfo> users = Arrays.asList(
                new UserInfo("uname1", "firstname", "lastname", "email", "department"));
        expect(ldapClient.listUsers(anyObject(LdapClient.Filter.class))).andReturn(users).anyTimes();
        replay(ldapClient);

        UserInfo actual = ldapClient.findUser("uname1");
        assertNotNull(actual);
        assertEquals("uname1", actual.getUsername());
    }

    @Test
    public void testFindUser_userFilterIsNull() throws NoSuchFieldException {
        settings.setLdapUserFilter(null);
        UserInfo actual = ldapClient.findUser("uname1");
        assertNull(actual);
    }

    @Test
    public void testFindUser_userFilterIsEmpty() throws NoSuchFieldException {
        settings.setLdapUserFilter("");
        UserInfo actual = ldapClient.findUser("uname1");
        assertNull(actual);
    }

    @Test
    public void testFindUser_noResults() throws NoSuchFieldException {
        ldapClient = createMockBuilder(LdapClient.class)
                .addMockedMethod("listUsers", LdapClient.Filter.class)
                .createMock();
        setField(ldapClient, "settings", settings);
        settings.setLdapUserFilter("LDAP-FILTER");
        expect(ldapClient.listUsers(anyObject(LdapClient.Filter.class))).andReturn(Collections.emptyList()).anyTimes();
        replay(ldapClient);

        UserInfo actual = ldapClient.findUser("uname1");
        assertNull(actual);
    }

    @Test
    public void testListUsersByGroupCode() throws NoSuchFieldException {
        ldapClient = createMockBuilder(LdapClient.class)
                .addMockedMethod("listUsers", LdapClient.Filter.class)
                .createMock();
        setField(ldapClient, "settings", settings);
        String codes = "code1";
        UserInfo memberOfTeam1 = new UserInfo("uname1", null, null, null, null);
        UserInfo memberOfTeam1and2 = new UserInfo("uname1", null, null, null, null);
        List<UserInfo> team1users = Arrays.asList(memberOfTeam1, memberOfTeam1and2);
        expect(ldapClient.listUsers(anyObject(LdapClient.Filter.class))).andAnswer(
                () -> {
                    LdapClient.Filter filter = (LdapClient.Filter) EasyMock.getCurrentArguments()[0];
                    switch ((String) filter.getArgs()[0]) {
                        case "code1":
                            return team1users;
                        default:
                            return null;
                    }
                }
        ).anyTimes();
        replay(ldapClient);
        List<UserInfo> actual = ldapClient.listUsers(codes);
        assertEquals(2, actual.size());
        assertTrue(actual.contains(memberOfTeam1));
        assertTrue(actual.contains(memberOfTeam1and2));
    }

}

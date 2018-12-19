package com.artezio.arttime.services.integration.spi.ldap;

import com.artezio.arttime.config.Settings;
import com.artezio.arttime.datamodel.Employee;
import com.artezio.arttime.datamodel.Project;
import com.artezio.arttime.datamodel.TeamFilter;
import com.artezio.arttime.markers.IntegrationTest;
import com.artezio.arttime.repositories.WorkdaysCalendarRepository;
import com.google.common.collect.Sets;
import org.apache.directory.server.annotations.CreateLdapServer;
import org.apache.directory.server.annotations.CreateTransport;
import org.apache.directory.server.core.annotations.ApplyLdifFiles;
import org.apache.directory.server.core.annotations.ContextEntry;
import org.apache.directory.server.core.annotations.CreateDS;
import org.apache.directory.server.core.annotations.CreatePartition;
import org.apache.directory.server.core.integ.AbstractLdapTestUnit;
import org.apache.directory.server.core.integ.FrameworkRunner;
import org.easymock.TestSubject;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static junit.framework.TestCase.assertTrue;
import static junitx.util.PrivateAccessor.setField;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(FrameworkRunner.class)
@Category(IntegrationTest.class)
@CreateDS(
        partitions = {
            @CreatePartition(
                name = "root",
                suffix = "dc=example, dc=com",
                contextEntry = @ContextEntry(
                    entryLdif =
                        "dn: dc=example, dc=com\n" +
                        "dc: example\n" +
                        "objectClass: domain\n" +
                        "objectClass: top\n\n"
        ))
})
@ApplyLdifFiles({
        "ldif/schema-extension.ldif",
        "ldif/base.ldif",
        "ldif/project-groups.ldif",
        "ldif/employees.ldif"})
@CreateLdapServer(
        transports = {@CreateTransport(protocol = "LDAP", port = 10389)},
        allowAnonymousAccess = true
)
public class LdapAdapterIntegrationTest extends AbstractLdapTestUnit {

    @TestSubject
    private LdapAdapter ldapAdapter = new LdapAdapter();
    private LdapClient ldapClient;
    private WorkdaysCalendarRepository workdaysCalendarRepository;

    @Before
    public void setUp() throws NoSuchFieldException {
        workdaysCalendarRepository = mock(WorkdaysCalendarRepository.class);
        ldapClient = createLdapClient();
        setField(ldapAdapter, "workdaysCalendarRepository", workdaysCalendarRepository);
        setField(ldapAdapter, "ldapClient", ldapClient);
    }

    @Test
    public void testFindEmployee() {
        when(workdaysCalendarRepository.findDefaultCalendar(any())).thenReturn(null);
        Employee employee = ldapAdapter.findEmployee("testuser1");
        assertNotNull(employee);
        assertEquals("testuser1", employee.getUserName());
    }

    @Test
    public void testFindEmployee_searchMustBeCaseInsensitive() {
        when(workdaysCalendarRepository.findDefaultCalendar(any())).thenReturn(null);
        Employee employee = ldapAdapter.findEmployee("TESTUSER1");
        assertNotNull(employee);
        assertEquals("testuser1", employee.getUserName());
    }

    @Test
    public void testGetTeamByGroupCode() {
        Project project = new Project();
        TeamFilter teamFilter = new TeamFilter(TeamFilter.Type.PROJECT_CODES, "projectGroup1");
        project.setTeamFilter(teamFilter);
        when(workdaysCalendarRepository.findDefaultCalendar(any())).thenReturn(null);

        List<Employee> team = ldapAdapter.getTeamByGroupCode(teamFilter.getValue());

        assertEquals(1, team.size());
        Employee employee = team.get(0);
        assertEquals(employee.getUserName(), "testuser1");
    }

    @Test
    public void testGetDepartments() {
        Project project = new Project();
        TeamFilter teamFilter = new TeamFilter(TeamFilter.Type.DISABLED, "");
        project.setTeamFilter(teamFilter);
        Set<String> expected = Sets.newHashSet("Department1", "Department2");

        Set<String> actual = ldapAdapter.getDepartments();

        assertEquals(expected, actual);
    }

    private LdapClient createLdapClient() {
        Settings settings = new Settings(new HashMap<>());
        settings.setLdapBindCredentials("");
        settings.setLdapPrincipalUsername("");
        settings.setLdapPrincipalSuffix("");
        settings.setLdapServerHost("localhost");
        settings.setLdapServerPort(10389);
        settings.setLdapMailAttribute("");
        settings.setLdapLastNameAttribute("sn");
        settings.setLdapFirstNameAttribute("givenName");
        settings.setLdapUserNameAttribute("sAMAccountName");
        settings.setLdapDepartmentAttribute("physicalDeliveryOfficeName");
        settings.setLdapUserContextDN("ou=users,dc=example,dc=com");
        settings.setLdapEmployeeFilter("(&(objectClass=person)(sn=*)(givenName=*))");
        settings.setLdapGroupMemberFilter("(memberOf=ou={0},ou=users,dc=example,dc=com)");
        settings.setLdapUserFilter("(&(objectclass=*)(sAMAccountName={0}))");
        settings.setLdapDepartmentFilterDepartmentAttribute("physicalDeliveryOfficeName");
        settings.setLdapDepartmentFilter("(&(physicalDeliveryOfficeName=*)(objectClass=person))");
        return new LdapClient(settings);
    }

}

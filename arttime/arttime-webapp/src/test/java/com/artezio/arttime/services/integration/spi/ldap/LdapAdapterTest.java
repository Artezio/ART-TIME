package com.artezio.arttime.services.integration.spi.ldap;

import com.artezio.arttime.datamodel.Employee;
import com.artezio.arttime.datamodel.Project;
import com.artezio.arttime.datamodel.TeamFilter;
import com.artezio.arttime.repositories.WorkdaysCalendarRepository;
import com.artezio.arttime.services.integration.spi.UserInfo;
import org.easymock.EasyMock;
import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.easymock.EasyMock.*;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.*;
import static org.junit.Assert.assertTrue;
import static org.powermock.api.easymock.PowerMock.replay;

@RunWith(EasyMockRunner.class)
public class LdapAdapterTest {

    @TestSubject
    private LdapAdapter ldapAdapter = new LdapAdapter();
    @Mock
    private LdapClient ldapClient;
    @Mock
    private WorkdaysCalendarRepository workdaysCalendarRepository;

    private void assertContainsEmployeeOfUserInfo(Collection<Employee> employees, UserInfo userInfo) {
        assertTrue(employees.stream().anyMatch(e -> e.getUserName().equals(userInfo.getUsername())));
    }

    @Test
    public void testFindEmployeesByFullName() {
        String fullNameToSearch = "expected";
        Employee expectedEmployee1 = new Employee("user1", "expectedfname1", "lname1", "em1");
        Employee expectedEmployee2 = new Employee("user2", "someexpected", "lname2", "em2");
        Employee unexpectedEmployee = new Employee("user3", "fname3", "lname3", "em3");
        List<Employee> employees = Arrays.asList(expectedEmployee1, expectedEmployee2, unexpectedEmployee);
        ldapAdapter = createMockBuilder(LdapAdapter.class).addMockedMethod("getEmployees").createMock();
        expect(ldapAdapter.getEmployees()).andReturn(employees).anyTimes();
        replay(ldapAdapter);

        List<Employee> actual = ldapAdapter.findEmployeesByFullName(fullNameToSearch);

        assertEquals(2, actual.size());
        assertTrue(actual.contains(expectedEmployee1));
        assertTrue(actual.contains(expectedEmployee2));
    }

    @Test
    public void testGetEmployees() {
        UserInfo user1 = createUserInfo("b_user");
        UserInfo user2 = createUserInfo("a_user");
        expect(ldapClient.listUsers()).andReturn(Arrays.asList(user2, user1));
        expect(workdaysCalendarRepository.findDefaultCalendar(anyObject())).andReturn(null).anyTimes();
        replay(ldapClient, workdaysCalendarRepository);

        List<Employee> actual = ldapAdapter.getEmployees();

        verify(ldapClient);
        assertEquals(2, actual.size());
        assertEquals(user2.getUsername(), actual.get(0).getUserName());
        assertEquals(user1.getUsername(), actual.get(1).getUserName());
    }

    @Test
    public void testGetTeamByDepartment() {
        String department = "dep1";
        String dummyDepartment = "someOtherDepartment";
        UserInfo user1 = new UserInfo("a_user", "a", "a", null, department);
        UserInfo user2 = new UserInfo("b_user", "b", "b", null, dummyDepartment);
        UserInfo user3 = new UserInfo("c_user", "c", "c", null, department);
        List<UserInfo> users = Arrays.asList(user2, user3, user1);
        expect(ldapClient.listUsers()).andReturn(users);
        expect(workdaysCalendarRepository.findDefaultCalendar(anyObject())).andReturn(null).anyTimes();
        EasyMock.replay(ldapClient, workdaysCalendarRepository);

        List<Employee> actual = ldapAdapter.getTeamByDepartment(department);

        verify(ldapClient);
        assertEquals(2, actual.size());
        assertEquals(user1.getUsername(), actual.get(0).getUserName());
        assertEquals(user3.getUsername(), actual.get(1).getUserName());
    }

    private UserInfo createUserInfo(String username) {
        return new UserInfo(username, username, username, username, null);
    }

}

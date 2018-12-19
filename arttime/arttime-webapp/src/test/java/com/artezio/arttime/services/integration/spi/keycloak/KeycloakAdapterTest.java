package com.artezio.arttime.services.integration.spi.keycloak;

import com.artezio.arttime.datamodel.Employee;
import com.artezio.arttime.repositories.WorkdaysCalendarRepository;
import com.artezio.arttime.services.integration.spi.UserInfo;
import com.google.common.collect.Sets;
import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

@RunWith(EasyMockRunner.class)
public class KeycloakAdapterTest {

    @TestSubject
    private KeycloakAdapter keycloak = new KeycloakAdapter();
    @Mock
    private KeycloakClient keycloakClient;
    @Mock
    private WorkdaysCalendarRepository workdaysCalendarRepository;

    @Test
    public void testGetDepartments() {
        Set<String> departments = Sets.newHashSet("dep1", "dep2");
        List<String> expected = Arrays.asList("Dep1", "Dep2");
        expect(keycloakClient.listDepartments()).andReturn(departments);
        replay(keycloakClient);

        Collection<String> actual = keycloak.getDepartments();

        verify(keycloakClient);
        assertEquals(expected, actual);
    }

    @Test
    public void testGetEmployees() {
        UserInfo userInfo1 = new UserInfo("uname1", "a_fname", "a_lname", "em1", "dep1");
        UserInfo userInfo2 = new UserInfo("uname2", "b_fname", "b_lname", "em2", "dep2");
        UserInfo userInfo3 = new UserInfo("uname3", "c_fname", "a_lname", "em2", "dep2");
        expect(keycloakClient.listUsers()).andReturn(Arrays.asList(userInfo3, userInfo1, userInfo2));
        expect(workdaysCalendarRepository.findDefaultCalendar(anyString())).andReturn(null).times(3);
        replay(keycloakClient, workdaysCalendarRepository);

        List<Employee> actual = keycloak.getEmployees();

        verify(keycloakClient, workdaysCalendarRepository);
        assertEquals(3, actual.size());
        assertEquals("uname1", actual.get(0).getUserName());
        assertEquals("uname3", actual.get(1).getUserName());
        assertEquals("uname2", actual.get(2).getUserName());

    }

    @Test
    public void testFindEmployee() {
        UserInfo userInfo1 = new UserInfo("uname1", "a_fname", "a_lname", "em1", "dep1");
        UserInfo userInfo2 = new UserInfo("uname2", "b_fname", "b_lname", "em2", "dep2");
        UserInfo userInfo3 = new UserInfo("uname3", "c_fname", "a_lname", "em2", "dep2");
        expect(keycloakClient.listUsers()).andReturn(Arrays.asList(userInfo3, userInfo1, userInfo2));
        replay(keycloakClient);

        Employee actual = keycloak.findEmployee("uname2");

        verify(keycloakClient);
        assertNotNull(actual);
        assertEmployeeEqualsUserInfo(actual, userInfo2);
    }

    @Test
    public void testFindEmployee_noMatch() {
        UserInfo userInfo1 = new UserInfo("uname1", "a_fname", "a_lname", "em1", "dep1");
        UserInfo userInfo3 = new UserInfo("uname3", "c_fname", "a_lname", "em2", "dep2");
        expect(keycloakClient.listUsers()).andReturn(Arrays.asList(userInfo3, userInfo1));
        replay(keycloakClient);

        Employee actual = keycloak.findEmployee("uname2");

        verify(keycloakClient);
        assertNull(actual);
    }

    @Test
    public void testFindEmployeesByFullName() {
        UserInfo userInfo1 = new UserInfo("uname1", "a_fname", "a_lname", "em1", "dep1");
        UserInfo userInfo2 = new UserInfo("uname2", "b_fname", "b_lname", "em2", "dep2");
        UserInfo userInfo3 = new UserInfo("uname3", "c_fname", "a_lname", "em2", "dep2");
        expect(keycloakClient.listUsers()).andReturn(Arrays.asList(userInfo3, userInfo1, userInfo2));
        expect(workdaysCalendarRepository.findDefaultCalendar(anyString())).andReturn(null).times(3);
        replay(keycloakClient, workdaysCalendarRepository);

        List<Employee> actual = keycloak.findEmployeesByFullName("a_lname");

        verify(keycloakClient, workdaysCalendarRepository);
        assertFalse(actual.isEmpty());
        assertEquals("uname1", actual.get(0).getUserName());
        assertEquals("uname3", actual.get(1).getUserName());
    }

    @Test
    public void testCreateEmployee() {
        UserInfo userInfo = new UserInfo("username", "firstname", "lastname", "email", "department");
        Employee actual = keycloak.createEmployee(userInfo);
        assertEmployeeEqualsUserInfo(actual, userInfo);
    }

    @Test
    public void testGetTeamByGroupCode() {
        String code = "code";
        UserInfo user1 = new UserInfo("2user", "b", "b", null, null);
        UserInfo user2 = new UserInfo("1user", "a", "a", null, null);
        List<UserInfo> users = Arrays.asList(user2, user1);
        expect(keycloakClient.listUsers(code)).andReturn(users);
        expect(workdaysCalendarRepository.findDefaultCalendar(anyObject())).andReturn(null).anyTimes();
        replay(keycloakClient, workdaysCalendarRepository);

        List<Employee> actual = keycloak.getTeamByGroupCode(code);

        verify(keycloakClient);
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
        expect(keycloakClient.listUsers()).andReturn(users);
        expect(workdaysCalendarRepository.findDefaultCalendar(anyObject())).andReturn(null).anyTimes();
        replay(keycloakClient, workdaysCalendarRepository);

        List<Employee> actual = keycloak.getTeamByDepartment(department);

        verify(keycloakClient);
        assertEquals(2, actual.size());
        assertEquals(user1.getUsername(), actual.get(0).getUserName());
        assertEquals(user3.getUsername(), actual.get(1).getUserName());
    }

    private void assertEmployeeEqualsUserInfo(Employee employee, UserInfo userInfo) {
        assertEquals(employee.getUserName(), userInfo.getUsername());
        assertEquals(employee.getFirstName(), userInfo.getFirstName());
        assertEquals(employee.getLastName(), userInfo.getLastName());
        assertEquals(employee.getEmail(), userInfo.getEmail());
        assertEquals(employee.getDepartment().toLowerCase(), userInfo.getDepartment().toLowerCase());
    }
}

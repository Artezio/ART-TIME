package com.artezio.arttime.web;

import com.artezio.arttime.datamodel.Employee;
import com.artezio.arttime.repositories.EmployeeRepository;
import com.artezio.arttime.services.DepartmentService;
import com.artezio.arttime.services.EmployeeService;
import com.google.common.collect.Sets;
import junitx.util.PrivateAccessor;
import org.apache.commons.lang.WordUtils;
import org.easymock.*;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.*;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

@RunWith(EasyMockRunner.class)
public class DepartmentAccessBeanTest {

    @TestSubject
    private DepartmentAccessBean departmentAccessBean = new DepartmentAccessBean();
    @Mock
    private DepartmentService departmentService;
    @Mock
    private EmployeeService employeeService;
    @Mock
    private EmployeeRepository employeeRepository;

    @Test
    public void testGetAccessToDepartments_null() throws NoSuchFieldException {
        PrivateAccessor.setField(departmentAccessBean, "accessToDepartments", null);
        String department1 = toNameCase("dep1");
        String department2 = toNameCase("dep2");
        String department3 = toNameCase("dep3");
        List<String> departments = Arrays.asList(department1, department2, department3);
        Employee withAccessTo1 = new Employee("uname1", "fname1", "lname", "email");
        withAccessTo1.setAccessibleDepartments(Sets.newHashSet(department1));
        Employee withAccessTo1and2 = new Employee("uname2", "fname2", "lname", "email");
        withAccessTo1and2.setAccessibleDepartments(Sets.newHashSet(department1, department2));
        expect(departmentService.getAll()).andReturn(departments).anyTimes();
        expect(employeeService.getEmployeesHavingAccessToAnyDepartment()).andReturn(Arrays.asList(withAccessTo1, withAccessTo1and2)).anyTimes();
        replay(departmentService, employeeService);

        Map<String, List<Employee>> actual = departmentAccessBean.getAccessToDepartments();

        assertTrue(actual.containsKey(department1));
        assertTrue(actual.containsKey(department2));
        assertTrue(actual.containsKey(department3));
        List<Employee> employeesWithAccessTo1 = actual.get(department1);
        List<Employee> employeesWithAccessTo2 = actual.get(department2);
        List<Employee> employeesWithAccessTo3 = actual.get(department3);
        assertTrue(employeesWithAccessTo1.contains(withAccessTo1));
        assertTrue(employeesWithAccessTo1.contains(withAccessTo1and2));
        assertFalse(employeesWithAccessTo2.contains(withAccessTo1));
        assertTrue(employeesWithAccessTo2.contains(withAccessTo1and2));
        assertTrue(employeesWithAccessTo3.isEmpty());
    }

    @Test
    public void testGetAccessToDepartments_notNull() throws NoSuchFieldException {
        HashMap<String, List<Employee>> map = new HashMap<>();
        PrivateAccessor.setField(departmentAccessBean, "accessToDepartments", map);
        Map<String, List<Employee>> actual = departmentAccessBean.getAccessToDepartments();
        assertSame(map, actual);
    }

    @Test
    public void testGrantAccessForChangedEmployees() throws NoSuchFieldException {
        String department1 = toNameCase("department1");
        String department2 = toNameCase("department2");
        Employee employee1 = new Employee("name1");
        employee1.setAccessibleDepartments(Sets.newHashSet(department2));
        Employee employee2 = new Employee("uname2");
        employee2.setAccessibleDepartments(Sets.newHashSet(department1, department2));
        Map<String, List<Employee>> selectedAccess = new HashMap<>();
        selectedAccess.put(department1, Arrays.asList(employee1, employee2));
        selectedAccess.put(department2, Arrays.asList(employee1, employee2));
        PrivateAccessor.setField(departmentAccessBean, "accessToDepartments", selectedAccess);
        expect(employeeService.getEmployeesHavingAccessToAnyDepartment()).andReturn(Arrays.asList(employee1, employee2)).anyTimes();
        Capture<Collection<Employee>> updatedEmployees = Capture.newInstance();
        employeeService.update(capture(updatedEmployees));
        expectLastCall();
        replay(employeeService);
        assertFalse(employee1.hasAccessTo(department1));
        assertTrue(employee1.hasAccessTo(department2));
        assertTrue(employee2.hasAccessTo(department1));
        assertTrue(employee2.hasAccessTo(department2));

        departmentAccessBean.grantAccessForChangedEmployees(department1);

        assertTrue(updatedEmployees.hasCaptured());
        Collection<Employee> capturedEmployees = updatedEmployees.getValue();
        assertTrue(capturedEmployees.contains(employee1));
        assertFalse(capturedEmployees.contains(employee2));
        assertTrue(employee1.hasAccessTo(department1));
        assertTrue(employee1.hasAccessTo(department2));
        assertTrue(employee2.hasAccessTo(department1));
        assertTrue(employee2.hasAccessTo(department2));
    }

    @Test
    public void testRevokeAccessForChangedEmployees() throws NoSuchFieldException {
        String department1 = toNameCase("department1");
        String department2 = toNameCase("department2");
        Employee employee1 = new Employee("name1");
        employee1.setAccessibleDepartments(Sets.newHashSet(department1, department2));
        Employee employee2 = new Employee("uname2");
        employee2.setAccessibleDepartments(Sets.newHashSet(department1, department2));
        Map<String, List<Employee>> selectedAccess = new HashMap<>();
        selectedAccess.put(department1, Arrays.asList(employee2));
        selectedAccess.put(department2, Arrays.asList(employee1, employee2));
        PrivateAccessor.setField(departmentAccessBean, "accessToDepartments", selectedAccess);
        expect(employeeService.getEmployeesHavingAccessToAnyDepartment()).andReturn(Arrays.asList(employee1, employee2)).anyTimes();
        Capture<Collection<Employee>> updatedEmployees = Capture.newInstance();
        employeeService.update(capture(updatedEmployees));
        expectLastCall();
        replay(employeeService);

        assertTrue(employee1.hasAccessTo(department1));
        assertTrue(employee1.hasAccessTo(department2));
        assertTrue(employee2.hasAccessTo(department1));
        assertTrue(employee2.hasAccessTo(department2));

        departmentAccessBean.revokeAccessForChangedEmployees(department1);

        assertTrue(updatedEmployees.hasCaptured());
        Collection<Employee> capturedEmployees = updatedEmployees.getValue();
        assertTrue(capturedEmployees.contains(employee1));
        assertFalse(capturedEmployees.contains(employee2));
        assertFalse(employee1.hasAccessTo(department1));
        assertTrue(employee1.hasAccessTo(department2));
        assertTrue(employee2.hasAccessTo(department1));
        assertTrue(employee2.hasAccessTo(department2));
    }

    @Test
    public void testSave() {
        EasyMock.expect(departmentService.getAll()).andReturn(Collections.emptyList()).anyTimes();
        EasyMock.replay(departmentService);

        departmentAccessBean.save();

        EasyMock.verify(departmentService);
    }

    protected String toNameCase(String department) {
        return WordUtils.capitalizeFully(department, new char[]{'-', ' '});
    }

}

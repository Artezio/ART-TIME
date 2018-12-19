package com.artezio.arttime.services;

import com.artezio.arttime.datamodel.Employee;
import com.artezio.arttime.datamodel.WorkdaysCalendar;
import com.artezio.arttime.repositories.DepartmentRepository;
import com.artezio.arttime.repositories.EmployeeRepository;
import com.artezio.arttime.repositories.WorkdaysCalendarRepository;
import com.artezio.arttime.security.auth.UserRoles;
import com.artezio.arttime.services.integration.DepartmentTrackingSystem;
import org.easymock.EasyMock;
import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.easymock.internal.LastControl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import javax.ejb.SessionContext;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.security.Principal;
import java.util.*;

import static java.util.Arrays.asList;
import static junitx.util.PrivateAccessor.setField;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;

@RunWith(EasyMockRunner.class)
public class DepartmentServiceTest {
    @TestSubject
    private DepartmentService departmentService = new DepartmentService();
    @Mock
    private DepartmentTrackingSystem departmentTrackingSystem;
    @Mock
    private DepartmentRepository departmentRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private WorkdaysCalendarRepository workdaysCalendarRepository;
    @Mock
    private EmployeeService employeeService;
    @Mock
    private SessionContext sessionContext;

    private EntityManager entityManager;
    private EntityManagerFactory entityManagerFactory;

    @Before
    public void setUp() throws Exception {
        Map<String, String> properties = new HashMap<>();
        properties.put("javax.persistence.validation.mode", "none");
        entityManagerFactory = Persistence.createEntityManagerFactory("test-pu", properties);
        entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        LastControl.pullMatchers();
    }

    @After
    public void tearDown() throws Exception {
        if (entityManager.getTransaction().isActive()) {
            if (entityManager.getTransaction().getRollbackOnly()) {
                entityManager.getTransaction().rollback();
            } else {
                entityManager.getTransaction().commit();
            }
            entityManagerFactory.close();
        }
        LastControl.pullMatchers();
    }

    @Test
    public void testGetAll() {
        List<String> externalDepts = asList("Dep1", "Dep2");
        List<String> storedDepts = asList("Dep2", "Dep3");
        Set<String> expectedSet = new HashSet<>(externalDepts);
        expectedSet.addAll(storedDepts);
        expect(sessionContext.isCallerInRole(UserRoles.EXEC_ROLE)).andReturn(true).anyTimes();
        expect(sessionContext.isCallerInRole(UserRoles.PM_ROLE)).andReturn(false).anyTimes();
        expect(sessionContext.isCallerInRole(UserRoles.OFFICE_MANAGER)).andReturn(false).anyTimes();
        expect(departmentTrackingSystem.getDepartments()).andReturn(externalDepts);
        expect(departmentRepository.getDepartments()).andReturn(storedDepts);
        replay(departmentTrackingSystem, departmentRepository, sessionContext);

        List<String> actual = departmentService.getAll();

        verify(departmentRepository, departmentTrackingSystem);
        assertEquals(new ArrayList<>(expectedSet), actual);
    }

    @Test
    public void testGetAll_officeManager() {
        List<String> externalDepts = asList("Dep1", "Dep2");
        List<String> storedDepts = asList("Dep2", "Dep3");
        Set<String> expectedSet = new HashSet<>(externalDepts);
        expectedSet.addAll(storedDepts);
        expectedSet.remove("Dep1");
        expect(sessionContext.isCallerInRole(UserRoles.EXEC_ROLE)).andReturn(false).anyTimes();
        expect(sessionContext.isCallerInRole(UserRoles.PM_ROLE)).andReturn(false).anyTimes();
        expect(sessionContext.isCallerInRole(UserRoles.OFFICE_MANAGER)).andReturn(true).anyTimes();
        expect(departmentTrackingSystem.getDepartments()).andReturn(externalDepts);
        Employee caller = new Employee("username");
        caller.setAccessibleDepartments(new HashSet<>(Arrays.asList("Dep2", "Dep3")));
        expect(employeeService.getLoggedEmployee()).andReturn(Optional.of(caller));
        replay(sessionContext, employeeService);

        List<String> actual = departmentService.getAll();

        verify(sessionContext, employeeService);

        assertEquals(new ArrayList<>(expectedSet), actual);
    }

    @Test
    public void testSetCalendar_departmentHadCalendar() throws NoSuchFieldException {
        String department = "ExpectedDepartment";
        WorkdaysCalendar storedCalendar = new WorkdaysCalendar("StoredCalendar");
        setField(storedCalendar, "id", 1L);
        WorkdaysCalendar otherStoredCalendar = new WorkdaysCalendar("OtherStoredCalendar");
        setField(otherStoredCalendar, "id", 2L);
        WorkdaysCalendar newCalendar = new WorkdaysCalendar("NewCalendar");
        setField(newCalendar, "id", 3L);
        Set<String> newDepts = new HashSet<>();
        newDepts.add("SomeDepartment");
        newCalendar.setDepartments(newDepts);
        Set<String> storedDepts = new HashSet<>();
        storedDepts.add(department);
        storedCalendar.setDepartments(storedDepts);
        Employee employee1 = new Employee();
        employee1.setCalendar(storedCalendar);
        Employee employee2 = new Employee();
        Employee employee3 = new Employee();
        employee3.setCalendar(otherStoredCalendar);
        List<Employee> employees = asList(employee1, employee2, employee3);
        EmployeeRepository.EmployeeQuery query = Mockito.mock(EmployeeRepository.EmployeeQuery.class, RETURNS_DEEP_STUBS);

        Mockito.when(query
                .department(department)
                .list()).thenReturn(employees);
        EasyMock.expect(employeeRepository.query()).andReturn(query);
        EasyMock.expect(workdaysCalendarRepository.findByDepartment(department)).andReturn(storedCalendar);
        EasyMock.expect(workdaysCalendarRepository.attachAndRefresh(newCalendar)).andReturn(newCalendar);
        EasyMock.replay(employeeRepository, workdaysCalendarRepository);

        departmentService.setCalendarToDepartment(department, newCalendar);

        EasyMock.verify(employeeRepository, workdaysCalendarRepository);

        assertTrue(newCalendar.getDepartments().contains(department));
        assertEquals(newCalendar, employee1.getCalendar());
        assertEquals(newCalendar, employee2.getCalendar());
        assertEquals(otherStoredCalendar, employee3.getCalendar());
    }

    @Test
    public void testSetCalendar_departmentHadNoCalendar() throws NoSuchFieldException {
        String department = "ExpectedDepartment";
        WorkdaysCalendar otherStoredCalendar = new WorkdaysCalendar("OtherStoredCalendar");
        setField(otherStoredCalendar, "id", 2L);
        WorkdaysCalendar newCalendar = new WorkdaysCalendar("NewCalendar");
        setField(newCalendar, "id", 3L);
        Set<String> newDepts = new HashSet<>();
        newDepts.add("SomeDepartment");
        newCalendar.setDepartments(newDepts);
        Employee employee1 = new Employee();
        Employee employee2 = new Employee();
        Employee employee3 = new Employee();
        employee3.setCalendar(otherStoredCalendar);
        List<Employee> employees = asList(employee1, employee2, employee3);
        EmployeeRepository.EmployeeQuery query = Mockito.mock(EmployeeRepository.EmployeeQuery.class, RETURNS_DEEP_STUBS);

        Mockito.when(query
                .department(department)
                .list()).thenReturn(employees);
        EasyMock.expect(employeeRepository.query()).andReturn(query);
        EasyMock.expect(workdaysCalendarRepository.findByDepartment(department)).andReturn(null);
        EasyMock.expect(workdaysCalendarRepository.attachAndRefresh(newCalendar)).andReturn(newCalendar);
        EasyMock.replay(employeeRepository, workdaysCalendarRepository);

        departmentService.setCalendarToDepartment(department, newCalendar);

        EasyMock.verify(employeeRepository, workdaysCalendarRepository);

        assertTrue(newCalendar.getDepartments().contains(department));
        assertEquals(newCalendar, employee1.getCalendar());
        assertEquals(newCalendar, employee2.getCalendar());
        assertEquals(otherStoredCalendar, employee3.getCalendar());
    }

}

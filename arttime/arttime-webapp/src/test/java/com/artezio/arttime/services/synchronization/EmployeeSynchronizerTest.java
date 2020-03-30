package com.artezio.arttime.services.synchronization;

import com.artezio.arttime.datamodel.Employee;
import com.artezio.arttime.services.EmployeeService;
import com.artezio.arttime.services.integration.EmployeeTrackingSystem;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import javax.transaction.UserTransaction;

import static junitx.util.PrivateAccessor.setField;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

public class EmployeeSynchronizerTest {
    private EmployeeTrackingSystem employeeTrackingSystem;
    private EmployeeSynchronizer employeeSynchronizer;
    private EmployeeService employeeService;
    private UserTransaction transaction;

    @Before
    public void setUp() throws NoSuchFieldException {
        employeeSynchronizer = new EmployeeSynchronizer();
        employeeTrackingSystem = createMock(EmployeeTrackingSystem.class);
        employeeService = createMock(EmployeeService.class);
        transaction = createMock(UserTransaction.class);

        setField(employeeSynchronizer, "employeeTrackingSystem", employeeTrackingSystem);
        setField(employeeSynchronizer, "employeeService", employeeService);
        setField(employeeSynchronizer, "transaction", transaction);
    }

    @Test
    public void testSynchronizeEmployees() {
        String userName = "ssmirnoff";
        Employee externalEmployee = new Employee(userName, "Sergei", "Smirnoff", "ssmirnoff@domain.com");
        Employee internalEmployee = new Employee(userName, "Oldfirst", "Oldlast", "Oldmail");
        internalEmployee.setFormer(true);
        List<Employee> internalEmployeeList = Arrays.asList(internalEmployee);

        expect(employeeService.getAll()).andReturn(internalEmployeeList);
        expect(employeeTrackingSystem.findEmployee(userName)).andReturn(externalEmployee);
        expect(employeeService.update(externalEmployee)).andReturn(externalEmployee);
        replay(employeeService, employeeTrackingSystem);

        employeeSynchronizer.synchronizeEmployees();

        verify(employeeService, employeeTrackingSystem);
        assertFalse(internalEmployee.isFormer());
    }

    @Test
    public void testSynchronizeEmployees_externalEmployeeNotFound() {
        String userName = "ssmirnoff";
        Employee externalEmployee = null;
        Employee internalEmployee = new Employee(userName, "Oldfirst", "Oldlast", "Oldmail");
        List<Employee> internalEmployeeList = Arrays.asList(internalEmployee);

        expect(employeeService.getAll()).andReturn(internalEmployeeList);
        expect(employeeTrackingSystem.findEmployee(userName)).andReturn(externalEmployee);
        expect(employeeService.update(internalEmployee)).andReturn(internalEmployee);
        replay(employeeService, employeeTrackingSystem);

        employeeSynchronizer.synchronizeEmployees();

        verify(employeeService, employeeTrackingSystem);
        assertTrue(internalEmployee.isFormer());
    }
    
    @Test
    public void testSynchronizeEmployees_synchronizationError() {
        String userNameWrongEmployee = "ssmirnoff_error";
        Employee externalWrongEmployee = new Employee(userNameWrongEmployee, "Sergei_error", "Smirnoff_error", "ssmirnoff@domain.com_error", "department_error");
        Employee internalWrongEmployee = new Employee(userNameWrongEmployee, "Oldfirst", "Oldlast", "Oldmail", "Olddepartment");
        internalWrongEmployee.setFormer(true);
        
        String userNameCorrectEmployee = "ssmirnoff_ok";
        Employee externalCorrectEmployee = new Employee(userNameCorrectEmployee, "Sergei_ok", "Smirnoff_ok", "ssmirnoff@domain.com_ok", "department_ok");
        Employee internalCorrectEmployee = new Employee(userNameCorrectEmployee, "Oldfirst", "Oldlast", "Oldmail", "Olddepartment");
        internalCorrectEmployee.setFormer(true);
        
        List<Employee> internalEmployeeList = Arrays.asList(internalWrongEmployee, internalCorrectEmployee);

        expect(employeeService.getAll()).andReturn(internalEmployeeList);
        expect(employeeTrackingSystem.findEmployee(userNameWrongEmployee)).andReturn(externalWrongEmployee);
        expect(employeeService.update(internalWrongEmployee)).andThrow(new RuntimeException());
        expect(employeeTrackingSystem.findEmployee(userNameCorrectEmployee)).andReturn(externalCorrectEmployee);
        expect(employeeService.update(externalCorrectEmployee)).andReturn(externalCorrectEmployee);
        
        replay(employeeService, employeeTrackingSystem);

        employeeSynchronizer.synchronizeEmployees();

        verify(employeeService, employeeTrackingSystem);
        assertFalse(internalCorrectEmployee.isFormer());
        assertEquals(externalCorrectEmployee.getUserName(), internalCorrectEmployee.getUserName());
        assertEquals(externalCorrectEmployee.getLastName(), internalCorrectEmployee.getLastName());
        assertEquals(externalCorrectEmployee.getFirstName(), internalCorrectEmployee.getFirstName());
        assertEquals(externalCorrectEmployee.getDepartment(), internalCorrectEmployee.getDepartment());
        
    }

}

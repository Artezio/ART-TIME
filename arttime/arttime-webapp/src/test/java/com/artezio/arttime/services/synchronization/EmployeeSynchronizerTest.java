package com.artezio.arttime.services.synchronization;

import com.artezio.arttime.datamodel.Employee;
import com.artezio.arttime.services.EmployeeService;
import com.artezio.arttime.services.integration.EmployeeTrackingSystem;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static junitx.util.PrivateAccessor.setField;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class EmployeeSynchronizerTest {
	private EmployeeTrackingSystem employeeTrackingSystem;
	private EmployeeSynchronizer employeeSynchronizer;
	private EmployeeService employeeService;

	@Before
	public void setUp() throws NoSuchFieldException {
		employeeSynchronizer = new EmployeeSynchronizer();
		employeeTrackingSystem = createMock(EmployeeTrackingSystem.class);
		employeeService = createMock(EmployeeService.class);

		setField(employeeSynchronizer, "employeeTrackingSystem", employeeTrackingSystem);
		setField(employeeSynchronizer, "employeeService", employeeService);
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

}

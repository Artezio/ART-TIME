package com.artezio.arttime.web.converters;

import static junitx.util.PrivateAccessor.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import com.artezio.arttime.services.integration.EmployeeTrackingSystem;
import org.easymock.EasyMock;
import org.easymock.EasyMockRunner;
import org.easymock.IMocksControl;
import org.easymock.Mock;
import org.easymock.internal.MocksControl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.artezio.arttime.datamodel.Employee;
import com.artezio.arttime.datamodel.Project;
import com.artezio.arttime.repositories.EmployeeRepository;

@RunWith(EasyMockRunner.class)
public class EmployeeConverterTest {	
	private EmployeeConverter converter;
	@Mock
	private UIComponent component;
	@Mock
	private FacesContext facesContext;
	@Mock
	private EmployeeTrackingSystem employeeTrackingSystem;
	@Mock
	private EmployeeRepository employeeRepository;	
	
	@Before
	public void setUp() {
		converter = new EmployeeConverter();
	}
	
	@Test
	public void testGetAsObject_ifFoundInRepository() throws Exception {
		setField(converter, "employeeRepository", employeeRepository);
		Employee employee = new Employee();
		expect(employeeRepository.find("iivanov")).andReturn(employee);
		expect(employeeRepository.fetchComplete(anyObject(Employee.class))).andAnswer(() -> (Employee)EasyMock.getCurrentArguments()[0]);
		replay(employeeRepository);
		
		
		Object actual = converter.getAsObject(facesContext, component, "iivanov");
		
		verify(employeeRepository);
		assertSame(employee, actual);
	}
	
	@Test
	public void testGetAsObject_ifFoundInService() throws Exception {
		setField(converter, "employeeTrackingSystem", employeeTrackingSystem);
		setField(converter, "employeeRepository", employeeRepository);
		Employee employee = new Employee();
		expect(employeeRepository.find("iivanov")).andReturn(null);
		expect(employeeTrackingSystem.findEmployee("iivanov")).andReturn(employee);
		replay(employeeRepository, employeeTrackingSystem);
		
		
		Object actual = converter.getAsObject(facesContext, component, "iivanov");
		
		verify(employeeRepository, employeeTrackingSystem);
		assertSame(employee, actual);
	}
	
	@Test
	public void testGetAsObject_ifNotFound() throws Exception {
		setField(converter, "employeeTrackingSystem", employeeTrackingSystem);
		setField(converter, "employeeRepository", employeeRepository);
		expect(employeeRepository.find("iivanov")).andReturn(null);
		expect(employeeTrackingSystem.findEmployee("iivanov")).andReturn(null);
		replay(employeeRepository, employeeTrackingSystem);
		
		
		Object actual = converter.getAsObject(facesContext, component, "iivanov");
		
		verify(employeeRepository, employeeTrackingSystem);
		assertNull(actual);
	}
	
	@Test
	public void testGetAsString_ifEmployee() {
		Employee employee = new Employee("iivanov");
		
		String actual = converter.getAsString(facesContext, component, employee);
		
		assertEquals("iivanov", actual);
	}
	
	@Test
	public void testGetAsString_ifNotEmployee() {
		Project project = new Project();
		
		String actual = converter.getAsString(facesContext, component, project);
		
		assertNull(actual);
	}
}

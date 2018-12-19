package com.artezio.arttime.web.converters;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;
import static junitx.util.PrivateAccessor.*;

import java.util.Arrays;
import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.artezio.arttime.datamodel.Employee;
import com.artezio.arttime.repositories.EmployeeRepository;

@RunWith(EasyMockRunner.class)
public class ListEmployeeConverterTest {
	private ListEmployeeConverter converter;
	@Mock
	private UIComponent component;
	@Mock
	private FacesContext facesContext;
	@Mock
	private EmployeeRepository employeeRepository;
	
	@Before
	public void setUp() {
		converter = new ListEmployeeConverter();
	}
	
	@Test
	public void testGetAsObject() throws Exception {
		setField(converter, "employeeRepository", employeeRepository);
		List<String> usernames = Arrays.asList("iivanov", "ppetrov");
		List<Employee> employees = Arrays.asList(new Employee("iivanov"), new Employee("ppetrov"));
		expect(employeeRepository.getEmployees(usernames)).andReturn(employees);
		replay(employeeRepository);
		
		Object actual = converter.getAsObject(facesContext, component, "iivanov,ppetrov");
		
		verify(employeeRepository);
		assertSame(employees, actual);
	}
	
	@Test
	public void testGetAsString_ifValueIsNull() {
		String actual = converter.getAsString(facesContext, component, null);
		
		assertNull(actual);
	}
	
	@Test
	public void testGetAsString_ifValueIsNotNull() {
		List<Employee> employees = Arrays.asList(new Employee("iivanov"), new Employee("ppetrov"));
		String expected = "iivanov,ppetrov";
		
		String actual = converter.getAsString(facesContext, component, employees);
		
		assertEquals(expected, actual);
	}

}

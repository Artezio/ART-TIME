package com.artezio.arttime.web;


import com.artezio.arttime.datamodel.Employee;
import com.artezio.arttime.datamodel.WorkdaysCalendar;
import com.artezio.arttime.services.EmployeeService;
import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.faces.context.ExternalContext;
import java.util.*;

import static junitx.util.PrivateAccessor.getField;
import static junitx.util.PrivateAccessor.setField;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

@RunWith(EasyMockRunner.class)
public class EmployeesBeanTest {
    @TestSubject
    private EmployeesBean bean = new EmployeesBean();
    @Mock
    private ExternalContext externalContext;
    @Mock
    private EmployeeService employeeService;

    @Test
    public void testSave() throws NoSuchFieldException {
        Employee employee = new Employee();
        setField(bean, "employee", employee);
        expect(employeeService.update(employee)).andReturn(employee);
        replay(employeeService);

        bean.save();

        verify(employeeService);
    }

    @Test
    public void testInit() throws Exception {
        Employee employee = new Employee("iivanov");
        Map<String, String> requestParams = new HashMap<>();
        requestParams.put("employee", "iivanov");
        expect(externalContext.getRequestParameterMap()).andReturn(requestParams);
        expect(employeeService.find("iivanov")).andReturn(employee);
        replay(externalContext, employeeService);

        bean.init();

        Employee actual = (Employee) getField(bean, "employee");

        verify(externalContext, employeeService);
        assertSame(employee, actual);
    }

    @Test
    public void testInit_ifUsernameIsNull() throws Exception {
        Map<String, String> requestParams = new HashMap<>();
        expect(externalContext.getRequestParameterMap()).andReturn(requestParams);
        replay(externalContext);

        bean.init();

        Employee actual = (Employee) getField(bean, "employee");

        verify(externalContext);
        assertNull(actual);
    }

    @Test
    public void testEditWorkload() throws NoSuchFieldException {
        Employee employee = new Employee("iivanov");

        bean.editWorkload(employee);

        Employee actual = (Employee) getField(bean, "employee");
        assertSame(employee, actual);
    }
    
    @Test
    public void testFilterByCalendar() throws NoSuchFieldException {
        WorkdaysCalendar calendar1 = new WorkdaysCalendar("calendar1");
        setField(calendar1, "id", 1L);
        WorkdaysCalendar calendar2 = new WorkdaysCalendar("calendar2");
        setField(calendar2, "id", 2L);
        List<WorkdaysCalendar> filter = Arrays.asList(calendar1, calendar2);
        
        boolean actual = bean.filterByCalendar(calendar1, filter, null);
        
        assertTrue(actual);
    }
    
    @Test
    public void testFilterByCalendar_FilterIsNull() throws NoSuchFieldException {
        WorkdaysCalendar calendar = new WorkdaysCalendar("calendar1");
        setField(calendar, "id", 1L);
        
        boolean actual = bean.filterByCalendar(calendar, null, null);
        
        assertTrue(actual);
    }
    
    @Test
    public void testFilterByCalendar_FilterIsEmpty() throws NoSuchFieldException {
        WorkdaysCalendar calendar = new WorkdaysCalendar("calendar1");
        setField(calendar, "id", 1L);
        List<WorkdaysCalendar> filter = Collections.emptyList();
        
        boolean actual = bean.filterByCalendar(calendar, filter, null);
        
        assertTrue(actual);
    }
    
    @Test
    public void testFilterByCalendar_ValueOutOfFilter() throws NoSuchFieldException {
        WorkdaysCalendar calendar1 = new WorkdaysCalendar("calendar1");
        setField(calendar1, "id", 1L);
        WorkdaysCalendar calendar2 = new WorkdaysCalendar("calendar2");
        setField(calendar2, "id", 2L);
        WorkdaysCalendar calendar3 = new WorkdaysCalendar("calendar3");
        setField(calendar3, "id", 3L);
        List<WorkdaysCalendar> filter = Arrays.asList(calendar1, calendar2);
        
        boolean actual = bean.filterByCalendar(calendar3, filter, null);
        
        assertFalse(actual);
    }

}

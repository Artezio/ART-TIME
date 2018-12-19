package com.artezio.arttime.web;

import com.artezio.arttime.datamodel.Employee;
import com.artezio.arttime.datamodel.WorkdaysCalendar;
import com.artezio.arttime.services.DepartmentService;
import com.artezio.arttime.services.EmployeeService;
import com.artezio.arttime.services.WorkdaysCalendarService;
import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.faces.context.ExternalContext;
import java.util.*;

import static com.artezio.arttime.security.auth.UserRoles.EXEC_ROLE;
import static com.artezio.arttime.security.auth.UserRoles.OFFICE_MANAGER;
import static junitx.util.PrivateAccessor.setField;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

@RunWith(EasyMockRunner.class)
public class WorkdaysCalendarsBeanTest {
    @TestSubject
    private WorkdaysCalendarsBean bean = new WorkdaysCalendarsBean();
    @Mock
    private WorkdaysCalendarService workdaysCalendarService;
    @Mock
    private DepartmentService departmentService;
    @Mock
    private EmployeeService employeeService;

    @Test
    public void testGetCalendars_ifNotNull() throws NoSuchFieldException {
        List<WorkdaysCalendar> calendars = new ArrayList<WorkdaysCalendar>();
        setField(bean, "calendars", calendars);

        List<WorkdaysCalendar> actual = bean.getCalendars();

        assertSame(calendars, actual);
    }

    @Test
    public void testGetCalendars_ifNull() throws NoSuchFieldException {
        setField(bean, "calendars", null);
        List<WorkdaysCalendar> calendars = new ArrayList<WorkdaysCalendar>();
        expect(workdaysCalendarService.getCalendarsForManaging()).andReturn(calendars);
        replay(workdaysCalendarService);

        List<WorkdaysCalendar> actual = bean.getCalendars();

        assertSame(calendars, actual);
    }

    @Test
    public void testRemove() {
        WorkdaysCalendar workdaysCalendar = new WorkdaysCalendar();
        workdaysCalendarService.remove(workdaysCalendar);
        replay(workdaysCalendarService);

        bean.remove(workdaysCalendar);

        verify(workdaysCalendarService);
    }

    @Test
    public void testGetWorkdaysCalendarsByDepartments_whenNull() throws NoSuchFieldException {
        Map<String, WorkdaysCalendar> calendarMap = new HashMap<>();
        String departmentWithCalendar = "Dept1";
        String departmentWithoutCalendar = "Dept2";
        WorkdaysCalendar calendar = new WorkdaysCalendar("Cal1");
        calendarMap.put(departmentWithCalendar, calendar);
        expect(workdaysCalendarService.getCalendarsByDepartments()).andReturn(calendarMap);
        expect(departmentService.getAll()).andReturn(Arrays.asList(departmentWithCalendar, departmentWithoutCalendar));
        replay(workdaysCalendarService, departmentService);

        Map<String, WorkdaysCalendar> actual = bean.getWorkdaysCalendarsByDepartments();

        verify(workdaysCalendarService, departmentService);
        assertTrue(actual.containsKey(departmentWithCalendar));
        assertTrue(actual.containsKey(departmentWithoutCalendar));
        assertEquals(calendar, actual.get(departmentWithCalendar));
        assertNull(actual.get(departmentWithoutCalendar));
    }

    @Test
    public void testGetWorkdaysCalendarsByDepartments_whenNotNull() throws NoSuchFieldException {
        Map<String, WorkdaysCalendar> calendarMap = new HashMap<>();
        calendarMap.put("Dept1", new WorkdaysCalendar("aa"));
        calendarMap.put("Dept2", new WorkdaysCalendar("bb"));
        setField(bean, "workdaysCalendarsByDepartments", calendarMap);

        Map<String, WorkdaysCalendar> actual = bean.getWorkdaysCalendarsByDepartments();

        assertEquals(calendarMap, actual);
    }

    @Test
    public void testGetDepartments_whenNull() throws NoSuchFieldException {
        List<String> departments = Arrays.asList("Dept1", "Dept2");
        expect(departmentService.getAll()).andReturn(departments);
        replay(departmentService);
        List<String> actual = bean.getDepartments();
        verify(departmentService);
        assertEquals(departments, actual);
    }

    @Test
    public void testGetDepartments_whenNotNull() throws NoSuchFieldException {
        List<String> departments = Arrays.asList("Dept1", "Dept2");
        setField(bean, "departments", departments);
        List<String> actual = bean.getDepartments();
        assertEquals(departments, actual);
    }

    @Test
    public void testSaveCalendarsByDepartments() throws NoSuchFieldException {
        List<String> departments = Arrays.asList("Dept1", "Dept2");
        Map<String, WorkdaysCalendar> calendarMap = new HashMap<>();
        WorkdaysCalendar calendar = new WorkdaysCalendar("Cal1");
        calendarMap.put("Dept1", calendar);
        calendarMap.put("Dept2", null);
        setField(bean, "departments", departments);
        setField(bean, "workdaysCalendarsByDepartments", calendarMap);
        
        departmentService.setCalendarToDepartment("Dept1", calendar);
        departmentService.setCalendarToDepartment("Dept2", null);
        replay(departmentService);
        
        bean.save();
        
        verify(departmentService);
    }
}

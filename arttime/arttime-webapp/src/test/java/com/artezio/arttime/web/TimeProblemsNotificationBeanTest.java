package com.artezio.arttime.web;

import com.artezio.arttime.datamodel.Employee;
import com.artezio.arttime.filter.Filter;
import com.artezio.arttime.services.NotificationManager;
import com.artezio.arttime.services.WorkTimeService;
import com.artezio.arttime.repositories.EmployeeRepository;
import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static junitx.util.PrivateAccessor.setField;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(EasyMockRunner.class)
public class TimeProblemsNotificationBeanTest {

    private TimeProblemsNotificationBean bean;
    @Mock
    private FilterBean filterBean;
    @Mock
    private WorkTimeService workTimeService;
    @Mock
    private NotificationManager notificationManager;
    @Mock
    private EmployeeRepository employeeRepository;

    @Before
    public void setUp() {
        bean = new TimeProblemsNotificationBean();
    }

    @Test
    public void testGetSelectedEmployees_ifNotNull() throws Exception {
        bean.setSelectedRows(new ArrayList<>());

        List<Employee> actual = bean.getSelectedEmployees();

        assertNotNull(actual);
        assertTrue(actual.isEmpty());
    }

    @Test
    public void testGetSelectedEmployees_ifNull() throws Exception {
        bean = createMockBuilder(TimeProblemsNotificationBean.class)
                .addMockedMethod("getProblematicEmployees")
                .createMock();
        bean.setSelectedRows(null);
        expect(bean.getProblematicEmployees()).andReturn(new ArrayList<>());
        replay(bean);

        List<Employee> actual = bean.getSelectedEmployees();

        verify(bean);
        assertNotNull(actual);
        assertTrue(actual.isEmpty());
    }

    @Test
    public void testSendNotifications() throws Exception {
        Filter filter = new Filter();
        Employee employee = new Employee("employee");
        WorkTimeService.WorkTime efforts = new WorkTimeService.WorkTime(employee);
        List<Object> selectedRows = Arrays.asList(efforts);
        setField(bean, "filterBean", filterBean);
        setField(bean, "comment", "This is a comment.");
        setField(bean, "selectedRows", selectedRows);
        setField(bean, "notificationManager", notificationManager);
        expect(filterBean.getCurrentFilter()).andReturn(filter);
        replay(filterBean);

        notificationManager.notifyAboutWorkTimeProblems(Arrays.asList(employee), filter.getPeriod(), "This is a comment.");
        replay(notificationManager);

        bean.sendNotifications();

        verify(notificationManager);
    }

    @Test
    public void testGetProblematicEmployees_ifNull() throws Exception {
        Filter filter = new Filter();
        filter.setEmployees(new ArrayList<>());
        filter.setDepartments(new ArrayList<>());
        Employee employee = new Employee("test");
        setField(bean, "filterBean", filterBean);
        setField(bean, "employeesProblemTime", null);
        setField(bean, "workTimeService", workTimeService);
        expect(filterBean.getCurrentFilter()).andReturn(filter).anyTimes();
        expect(workTimeService.getProblemWorkTime(filter))
                .andReturn(Collections.singletonList(new WorkTimeService.WorkTime(employee)));
        replay(workTimeService, filterBean);

        List<WorkTimeService.WorkTime> actual = bean.getProblematicEmployees();

        verify(workTimeService);
        assertNotNull(actual);
    }

}

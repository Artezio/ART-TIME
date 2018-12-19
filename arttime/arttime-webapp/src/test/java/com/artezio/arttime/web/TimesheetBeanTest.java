package com.artezio.arttime.web;

import com.artezio.arttime.datamodel.Employee;
import com.artezio.arttime.datamodel.Hours;
import com.artezio.arttime.datamodel.Project;
import com.artezio.arttime.exceptions.SaveApprovedHoursException;
import com.artezio.arttime.filter.Filter;
import com.artezio.arttime.repositories.FilterRepository;
import com.artezio.arttime.services.EmployeeService;
import com.artezio.arttime.services.FilterService;
import com.artezio.arttime.services.HoursService;
import com.artezio.arttime.web.spread_sheet.SpreadSheet;
import org.easymock.EasyMock;
import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static junitx.util.PrivateAccessor.getField;
import static junitx.util.PrivateAccessor.setField;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

@RunWith(EasyMockRunner.class)
public class TimesheetBeanTest {

    private TimesheetBean bean = new TimesheetBean();
    @Mock
    private FilterBean filterBean;
    @Mock
    private FilterService filterService;
    @Mock
    private FilterRepository filterRepository;
    @Mock
    private SpreadSheet spreadSheet;
    @Mock
    private HoursService hoursService;
    @Mock
    private EmployeeService employeeService;

    @Before
    public void setUp() throws NoSuchFieldException {
        setField(bean, "spreadSheet", null);
        setField(bean, "filterBean", filterBean);
        setField(bean, "filterService", filterService);
        setField(bean, "hoursService", hoursService);
        setField(bean, "employeeService", employeeService);
    }

    @Test
    public void testGetSpreadSheet_ifNull() {
        Filter currentFilter = createMock(Filter.class);
        Filter personalTimesheetFilter = createMock(Filter.class);

        expect(employeeService.getLoggedEmployee()).andReturn(Optional.of(new Employee()));
        expect(filterService.getTimesheetFilter()).andReturn(personalTimesheetFilter);
        expect(filterBean.getCurrentFilter()).andReturn(currentFilter);
        replay(filterBean, filterService, employeeService);

        SpreadSheet actual = bean.getSpreadSheet();

        verify(filterBean, filterService, employeeService);
        assertNotNull(actual);
    }

    @Test
    public void testSaveHours() throws SaveApprovedHoursException, ReflectiveOperationException {
        bean = createMockBuilder(TimesheetBean.class).addMockedMethod("resetComponentsTree").createMock();
        Set<Hours> hours = new HashSet<>();
        setField(bean, "hoursService", hoursService);
        setField(bean, "spreadSheet", spreadSheet);

        EasyMock.expect(spreadSheet.getUpdatedHours()).andReturn(hours);
        EasyMock.expect(filterService.getTimesheetFilter());
        hoursService.reportHours(hours);
        bean.resetComponentsTree();
        EasyMock.replay(spreadSheet, hoursService, bean);

        bean.saveHours();

        verify(spreadSheet, hoursService, bean);
    }

    @Test
    public void testResetData() throws NoSuchFieldException {
        bean = createMockBuilder(TimesheetBean.class).addMockedMethod("resetComponentsTree").createMock();
        setField(bean, "spreadSheet", spreadSheet);
        bean.resetComponentsTree();
        replay(bean);

        bean.resetData();

        verify(bean);
        assertNull(getField(bean, "spreadSheet"));
    }

    @Test
    public void testGetStyleClass() {
        Project project = new Project();
        project.setAllowEmployeeReportTime(false);
        Hours hours = new Hours();
        hours.setProject(project);

        String actual = bean.getStyleClass(hours);

        assertEquals("blockedHours", actual);

    }

    @Test
    public void testGetStyleClass_ifHoursIsNull() {
        String actual = bean.getStyleClass(null);

        assertEquals("emptyCell", actual);
    }

}

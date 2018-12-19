package com.artezio.arttime.web;

import com.artezio.arttime.datamodel.*;
import com.artezio.arttime.filter.Filter;
import com.artezio.arttime.services.WorkdaysCalendarService;
import com.artezio.arttime.utils.CalendarUtils;
import com.artezio.arttime.web.spread_sheet.SpreadSheet;

import junitx.framework.ListAssert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import javax.faces.application.Application;
import javax.faces.application.ViewHandler;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.artezio.arttime.web.EffortsGrouping.BY_PROJECTS;
import static junitx.util.PrivateAccessor.getField;
import static junitx.util.PrivateAccessor.setField;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({CalendarUtils.class})
public class EffortsBeanTest {

    private EffortsBean effortsBean;
    private SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");

    @Before
    public void setUp() {
        effortsBean = new ManageEffortsBean();
    }

    @Test
    public void testGetSpreadSheet_ifNotNull() throws Exception {
        SpreadSheet spreadSheet = createMock(SpreadSheet.class);
        setField(effortsBean, "spreadSheet", spreadSheet);

        SpreadSheet actual = effortsBean.getSpreadSheet();

        assertSame(spreadSheet, actual);
    }

    @Test
    public void testGetSpreadSheet_ifNull() throws Exception {
        effortsBean = createMockBuilder(EffortsBean.class)
            .addMockedMethod("initSpreadSheet")
            .createMock();
        setField(effortsBean, "spreadSheet", null);
        SpreadSheet spreadSheet = createMock(SpreadSheet.class);
        expect(effortsBean.initSpreadSheet()).andReturn(spreadSheet);
        replay(effortsBean);

        SpreadSheet actual = effortsBean.getSpreadSheet();

        verify(effortsBean);
        assertSame(spreadSheet, actual);
    }

    @Test
    public void testResetData() throws Exception {
        effortsBean = createMockBuilder(EffortsBean.class)
            .addMockedMethod("resetComponentsTree")
            .createMock();
        setField(effortsBean, "spreadSheet", createMock(SpreadSheet.class));
        setField(effortsBean, "calendarDays", new HashMap<WorkdaysCalendar, List<Day>>());
        effortsBean.resetComponentsTree();
        replay(effortsBean);

        effortsBean.resetData();

        verify(effortsBean);
        assertNull(getField(effortsBean, "spreadSheet"));
        assertNull(getField(effortsBean, "calendarDays"));
    }

    @Test
    public void testResetComponentsTree() throws Exception {
        FacesContext facesContext = createMock(FacesContext.class);
        Application application = createMock(Application.class);
        ViewHandler viewHandler = createMock(ViewHandler.class);
        UIViewRoot viewRoot = createMock(UIViewRoot.class);
        String viewId = "viewId";
        setField(effortsBean, "facesContext", facesContext);
        expect(facesContext.getApplication()).andReturn(application);
        expect(application.getViewHandler()).andReturn(viewHandler);
        expect(facesContext.getViewRoot()).andReturn(viewRoot);
        expect(viewRoot.getViewId()).andReturn(viewId);
        expect(viewHandler.createView(facesContext, viewId)).andReturn(viewRoot);
        facesContext.setViewRoot(viewRoot);
        replay(facesContext, application, viewHandler, viewRoot);

        effortsBean.resetComponentsTree();

        verify(facesContext, application, viewHandler, viewRoot);
    }

    @Test
    public void testGetStyleClass_ifHoursIsNull() {
        Hours hours = null;

        String actual = effortsBean.getStyleClass(hours);

        assertEquals("emptyCell", actual);
    }

    @Test
    public void testGetStyleClass_ifWorkdayAndApproved() throws Exception {
        Project project = new Project();
        Employee employee = new Employee();
        HourType hourType = new HourType();
        Date date = sdf.parse("1-05-2015");
        Hours hours = new Hours(project, date, employee, hourType);
        hours.setApproved(true);
        effortsBean = createMockBuilder(EffortsBean.class)
                .addMockedMethod("isWeekend")
                .createMock();
        expect(effortsBean.isWeekend(anyObject(WorkdaysCalendar.class), eq(date))).andReturn(false);
        replay(effortsBean);

        String actual = effortsBean.getStyleClass(hours);

        verify(effortsBean);
        assertEquals("approvedHours", actual);
    }

    @Test
    public void testGetStyleClass_ifWeekendAndApproved() throws Exception {
        Project project = new Project();
        Employee employee = new Employee();
        HourType hourType = new HourType();
        Date date = sdf.parse("1-05-2015");
        Hours hours = new Hours(project, date, employee, hourType);
        hours.setApproved(true);
        effortsBean = createMockBuilder(EffortsBean.class)
                .addMockedMethod("isWeekend")
                .createMock();
        expect(effortsBean.isWeekend(anyObject(WorkdaysCalendar.class), eq(date))).andReturn(true);
        replay(effortsBean);

        String actual = effortsBean.getStyleClass(hours);

        verify(effortsBean);
        assertEquals("approvedWeekendHours", actual);
    }

    @Test
    public void testGetStyleClass_ifWeekendAndNotApproved() throws Exception {
        Project project = new Project();
        Employee employee = new Employee();
        HourType hourType = new HourType();
        Date date = sdf.parse("1-05-2015");
        Hours hours = new Hours(project, date, employee, hourType);
        hours.setApproved(false);
        effortsBean = createMockBuilder(EffortsBean.class)
                .addMockedMethod("isWeekend")
                .createMock();
        expect(effortsBean.isWeekend(anyObject(WorkdaysCalendar.class), eq(date))).andReturn(true);
        replay(effortsBean);

        String actual = effortsBean.getStyleClass(hours);

        verify(effortsBean);
        assertEquals("weekendCell", actual);
    }

    @Test
    public void testGetStyleClass_ifWorkdayAndNotApproved() throws Exception {
        Project project = new Project();
        Employee employee = new Employee();
        HourType hourType = new HourType();
        Date date = sdf.parse("1-05-2015");
        Hours hours = new Hours(project, date, employee, hourType);
        hours.setApproved(false);
        effortsBean = createMockBuilder(EffortsBean.class)
                .addMockedMethod("isWeekend")
                .createMock();
        expect(effortsBean.isWeekend(anyObject(WorkdaysCalendar.class), eq(date))).andReturn(false);
        replay(effortsBean);

        String actual = effortsBean.getStyleClass(hours);

        verify(effortsBean);
        assertEquals("", actual);
    }

    @Test
    public void testIsWeekend_ifFalse() throws Exception {
        effortsBean = createMockBuilder(EffortsBean.class)
                .addMockedMethod("getDay", WorkdaysCalendar.class, Date.class)
                .createMock();
        Date date = sdf.parse("1-05-2015");
        WorkdaysCalendar calendar = new WorkdaysCalendar();
        Day day = new Day(date, calendar, true);
        expect(effortsBean.getDay(calendar, date)).andReturn(day);
        replay(effortsBean);

        boolean actual = effortsBean.isWeekend(calendar, date);

        verify(effortsBean);
        assertFalse(actual);
    }

    @Test
    public void testIsWeekend_ifTrue() throws Exception {
        effortsBean = createMockBuilder(EffortsBean.class)
                .addMockedMethod("getDay", WorkdaysCalendar.class, Date.class)
                .createMock();
        Date date = sdf.parse("1-05-2015");
        WorkdaysCalendar calendar = new WorkdaysCalendar();
        Day day = new Day(date, calendar, false);
        expect(effortsBean.getDay(calendar, date)).andReturn(day);
        replay(effortsBean);

        boolean actual = effortsBean.isWeekend(calendar, date);

        verify(effortsBean);
        assertTrue(actual);
    }

    @Test
    public void testGetDay() throws Exception {
        Date date1 = sdf.parse("1-05-2015");
        Date date2 = sdf.parse("2-05-2015");
        WorkdaysCalendar calendar = new WorkdaysCalendar();
        Day day1 = new Day(date1, calendar, false);
        Day day2 = new Day(date2, calendar, false);
        Map<WorkdaysCalendar, List<Day>> calendarDays = new HashMap<WorkdaysCalendar, List<Day>>();
        calendarDays.put(calendar, Arrays.asList(day1, day2));
        setField(effortsBean, "calendarDays", calendarDays);

        Day actual = effortsBean.getDay(calendar, date1);

        assertSame(day1, actual);
    }

    @Test
    public void testGetDay_ifCalendarDaysNotCached() throws Exception {
        Date date1 = sdf.parse("1-05-2015");
        Date date2 = sdf.parse("2-05-2015");
        WorkdaysCalendar calendar = new WorkdaysCalendar();
        Day day1 = new Day(date1, calendar, false);
        Day day2 = new Day(date2, calendar, false);
        Filter filter = new Filter();
        FilterBean filterBean = createMock(FilterBean.class);
        WorkdaysCalendarService workdaysCalendarService = createMock(WorkdaysCalendarService.class);
        setField(effortsBean, "filterBean", filterBean);
        setField(effortsBean, "workdaysCalendarService", workdaysCalendarService);
        expect(filterBean.getCurrentFilter()).andReturn(filter);
        expect(workdaysCalendarService.getDays(calendar, filter.getPeriod())).andReturn(Arrays.asList(day1, day2));
        replay(filterBean, workdaysCalendarService);

        Day actual = effortsBean.getDay(calendar, date1);

        verify(filterBean, workdaysCalendarService);
        assertSame(day1, actual);
    }

    @Test
    public void testGetHeaderStyleClass_ifWeekend() {
        Date date = new Date();
        PowerMock.mockStatic(CalendarUtils.class);
        expect(CalendarUtils.isWeekend(date)).andReturn(true);
        PowerMock.replayAll(CalendarUtils.class);

        String actual = effortsBean.getHeaderStyleClass(date);

        PowerMock.verifyAll();
        assertEquals("weekendHeader", actual);
    }

    @Test
    public void testGetHeaderStyleClass_ifWorkday() {
        Date date = new Date();
        PowerMock.mockStatic(CalendarUtils.class);
        expect(CalendarUtils.isWeekend(date)).andReturn(false);
        PowerMock.replayAll(CalendarUtils.class);

        String actual = effortsBean.getHeaderStyleClass(date);

        PowerMock.verifyAll();
        assertEquals("workdayHeader", actual);
    }

    @Test
    public void testInitByGetRequest() throws Exception {
        Filter filter = new Filter();
        List<Employee> employees = Arrays.asList(new Employee());
        FilterBean filterBean = createMock(FilterBean.class);
        Period period = new Period(sdf.parse("1-01-2015"), sdf.parse("15-01-2015"));
        setField(effortsBean, "filterBean", filterBean);
        setField(effortsBean, "period", period);
        setField(effortsBean, "employees", employees);
        expect(filterBean.getCurrentFilter()).andReturn(filter);
        replay(filterBean);

        effortsBean.initByGetRequest();

        verify(filterBean);
        assertEquals(period, filter.getPeriod());
        ListAssert.assertEquals(employees, new ArrayList<>(filter.getEmployees()));
    }

    @Test
    public void testInitByGetRequest_ifPeriodHasNullDate() throws Exception {
        Filter filter = new Filter();
        List<Employee> employees = Arrays.asList(new Employee());
        FilterBean filterBean = createMock(FilterBean.class);
        Period period = new Period(null, sdf.parse("15-01-2015"));
        setField(effortsBean, "filterBean", filterBean);
        setField(effortsBean, "period", period);
        setField(effortsBean, "employees", employees);

        effortsBean.initByGetRequest();

        assertNotEquals(period, filter.getPeriod());
        assertEquals(0, filter.getEmployees().size());
    }

    @Test
    public void testInitByGetRequest_ifEmployeesIsEmpty() throws Exception {
        Filter filter = new Filter();
        List<Employee> employees = Collections.emptyList();
        FilterBean filterBean = createMock(FilterBean.class);
        Period period = new Period(sdf.parse("1-01-2015"), sdf.parse("15-01-2015"));
        setField(effortsBean, "filterBean", filterBean);
        setField(effortsBean, "period", period);
        setField(effortsBean, "employees", employees);
        expect(filterBean.getCurrentFilter()).andReturn(filter);
        replay(filterBean);

        effortsBean.initByGetRequest();

        verify(filterBean);
        assertEquals(period, filter.getPeriod());
        assertEquals(employees, filter.getEmployees());
    }

    @Test
    public void testInitByGetRequest_ifEmployeesIsNull() throws Exception {
        Filter filter = new Filter();
        List<Employee> employees = null;
        FilterBean filterBean = createMock(FilterBean.class);
        Period period = new Period(sdf.parse("1-01-2015"), sdf.parse("15-01-2015"));
        setField(effortsBean, "filterBean", filterBean);
        setField(effortsBean, "period", period);
        setField(effortsBean, "employees", employees);
        expect(filterBean.getCurrentFilter()).andReturn(filter);
        replay(filterBean);

        effortsBean.initByGetRequest();

        verify(filterBean);
        assertEquals(period, filter.getPeriod());
        assertEquals(Collections.emptyList(), filter.getEmployees());
    }

    @Test
    public void testGetGrouping_GroupingIsNotNull() throws NoSuchFieldException {
        setField(effortsBean, "grouping", BY_PROJECTS);

        EffortsGrouping expected = BY_PROJECTS;
        EffortsGrouping actual = effortsBean.getGrouping();
        assertEquals(expected, actual);
    }

    @Test
    public void testGetGrouping_GroupingIsNullCookieIsNull() throws NoSuchFieldException {
        EffortsGrouping defaultGrouping = BY_PROJECTS;
        FacesContext facesContext = createMock(FacesContext.class);
        ExternalContext externalContext = createMock(ExternalContext.class);
        HttpServletRequest httpServletRequest = createMock(HttpServletRequest.class);
        HttpServletResponse httpServletResponse = createMock(HttpServletResponse.class);
        Cookie[] cookies = new Cookie[0];

        setField(effortsBean, "facesContext", facesContext);
        expect(facesContext.getExternalContext()).andReturn(externalContext).anyTimes();
        expect(externalContext.getRequest()).andReturn(httpServletRequest);
        expect(externalContext.getResponse()).andReturn(httpServletResponse);
        expect(httpServletRequest.getCookies()).andReturn(cookies).anyTimes();
        replay(facesContext, externalContext, httpServletRequest);

        EffortsGrouping expected = defaultGrouping;
        EffortsGrouping actual = effortsBean.getGrouping();
        assertEquals(expected, actual);
    }

    @Test
    public void testGetGrouping_GroupingIsNullCookieIsNotNull() throws NoSuchFieldException {
        FacesContext facesContext = createMock(FacesContext.class);
        ExternalContext externalContext = createMock(ExternalContext.class);
        HttpServletRequest httpServletRequest = createMock(HttpServletRequest.class);
        HttpServletResponse httpServletResponse = createMock(HttpServletResponse.class);
        Cookie[] cookies = new Cookie[] {new Cookie("efforts_grouping", BY_PROJECTS.toString())};

        setField(effortsBean, "facesContext", facesContext);
        expect(facesContext.getExternalContext()).andReturn(externalContext).anyTimes();
        expect(externalContext.getRequest()).andReturn(httpServletRequest);
        expect(externalContext.getResponse()).andReturn(httpServletResponse);
        expect(httpServletRequest.getCookies()).andReturn(cookies);
        replay(facesContext, externalContext, httpServletRequest);

        EffortsGrouping expected = BY_PROJECTS;
        EffortsGrouping actual = effortsBean.getGrouping();
        assertEquals(expected, actual);
    }

    @Test
    public void testExtractEffortsGroupingFromCookie() throws Exception {
        FacesContext facesContext = createMock(FacesContext.class);
        ExternalContext externalContext = createMock(ExternalContext.class);
        HttpServletRequest httpServletRequest = createMock(HttpServletRequest.class);
        HttpServletResponse httpServletResponse = createMock(HttpServletResponse.class);
        Cookie[] cookies = new Cookie[] {new Cookie("efforts_grouping", BY_PROJECTS.toString())};

        setField(effortsBean, "facesContext", facesContext);
        expect(facesContext.getExternalContext()).andReturn(externalContext).anyTimes();
        expect(externalContext.getRequest()).andReturn(httpServletRequest);
        expect(externalContext.getResponse()).andReturn(httpServletResponse);
        expect(httpServletRequest.getCookies()).andReturn(cookies).anyTimes();
        replay(facesContext, externalContext, httpServletRequest);

        EffortsGrouping expected = BY_PROJECTS;
        EffortsGrouping actual = Whitebox.invokeMethod(effortsBean, "extractEffortsGroupingFromCookie");
        assertEquals(expected, actual);
    }

}

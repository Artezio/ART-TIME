package com.artezio.arttime.web.components;

import com.artezio.arttime.datamodel.Employee;
import com.artezio.arttime.datamodel.HourType;
import com.artezio.arttime.datamodel.Period;
import com.artezio.arttime.filter.Filter;
import com.artezio.arttime.repositories.HourTypeRepository;
import com.artezio.arttime.repositories.HoursRepository;
import com.artezio.arttime.services.EmployeeService;
import com.artezio.arttime.services.HourTypeService;
import com.artezio.arttime.services.WorkTimeService;
import com.artezio.arttime.utils.MessagesUtil;
import com.artezio.arttime.web.FilterBean;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.faces.application.Application;
import javax.faces.context.FacesContext;
import java.math.BigDecimal;
import java.util.Optional;

import static junitx.util.PrivateAccessor.setField;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({FacesContext.class, MessagesUtil.class})
public class ReportedHoursIndicatorTest {

    private ReportedHoursIndicator hoursIndicator;
    private WorkTimeService workTimeService;
    private EmployeeService employeeService;
    private Period period;
    private Filter filter;
    private HourTypeService hourTypeService;
    private String userName = "employ";
    private Employee loggedEmployee;

    @Before
    public void setUp() throws Exception {
        loggedEmployee = new Employee(userName);
        hoursIndicator = createMockBuilder(ReportedHoursIndicator.class).addMockedMethod("getFilter").createMock();
        period = new Period();
        workTimeService = createMock(WorkTimeService.class);
        employeeService = createMock(EmployeeService.class);
        filter = createMock(Filter.class);
        hourTypeService = createMock(HourTypeService.class);
        expect(employeeService.getLoggedEmployee()).andReturn(Optional.of(loggedEmployee)).anyTimes();
        replay(employeeService);

        setField(hoursIndicator, "workTimeService", workTimeService);
        setField(hoursIndicator, "employeeService", employeeService);
        setField(hoursIndicator, "hourTypeService", hourTypeService);
    }

    @Test
    public void testGetRequiredTime() {
        BigDecimal expected = new BigDecimal("160");
        expect(hoursIndicator.getFilter()).andReturn(filter);
        expect(filter.getPeriod()).andReturn(period);
        expect(workTimeService.getRequiredWorkHours(loggedEmployee, period)).andReturn(expected);
        replay(hoursIndicator, filter, workTimeService);

        BigDecimal actual = hoursIndicator.getRequiredTime();

        verify(hoursIndicator, filter, workTimeService);
        assertEquals(expected, actual);
    }

    @Test
    public void testGetReportedTime() {
        BigDecimal expected = new BigDecimal("80");
        expect(hoursIndicator.getFilter()).andReturn(filter);
        expect(filter.getPeriod()).andReturn(period);
        expect(workTimeService.getActualWorkHours(loggedEmployee, period)).andReturn(expected);
        replay(workTimeService, filter, hoursIndicator);

        BigDecimal actual = hoursIndicator.getReportedTime();

        verify(workTimeService, filter, hoursIndicator);
        assertEquals(expected, actual);
    }

    @Test
    public void testGetReportedTimePercents() {
        expect(hoursIndicator.getFilter()).andReturn(filter).times(2);
        expect(filter.getPeriod()).andReturn(period).times(2);
        expect(workTimeService.getRequiredWorkHours(loggedEmployee, period)).andReturn(new BigDecimal("160"));
        expect(workTimeService.getActualWorkHours(loggedEmployee, period)).andReturn(new BigDecimal("100"));
        replay(workTimeService, filter, hoursIndicator);

        int actual = hoursIndicator.getReportedTimePercents();

        assertEquals(62, actual);
        verify(workTimeService, filter, hoursIndicator);
    }

    @Test
    public void testGetReportedTimePercents_RoundHalfDown() {
        expect(hoursIndicator.getFilter()).andReturn(filter).times(2);
        expect(filter.getPeriod()).andReturn(period).times(2);
        expect(workTimeService.getRequiredWorkHours(loggedEmployee, period)).andReturn(new BigDecimal("160"));
        expect(workTimeService.getActualWorkHours(loggedEmployee, period)).andReturn(new BigDecimal("4"));
        replay(workTimeService, filter, hoursIndicator);

        int actual = hoursIndicator.getReportedTimePercents();

        assertEquals(2, actual);
        verify(workTimeService, filter, hoursIndicator);
    }

    @Test
    public void testGetOverTimePercents() {
        expect(hoursIndicator.getFilter()).andReturn(filter).times(2);
        expect(filter.getPeriod()).andReturn(period).times(2);
        expect(workTimeService.getRequiredWorkHours(loggedEmployee, period)).andReturn(new BigDecimal("160"));
        expect(workTimeService.getActualWorkHours(loggedEmployee, period)).andReturn(new BigDecimal("180"));
        replay(workTimeService, filter, hoursIndicator);

        int actual = hoursIndicator.getOverTimePercents();

        assertEquals(11, actual);
        verify(workTimeService, filter, hoursIndicator);
    }
    
    @Test
    public void testGetOverTimePercents_ifReportedLessThenRequired() {
        expect(hoursIndicator.getFilter()).andReturn(filter).times(2);
        expect(filter.getPeriod()).andReturn(period).times(2);
        expect(workTimeService.getRequiredWorkHours(loggedEmployee, period)).andReturn(new BigDecimal("180"));
        expect(workTimeService.getActualWorkHours(loggedEmployee, period)).andReturn(new BigDecimal("160"));
        replay(workTimeService, filter, hoursIndicator);

        int actual = hoursIndicator.getOverTimePercents();

        assertEquals(0, actual);
        verify(workTimeService, filter, hoursIndicator);
    }

    @Test
    public void testIsTimeReportedIncorrectly_NotAllTimeReported() {
        expect(hoursIndicator.getFilter()).andReturn(filter).times(2);
        expect(filter.getPeriod()).andReturn(period).times(2);
        expect(workTimeService.getRequiredWorkHours(loggedEmployee, period)).andReturn(new BigDecimal("160"));
        expect(workTimeService.getActualWorkHours(loggedEmployee, period)).andReturn(new BigDecimal("80"));
        replay(workTimeService, filter, hoursIndicator);

        assertTrue(hoursIndicator.isTimeReportedIncorrectly());
        verify(workTimeService, filter, hoursIndicator);
    }

    @Test
    public void testIsTimeReportedIncorrectly() {
        expect(hoursIndicator.getFilter()).andReturn(filter).times(2);
        expect(filter.getPeriod()).andReturn(period).times(2);
        expect(workTimeService.getRequiredWorkHours(loggedEmployee, period)).andReturn(new BigDecimal("160"));
        expect(workTimeService.getActualWorkHours(loggedEmployee, period)).andReturn(new BigDecimal("160"));
        replay(workTimeService, filter, hoursIndicator);

        assertFalse(hoursIndicator.isTimeReportedIncorrectly());
        verify(workTimeService, filter, hoursIndicator);
    }

    @Test
    public void testIsTimeReportedIncorrectly_DifferentScale() {
        expect(hoursIndicator.getFilter()).andReturn(filter).times(2);
        expect(filter.getPeriod()).andReturn(period).times(2);
        expect(workTimeService.getRequiredWorkHours(loggedEmployee, period)).andReturn(new BigDecimal("160"));
        expect(workTimeService.getActualWorkHours(loggedEmployee, period)).andReturn(new BigDecimal("160.00"));
        replay(workTimeService, filter, hoursIndicator);

        assertFalse(hoursIndicator.isTimeReportedIncorrectly());
        verify(workTimeService, filter, hoursIndicator);
    }

    @Test
    public void testGetWarningMessage_withoutActualTime(){
    	expect(hourTypeService.findActualTime()).andReturn(null);
    	replay(hourTypeService);
    	String message = hoursIndicator.getWarningMessage();
    	
    	verify(hourTypeService);
    	assertTrue(StringUtils.isEmpty(message));
    }
    
    @Test
    public void testGWarningMessage(){
    	expect(hourTypeService.findActualTime()).andReturn(new HourType());
    	PowerMock.mockStatic(MessagesUtil.class);
    	expect(MessagesUtil.getLocalizedString(anyString(), anyObject())).andReturn("mockString");
    	PowerMock.replayAll(hourTypeService, MessagesUtil.class);

    	String message = hoursIndicator.getWarningMessage();
    	PowerMock.verifyAll();
    	assertTrue(!StringUtils.isEmpty(message));
    	
    }
    
    @Test
    public void testGetFilter() {
    	hoursIndicator = new ReportedHoursIndicator();
    	FacesContext facesContext = createMock(FacesContext.class);
    	Application application = createMock(Application.class);
    	FilterBean filterBean = createMock(FilterBean.class);
    	Filter filter = new Filter();
    	PowerMock.mockStatic(FacesContext.class);
    	expect(FacesContext.getCurrentInstance()).andReturn(facesContext);
    	expect(facesContext.getApplication()).andReturn(application);
    	expect(application.evaluateExpressionGet(facesContext, "#{filterBean}", Object.class)).andReturn(filterBean);
    	expect(filterBean.getCurrentFilter()).andReturn(filter);
    	PowerMock.replayAll(FacesContext.class, facesContext, application, filterBean);
    	
    	Filter actual = hoursIndicator.getFilter();
    	
    	PowerMock.verifyAll();
    	assertSame(filter, actual);
    }
}
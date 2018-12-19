package com.artezio.arttime.web;

import com.artezio.arttime.datamodel.Day;
import com.artezio.arttime.datamodel.Period;
import com.artezio.arttime.datamodel.WorkdaysCalendar;
import com.artezio.arttime.services.WorkdaysCalendarService;
import com.artezio.arttime.utils.CalendarUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.primefaces.event.SelectEvent;

import javax.faces.context.ExternalContext;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static junitx.util.PrivateAccessor.getField;
import static junitx.util.PrivateAccessor.setField;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;
import static org.powermock.api.easymock.PowerMock.replayAll;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ CalendarUtils.class })
public class WorkdaysCalendarBeanTest {

    private WorkdaysCalendarBean calendarBean;
    private ExternalContext externalContext;
    private WorkdaysCalendarService workdaysCalendarService;
    private SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");

    @Before
    public void setUp() throws NoSuchFieldException {
        calendarBean = new WorkdaysCalendarBean();
        externalContext = createMock(ExternalContext.class);
        workdaysCalendarService = createMock(WorkdaysCalendarService.class);
        setField(calendarBean, "externalContext", externalContext);
        setField(calendarBean, "workdaysCalendarService", workdaysCalendarService);
    }

    @Test
    public void testCreate() throws NoSuchFieldException {
        WorkdaysCalendar workdaysCalendar = new WorkdaysCalendar();
        setField(calendarBean, "workdaysCalendar", workdaysCalendar);
        expect(workdaysCalendarService.create(workdaysCalendar)).andReturn(workdaysCalendar);
        replay(workdaysCalendarService);

        calendarBean.create();

        verify(workdaysCalendarService);
    }

    @Test
    public void testUpdateWorkdaysCalendar() throws NoSuchFieldException {
        WorkdaysCalendar workdaysCalendar = new WorkdaysCalendar();
        List<Day> days = new ArrayList<Day>();
        setField(calendarBean, "workdaysCalendar", workdaysCalendar);
        workdaysCalendarService.update(workdaysCalendar, days);
        replay(workdaysCalendarService);

        calendarBean.updateWorkdaysCalendar(days);

        verify(workdaysCalendarService);
    }

    @Test
    @Ignore
    public void testGetSelectedDate_SelectedDateIsNull() {
        Date expected = new Date();
        Date actual = calendarBean.getSelectedDate();

        assertEquals(expected, actual);
    }

    @Test
    public void testGetSelectedDate_SelectedDateIsNotNull() throws NoSuchFieldException {
        Date selectedDate = new Date();
        setField(calendarBean, "selectedDate", selectedDate);

        Date expected = selectedDate;
        Date actual = calendarBean.getSelectedDate();
        assertEquals(expected, actual);
    }

    @Test
    public void testGetDaysOff() throws NoSuchFieldException, ParseException {
        DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        Period extendedPeriod = new Period(df.parse("01-12-2014"), df.parse("28-02-2015"));
        WorkdaysCalendar workdaysCalendar = new WorkdaysCalendar();
        setField(calendarBean, "workdaysCalendar", workdaysCalendar);
        setField(calendarBean, "extendedPeriod", extendedPeriod);
        Day day1 = new Day(df.parse("01-01-2015"), workdaysCalendar);
        day1.setWorking(false);
        Day day2 = new Day(df.parse("02-01-2015"), workdaysCalendar);
        day2.setWorking(true);
        Map<Date, Day> days = new HashMap<Date, Day>();
        days.put(day1.getDate(), day1);
        days.put(day2.getDate(), day2);
        setField(calendarBean, "days", days);
        assertEquals("1-1-2015", calendarBean.getDaysOff());
    }

    @Test
    public void testPopulateDays() throws NoSuchFieldException, ParseException {
        Map<Date, Day> days = new HashMap<Date, Day>();
        setField(calendarBean, "days", days);
        DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        Period extendedPeriod = new Period(df.parse("01-12-2014"), df.parse("28-02-2015"));
        setField(calendarBean, "extendedPeriod", extendedPeriod);
        WorkdaysCalendar workdaysCalendar = new WorkdaysCalendar();
        setField(calendarBean, "workdaysCalendar", workdaysCalendar);
        List<Day> result = new ArrayList<Day>();
        for (Date date : extendedPeriod.getDays()) {
            result.add(new Day(date, workdaysCalendar));
        }
        expect(workdaysCalendarService.getDays(workdaysCalendar, extendedPeriod)).andReturn(result);
        replay(workdaysCalendarService);

        calendarBean.populateDays();

        verify(workdaysCalendarService);
        assertArrayEquals(result.toArray(), days.values().toArray());
    }

    @Test
    public void testInit_ifCalendarPassAsParam() throws Exception {
        calendarBean = createMockBuilder(WorkdaysCalendarBean.class)
            .addMockedMethod("updatePeriod", Date.class, Date.class)
            .createMock();
        setField(calendarBean, "externalContext", externalContext);
        setField(calendarBean, "workdaysCalendarService", workdaysCalendarService);
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        Date date1 = sdf.parse("1-01-2015");
        Date date2 = sdf.parse("31-01-2015");
        WorkdaysCalendar calendar = new WorkdaysCalendar();
        Map<String, String> requestParams = new HashMap<String, String>();
        requestParams.put("calendar", "1");
        PowerMock.mockStatic(CalendarUtils.class);
        expect(externalContext.getRequestParameterMap()).andReturn(requestParams);
        expect(workdaysCalendarService.findById(1L)).andReturn(calendar);
        expect(CalendarUtils.firstDayOfMonth(anyObject(Date.class))).andReturn(date1);
        expect(CalendarUtils.lastDayOfMonth(anyObject(Date.class))).andReturn(date2);
        replayAll(CalendarUtils.class, externalContext, workdaysCalendarService);

        calendarBean.init();

        PowerMock.verifyAll();
    }

    @Test
    public void testInit_ifCalendarNotPassAsParam() throws Exception {
        setField(calendarBean, "externalContext", externalContext);
        Map<String, String> requestParams = new HashMap<String, String>();
        expect(externalContext.getRequestParameterMap()).andReturn(requestParams);
        replay(externalContext);

        calendarBean.init();

        verify(externalContext);
        assertNotNull(getField(calendarBean, "workdaysCalendar"));
    }

    @Test
    public void testOnDateSelect() throws Exception {
        SelectEvent event = createMock(SelectEvent.class);
        Date date1 = sdf.parse("1-01-2015");
        Date date2 = sdf.parse("2-01-2015");
        Period period = new Period(sdf.parse("1-01-2015"), sdf.parse("16-01-2015"));
        Map<Date, Day> days = new HashMap<Date, Day>();
        days.put(date1, new Day(date1, null, true));
        days.put(date2, new Day(date2, null, true));
        setField(calendarBean, "days", days);
        setField(calendarBean, "extendedPeriod", period);
        expect(event.getObject()).andReturn(date1);
        replay(event);

        calendarBean.onDateSelect(event);

        verify(event);
        assertFalse(days.get(date1).isWorking());
        assertNotNull(calendarBean.getDaysOff());
    }

    @Test
    public void testNextMonth() throws Exception {
        Map<String, String> map = new HashMap<>();
        map.put("monthCount", "1");
        Period period = new Period(sdf.parse("1-01-2015"), sdf.parse("31-01-2015"));
        setField(calendarBean, "period", period);
        Period expected = new Period(sdf.parse("1-02-2015"), sdf.parse("28-02-2015"));
        expect(externalContext.getRequestParameterMap()).andReturn(map);
        expect(workdaysCalendarService.getDays(anyObject(WorkdaysCalendar.class), anyObject(Period.class))).andReturn(new ArrayList<Day>());
        replayAll(workdaysCalendarService, externalContext);

        calendarBean.nextMonth();

        verify(workdaysCalendarService);
        Period actual = (Period) getField(calendarBean, "period");
        assertEquals(expected, actual);
    }

    @Test
    public void testPrevMonth() throws Exception {
        Map<String, String> map = new HashMap<>();
        map.put("monthCount", "1");
        Period period = new Period(sdf.parse("1-01-2015"), sdf.parse("31-01-2015"));
        setField(calendarBean, "period", period);
        Period expected = new Period(sdf.parse("1-12-2014"), sdf.parse("31-12-2014"));
        expect(externalContext.getRequestParameterMap()).andReturn(map);
        expect(workdaysCalendarService.getDays(anyObject(WorkdaysCalendar.class), anyObject(Period.class))).andReturn(new ArrayList<Day>());
        replayAll(workdaysCalendarService, externalContext);

        calendarBean.prevMonth();

        verify(workdaysCalendarService);
        Period actual = (Period) getField(calendarBean, "period");
        assertEquals(expected, actual);
    }

    @Test
    public void testGetDaysOff_ifDayIsWorking() throws Exception {
        Period extendedPeriod = new Period(sdf.parse("01-12-2014"), sdf.parse("28-02-2015"));
        setField(calendarBean, "extendedPeriod", extendedPeriod);
        Date date = sdf.parse("1-01-2015");
        Map<Date, Day> days = new HashMap<Date, Day>();
        days.put(date, new Day(date, null, true));
        setField(calendarBean, "days", days);

        assertEquals(calendarBean.getDaysOff(), "");
    }

    @Test
    public void testUpdateDaysOff_ifExtendedPeriodNotContainsDate() throws Exception {
        Period extendedPeriod = new Period(sdf.parse("01-12-2014"), sdf.parse("28-02-2015"));
        setField(calendarBean, "extendedPeriod", extendedPeriod);
        Date date = sdf.parse("1-03-2015");
        Map<Date, Day> days = new HashMap<Date, Day>();
        days.put(date, new Day(date, null, false));
        setField(calendarBean, "days", days);

        assertEquals(calendarBean.getDaysOff(), "");
    }

    @Test
    public void getDays() throws Exception {
        Day day1 = createDay(1L, sdf.parse("1-01-2015"));
        Day day2 = createDay(2L, sdf.parse("2-01-2015"));
        Day day3 = createDay(3L, sdf.parse("3-01-2015"));
        Map<Date, Day> days = new HashMap<Date, Day>();
        days.put(day1.getDate(), day1);
        days.put(day2.getDate(), day2);
        days.put(day3.getDate(), day3);
        setField(calendarBean, "days", days);
        List<Day> expected = Arrays.asList(day1, day2, day3);

        List<Day> actual = calendarBean.getDays();

        actual.sort(Day.DATE_COMPARATOR);
        assertEquals(expected, actual);
    }

    @Test
    public void testGetDaysShift_ifExist() throws ParseException, Exception {
        DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        Period period = new Period(df.parse("01-12-2014"), df.parse("28-02-2015"));
        WorkdaysCalendar workdaysCalendar = new WorkdaysCalendar();
        setField(calendarBean, "workdaysCalendar", workdaysCalendar);
        Day day1 = createDay(1L, sdf.parse("1-01-2015"));
        day1.setShiftedFrom(sdf.parse("1-01-2015"));
        day1.setShiftedTo(sdf.parse("5-01-2015"));
        Map<Date, Day> days = new HashMap<Date, Day>();
        days.put(day1.getDate(), day1);
        setField(calendarBean, "days", days);
        setField(calendarBean, "period", period);
        List<Day> expected = Arrays.asList(day1);

        List<Day> actual = calendarBean.getDaysShift();

        assertEquals(expected, actual);
    }

    @Test
    public void testGetDaysShift_ifNotExist() throws ParseException, Exception {
        Day day1 = createDay(1L, sdf.parse("1-01-2015"));
        Map<Date, Day> days = new HashMap<Date, Day>();
        days.put(day1.getDate(), day1);
        setField(calendarBean, "days", days);

        List<Day> actual = calendarBean.getDaysShift();

        assertTrue(actual.isEmpty());
    }

    @Test
    public void testGetHolidays() throws ParseException, Exception {
        Day day1 = createDay(1L, sdf.parse("1-01-2015"));
        Day day2 = createDay(2L, sdf.parse("2-01-2015"));
        day1.setHoliday(true);
        day2.setHoliday(false);
        Map<Date, Day> days = new HashMap<Date, Day>();
        days.put(day1.getDate(), day1);
        days.put(day2.getDate(), day2);
        Period extendedPeriod = new Period(sdf.parse("01-12-2014"), sdf.parse("28-02-2015"));
        setField(calendarBean, "extendedPeriod", extendedPeriod);
        setField(calendarBean, "days", days);

        String actual = calendarBean.getHolidays();

        assertEquals("1-1-2015", actual);
    }

    @Test
    public void testAddDayShift_ifWorkingDayShiftToWeekend() throws Exception{
        DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        Period period = new Period(df.parse("01-12-2014"), df.parse("28-02-2015"));
        Day day1 = createDay(1L, sdf.parse("1-01-2015"));
        Day day2 = createDay(2L, sdf.parse("5-01-2015"));
        day1.setWorking(true);
        day2.setWorking(false);
        Map<Date, Day> days = new HashMap<Date, Day>();
        days.put(day1.getDate(), day1);
        days.put(day2.getDate(), day2);
        setField(calendarBean, "days", days);
        setField(calendarBean, "shiftFrom", sdf.parse("1-01-2015"));
        setField(calendarBean, "shiftTo", sdf.parse("5-01-2015"));
        setField(calendarBean, "period", period);

        calendarBean.addDayShift();

        assertArrayEquals(calendarBean.getDaysShift().toArray(), new Day[]{day1});
    }

    @Test
    public void testAddDayShift_ifWeekendShiftToWorkingDay() throws Exception{
        DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        Period period = new Period(df.parse("01-12-2014"), df.parse("28-02-2015"));
        Day day1 = createDay(1L, sdf.parse("1-01-2015"));
        Day day2 = createDay(2L, sdf.parse("5-01-2015"));
        day1.setWorking(false);
        day2.setWorking(true);
        Map<Date, Day> days = new HashMap<Date, Day>();
        days.put(day1.getDate(), day1);
        days.put(day2.getDate(), day2);
        setField(calendarBean, "days", days);
        setField(calendarBean, "shiftFrom", sdf.parse("1-01-2015"));
        setField(calendarBean, "shiftTo", sdf.parse("5-01-2015"));
        setField(calendarBean, "period", period);

        calendarBean.addDayShift();

        assertArrayEquals(calendarBean.getDaysShift().toArray(), new Day[]{day1});
    }

    @Test
    public void testRemoveDayShift() throws Exception{
        Day day1 = createDay(1L, sdf.parse("1-01-2015"));
        Day day2 = createDay(2L, sdf.parse("5-01-2015"));
        day1.setShiftedFrom(day1.getDate());
        day1.setShiftedTo(day2.getDate());
        day1.setWorking(true);
        Map<Date, Day> days = new HashMap<Date, Day>();
        days.put(day1.getDate(), day1);
        days.put(day2.getDate(), day2);
        setField(calendarBean, "days", days);

        calendarBean.removeDayShift(day1);

        assertArrayEquals(calendarBean.getDaysShift().toArray(), new Day[]{});
    }

    private Day createDay(Long id, Date date) throws Exception {
        WorkdaysCalendar workdaysCalendar = new WorkdaysCalendar();
        setField(calendarBean, "workdaysCalendar", workdaysCalendar);
        Day day = new Day(date,workdaysCalendar);
        setField(day, "id", id);
        return day;
    }

}

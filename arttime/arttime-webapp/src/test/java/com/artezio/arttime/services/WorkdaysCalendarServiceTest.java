package com.artezio.arttime.services;

import com.artezio.arttime.datamodel.Day;
import com.artezio.arttime.datamodel.Period;
import com.artezio.arttime.datamodel.WorkdaysCalendar;
import com.artezio.arttime.repositories.WorkdaysCalendarRepository;
import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.*;

import static com.artezio.arttime.test.utils.CalendarUtils.getOffsetDate;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(EasyMockRunner.class)
public class WorkdaysCalendarServiceTest {

    @TestSubject
    private WorkdaysCalendarService workdaysCalendarService = new WorkdaysCalendarService();
    @Mock
    private WorkdaysCalendarRepository workdaysCalendarRepository;

    @Test
    public void testGetCalendarsByDepartments() {
        WorkdaysCalendar calendar1 = new WorkdaysCalendar("cal1");
        calendar1.getDepartments().add("Minsk");
        calendar1.getDepartments().add("Moscow");
        WorkdaysCalendar calendar2 = new WorkdaysCalendar("cal2");
        calendar2.getDepartments().add("Vitebsk");
        Map<String, WorkdaysCalendar> expected = new HashMap<>();
        expected.put("Minsk", calendar1);
        expected.put("Moscow", calendar1);
        expected.put("Vitebsk", calendar2);

        expect(workdaysCalendarRepository.getWorkdaysCalendars()).andReturn(Arrays.asList(calendar1, calendar2));
        replay(workdaysCalendarRepository);

        Map<String, WorkdaysCalendar> actual = workdaysCalendarService.getCalendarsByDepartments();

        assertEquals(expected, actual);
    }

    @Test
    public void testCreateDays() {
        Date date1 = new GregorianCalendar(2011, 1, 3).getTime();
        Date date2 = new GregorianCalendar(2011, 1, 0).getTime();
        List<Date> dates = Arrays.asList(date1, date2);
        WorkdaysCalendar workdaysCalendar = new WorkdaysCalendar("calendar");

        List<Day> actuals = workdaysCalendarService.createDays(dates, workdaysCalendar);

        assertEquals(2, actuals.size());
        Day actual = actuals.get(0);
        assertEquals(date1, actual.getDate());
        assertEquals(workdaysCalendar.getName(), actual.getWorkdaysCalendar().getName());
    }

    @Test
    public void testGetDays() {
        Date start = new Date();
        Date finish = getOffsetDate(2);
        Period period = new Period(start, finish);
        WorkdaysCalendar calendar = new WorkdaysCalendar("calendar");
        Day day = new Day(new Date(), calendar);

        expect(workdaysCalendarRepository.getSpecialDays(anyObject(), anyObject())).andReturn(new ArrayList<>());
        replay(workdaysCalendarRepository);

        List<Day> actual = workdaysCalendarService.getDays(calendar, period);

        assertNotNull(actual);
        assertEquals(3, actual.size());
    }

}

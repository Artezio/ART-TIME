package com.artezio.arttime.datamodel;

import com.ibm.icu.text.SimpleDateFormat;
import org.junit.Test;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import static org.junit.Assert.*;

public class DayTest {

	@Test
	public void testIsWeekend_SundayDate() {
		WorkdaysCalendar workdaysCalendar = new WorkdaysCalendar("calendar");
		Date sundayDate = new Date(2011, 0, Calendar.SUNDAY);
		Day day = new Day(sundayDate, workdaysCalendar);

		assertTrue(day.isWeekend());
	}

	@Test
	public void testIsWeekend_SaturdayDate() {
		WorkdaysCalendar workdaysCalendar = new WorkdaysCalendar("calendar");
		Date saturdayDate = new Date(2011, 0, Calendar.SATURDAY);
		Day day = new Day(saturdayDate, workdaysCalendar);

		assertTrue(day.isWeekend());
	}

	@Test
	public void testSwitchDayType_ifWeekend() {
		Day day = new Day();
		day.setWorking(false);

		day.switchDayType();

		assertTrue(day.isHoliday());
		assertFalse(day.isWorking());
	}

	@Test
	public void testSwitchDayType_ifWorkday() {
		Day day = new Day();
		day.setWorking(true);

		day.switchDayType();

		assertFalse(day.isWorking());
		assertFalse(day.isHoliday());
	}

	@Test
	public void testSwitchDayType_ifHoliday() {
		Day day = new Day();
		day.setHoliday(true);

		day.switchDayType();

		assertTrue(day.isWorking());
		assertFalse(day.isHoliday());
	}

	@Test
	public void testConstructor_Date_Calendar_isWorking() {
		Date date = new Date();
		WorkdaysCalendar workdaysCalendar = new WorkdaysCalendar();

		Day actual = new Day(date, workdaysCalendar, false);

		assertSame(date, actual.getDate());
		assertSame(workdaysCalendar, actual.getWorkdaysCalendar());
		assertFalse(actual.isWorking());
	}

	@Test
	public void testToString() throws Exception {
		WorkdaysCalendar workdaysCalendar = new WorkdaysCalendar("calendar");
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
		DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
		Day day = new Day(sdf.parse("1-01-2014"), workdaysCalendar);
		day.setWorking(true);
		String expected = "Day {date=" + df.format(day.getDate()) + ", isWorking=true}";

		String actual = day.toString();

		assertEquals(expected, actual);
	}

    @Test
    public void testSettingHolidayChangedWorking() {
        Day day = new Day();
        day.setWorking(true);
        day.setHoliday(true);
        assertFalse(day.isWorking());
        assertTrue(day.isHoliday());
    }

}

package com.artezio.arttime.datamodel;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class WorkdaysCalendarTest {
	
	@Test
	public void testToString() {
		WorkdaysCalendar workdaysCalendar = new WorkdaysCalendar("Belarus");
		String expected = "WorkdaysCalendar {id=null, name=Belarus}";
		
		String actual = workdaysCalendar.toString();
		
		assertEquals(expected, actual);
	}
}

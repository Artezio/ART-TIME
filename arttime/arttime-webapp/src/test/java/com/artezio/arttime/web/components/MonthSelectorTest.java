package com.artezio.arttime.web.components;

import static junitx.util.PrivateAccessor.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.artezio.arttime.datamodel.Period;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Locale.class)
public class MonthSelectorTest {
	private MonthSelector monthSelector;
	private static SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
	
	@Before
	public void setUp() {
		monthSelector = new MonthSelector();
	}
	
	@Test
	public void setPreviousPeriod() throws ParseException, NoSuchFieldException {
		Date firstDayOfMonth = sdf.parse("1-05-2014");
		Date lastDayOfMonth = sdf.parse("31-05-2014");
		Period period = new Period(firstDayOfMonth, lastDayOfMonth);
		setField(monthSelector, "period", period);
		PowerMock.mockStatic(Locale.class);
		expect(Locale.getDefault()).andReturn(Locale.US);
		Period expected = new Period(sdf.parse("1-04-2014"), sdf.parse("30-04-2014"));
		
		monthSelector.setPreviousPeriod();
		
		Period actual = (Period) getField(monthSelector, "period");
		assertEquals(expected, actual);
	}
	
	@Test
	public void setNextPeriod() throws ParseException, NoSuchFieldException {
		Date firstDayOfMonth = sdf.parse("1-05-2014");
		Date lastDayOfMonth = sdf.parse("31-05-2014");
		Period period = new Period(firstDayOfMonth, lastDayOfMonth);
		setField(monthSelector, "period", period);
		PowerMock.mockStatic(Locale.class);
		expect(Locale.getDefault()).andReturn(Locale.US);
		Period expected = new Period(sdf.parse("1-06-2014"), sdf.parse("30-06-2014"));
		
		monthSelector.setNextPeriod();
		
		Period actual = (Period) getField(monthSelector, "period");
		assertEquals(expected, actual);
	}
	
	@Test
	public void testInit() throws Exception {
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
		monthSelector = createMockBuilder(MonthSelector.class)
				.addMockedMethod("getAttributes")
				.createMock();
		Period period = new Period(sdf.parse("1-01-2015"), sdf.parse("31-01-2015"));
		Map<String, Object> attrs = new HashMap<String, Object>();
		attrs.put("period", period);
		expect(monthSelector.getAttributes()).andReturn(attrs);
		replay(monthSelector);
		
		monthSelector.init();
		
		verify(monthSelector);
		assertEquals(period, getField(monthSelector, "period"));
	}
}

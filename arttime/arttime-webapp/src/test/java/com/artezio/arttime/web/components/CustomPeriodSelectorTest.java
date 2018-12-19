package com.artezio.arttime.web.components;

import static junitx.util.PrivateAccessor.getField;
import static junitx.util.PrivateAccessor.setField;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.artezio.arttime.datamodel.Period;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Locale.class)
public class CustomPeriodSelectorTest {
	private CustomPeriodSelector periodSelector;
	private static SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
	
	@Before
	public void setUp() {
		periodSelector = new CustomPeriodSelector();
	}
	
	@Test
	public void setPreviousPeriod() throws ParseException, NoSuchFieldException {
		Date firstDayOfPeriod = sdf.parse("1-05-2014");
		Date lastDayOfPeriod = sdf.parse("10-05-2014");
		Period period = new Period(firstDayOfPeriod, lastDayOfPeriod);
		setField(periodSelector, "period", period);
		PowerMock.mockStatic(Locale.class);
		expect(Locale.getDefault()).andReturn(Locale.US);
		Period expected = new Period(sdf.parse("21-04-2014"), sdf.parse("30-04-2014"));
		
		periodSelector.setPreviousPeriod();
		
		Period actual = (Period) getField(periodSelector, "period");
		assertEquals(expected, actual);
	}
	
	@Test
	public void setNextPeriod() throws ParseException, NoSuchFieldException {
		Date firstDayOfPeriod = sdf.parse("1-05-2014");
		Date lastDayOfPeriod = sdf.parse("10-05-2014");
		Period period = new Period(firstDayOfPeriod, lastDayOfPeriod);
		setField(periodSelector, "period", period);
		PowerMock.mockStatic(Locale.class);
		expect(Locale.getDefault()).andReturn(Locale.US);
		Period expected = new Period(sdf.parse("11-05-2014"), sdf.parse("20-05-2014"));
		
		periodSelector.setNextPeriod();
		
		Period actual = (Period) getField(periodSelector, "period");
		assertEquals(expected, actual);
	}	
}

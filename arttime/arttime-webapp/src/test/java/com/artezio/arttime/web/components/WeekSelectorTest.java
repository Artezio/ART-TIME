package com.artezio.arttime.web.components;

import static junitx.util.PrivateAccessor.setField;
import static junitx.util.PrivateAccessor.getField;
import static org.junit.Assert.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

import com.artezio.arttime.datamodel.Period;
import com.artezio.arttime.test.utils.DefaultLocaleRule;

@RunWith(PowerMockRunner.class)
public class WeekSelectorTest {
	private WeekSelector weekSelector;
	private static SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
	
	@Rule
        public DefaultLocaleRule defaultLocaleRule = new DefaultLocaleRule(Locale.US);
	
	@Before
	public void setUp() {
		weekSelector = new WeekSelector();
	}
	
	@Test
	public void setPreviousPeriod() throws ParseException, NoSuchFieldException {
		Date firstDayOfWeek = sdf.parse("28-04-2014");
		Date lastDayOfWeek = sdf.parse("4-05-2014");
		Period period = new Period(firstDayOfWeek, lastDayOfWeek);
		setField(weekSelector, "period", period);
		Period expected = new Period(sdf.parse("21-04-2014"), sdf.parse("27-04-2014"));
		
		weekSelector.setPreviousPeriod();
		
		Period actual = (Period) getField(weekSelector, "period");
		assertEquals(expected, actual);
	}
	
	@Test
	public void setNextPeriod() throws ParseException, NoSuchFieldException {
		Date firstDayOfWeek = sdf.parse("28-04-2014");
		Date lastDayOfWeek = sdf.parse("4-05-2014");
		Period period = new Period(firstDayOfWeek, lastDayOfWeek);
		setField(weekSelector, "period", period);
		Period expected = new Period(sdf.parse("5-05-2014"), sdf.parse("11-05-2014"));
		
		weekSelector.setNextPeriod();
		
		Period actual = (Period) getField(weekSelector, "period");
		assertEquals(expected, actual);
	}
}

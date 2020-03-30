package com.artezio.arttime.web.components;

import static junitx.util.PrivateAccessor.getField;
import static junitx.util.PrivateAccessor.setField;
import static org.junit.Assert.assertEquals;

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
public class CustomPeriodSelectorTest {
	private CustomPeriodSelector periodSelector;
	private static SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
	
	@Rule
        public DefaultLocaleRule defaultLocaleRule = new DefaultLocaleRule(Locale.US);
	
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
		Period expected = new Period(sdf.parse("11-05-2014"), sdf.parse("20-05-2014"));
		
		periodSelector.setNextPeriod();
		
		Period actual = (Period) getField(periodSelector, "period");
		assertEquals(expected, actual);
	}	
}

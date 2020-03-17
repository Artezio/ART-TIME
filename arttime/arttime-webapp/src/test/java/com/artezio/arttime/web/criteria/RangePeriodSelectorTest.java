package com.artezio.arttime.web.criteria;

import static junitx.util.PrivateAccessor.getField;
import static junitx.util.PrivateAccessor.setField;
import static org.junit.Assert.assertEquals;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

import com.artezio.arttime.datamodel.Period;
import com.artezio.arttime.test.utils.DefaultLocaleRule;
import com.artezio.arttime.web.criteria.RangePeriodSelector.Range;

@RunWith(PowerMockRunner.class)
public class RangePeriodSelectorTest {
	private RangePeriodSelector rangePeriodSelector;
	private static SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
	
	@Rule
	public DefaultLocaleRule defaultLocaleRule = new DefaultLocaleRule(Locale.US);
	
	@Before
	public void setUp() {
		rangePeriodSelector = new RangePeriodSelector();
	}
	
	@Test
	public void testSetWeekPeriod() throws ParseException, NoSuchFieldException {
		Period period = new Period(sdf.parse("1-05-2014"), sdf.parse("3-05-2014"));
		setField(rangePeriodSelector, "period", period);
		setField(rangePeriodSelector, "range", Range.CUSTOM);
		Period expectedPeriod = new Period(sdf.parse("27-04-2014"), sdf.parse("3-05-2014"));
		
		rangePeriodSelector.setWeekPeriod();
		
		Range actualRange = (Range) getField(rangePeriodSelector, "range");
		Period actualPeriod = (Period) getField(rangePeriodSelector, "period");
		assertEquals(Range.WEEK, actualRange);
		assertEquals(expectedPeriod, actualPeriod);
	}
	
	@Test
	public void testSetMonthPeriod() throws ParseException, NoSuchFieldException {
		Period period = new Period(sdf.parse("1-05-2014"), sdf.parse("3-05-2014"));
		setField(rangePeriodSelector, "period", period);
		setField(rangePeriodSelector, "range", Range.CUSTOM);
		Period expectedPeriod = new Period(sdf.parse("1-05-2014"), sdf.parse("31-05-2014"));
		
		rangePeriodSelector.setMonthPeriod();
		
		Range actualRange = (Range) getField(rangePeriodSelector, "range");
		Period actualPeriod = (Period) getField(rangePeriodSelector, "period");
		assertEquals(Range.MONTH, actualRange);
		assertEquals(expectedPeriod, actualPeriod);
	}
	
	@Test
	public void testSetCustomPeriod() throws ParseException, NoSuchFieldException {
		Period period = new Period(sdf.parse("1-05-2014"), sdf.parse("3-05-2014"));
		setField(rangePeriodSelector, "period", period);
		setField(rangePeriodSelector, "range", Range.WEEK);
		Period expectedPeriod = new Period(sdf.parse("1-05-2014"), sdf.parse("3-05-2014"));
		
		rangePeriodSelector.setCustomPeriod();
		
		Range actualRange = (Range) getField(rangePeriodSelector, "range");
		Period actualPeriod = (Period) getField(rangePeriodSelector, "period");
		assertEquals(Range.CUSTOM, actualRange);
		assertEquals(expectedPeriod, actualPeriod);
	}
	
}

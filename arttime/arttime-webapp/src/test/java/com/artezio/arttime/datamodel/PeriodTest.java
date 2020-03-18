package com.artezio.arttime.datamodel;

import junitx.framework.ListAssert;
import org.junit.Assert;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.artezio.arttime.test.utils.CalendarUtils.getOffsetDate;
import static org.junit.Assert.*;

public class PeriodTest {

	@Test
	public void testContains() {
		Period period = new Period(getOffsetDate(-5), getOffsetDate(3));
		Date expected = getOffsetDate(3);
		Date unexpected1 = getOffsetDate(-10);

		Assert.assertTrue(period.contains(expected));
		Assert.assertFalse(period.contains(unexpected1));
	}

	@Test
	public void testContains_WithNullEnds() {
		Period period1 = new Period(null, getOffsetDate(3));
		Period period2 = new Period(getOffsetDate(-2), null);
		Date date = getOffsetDate(3);
		Date date2 = getOffsetDate(-10);

		Assert.assertTrue(period1.contains(date));
		Assert.assertTrue(period1.contains(date2));

		Assert.assertTrue(period2.contains(date));
		Assert.assertFalse(period2.contains(date2));
	}

	@Test
	public void testContains_ifPeriodStartDateIsNull_PeriodFinishBeforeDate() {
		Period period = new Period(null, getOffsetDate(2));
		Date date = getOffsetDate(5);

		boolean actual = period.contains(date);

		assertFalse(actual);
	}

	@Test
	public void testIsIntersect_BothPeriodsOpened() {
		Period period1 = new Period(new Date(), null);
		Period period2 = new Period(new Date(), null);

		assertTrue(period1.isIntersect(period2));
	}

	@Test
	public void testIsIntersect_OnePeriodOpened_OtherStartsBeforeAndFinishesBefore() {
		Period period1 = new Period(new Date(), null);
		Period period2 = new Period(getOffsetDate(-2), getOffsetDate(-1));

		assertFalse(period1.isIntersect(period2));

	}

	@Test
	public void testIsIntersect_OnePeriodOpened_OtherStartsBeforeAndFinishesAfterOtherStarts() {
		Period period1 = new Period(getOffsetDate(1), null);
		Period period2 = new Period(getOffsetDate(-2), getOffsetDate(1));

		assertTrue(period1.isIntersect(period2));

	}

	@Test
	public void testIsIntersect_OnePeriodClosed_OtherOpenedAndStartsInPeriod() {
		Period period1 = new Period(getOffsetDate(-1), getOffsetDate(1));
		Period period2 = new Period(getOffsetDate(1), null);

		assertTrue(period1.isIntersect(period2));

	}

	@Test
	public void testIsIntersect_OnePeriodClosed_OtherOpenedAndStartsAfterPeriod() {
		Period period1 = new Period(getOffsetDate(-1), getOffsetDate(1));
		Period period2 = new Period(getOffsetDate(2), null);

		assertFalse(period1.isIntersect(period2));
	}

	@Test
	public void testIsIntersect_OnePeriodClosed_OtherOpenedAndStartsABeforePeriod() {
		Period period1 = new Period(getOffsetDate(-1), getOffsetDate(1));
		Period period2 = new Period(getOffsetDate(-2), null);

		assertTrue(period1.isIntersect(period2));
	}

	@Test
	public void testIsIntersect_OtherPerionInPeriod() {
		Period period1 = new Period(getOffsetDate(-2), getOffsetDate(2));
		Period period2 = new Period(getOffsetDate(-1), getOffsetDate(1));

		assertTrue(period1.isIntersect(period2));
	}

	@Test
	public void testIsIntersect_OtherPerionStratsBeforeStartAndFinishesAfterFinish() {
		Period period1 = new Period(getOffsetDate(-1), getOffsetDate(1));
		Period period2 = new Period(getOffsetDate(-2), getOffsetDate(2));

		assertTrue(period1.isIntersect(period2));
	}

	@Test
	public void testIsIntersect_OtherPerionStratsAfterStartAndFinishesAfterFinish() {
		Period period1 = new Period(getOffsetDate(-2), getOffsetDate(1));
		Period period2 = new Period(getOffsetDate(-1), getOffsetDate(2));

		assertTrue(period1.isIntersect(period2));
	}

	@Test
	public void testIsIntersect_OtherPerionStratsBeforeStartAndFinishesAfterStart() {
		Period period1 = new Period(getOffsetDate(-1), getOffsetDate(2));
		Period period2 = new Period(getOffsetDate(-2), getOffsetDate(1));

		assertTrue(period1.isIntersect(period2));
	}

	@Test
	public void testIsIntersect_PeriodInOtherPEriod() {
		Period period1 = new Period(getOffsetDate(-1), getOffsetDate(1));
		Period period2 = new Period(getOffsetDate(-2), getOffsetDate(2));

		assertTrue(period1.isIntersect(period2));
	}

	@Test
	public void testIsIntersect_PeriodAfterOtherPeriod() {
		Period period1 = new Period(getOffsetDate(0), getOffsetDate(1));
		Period period2 = new Period(getOffsetDate(-2), getOffsetDate(-1));

		assertFalse(period1.isIntersect(period2));
	}

	@Test
	public void testIsIntersect_PeriodBeforeOtherPeriod() {
		Period period1 = new Period(getOffsetDate(-2), getOffsetDate(-1));
		Period period2 = new Period(getOffsetDate(1), getOffsetDate(2));

		assertFalse(period1.isIntersect(period2));
	}

	@Test
	public void testGetIntersection_ifTwoPeriodsStartDateIsNull() {
		Period period1 = new Period(null, getOffsetDate(3));
		Period period2 = new Period(null, getOffsetDate(10));
		Period expected = new Period(null, period1.getFinish());

		Period actual = period1.getIntersection(period2);

		assertEquals(expected, actual);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testGetDays_ifStartDateIsNull() {
		Period period = new Period(null, new Date());

		List<Date> actual = period.getDays();
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testGetDays_ifFinishDateIsNull() {
		Period period = new Period(new Date(), null);

		List<Date> actual = period.getDays();
	}

	@Test
	public void testGetDays() throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
		Period period = new Period(sdf.parse("30-12-2013 15:23"), sdf.parse("3-1-2014 4:21"));
		List<Date> expected = Arrays.asList(sdf.parse("30-12-2013 00:00"), sdf.parse("31-12-2013 00:00"), 
				sdf.parse("1-1-2014 00:00"), sdf.parse("2-1-2014 00:00"), sdf.parse("3-1-2014 00:00"));

		List<Date> actual = period.getDays();

		ListAssert.assertEquals(expected, actual);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testLength_ifStartDateIsNull() {
		Period period = new Period(null, new Date());

		int actual = period.length();
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testLength_ifFinishDateIsNull() {
		Period period = new Period(new Date(), null);

		int actual = period.length();
	}

	@Test
	public void testLength() throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
		Period period = new Period(sdf.parse("30-12-2013 15:23"), sdf.parse("3-1-2014 4:21"));

		int actual = period.length();

		assertEquals(5, actual);
	}

	@Test
	public void testGetIntersection_ifNotIntersect() throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
		Period period1 = new Period(sdf.parse("15-03-2014"), sdf.parse("20-03-2014"));
		Period period2 = new Period(sdf.parse("15-04-2014"), sdf.parse("20-04-2014"));

		Period actual = period1.getIntersection(period2);

		assertNull(actual);
	}

	@Test
	public void testGetIntersection() throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
		Period period1 = new Period(sdf.parse("15-03-2014"), sdf.parse("20-04-2014"));
		Period period2 = new Period(sdf.parse("15-04-2014"), sdf.parse("20-05-2014"));
		Period expected = new Period(sdf.parse("15-04-2014"), sdf.parse("20-04-2014"));

		Period actual = period1.getIntersection(period2);

		assertEquals(expected, actual);
	}

	@Test
	public void testHasNullDates() {
		Date start = null;
		Date finish = null;
		Period period = new Period(start, finish);
		assertTrue(period.hasNullDates());

		start = new Date();
		finish = null;
		period = new Period(start, finish);
		assertTrue(period.hasNullDates());

		start = null;
		finish = new Date();
		period = new Period(start, finish);
		assertTrue(period.hasNullDates());

		start = new Date();
		finish = new Date();
		period = new Period(start, finish);
		assertFalse(period.hasNullDates());

	}
	
	@Test
	public void testIsValid_ifStartIsNull() {
		Period period = new Period(null, getOffsetDate(3));
		
		boolean actual = period.isValid();
		
		assertTrue(actual);
	}
	
	@Test
	public void testIsValid_ifFinishIsNull() {
		Period period = new Period(getOffsetDate(3), null);
		
		boolean actual = period.isValid();
		
		assertTrue(actual);
	}
	
	@Test
	public void testIsValid_ifFinishLessThanStart() {
		Period period = new Period(getOffsetDate(3), getOffsetDate(-3));
		
		boolean actual = period.isValid();
		
		assertFalse(actual);
	}
	
	@Test
	public void testIsValid() {
		Period period = new Period(getOffsetDate(1), getOffsetDate(3));
		
		boolean actual = period.isValid();
		
		assertTrue(actual);
	}		
}

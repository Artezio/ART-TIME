package com.artezio.arttime.utils;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import com.ibm.icu.text.SimpleDateFormat;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Locale.class, Calendar.class, CalendarUtils.class, FacesContext.class, ExternalContext.class})
public class CalendarUtilsTest {
    private SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");

    @Test
    public void testCurrentWeekStartDate() throws ParseException {
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.set(2015, 5, 9);
        PowerMock.mockStatic(Locale.class);
        Locale locale = Locale.ENGLISH;

        expect(Locale.getDefault()).andReturn(locale);
        PowerMock.mockStatic(Calendar.class);
        expect(Calendar.getInstance(locale)).andReturn(calendar);
        PowerMock.replayAll(Calendar.class, Locale.class);

        Date actual = CalendarUtils.currentWeekStartDate();

        PowerMock.verifyAll();

        assertEquals(sdf.parse("7-06-2015"), actual);
    }

    @Test
    public void testCurrentWeekEndDate() throws ParseException {
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.set(2015, 5, 9);

        PowerMock.mockStatic(Locale.class);
        expect(Locale.getDefault()).andReturn(Locale.ENGLISH);
        PowerMock.mockStatic(Calendar.class);
        expect(Calendar.getInstance(Locale.ENGLISH)).andReturn(calendar);
        PowerMock.replayAll(Calendar.class, Locale.class);

        Date actual = CalendarUtils.currentWeekEndDate();

        PowerMock.verifyAll();

        assertEquals(sdf.parse("13-06-2015"), actual);
    }

    @Test
    public void testGetLocale_ifFacesContextIsNull() {
        PowerMock.mockStatic(Locale.class);
        PowerMock.mockStatic(FacesContext.class);

        expect(FacesContext.getCurrentInstance()).andReturn(null);
        expect(Locale.getDefault()).andReturn(Locale.ENGLISH);
        PowerMock.replayAll(Locale.class, FacesContext.class);

        Locale actual = CalendarUtils.getLocale();

        PowerMock.verifyAll();

        assertEquals(Locale.ENGLISH, actual);
    }

    @Test
    public void testGetLocale_ifFacesContextNotNull() {
        FacesContext facesContext = createMock(FacesContext.class);
        ExternalContext externalContext = createMock(ExternalContext.class);

        PowerMock.mockStatic(FacesContext.class);
        expect(FacesContext.getCurrentInstance()).andReturn(facesContext);
        expect(facesContext.getExternalContext()).andReturn(externalContext);
        expect(externalContext.getRequestLocale()).andReturn(Locale.CANADA);
        PowerMock.replayAll(FacesContext.class, facesContext, externalContext);

        Locale actual = CalendarUtils.getLocale();

        PowerMock.verifyAll();

        assertEquals(Locale.CANADA, actual);
    }

    @Test
    public void testFirstDayOfWeek() throws Exception {
        PowerMock.mockStatic(Locale.class);
        expect(Locale.getDefault()).andReturn(Locale.ENGLISH);
        PowerMock.replayAll(Locale.class);

        Date actual = CalendarUtils.firstDayOfWeek(sdf.parse("9-06-2015"));

        PowerMock.verifyAll();
        assertEquals(sdf.parse("7-06-2015"), actual);
    }

    @Test
    public void testLastDayOfWeek() throws Exception {
        PowerMock.mockStatic(Locale.class);
        expect(Locale.getDefault()).andReturn(Locale.ENGLISH);
        PowerMock.replayAll(Locale.class);

        Date actual = CalendarUtils.lastDayOfWeek(sdf.parse("9-06-2015"));

        PowerMock.verifyAll();

        assertEquals(sdf.parse("13-06-2015"), actual);
    }

    @Test
    public void testFirstDayOfMonth() throws Exception {
        Date actual = CalendarUtils.firstDayOfMonth(sdf.parse("9-06-2015"));

        assertEquals(sdf.parse("1-06-2015"), actual);
    }

    @Test
    public void testLastDayOfMonth() throws Exception {
        Date actual = CalendarUtils.lastDayOfMonth(sdf.parse("9-06-2015"));

        assertEquals(sdf.parse("30-06-2015"), actual);
    }

    @Test
    public void testGetMax_ifBothDateAreNull() {
        Date actual = CalendarUtils.max(null, null);

        assertNull(actual);
    }

    @Test
    public void testGetMax_ifFirstDateIsNull() {
        Date date1 = null;
        Date date2 = new Date();

        Date actual = CalendarUtils.max(date1, date2);

        assertSame(date2, actual);
    }

    @Test
    public void testGetMax_ifSecondDateIsNull() {
        Date date1 = new Date();
        Date date2 = null;

        Date actual = CalendarUtils.max(date1, date2);

        assertSame(date1, actual);
    }

    @Test
    public void testGetMax_ifFirsrDateLess() throws Exception {
        Date date1 = sdf.parse("1-01-2015");
        Date date2 = sdf.parse("1-03-2015");

        Date actual = CalendarUtils.max(date1, date2);

        assertSame(date2, actual);
    }

    @Test
    public void testGetMax_ifFirsrDateGreater() throws Exception {
        Date date1 = sdf.parse("1-03-2015");
        Date date2 = sdf.parse("1-01-2015");

        Date actual = CalendarUtils.max(date1, date2);

        assertSame(date1, actual);
    }

    @Test
    public void testGetMin_ifBothDateAreNull() {
        Date actual = CalendarUtils.min(null, null);

        assertNull(actual);
    }

    @Test
    public void testGetMin_ifFirstDateIsNull() {
        Date date1 = null;
        Date date2 = new Date();

        Date actual = CalendarUtils.min(date1, date2);

        assertSame(date2, actual);
    }

    @Test
    public void testGetMin_ifSecondDateIsNull() {
        Date date1 = new Date();
        Date date2 = null;

        Date actual = CalendarUtils.min(date1, date2);

        assertSame(date1, actual);
    }

    @Test
    public void testGetMin_ifFirsrDateLess() throws Exception {
        Date date1 = sdf.parse("1-01-2015");
        Date date2 = sdf.parse("1-03-2015");

        Date actual = CalendarUtils.min(date1, date2);

        assertSame(date1, actual);
    }

    @Test
    public void testGetMin_ifFirsrDateGreater() throws Exception {
        Date date1 = sdf.parse("1-03-2015");
        Date date2 = sdf.parse("1-01-2015");

        Date actual = CalendarUtils.min(date1, date2);

        assertSame(date2, actual);
    }

    @Test
    public void testIsWeekend_ifFalse() throws Exception {
        Date date = sdf.parse("12-06-2015");

        boolean actual = CalendarUtils.isWeekend(date);

        assertFalse(actual);
    }

    @Test
    public void testIsWeekend_ifSuterday() throws Exception {
        Date date = sdf.parse("13-06-2015");

        boolean actual = CalendarUtils.isWeekend(date);

        assertTrue(actual);
    }

    @Test
    public void testIsWeekend_ifSunday() throws Exception {
        Date date = sdf.parse("14-06-2015");

        boolean actual = CalendarUtils.isWeekend(date);

        assertTrue(actual);
    }
}

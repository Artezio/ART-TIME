package com.artezio.arttime.services.mailing;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.commons.lang.time.DateUtils;
import org.easymock.EasyMockRunner;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.artezio.arttime.config.Settings;
import com.artezio.arttime.datamodel.Employee;
import com.artezio.arttime.datamodel.Period;
import com.artezio.arttime.services.EmployeeService;
import com.artezio.arttime.services.HoursService;
import com.artezio.arttime.services.NotificationManagerLocal;
import com.artezio.arttime.services.SettingsService;

@RunWith(EasyMockRunner.class)
public class NotificationSchedulerTest extends EasyMockSupport {

    @TestSubject
    private NotificationScheduler notificationScheduler = partialMockBuilder(NotificationScheduler.class)
            .addMockedMethod("getPeriodForPreviousMonth").createMock();
    @Mock
    private SettingsService settingsService;
    @Mock
    private Settings settings;
    @Mock
    private NotificationManagerLocal notificationManager;
    @Mock
    private EmployeeService employeeService;
    @Mock
    private HoursService hoursService;

    @Test
    public void testNotifyAboutIncorrectTimesheet() {

        List<Employee> employees = Arrays.asList(new Employee("test_user"));
        Date start = new GregorianCalendar(2020, 1, 1).getTime();
        Date finish = new GregorianCalendar(2020, 1, 29).getTime();
        Period period = new Period(start, finish);

        expect(settingsService.getSettings()).andReturn(settings);
        expect(settings.isIncorrectTimesheetNotificationEnabled()).andReturn(true);
        expect(employeeService.getCurrent()).andReturn(employees);
        expect(notificationScheduler.getPeriodForPreviousMonth()).andReturn(period);
        notificationManager.notifyAboutIncorrectTimesheet(employees, period);
        expectLastCall().once();
        replayAll();

        notificationScheduler.notifyAboutIncorrectTimesheet();

        verifyAll();
    }

    @Test
    public void testNotifyAboutIncorrectTimesheet_notEnabled() {

        expect(settingsService.getSettings()).andReturn(settings);
        expect(settings.isIncorrectTimesheetNotificationEnabled()).andReturn(false);
        replayAll();

        notificationScheduler.notifyAboutIncorrectTimesheet();

        verifyAll();
    }

    @Test
    public void testGetPeriodForPreviousMonth() {
        notificationScheduler = partialMockBuilder(NotificationScheduler.class).addMockedMethod("getCurrentDate")
                .createMock();

        Date start = new GregorianCalendar(2020, 1, 1).getTime();
        Date finish = new GregorianCalendar(2020, 1, 29).getTime();
        Period expected = new Period(start, finish);
        Date now = new GregorianCalendar(2020, 2, 1).getTime();

        expect(notificationScheduler.getCurrentDate()).andReturn(now);
        replayAll();

        Period actual = notificationScheduler.getPeriodForPreviousMonth();

        verifyAll();
        assertEquals(expected, actual);
    }

    @Test
    public void testGetCurrentDate() {
        Date expected = new Date();
        Date actual = new NotificationScheduler().getCurrentDate();

        assertEquals(DateUtils.truncate(expected, Calendar.DAY_OF_MONTH),
                DateUtils.truncate(actual, Calendar.DAY_OF_MONTH));
    }

    @Test
    public void testNotifyAboutUnapprovedHours() {

        List<Employee> managers = Arrays.asList(new Employee("test_user"));
        Date start = new GregorianCalendar(2020, 1, 1).getTime();
        Date finish = new GregorianCalendar(2020, 1, 29).getTime();
        Period period = new Period(start, finish);

        expect(settingsService.getSettings()).andReturn(settings);
        expect(settings.isUnapprovedHoursNotificationEnabled()).andReturn(true);
        expect(notificationScheduler.getPeriodForPreviousMonth()).andReturn(period);
        expect(hoursService.getManagersForUnapprovedHours(period)).andReturn(managers);
        notificationManager.notifyAboutUnapprovedHours(managers, period);
        expectLastCall().once();
        replayAll();

        notificationScheduler.notifyAboutUnapprovedHours();

        verifyAll();
    }
    
    @Test
    public void testNotifyAboutUnapprovedHours_notEnabled() {

        expect(settingsService.getSettings()).andReturn(settings);
        expect(settings.isUnapprovedHoursNotificationEnabled()).andReturn(false);
        replayAll();

        notificationScheduler.notifyAboutUnapprovedHours();

        verifyAll();
    }

}

package com.artezio.arttime.services.mailing;

import static com.artezio.arttime.security.auth.UserRoles.SYSTEM_ROLE;

import java.util.Date;
import java.util.List;

import javax.annotation.security.RunAs;
import javax.ejb.Schedule;
import javax.ejb.Stateless;
import javax.inject.Inject;

import org.apache.commons.lang.time.DateUtils;

import com.artezio.arttime.datamodel.Employee;
import com.artezio.arttime.datamodel.Period;
import com.artezio.arttime.services.EmployeeService;
import com.artezio.arttime.services.HoursService;
import com.artezio.arttime.services.NotificationManagerLocal;
import com.artezio.arttime.services.SettingsService;
import com.artezio.arttime.utils.CalendarUtils;

@Stateless
@RunAs(SYSTEM_ROLE)
public class NotificationScheduler {
    
    private static final String NOTIFY_ABOUT_INCORRECT_TIMESHEET_INFO = "On the 1st day of the month notify employees if a timesheet is incorrect";
    private static final String NOTIFY_ABOUT_UNAPPROVED_HOURS_INFO = "On the 2nd day of the month notify project managers if there are unapproved hours in their projects.";

    @Inject
    private SettingsService settingsService;
    @Inject
    private NotificationManagerLocal notificationManager;
    @Inject
    private EmployeeService employeeService;
    @Inject
    private HoursService hoursService;

    @Schedule(dayOfMonth="1", persistent = false, info = NOTIFY_ABOUT_INCORRECT_TIMESHEET_INFO)
    public void notifyAboutIncorrectTimesheet() {
        if (settingsService.getSettings().isIncorrectTimesheetNotificationEnabled()) {
            List<Employee> employees = employeeService.getCurrent();
            Period period = getPeriodForPreviousMonth();
            notificationManager.notifyAboutIncorrectTimesheet(employees, period);
        }
    }
    
    @Schedule(dayOfMonth="2", persistent = false, info = NOTIFY_ABOUT_UNAPPROVED_HOURS_INFO)
    public void notifyAboutUnapprovedHours() {
        if (settingsService.getSettings().isUnapprovedHoursNotificationEnabled()) {
            Period period = getPeriodForPreviousMonth();
            List<Employee> managers = hoursService.getManagersForUnapprovedHours(period);
            notificationManager.notifyAboutUnapprovedHours(managers, period);
        }
    }

    protected Period getPeriodForPreviousMonth() {
        Date previousMonth = DateUtils.addMonths(getCurrentDate(), -1);
        return new Period(CalendarUtils.firstDayOfMonth(previousMonth), CalendarUtils.lastDayOfMonth(previousMonth));
    }

    protected Date getCurrentDate() {
        return new Date();
    }

}

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
import com.artezio.arttime.services.NotificationManagerLocal;
import com.artezio.arttime.services.SettingsService;
import com.artezio.arttime.utils.CalendarUtils;

@Stateless
@RunAs(SYSTEM_ROLE)
public class NotificationScheduler {
    
    private static final String NOTIFY_ABOUT_INCORRECT_TIMESHEET_INFO = "On the 1st day of the month notify employees if a timesheet is incorrect";

    @Inject
    private SettingsService settingsService;
    @Inject
    private NotificationManagerLocal notificationManager;
    @Inject
    private EmployeeService employeeService;

    @Schedule(dayOfMonth="1", persistent = false, info = NOTIFY_ABOUT_INCORRECT_TIMESHEET_INFO)
    public void notifyAboutIncorrectTimesheet() {
        if (settingsService.getSettings().isIncorrectTimesheetNotificationEnabled()) {
            List<Employee> employees = employeeService.getCurrent();
            Period period = getPeriodForPreviousMonth();
            notificationManager.notifyAboutIncorrectTimesheet(employees, period);
        }
    }

    private Period getPeriodForPreviousMonth() {
        Date previousMonth = DateUtils.addMonths(getCurrentDate(), -1);
        return new Period(CalendarUtils.firstDayOfMonth(previousMonth), CalendarUtils.lastDayOfMonth(previousMonth));
    }

    protected Date getCurrentDate() {
        return new Date();
    }

}

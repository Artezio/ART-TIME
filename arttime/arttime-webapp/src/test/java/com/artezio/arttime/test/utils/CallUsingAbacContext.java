package com.artezio.arttime.test.utils;

import com.artezio.javax.jpa.abac.AbacContext;

import javax.ejb.Stateless;
import java.util.concurrent.Callable;

import static com.artezio.arttime.security.AbacContexts.*;

@Stateless
public class CallUsingAbacContext {
    @AbacContext(VIEW_TIMESHEET)
    public <V> V callInViewTimesheetContext(Callable<V> callable) throws Exception {
        return callable.call();
    }
    @AbacContext(REPORT_TIME)
    public <V> V callInReportTimeContext(Callable<V> callable) throws Exception {
        return callable.call();
    }
    @AbacContext(MANAGE_PROJECTS)
    public <V> V callInManageProjectsContext(Callable<V> callable) throws Exception {
        return callable.call();
    }
    @AbacContext(MANAGE_EMPLOYEES)
    public <V> V callInManageEmployeesContext(Callable<V> callable) throws Exception {
        return callable.call();
    }
    @AbacContext(MANAGE_CALENDARS)
    public <V> V callInManageCalendarsContext(Callable<V> callable) throws Exception {
        return callable.call();
    }
    @AbacContext(REPORT_ACTUAL_WORKTIME_PROBLEMS)
    public <V> V callInReportWorktimeProblemsContext(Callable<V> callable) throws Exception {
        return callable.call();
    }
}

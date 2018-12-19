package com.artezio.arttime.services;

import com.artezio.arttime.admin_tool.cache.WebCached;
import com.artezio.arttime.datamodel.Day;
import com.artezio.arttime.datamodel.Period;
import com.artezio.arttime.datamodel.WorkdaysCalendar;
import com.artezio.arttime.repositories.WorkdaysCalendarRepository;
import com.artezio.arttime.web.interceptors.FacesMessage;
import com.artezio.javax.jpa.abac.AbacContext;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.*;

import static com.artezio.arttime.admin_tool.cache.WebCached.Scope.REQUEST_SCOPED;
import static com.artezio.arttime.security.AbacContexts.MANAGE_CALENDARS;
import static com.artezio.arttime.security.auth.UserRoles.*;

@Named
@Stateless
@RolesAllowed({EXEC_ROLE, OFFICE_MANAGER})
public class WorkdaysCalendarService implements Serializable {

    @Inject
    private WorkdaysCalendarRepository workdaysCalendarRepository;

    @AbacContext(MANAGE_CALENDARS)
    @FacesMessage(onCompleteMessageKey = "message.calendarIsSaved")
    public WorkdaysCalendar create(WorkdaysCalendar workdaysCalendar) {
        return workdaysCalendarRepository.create(workdaysCalendar);
    }

    @AbacContext(MANAGE_CALENDARS)
    @FacesMessage(onCompleteMessageKey = "message.calendarIsSaved")
    public void update(WorkdaysCalendar workdaysCalendar, List<Day> days) {
        workdaysCalendarRepository.update(workdaysCalendar, days);
    }

    @AbacContext(MANAGE_CALENDARS)
    public WorkdaysCalendar update(WorkdaysCalendar workdaysCalendar) {
        return workdaysCalendarRepository.update(workdaysCalendar);
    }

    @AbacContext(MANAGE_CALENDARS)
    @FacesMessage(onCompleteMessageKey = "message.calendarIsDeleted")
    public void remove(WorkdaysCalendar workdaysCalendar) {
        workdaysCalendarRepository.remove(workdaysCalendar);
    }

    @RolesAllowed(INTEGRATION_CLIENT_ROLE)
    public List<WorkdaysCalendar> getAll() {
        return workdaysCalendarRepository.getWorkdaysCalendars();
    }

    @AbacContext(MANAGE_CALENDARS)
    @RolesAllowed({EXEC_ROLE, OFFICE_MANAGER})
    public List<WorkdaysCalendar> getCalendarsForManaging() {
        return workdaysCalendarRepository.getWorkdaysCalendars();
    }

    @WebCached(scope = REQUEST_SCOPED)
    @AbacContext(MANAGE_CALENDARS)
    @RolesAllowed({EXEC_ROLE, OFFICE_MANAGER, SYSTEM_ROLE})
    public Map<String, WorkdaysCalendar> getCalendarsByDepartments() {
        Map<String, WorkdaysCalendar> result = new HashMap<>();
        getAll().forEach(calendar -> calendar.getDepartments()
                .forEach(department -> result.put(department, calendar)));
        return result;
    }

    @PermitAll
    @WebCached(scope = REQUEST_SCOPED)
    public List<Day> getDays(WorkdaysCalendar workdaysCalendar, Period period) {
        List<Day> result = workdaysCalendarRepository.getSpecialDays(workdaysCalendar, period);
        List<Date> existedDates = getDates(result);
        List<Date> newDates = new ArrayList<>(period.getDays());
        newDates.removeAll(existedDates);
        List<Day> createdDays = createDays(newDates, workdaysCalendar);
        result.addAll(createdDays);
        return result;
    }

    @PermitAll
    public WorkdaysCalendar findById(Long id) {
        return workdaysCalendarRepository.findWorkdaysCalendar(id);
    }

    private List<Date> getDates(List<Day> days) {
        List<Date> result = new ArrayList<>();
        for (Day day : days) {
            result.add(day.getDate());
        }
        return result;
    }

    protected List<Day> createDays(List<Date> dates, WorkdaysCalendar calendar) {
        List<Day> result = new ArrayList<>();
        for (Date date : dates) {
            result.add(new Day(date, calendar));
        }
        return result;
    }

}

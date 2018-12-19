package com.artezio.arttime.web;

import com.artezio.arttime.datamodel.WorkdaysCalendar;
import com.artezio.arttime.services.DepartmentService;
import com.artezio.arttime.services.WorkdaysCalendarService;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Named
@ViewScoped
public class WorkdaysCalendarsBean implements Serializable {

    private static final long serialVersionUID = 8608894182647428813L;

    @Inject
    private DepartmentService departmentService;
    @Inject
    private WorkdaysCalendarService workdaysCalendarService;
    private Map<String, WorkdaysCalendar> workdaysCalendarsByDepartments;
    private List<WorkdaysCalendar> calendars;
    private List<String> departments;

    public List<WorkdaysCalendar> getCalendars() {
        if (calendars == null) {
            calendars = workdaysCalendarService.getCalendarsForManaging();
        }
        return calendars;
    }

    public void setCalendars(List<WorkdaysCalendar> calendars) {
        this.calendars = calendars;
    }

    public void remove(WorkdaysCalendar workdaysCalendar) {
        workdaysCalendarService.remove(workdaysCalendar);
        calendars = null;
    }

    public Map<String, WorkdaysCalendar> getWorkdaysCalendarsByDepartments() {
        if (workdaysCalendarsByDepartments == null) {
            workdaysCalendarsByDepartments = workdaysCalendarService.getCalendarsByDepartments();
            getDepartments().forEach(department -> workdaysCalendarsByDepartments.putIfAbsent(department, null));
        }
        return workdaysCalendarsByDepartments;
    }

    public void save() {
        getDepartments().forEach(department ->
                departmentService.setCalendarToDepartment(department, workdaysCalendarsByDepartments.get(department)));
    }

    public List<String> getDepartments() {
        if (departments == null) {
            departments = departmentService.getAll();
        }
        return departments;
    }

}

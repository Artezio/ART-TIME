package com.artezio.arttime.web;

import com.artezio.arttime.datamodel.*;
import com.artezio.arttime.filter.Filter;
import com.artezio.arttime.services.FilterService;
import com.artezio.arttime.services.WorkdaysCalendarService;
import com.artezio.arttime.utils.CalendarUtils;
import com.artezio.arttime.web.spread_sheet.SpreadSheet;

import javax.faces.annotation.ManagedProperty;
import javax.faces.application.Application;
import javax.faces.application.ViewHandler;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class EffortsBean implements Serializable {

    private static final long serialVersionUID = 6796778823308301114L;
    private static final String EFFORTS_GROUPING_COOKIE_NAME = "efforts_grouping";

    @Inject
    protected FilterService filterService;
    protected EffortsGrouping grouping;
    @Inject @ManagedProperty("#{filterBean}")
    protected FilterBean filterBean;
    private SpreadSheet spreadSheet;
    @Inject
    private FacesContext facesContext;
    @Inject
    private WorkdaysCalendarService workdaysCalendarService;
    private Map<WorkdaysCalendar, List<Day>> calendarDays = new HashMap<>();
    private List<Employee> employees;
    private Period period = new Period();

    protected abstract SpreadSheet initSpreadSheet();

    public SpreadSheet getSpreadSheet() {
        if (spreadSheet == null) {
            spreadSheet = initSpreadSheet();
        }
        return spreadSheet;
    }

    public void resetData() {
        spreadSheet = null;
        calendarDays = null;
        resetComponentsTree();
    }

    protected void resetComponentsTree() {
        Application application = facesContext.getApplication();
        ViewHandler viewHandler = application.getViewHandler();
        UIViewRoot viewRoot = viewHandler.createView(facesContext, facesContext.getViewRoot().getViewId());
        facesContext.setViewRoot(viewRoot);
    }

    public String getStyleClass(Hours hours) {
        if (hours == null) {
            return "emptyCell";
        }
        Date date = hours.getDate();
        Employee employee = hours.getEmployee();
        boolean weekend = isWeekend(employee.getCalendar(), date);
        if (hours.isApproved() && !weekend) {
            return "approvedHours";
        } else if (hours.isApproved() && weekend) {
            return "approvedWeekendHours";
        } else if (weekend) {
            return "weekendCell";
        }
        return "";
    }

    protected boolean isWeekend(WorkdaysCalendar workdaysCalendar, Date date) {
        Day day = getDay(workdaysCalendar, date);
        return !day.isWorking();
    }

    protected Day getDay(WorkdaysCalendar workdaysCalendar, Date date) {
        if (!calendarDays.containsKey(workdaysCalendar)) {
            List<Day> days = workdaysCalendarService.getDays(workdaysCalendar, filterBean.getCurrentFilter().getPeriod());
            calendarDays.put(workdaysCalendar, days);
        }
        return findDay(calendarDays.get(workdaysCalendar), date);
    }

    protected Day findDay(List<Day> days, Date date) {
        return days.stream()
                .filter(day -> date.equals(day.getDate()))
                .findFirst().orElse(null);
    }

    public String getHeaderStyleClass(Date date){
        return CalendarUtils.isWeekend(date)
                ? "weekendHeader"
                : "workdayHeader";
    }

    public void setPeriod(Period period) {
        this.period = period;
    }

    public Period getPeriod() {
        return period;
    }

    public List<Employee> getEmployees() {
        return employees;
    }

    public void setEmployees(List<Employee> employees) {
        this.employees = employees;
    }

    public void initByGetRequest() {
        if (!period.hasNullDates()) {
            Filter currentFilter = filterBean.getCurrentFilter();
            currentFilter.setCustomPeriod(period);
            if (employees != null && !employees.isEmpty()) {
                currentFilter.setEmployees(employees);
            }
        }
    }

    public EffortsGrouping getGrouping() {
        if (grouping == null && (grouping = extractEffortsGroupingFromCookie()) == null) {
            EffortsGrouping defaultGrouping = EffortsGrouping.BY_PROJECTS;
            setEffortsGroupingCookie(defaultGrouping);
            grouping = defaultGrouping;
        }
        return grouping;
    }

    public void setGrouping(EffortsGrouping grouping) {
        this.grouping = grouping;
        setEffortsGroupingCookie(grouping);
    }

    private void setEffortsGroupingCookie(EffortsGrouping grouping) {
        HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
        Cookie effortsGroupingCookie = new Cookie(EFFORTS_GROUPING_COOKIE_NAME, grouping.toString());
        effortsGroupingCookie.setMaxAge(Integer.MAX_VALUE);
        response.addCookie(effortsGroupingCookie);
    }

    private EffortsGrouping extractEffortsGroupingFromCookie() {
        HttpServletRequest request = (HttpServletRequest) facesContext.getExternalContext().getRequest();
        for (Cookie cookie : request.getCookies()) {
            if (cookie.getName().equals(EFFORTS_GROUPING_COOKIE_NAME)) {
                return EffortsGrouping.valueOf(cookie.getValue());
            }
        }
        return null;
    }

}

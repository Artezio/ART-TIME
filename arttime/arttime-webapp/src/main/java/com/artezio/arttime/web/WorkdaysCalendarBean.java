package com.artezio.arttime.web;

import com.artezio.arttime.datamodel.Day;
import com.artezio.arttime.datamodel.Period;
import com.artezio.arttime.datamodel.WorkdaysCalendar;
import com.artezio.arttime.services.WorkdaysCalendarService;
import com.artezio.arttime.utils.CalendarUtils;
import org.apache.commons.lang.time.DateUtils;
import org.primefaces.event.SelectEvent;

import javax.annotation.PostConstruct;
import javax.faces.component.UIInput;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Named
@ViewScoped
public class WorkdaysCalendarBean implements Serializable {

    private static final long serialVersionUID = 7254105710714398312L;
    @Inject
    private ExternalContext externalContext;
    private WorkdaysCalendar workdaysCalendar;
    @Inject
    private WorkdaysCalendarService workdaysCalendarService;
    private Date selectedDate;
    private Period period = new Period();
    private Period extendedPeriod = new Period();
    private Map<Date, Day> days = new HashMap<>();
    private Date shiftFrom = null;
    private Date shiftTo = null;
    private String comment = null;

    @PostConstruct
    public void init() {
        Map<String, String> requestParams = externalContext.getRequestParameterMap();
        String calendarId = requestParams.get("calendar");
        if (calendarId == null) {
            this.workdaysCalendar = new WorkdaysCalendar();
        } else {
            this.workdaysCalendar = workdaysCalendarService.findById(Long.parseLong(calendarId));
            Date start = CalendarUtils.firstDayOfMonth(new Date());
            Date finish = CalendarUtils.lastDayOfMonth(new Date());
            updatePeriod(start, finish);
        }
    }

    public WorkdaysCalendar getWorkdaysCalendar() {
        return workdaysCalendar;
    }

    public void create() {
        workdaysCalendarService.create(workdaysCalendar);
    }

    public void updateWorkdaysCalendar(List<Day> days) {
        workdaysCalendarService.update(workdaysCalendar, days);
    }

    public void removeDayShift(Day selectedDay) {
        Day dayFrom = days.get(selectedDay.getDate());
        Day dayTo = days.get(selectedDay.getShiftedTo());
        boolean isShiftToWorkDay = dayTo.isWorking();
        dayTo.setWorking(dayFrom.isWorking());
        dayFrom.setWorking(isShiftToWorkDay);
        boolean isShiftToHoliday = dayTo.isHoliday();
        dayTo.setHoliday(dayFrom.isHoliday());
        dayFrom.setHoliday(isShiftToHoliday);
        dayFrom.setComment(null);
        dayTo.setComment(null);
        selectedDay.setShiftedFrom(null);
        selectedDay.setShiftedTo(null);
    }

    public void addDayShift() {
        Day dayTo = days.get(shiftTo);
        Day dayFrom = days.get(shiftFrom);
        dayFrom.setShiftedFrom(dayFrom.getDate());
        dayFrom.setShiftedTo(dayTo.getDate());
        boolean isShiftToWorkDay = dayTo.isWorking();
        dayTo.setWorking(dayFrom.isWorking());
        dayFrom.setWorking(isShiftToWorkDay);
        boolean isShiftToHoliday = dayTo.isHoliday();
        dayTo.setHoliday(dayFrom.isHoliday());
        dayFrom.setHoliday(isShiftToHoliday);
        dayTo.setComment(comment);
        dayFrom.setComment(comment);
        shiftFrom = null;
        shiftTo = null;
        comment = null;
    }

    public Date getSelectedDate() {
        if (selectedDate == null) {
            selectedDate = new Date();
        }
        return selectedDate;
    }

    public void setSelectedDate(Date selectedDate) {
        this.selectedDate = selectedDate;
    }

    public void onDateSelect(SelectEvent event) {
        Date selected = (Date) event.getObject();
        Day day = days.get(selected);
        day.switchDayType();
    }

    public void nextMonth() {
        int monthCount = Integer.parseInt(externalContext.getRequestParameterMap().get("monthCount"));
        Date selectedDate = period.getStart();
        selectedDate = DateUtils.addMonths(selectedDate, monthCount);
        Date start = CalendarUtils.firstDayOfMonth(selectedDate);
        Date finish = CalendarUtils.lastDayOfMonth(selectedDate);
        updatePeriod(start, finish);
    }

    public void prevMonth() {
        int monthCount = Integer.parseInt(externalContext.getRequestParameterMap().get("monthCount"));
        Date selectedDate = period.getStart();
        selectedDate = DateUtils.addMonths(selectedDate, -monthCount);
        Date start = CalendarUtils.firstDayOfMonth(selectedDate);
        Date finish = CalendarUtils.lastDayOfMonth(selectedDate);
        updatePeriod(start, finish);
    }

    protected void updatePeriod(Date start, Date finish) {
        period.setStart(start);
        period.setFinish(finish);
        extendedPeriod.setStart(CalendarUtils.firstDayOfMonth(DateUtils.addMonths(start, -10)));
        extendedPeriod.setFinish(CalendarUtils.lastDayOfMonth(DateUtils.addMonths(start, 10)));
        populateDays();
    }

    protected void populateDays() {
        List<Day> daysInPeriod = workdaysCalendarService.getDays(workdaysCalendar, extendedPeriod);
        for (Day day : daysInPeriod) {
            days.putIfAbsent(day.getDate(), day);
        }
    }

    public List<Day> getDaysShift() {
        return days.values().stream()
                .filter(day -> (day.getShiftedFrom() != null && day.getShiftedTo() != null
                        && (period.contains(day.getShiftedFrom()) || period.contains(day.getShiftedTo()))))
                .sorted(Day.DATE_COMPARATOR).collect(Collectors.toList());
    }

    public String getDaysOff() {
        return days.values().stream()
                .filter(day -> (!day.isWorking() && !day.isHoliday() && extendedPeriod.contains(day.getDate())))
                .map(day -> (new SimpleDateFormat("d-M-yyyy").format(day.getDate())).toString())
                .collect(Collectors.joining(","));
    }

    public String getHolidays() {
        return days.values().stream().filter(day -> (day.isHoliday() && extendedPeriod.contains(day.getDate())))
                .map(day -> (new SimpleDateFormat("d-M-yyyy").format(day.getDate())).toString())
                .collect(Collectors.joining(","));
    }

    public void setDaysOff(String daysOff) {
    }

    public void setHolidays(String holidays) {
    }

    public List<Day> getDays() {
        return new ArrayList<Day>(days.values());
    }

    public Period getPeriod() {
        return period;
    }

    public Date getShiftTo() {
        return shiftTo;
    }

    public void setShiftTo(Date shiftTo) {
        this.shiftTo = shiftTo;
    }

    public Date getShiftFrom() {
        return shiftFrom;
    }

    public void setShiftFrom(Date shiftFrom) {
        this.shiftFrom = shiftFrom;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public boolean validateDateBeforeCreateDayShift(FacesContext context, List<UIInput> components,
            List<Object> values) {
        Object shiftFrom = values.get(0);
        Object shiftTo = values.get(1);
        return !shiftFrom.equals(shiftTo) && !(shiftFrom == null && shiftTo == null)
                && !(shiftFrom != null && shiftTo == null) && !(shiftFrom == null && shiftTo != null);
    }

}

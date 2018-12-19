package com.artezio.arttime.web.criteria;

import com.artezio.arttime.datamodel.Period;
import com.artezio.arttime.utils.CalendarUtils;

import java.io.Serializable;
import java.util.Date;

public class RangePeriodSelector implements Serializable {

    private static final long serialVersionUID = 2L;

    public enum Range {WEEK, MONTH, CUSTOM}

    private Range range;
    private Period period;

    public RangePeriodSelector() {
        range = Range.WEEK;
        period = new Period(CalendarUtils.currentWeekStartDate(), CalendarUtils.currentWeekEndDate());
    }

    public RangePeriodSelector(Period period) {
        range = Range.WEEK;
        this.period = period;
    }

    public Range getRange() {
        return range;
    }

    public Period getPeriod() {
        return period;
    }

    public void setMonthPeriod() {
        this.range = Range.MONTH;
        Date date = period.getStart();
        period.setStart(CalendarUtils.firstDayOfMonth(date));
        period.setFinish(CalendarUtils.lastDayOfMonth(date));
    }

    public void setWeekPeriod() {
        this.range = Range.WEEK;
        Date date = period.getStart();
        period.setStart(CalendarUtils.firstDayOfWeek(date));
        period.setFinish(CalendarUtils.lastDayOfWeek(date));
    }

    public void setCustomPeriod() {
        this.range = Range.CUSTOM;
    }

}

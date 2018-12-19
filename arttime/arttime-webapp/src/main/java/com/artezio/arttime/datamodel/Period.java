package com.artezio.arttime.datamodel;

import com.artezio.arttime.utils.CalendarUtils;
import org.apache.commons.lang.time.DateUtils;

import javax.persistence.Embeddable;
import javax.persistence.Transient;
import javax.validation.constraints.AssertTrue;
import java.io.Serializable;
import java.util.*;

@Embeddable
public class Period implements Serializable {
    private static final long serialVersionUID = -4064847994092335348L;

    private Date start;
    private Date finish;
    @Transient
    private List<Date> days;

    public Period() {
    }

    public Period(Date start, Date finish) {
        this.start = start;
        this.finish = finish;
    }

    public void setStart(Date start) {
        this.start = start;
        this.days = null;
    }

    public Date getStart() {
        return start;
    }

    public void setFinish(Date finish) {
        this.finish = finish;
        this.days = null;
    }

    public Date getFinish() {
        return finish;
    }

    // TODO: refactor
    public boolean contains(Date date) {
        if (start == null && finishAboveOrEquals(date)) {
            return true;
        }
        if (finish == null && startLessOrEquals(date)) {
            return true;
        }
        if (start != null && finish != null && startLessOrEquals(date) && finishAboveOrEquals(date)) {
            return true;
        }
        return false;
    }

    public boolean isIntersect(Period otherPeriod) {
        if (finish == null && otherPeriod.finish == null) {
            return true;
        }
        if (otherPeriod.finish == null) {
            return !otherPeriod.start.after(finish);
        }
        if (finish == null) {
            return !otherPeriod.finish.before(start);
        }
        return !otherPeriod.finish.before(start) && !otherPeriod.start.after(finish);
    }

    public Period getIntersection(Period otherPeriod) {
        Date start = CalendarUtils.max(this.start, otherPeriod.start);
        Date finish = CalendarUtils.min(this.finish, otherPeriod.finish);
        return (start == null || start.compareTo(finish) <= 0)
                ? new Period(start, finish)
                : null;
    }

    protected boolean startLessOrEquals(Date date) {
        Date firstDayInPeriod = DateUtils.truncate(start, Calendar.DAY_OF_MONTH);
        return firstDayInPeriod.compareTo(date) <= 0;
    }

    protected boolean finishAboveOrEquals(Date date) {
        Date lastDayInPeriod = DateUtils.truncate(finish, Calendar.DAY_OF_MONTH);
        return lastDayInPeriod.compareTo(date) >= 0;
    }

    ///CLOVER:OFF
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((finish == null) ? 0 : finish.hashCode());
        result = prime * result + ((start == null) ? 0 : start.hashCode());
        return result;
    }
    ///CLOVER:ON

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Period other = (Period) obj;
        if (finish == null) {
            if (other.finish != null)
                return false;
        } else if (!finish.equals(other.finish))
            return false;
        if (start == null) {
            if (other.start != null)
                return false;
        } else if (!start.equals(other.start))
            return false;
        return true;
    }

    public int length() {
        if (start == null || finish == null) {
            throw new UnsupportedOperationException("Period should contain start and finish date to complete length() operation.");
        }
        Date date1 = DateUtils.truncate(start, Calendar.DAY_OF_MONTH);
        Date date2 = DateUtils.truncate(finish, Calendar.DAY_OF_MONTH);
        return (int)( (date2.getTime() - date1.getTime()) / (1000 * 60 * 60 * 24)) + 1;
    }

    public List<Date> getDays() {
        if (days == null) {
            days = Collections.unmodifiableList(populateDaysCollection());
        }
        return days;
    }

    // TODO: refactor
    private List<Date> populateDaysCollection() {
        List<Date> result = new ArrayList<Date>();
        if (start == null || finish == null) {
            throw new UnsupportedOperationException("Period should contain start and finish date to complete getDays() operation.");
        }
        Date day = DateUtils.truncate(start, Calendar.DAY_OF_MONTH);
        days = new ArrayList<Date>();
        while (contains(day)) {
            result.add(day);
            day = DateUtils.addDays(day, 1);
        }
        return result;
    }

    public boolean hasNullDates() {
        if (start != null && finish != null) {
            return false;
        } else {
            return true;
        }
    }

    @AssertTrue(message = "{period.notValidRange}")
    public boolean isValid() {
        return start == null
                || finish == null
                || start.compareTo(finish) <= 0;
    }

}

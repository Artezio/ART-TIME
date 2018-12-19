package com.artezio.arttime.test.utils;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

import com.artezio.arttime.datamodel.Employee;
import com.artezio.arttime.datamodel.HourType;
import com.artezio.arttime.datamodel.Hours;
import com.artezio.arttime.datamodel.Period;
import com.artezio.arttime.datamodel.Project;

public class CalendarUtils {
    public static Date getOffsetDate(int offset) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, offset);
        resetTime(calendar);
        return calendar.getTime();
    }

    public static Period createPeriod(int startOffset, int finishOffset) {
        return new Period(getOffsetDate(startOffset), getOffsetDate(finishOffset));
    }

    static void resetTime(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    public static Date resetTime(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        resetTime(calendar);
        return calendar.getTime();
    }

    public static Hours createHours(Project project, Employee employee, Date date, HourType actualType,
                                    BigDecimal quantity) {
        Hours hours = new Hours();
        hours.setEmployee(employee);
        hours.setDate(date);
        hours.setQuantity(quantity);
        hours.setType(actualType);
        hours.setProject(project);
        return hours;
    }
}

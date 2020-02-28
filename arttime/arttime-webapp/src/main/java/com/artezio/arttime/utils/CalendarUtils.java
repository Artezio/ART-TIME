package com.artezio.arttime.utils;

import org.apache.commons.lang.time.DateUtils;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Optional;

public class CalendarUtils {	

    public static Date currentWeekStartDate() {
		Calendar calendar = Calendar.getInstance(getLocale());
		calendar.get(Calendar.DAY_OF_WEEK);		
		calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());		
		return DateUtils.truncate(calendar, Calendar.DAY_OF_MONTH).getTime();
	}

	public static Date currentWeekEndDate() {
		Calendar calendar = Calendar.getInstance(getLocale());
		calendar.add(Calendar.WEEK_OF_YEAR, 1);
		calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
		calendar.add(Calendar.DATE, -1);
		return DateUtils.truncate(calendar, Calendar.DAY_OF_MONTH).getTime();
	}

	public static Locale getLocale() {
        return Optional.ofNullable(FacesContext.getCurrentInstance())
                .map(FacesContext::getExternalContext)
                .map(ExternalContext::getRequestLocale)
                .orElse(Locale.getDefault());
	}
	
    public static Date firstDayOfWeek(Date date){
		Calendar calendar = Calendar.getInstance(getLocale());
		calendar.setTime(date);				
		calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
		return calendar.getTime();
	}		
	
	public static Date lastDayOfWeek(Date date){
		Calendar calendar = Calendar.getInstance(getLocale());
		calendar.setTime(date);		
		calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
		calendar.add(Calendar.DAY_OF_YEAR, 6);
		return calendar.getTime();
	}

	public static Date firstDayOfMonth(Date date) {
		date = DateUtils.truncate(date, Calendar.DAY_OF_MONTH);
		return DateUtils.setDays(date, 1);
	}
	
	public static Date lastDayOfMonth(Date date) {
		date = DateUtils.truncate(date, Calendar.DAY_OF_MONTH);
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
		return calendar.getTime();
	}
	
    public static Date max(Date date1, Date date2) {
        if (date1 == null && date2 == null) return null;
        if (date1 == null) return date2;
        if (date2 == null) return date1;
        return (date1.after(date2)) ? date1 : date2;
    }
    
    public static Date min(Date date1, Date date2) {
        if (date1 == null && date2 == null) return null;
        if (date1 == null) return date2;
        if (date2 == null) return date1;
        return (date1.before(date2)) ? date1 : date2;
    }
    
    public static Boolean isWeekend(Date date) {
    	Calendar calendar = Calendar.getInstance();
    	calendar.setTime(date);
    	int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
    	return dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY;    	
    }
    
    public static java.sql.Date convertToSqlDate(Date date) {
        return new java.sql.Date(date.getTime());
    }
}

package com.artezio.arttime.web.components;

import com.artezio.arttime.utils.CalendarUtils;
import org.apache.commons.lang.time.DateUtils;

import javax.faces.component.FacesComponent;
import java.util.Date;

@FacesComponent("monthSelector")
public class MonthSelector extends PeriodSelector {

	@Override
	public void setPreviousPeriod() {
		Date selectedDate = getPeriod().getStart();
		selectedDate = DateUtils.addMonths(selectedDate, -1);
		Date start = CalendarUtils.firstDayOfMonth(selectedDate);
		Date finish = CalendarUtils.lastDayOfMonth(selectedDate);
		updatePeriod(start, finish);
	}	

	@Override
	public void setNextPeriod() {
		Date selectedDate = getPeriod().getStart();
		selectedDate = DateUtils.addMonths(selectedDate, 1);
		Date start = CalendarUtils.firstDayOfMonth(selectedDate);
		Date finish = CalendarUtils.lastDayOfMonth(selectedDate);
		updatePeriod(start, finish);		
	}

}

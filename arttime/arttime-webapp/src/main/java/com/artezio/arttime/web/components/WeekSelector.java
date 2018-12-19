package com.artezio.arttime.web.components;

import org.apache.commons.lang.time.DateUtils;

import javax.faces.component.FacesComponent;
import java.util.Date;

@FacesComponent("weekSelector")
public class WeekSelector extends PeriodSelector {

	@Override
	public void setPreviousPeriod() {
		Date start = DateUtils.addDays(getPeriod().getStart(), -7);
		Date finish = DateUtils.addDays(getPeriod().getFinish(), -7);
		updatePeriod(start, finish);		
	}

	@Override
	public void setNextPeriod() {
		Date start = DateUtils.addDays(getPeriod().getStart(), 7);
		Date finish = DateUtils.addDays(getPeriod().getFinish(), 7);
		updatePeriod(start, finish);
	}

}

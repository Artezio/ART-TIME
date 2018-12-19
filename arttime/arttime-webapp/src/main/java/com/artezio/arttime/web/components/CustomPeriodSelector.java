package com.artezio.arttime.web.components;

import org.apache.commons.lang.time.DateUtils;

import javax.faces.component.FacesComponent;
import java.util.Date;

@FacesComponent("customPeriodSelector")
public class CustomPeriodSelector extends PeriodSelector {

	@Override
	public void setPreviousPeriod() {		
		int periodLength = getPeriod().length();
		Date start = DateUtils.addDays(getPeriod().getStart(), -periodLength); 
		Date finish = DateUtils.addDays(getPeriod().getFinish(), -periodLength);
		updatePeriod(start, finish);

	}

	@Override
	public void setNextPeriod() {
		int periodLength = getPeriod().length();
		Date start = DateUtils.addDays(getPeriod().getStart(), periodLength);
		Date finish = DateUtils.addDays(getPeriod().getFinish(), periodLength);
		updatePeriod(start, finish);
	}

}

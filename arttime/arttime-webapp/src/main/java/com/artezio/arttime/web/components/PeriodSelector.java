package com.artezio.arttime.web.components;

import com.artezio.arttime.datamodel.Period;

import javax.faces.component.UINamingContainer;
import java.util.Date;

public abstract class PeriodSelector extends UINamingContainer {	
	private Period period;
		
	public void init() {
		period = (Period) getAttributes().get("period");		
	}	    	
	
	public Period getPeriod() {
		return period;
	}

	public void setPeriod(Period period) {
		this.period = period;
	}
	
	protected void updatePeriod(Date start, Date finish) {
		period.setStart(start);
		period.setFinish(finish);
	}

	public abstract void setPreviousPeriod();	
	public abstract void setNextPeriod();
}

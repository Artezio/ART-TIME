package com.artezio.arttime.services.integration;

import javax.xml.bind.annotation.*;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
public class HoursSearchCriteria implements Serializable {
	private static final long serialVersionUID = 7805097657419399172L;
	
	@XmlSchemaType(name="date", type=XMLGregorianCalendar.class)
	private Date dateFrom;
	@XmlSchemaType(name="date", type=XMLGregorianCalendar.class)
	private Date dateTo;
	private boolean approvedOnly;
    private boolean includeSubprojects;
    @XmlElementWrapper(name = "projectCodes", required = true)
    @XmlElement(name = "code")
    private List<String> projectCodes;

	public HoursSearchCriteria() {
	}

	public HoursSearchCriteria(Date dateFrom, Date dateTo, boolean approvedOnly) {
		this.dateFrom = dateFrom;
		this.dateTo = dateTo;
		this.approvedOnly = approvedOnly;
	}

	public HoursSearchCriteria(Date dateFrom, Date dateTo, boolean approvedOnly, List<String> projectCodes) {
	    this(dateFrom, dateTo, approvedOnly);
	    this.projectCodes = projectCodes;
    }

	public Date getDateFrom() {
		return dateFrom;
	}

	public void setDateFrom(Date dateFrom) {
		this.dateFrom = dateFrom;
	}

	public Date getDateTo() {
		return dateTo;
	}

	public void setDateTo(Date dateTo) {
		this.dateTo = dateTo;
	}

	public boolean isApprovedOnly() {
		return approvedOnly;
	}

	public void setApprovedOnly(boolean approvedOnly) {
		this.approvedOnly = approvedOnly;
	}

    public boolean isIncludeSubprojects() {
        return includeSubprojects;
    }

    public void setIncludeSubprojects(boolean includeSubprojects) {
        this.includeSubprojects = includeSubprojects;
    }

	public List<String> getProjectCodes() {
		return projectCodes;
	}

	public void setProjectCodes(List<String> projectCodes) {
		this.projectCodes = projectCodes;
	}
}

package com.artezio.arttime.web;

import com.artezio.arttime.datamodel.Hours;
import com.artezio.arttime.exceptions.SaveApprovedHoursException;
import com.artezio.arttime.filter.Filter;
import com.artezio.arttime.services.EmployeeService;
import com.artezio.arttime.services.HoursService;
import com.artezio.arttime.services.ProjectService;
import com.artezio.arttime.web.spread_sheet.PersonalEffortsSpreadSheet;
import com.artezio.arttime.web.spread_sheet.SpreadSheet;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.Set;

@Named
@ViewScoped
public class TimesheetBean extends EffortsBean {

    private static final long serialVersionUID = -1012227760029456067L;

    @Inject
    private EmployeeService employeeService;
    @Inject
    private HoursService hoursService;
    @Inject
    private ProjectService projectService;

    protected SpreadSheet initSpreadSheet() {
        Filter filter = filterService.getTimesheetFilter();
        filter.setRangePeriodSelector(filterBean.getCurrentFilter().getRangePeriodSelector());
        return new PersonalEffortsSpreadSheet(hoursService, projectService, employeeService, filter);
    }

    public void setFilterBean(FilterBean filterBean) {
        this.filterBean = filterBean;
    }

    public void saveHours() throws SaveApprovedHoursException, ReflectiveOperationException {
        Set<Hours> hours = getSpreadSheet().getUpdatedHours();
        hoursService.reportHours(hours);
        resetData();
    }

    public String getStyleClass(Hours hours) {
        if (hours != null && !hours.getProject().isAllowEmployeeReportTime() && !hours.isApproved()) {
            return "blockedHours";
        }
        return super.getStyleClass(hours);
    }

}

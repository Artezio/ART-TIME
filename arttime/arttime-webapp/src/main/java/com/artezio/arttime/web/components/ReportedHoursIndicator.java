package com.artezio.arttime.web.components;

import com.artezio.arttime.datamodel.HourType;
import com.artezio.arttime.filter.Filter;
import com.artezio.arttime.services.EmployeeService;
import com.artezio.arttime.services.HourTypeService;
import com.artezio.arttime.services.WorkTimeService;
import com.artezio.arttime.utils.MessagesUtil;
import com.artezio.arttime.web.FilterBean;

import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Optional;

@Named
@ViewScoped
public class ReportedHoursIndicator implements Serializable {
    private static final long serialVersionUID = -5792607307504434029L;

    @Inject
    private WorkTimeService workTimeService;
    @Inject
    private HourTypeService hourTypeService;
    @Inject
    private EmployeeService employeeService;

    public BigDecimal getRequiredTime() {
        return employeeService.getLoggedEmployee()
                .map(employee -> workTimeService.getRequiredWorkHours(employee, getFilter().getPeriod()))
                .orElse(BigDecimal.ZERO);
    }

    public BigDecimal getReportedTime() {
        return employeeService.getLoggedEmployee()
                .map(employee -> workTimeService.getActualWorkHours(employee, getFilter().getPeriod()))
                .orElse(BigDecimal.ZERO);
    }

    public int getReportedTimePercents() {
        BigDecimal required = getRequiredTime();
        BigDecimal reported = getReportedTime();
        BigDecimal result = percent(reported, required);
        return result.intValue();
    }

    public int getOverTimePercents() {
        BigDecimal required = getRequiredTime();
        BigDecimal reported = getReportedTime();
        if (reported.compareTo(required) > 0) {
            return new BigDecimal("100").subtract(percent(required, reported)).intValue();
        }
        return 0;
    }

    private BigDecimal percent(BigDecimal value1, BigDecimal value2) {
        BigDecimal hundred = new BigDecimal("100");
        BigDecimal result = new BigDecimal("100");
        if (value2.compareTo(BigDecimal.ZERO) != 0) {
            result = value1.multiply(hundred).divide(value2, BigDecimal.ROUND_HALF_DOWN);
        }
        return result.min(hundred);
    }

    public boolean isTimeReportedIncorrectly() {
        return getReportedTime().compareTo(getRequiredTime()) != 0;
    }

    @SuppressWarnings("unchecked")
    private <T> T findBean(String beanName) {
        FacesContext context = FacesContext.getCurrentInstance();
        return (T) context.getApplication().evaluateExpressionGet(context, "#{" + beanName + "}", Object.class);
    }

    protected Filter getFilter() {
        FilterBean filterBean = findBean("filterBean");
        return filterBean.getCurrentFilter();
    }

    public String getWarningMessage() {
        String title = "";
        HourType actualHourType = hourTypeService.findActualTime();
        if (actualHourType != null) {
            title = MessagesUtil.getLocalizedString("hoursIndicator.tooltip", "'" + actualHourType.getType() + "'");
        }
        return title;
    }

}

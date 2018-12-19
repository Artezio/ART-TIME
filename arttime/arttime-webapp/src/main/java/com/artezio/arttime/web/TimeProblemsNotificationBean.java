package com.artezio.arttime.web;

import com.artezio.arttime.datamodel.Employee;
import com.artezio.arttime.filter.Filter;
import com.artezio.arttime.services.NotificationManagerLocal;
import com.artezio.arttime.services.WorkTimeService;

import javax.faces.annotation.ManagedProperty;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

@Named
@ViewScoped
public class TimeProblemsNotificationBean implements Serializable {
    private static final long serialVersionUID = -2955420544322390401L;

    @Inject
    private NotificationManagerLocal notificationManager;
    @Inject
    private WorkTimeService workTimeService;
    private List<WorkTimeService.WorkTime> selectedRows;
    private List<WorkTimeService.WorkTime> employeesProblemTime;
    private String comment;
    @Inject @ManagedProperty("#{filterBean}")
    protected FilterBean filterBean;

    public List<WorkTimeService.WorkTime> getSelectedRows() {
        if (selectedRows == null) {
            selectedRows = getProblematicEmployees();
        }
        return selectedRows;
    }

    public void setSelectedRows(List<WorkTimeService.WorkTime> selectedRows) {
        this.selectedRows = selectedRows;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void sendNotifications() {
        notificationManager.notifyAboutWorkTimeProblems(getSelectedEmployees(),
                filterBean.getCurrentFilter().getPeriod(), comment);
    }

    public List<WorkTimeService.WorkTime> getProblematicEmployees() {
        if (employeesProblemTime == null) {
            employeesProblemTime = workTimeService.getProblemWorkTime(filterBean.getCurrentFilter());
            employeesProblemTime.sort(WorkTimeService.WorkTime.EMPLOYEE_COMPARATOR);
        }
        return employeesProblemTime;
    }

    public void setCurrentFilter(Filter currentFilter) {
        filterBean.setCurrentFilter(currentFilter);
        resetData();
    }

    public FilterBean getFilterBean() {
        return filterBean;
    }

    public void setFilterBean(FilterBean filterBean) {
        this.filterBean = filterBean;
    }

    public void resetData() {
        employeesProblemTime = null;
        selectedRows = null;
    }

    protected List<Employee> getSelectedEmployees() {
        return getSelectedRows().stream()
                .map(WorkTimeService.WorkTime::getEmployee)
                .collect(Collectors.toList());
    }

}

package com.artezio.arttime.web;

import com.artezio.arttime.datamodel.Employee;
import com.artezio.arttime.datamodel.HourType;
import com.artezio.arttime.datamodel.Hours;
import com.artezio.arttime.datamodel.Project;
import com.artezio.arttime.filter.Filter;
import com.artezio.arttime.report.OutputFormat;
import com.artezio.arttime.report.ReportEngine;
import com.artezio.arttime.services.*;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

import javax.faces.annotation.ManagedProperty;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.sql.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.artezio.arttime.report.datasource.pojo.DataSet.*;

@Named
@ViewScoped
public class ReportsBean implements Serializable {

    private static final long serialVersionUID = 2537452262275935640L;

    @Inject
    private ReportEngine reportEngine;
    @Inject
    private EmployeeService employeeService;
    @Inject
    private DepartmentService departmentService;
    @Inject
    private ProjectService projectService;
    @Inject
    private HoursService hoursService;
    @Inject
    private HourTypeService hourTypeService;
    @Inject @ManagedProperty("#{filterBean}")
    private FilterBean filterBean;
    private String templateName;
    private OutputFormat outputFormat;

    public StreamedContent getReportFile() {
        return new DefaultStreamedContent(generateReport(), outputFormat.getContentType(),
                getOutputFileName());
    }

    protected InputStream generateReport() {
        byte[] reportInBytes = reportEngine.generate(templateName, outputFormat, getReportParams());
        return new ByteArrayInputStream(reportInBytes);
    }

    protected Map<String, Object> getReportParams() {
        Map<String, Object> result = new HashMap<>();
        result.put(REPORT_PARAM_NAME_START_DATE, getSelectedStartDate());
        result.put(REPORT_PARAM_NAME_END_DATE, getSelectedEndDate());
        result.put(REPORT_PARAM_NAME_EMPLOYEE_USERNAMES, getSelectedEmployeeUserNames());
        result.put(REPORT_PARAM_NAME_DEPARTMENTS, getSelectedDepartments());
        result.put(REPORT_PARAM_NAME_PROJECT_IDS, getSelectedProjectIds());
        result.put(REPORT_PARAM_NAME_HOUR_TYPE_IDS, getSelectedHourTypeIds());
        return result;
    }

    protected Date getSelectedEndDate() {
        return new Date(getFilter().getPeriod().getFinish().getTime());
    }

    protected Date getSelectedStartDate() {
        return new Date(getFilter().getPeriod().getStart().getTime());
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public OutputFormat getOutputFormat() {
        return outputFormat;
    }

    public void setOutputFormat(OutputFormat outputFormat) {
        this.outputFormat = outputFormat;
    }

    public String getOutputFileName() {
        return templateName + "." + outputFormat.getFileExtension();
    }

    protected String[] getSelectedEmployeeUserNames() {
        return employeeService.getEffortsEmployees(getFilter()).stream()
                .map(Employee::getUserName)
                .toArray(String[]::new);
    }

    protected String[] getSelectedDepartments() {
        return (getFilter().containsAtLeastOneDepartment()
                ? getFilter().getDepartments()
                : departmentService.getAll())
                .toArray(new String[0]);
    }

    protected Long[] getSelectedProjectIds() {
        List<Hours> hours = hoursService.getHours(getFilter());
        Set<Project> projects = hours.stream()
                .map(Hours::getProject)
                .collect(Collectors.toSet());
        projects.addAll(projectService.getEffortsProjects(getFilter()));
        return projects.stream()
                .map(Project::getId)
                .toArray(Long[]::new);
    }

    protected Long[] getSelectedHourTypeIds() {
        List<HourType> hourTypes = getFilter().containsAtLeastOneHourType()
                ? getFilter().getHourTypes()
                : hourTypeService.getAll();
        return hourTypes
                .stream()
                .map(HourType::getId)
                .toArray(Long[]::new);
    }

    public void setFilterBean(FilterBean filterBean) {
        this.filterBean = filterBean;
    }

    private Filter getFilter() {
        return filterBean.getCurrentFilter();
    }

}

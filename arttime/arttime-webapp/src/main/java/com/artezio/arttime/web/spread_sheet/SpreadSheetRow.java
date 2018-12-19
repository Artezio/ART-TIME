package com.artezio.arttime.web.spread_sheet;

import com.artezio.arttime.datamodel.Employee;
import com.artezio.arttime.datamodel.HourType;
import com.artezio.arttime.datamodel.Project;
import com.artezio.arttime.web.EffortsGrouping;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public abstract class SpreadSheetRow<V> implements Serializable {
    private static final long serialVersionUID = -6007189624633842151L;

    protected Project project;
    protected Employee employee;
    protected HourType hourType;
    protected Map<Date, V> valuesMap = new HashMap<>();

    public abstract BigDecimal getRowTotal();

    public abstract V get(Date date);

    public abstract String getFirstColValue(EffortsGrouping grouping);

    public SpreadSheetRow() {
    }

    public SpreadSheetRow(Project project) {
        this.project = project;
    }

    public void setRowTotal(BigDecimal value) {
    }

    public SpreadSheetRow(Project project, Employee employee, HourType hourType, Map<Date, V> map) {
        this.project = project;
        this.employee = employee;
        this.hourType = hourType;
        this.valuesMap = map;
    }

    public SpreadSheetRow(Project project, Employee employee, HourType hourType) {
        this.project = project;
        this.employee = employee;
        this.hourType = hourType;
    }

    public SpreadSheetRow(Project project, HourType hourType) {
        this.project = project;
        this.hourType = hourType;
    }

    public SpreadSheetRow(Employee employee) {
        this.employee = employee;
    }

    public SpreadSheetRow(Employee employee, HourType hourType) {
        this.employee = employee;
        this.hourType = hourType;
    }

    public Project getProject() {
        return project;
    }

    public Employee getEmployee() {
        return employee;
    }

    public HourType getHourType() {
        return hourType;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public void setHourType(HourType hourType) {
        this.hourType = hourType;
    }

    public Map<Date, V> getValuesMap() {
        return valuesMap;
    }

    public int getKey() {
        return ((employee == null) ? 0 : employee.hashCode())
                + ((hourType == null) ? 0 : hourType.hashCode())
                + ((project == null) ? 0 : project.hashCode());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((employee == null) ? 0 : employee.hashCode());
        result = prime * result
                + ((hourType == null) ? 0 : hourType.hashCode());
        result = prime * result + ((project == null) ? 0 : project.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SpreadSheetRow other = (SpreadSheetRow) obj;
        if (employee == null) {
            if (other.employee != null)
                return false;
        } else if (!employee.equals(other.employee))
            return false;
        if (hourType == null) {
            if (other.hourType != null)
                return false;
        } else if (!hourType.equals(other.hourType))
            return false;
        if (project == null) {
            if (other.project != null)
                return false;
        } else if (!project.equals(other.project))
            return false;
        return true;
    }
}

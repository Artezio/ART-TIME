package com.artezio.arttime.web.spread_sheet;

import com.artezio.arttime.datamodel.Employee;
import com.artezio.arttime.datamodel.HourType;
import com.artezio.arttime.datamodel.Project;
import com.artezio.arttime.web.EffortsGrouping;

import java.math.BigDecimal;
import java.util.Date;

public class HeadSpreadSheetRow extends SpreadSheetRow<Object> {
    private static final long serialVersionUID = 2226469842174139080L;

    public HeadSpreadSheetRow(Project project) {
        super(project);
    }

    public HeadSpreadSheetRow(Employee employee) {
        super(employee);
    }

    public HeadSpreadSheetRow(Project project, Employee employee, HourType hourType) {
        super(project, employee, hourType);
    }

    @Override
    public BigDecimal getRowTotal() {
        return null;
    }

    public String getFirstColValue(EffortsGrouping grouping) {
        return (grouping == EffortsGrouping.BY_EMPLOYEES)
                ? getEmployee().getFullName()
                : getProject().getDisplayCode();
    }

    @Override
    public Object get(Date date) {
        return null;
    }
}

package com.artezio.arttime.web.spread_sheet;

import com.artezio.arttime.datamodel.Employee;
import com.artezio.arttime.datamodel.HourType;
import com.artezio.arttime.datamodel.Hours;
import com.artezio.arttime.datamodel.Project;
import com.artezio.arttime.web.EffortsGrouping;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class TotalsSpreadSheetRow extends SpreadSheetRow<BigDecimal> {

    private static final long serialVersionUID = 639156120656864989L;

    private List<HoursSpreadSheetRow> hoursRows;

    public TotalsSpreadSheetRow(Project project, HourType hourType, List<HoursSpreadSheetRow> hoursRows) {
        super(project, hourType);
        this.hoursRows = hoursRows;
    }

    public TotalsSpreadSheetRow(Employee employee, HourType hourType, List<HoursSpreadSheetRow> hoursRows) {
        super(employee, hourType);
        this.hoursRows = hoursRows;
    }

    @Override
    public BigDecimal get(Date date) {
        BigDecimal result = BigDecimal.ZERO;
        for (HoursSpreadSheetRow row : hoursRows) {
            Hours hour = row.get(date);
            if (hour != null && hour.getQuantity() != null) {
                result = result.add(hour.getQuantity());
            }
        }
        return result;
    }

    @Override
    public BigDecimal getRowTotal() {
        BigDecimal result = BigDecimal.ZERO;
        for (HoursSpreadSheetRow row : hoursRows) {
            for (Hours hour : row.getHours()) {
                if (hour != null && hour.getQuantity() != null) {
                    result = result.add(hour.getQuantity());
                }
            }
        }
        return result;
    }

    @Override
    public String getFirstColValue(EffortsGrouping grouping) {
        return null;
    }

    public int getKey() {
        return getProjects().size() > 0
                ? super.getKey() + hoursRows.stream().mapToInt(HoursSpreadSheetRow::getKey).sum()
                : super.getKey();
    }

    public boolean containsTotalBy(Project project) {
        return getProjects().contains(project);
    }

    private Set<Project> getProjects() {
        return hoursRows.stream()
            .map(SpreadSheetRow::getProject)
            .collect(Collectors.toSet());
    }

}

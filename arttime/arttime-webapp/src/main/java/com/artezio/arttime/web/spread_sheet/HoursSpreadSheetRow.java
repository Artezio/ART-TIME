package com.artezio.arttime.web.spread_sheet;

import com.artezio.arttime.datamodel.Employee;
import com.artezio.arttime.datamodel.HourType;
import com.artezio.arttime.datamodel.Hours;
import com.artezio.arttime.datamodel.Project;
import com.artezio.arttime.web.EffortsGrouping;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class HoursSpreadSheetRow extends SpreadSheetRow<Hours> {

    private static final long serialVersionUID = 954326652147197306L;

    public HoursSpreadSheetRow() {
    }

    public HoursSpreadSheetRow(Project project, Employee employee, HourType hourType, List<Hours> hours) {
        super(project, employee, hourType, buildMap(hours));
    }

    private static Map<Date, Hours> buildMap(List<Hours> hours) {
        return hours.stream()
                .collect(Collectors.toMap(Hours::getDate, hour -> hour));
    }

    @Override
    public Hours get(Date date) {
        if (date == null) return null;
        Hours result = valuesMap.get(date);
        if (result == null) {
            result = new Hours(project, date, employee, hourType);
            valuesMap.put(date, result);
        }
        return result;
    }

    @Override
    public BigDecimal getRowTotal() {
        BigDecimal result = BigDecimal.ZERO;
        for (Hours hour : valuesMap.values()) {
            if (hour != null && hour.getQuantity() != null) {
                result = result.add(hour.getQuantity());
            }
        }
        return result;
    }

    @Override
    public String getFirstColValue(EffortsGrouping grouping) {
        return (grouping == EffortsGrouping.BY_EMPLOYEES)
                ? getProject().getDisplayCode()
                : getEmployee().getFullName();
    }

    public List<Hours> getHours() {
        return valuesMap.values().stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

}

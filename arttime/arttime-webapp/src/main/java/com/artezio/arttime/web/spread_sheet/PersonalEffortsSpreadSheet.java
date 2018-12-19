package com.artezio.arttime.web.spread_sheet;

import com.artezio.arttime.datamodel.Employee;
import com.artezio.arttime.datamodel.HourType;
import com.artezio.arttime.datamodel.Hours;
import com.artezio.arttime.datamodel.Project;
import com.artezio.arttime.filter.Filter;
import com.artezio.arttime.services.EmployeeService;
import com.artezio.arttime.services.HoursService;
import com.artezio.arttime.services.ProjectService;
import com.google.common.collect.Sets;

import java.util.*;
import java.util.stream.Collectors;

import static com.artezio.arttime.datamodel.Project.Status.ACTIVE;

public class PersonalEffortsSpreadSheet extends SpreadSheet {

    private static final long serialVersionUID = -3996428704670724803L;

    public PersonalEffortsSpreadSheet(HoursService hoursService, ProjectService projectService,
                                      EmployeeService employeeService, Filter filter) {
        super(hoursService,
                projectService,
                employeeService,
                filter,
                employeeService.getLoggedEmployee()
                        .map(Collections::singleton)
                        .orElse(Collections.emptySet()));
    }

    @Override
    public Object getValue(SpreadSheetRow<?> row, Date date) {
        Object value = super.getValue(row, date);
        if (value instanceof Hours) {
            Hours hours = (Hours) value;
            if (row.getProject().isTeamMember(row.getEmployee())) {
                return hours;
            }
            if (!row.getProject().isTeamMember(row.getEmployee()) && hours.getQuantity() != null) {
                return hours;
            }
            return null;
        }
        return value;
    }

    @Override
    protected List<SpreadSheetRow<?>> buildSpreadSheetRows() {
        List<SpreadSheetRow<?>> rows = new ArrayList<>();
        if (!filter.containsAtLeastOneProject()) {
            return rows;
        }
        HoursIndexedBundle indexedHours = new HoursIndexedBundle(hoursService.getTimesheetHours(filter.getPeriod()));
        Employee employee = filter.getEmployees().get(0);
        List<Project> projects = getActiveOrReportedProjects(projectService.getMyProjects(), indexedHours);

        List<HoursSpreadSheetRow> hoursRows = new ArrayList<>();
        projects.stream()
                .sorted(Project.DISPLAY_CODE_COMPARATOR)
                .forEach(project -> hoursRows.addAll(buildHoursRows(employee, project, indexedHours)));
        rows.addAll(hoursRows);
        rows.addAll(buildTotalRows(employee, hoursRows));
        return rows;
    }

    protected List<Project> getActiveOrReportedProjects(List<Project> projects, HoursIndexedBundle hours) {
        Set<Project> allProjects = Sets.union(new HashSet<>(projects), new HashSet<>(hours.getProjects()));
        return allProjects.stream()
                .filter(project -> project.getStatus() == ACTIVE || hours.containsHours(project))
                .collect(Collectors.toList());
    }

    private List<HoursSpreadSheetRow> buildHoursRows(Employee employee, Project project, HoursIndexedBundle indexedHours) {
        List<HoursSpreadSheetRow> result = new LinkedList<>();
        getFilteredHourTypesUnion(employee, project, indexedHours).stream()
                .sorted(HourType.ACTUALTIME_TYPE_COMPARATOR)
                .forEach(hourType -> {
                    List<Hours> projectHours = indexedHours.findHours(project, employee, hourType);
                    result.add(new HoursSpreadSheetRow(project, employee, hourType, projectHours));
                });
        return result;
    }

    private List<TotalsSpreadSheetRow> buildTotalRows(Employee employee, List<HoursSpreadSheetRow> hoursRows) {
        Map<HourType, List<HoursSpreadSheetRow>> hoursRowsMap = hoursRows.stream()
                .collect(Collectors.groupingBy(SpreadSheetRow::getHourType,
                        () -> new TreeMap<>(HourType.ACTUALTIME_TYPE_COMPARATOR),
                        Collectors.toList()));
        List<TotalsSpreadSheetRow> result = new ArrayList<>();
        hoursRowsMap.forEach((type, rows) -> result.add(new TotalsSpreadSheetRow(employee, type, rows)));
        return result;
    }

    @Override
    protected List<Integer> calculateKeysOfTotalsRows(SpreadSheetRow<?> spreadSheetRow) {
        Employee employee = spreadSheetRow.getEmployee();
        return rows.stream()
                .filter(row -> row instanceof TotalsSpreadSheetRow && employee.equals(row.getEmployee()))
                .map(SpreadSheetRow::getKey)
                .collect(Collectors.toList());
    }

}

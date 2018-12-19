package com.artezio.arttime.web.spread_sheet;

import com.artezio.arttime.datamodel.Employee;
import com.artezio.arttime.datamodel.HourType;
import com.artezio.arttime.datamodel.Hours;
import com.artezio.arttime.datamodel.Project;
import com.artezio.arttime.filter.Filter;
import com.artezio.arttime.services.EmployeeService;
import com.artezio.arttime.services.HoursService;
import com.artezio.arttime.services.ProjectService;

import java.util.*;
import java.util.stream.Collectors;

import static com.artezio.arttime.datamodel.Project.Status.ACTIVE;

public class EmployeeEffortsSpreadSheet extends SpreadSheet {

    private static final long serialVersionUID = 8766325612366887278L;

    public EmployeeEffortsSpreadSheet(HoursService hoursService,
            ProjectService projectService, EmployeeService employeeService, Filter filter) {
        super(hoursService, projectService, employeeService, filter, employeeService.getEffortsEmployees());
    }

    @Override
    protected List<Integer> calculateKeysOfTotalsRows(SpreadSheetRow<?> spreadSheetRow) {
        Employee employee = spreadSheetRow.getEmployee();
        return rows.stream()
                .filter(row -> row instanceof TotalsSpreadSheetRow && employee.equals(row.getEmployee()))
                .map(SpreadSheetRow::getKey)
                .collect(Collectors.toList());
    }

    @Override
    protected List<SpreadSheetRow<?>> buildSpreadSheetRows() {
        HoursIndexedBundle hoursBundle = new HoursIndexedBundle(hoursService.getHours(filter));
        List<Project> availableProjects = projectService.fetchComplete(projectService.getEffortsProjects(filter)).stream()
                .filter(project -> project.getStatus() == ACTIVE || hoursBundle.containsHours(project)
                        || (isCustomFilter() && filter.getProjects().contains(project)))
                .collect(Collectors.toList());
        Map<Employee, List<Project>> projectsByEmployee = groupProjectsByEmployee(availableProjects, hoursBundle);
        return projectsByEmployee.keySet().parallelStream()
                .sorted(Employee.NAME_COMPARATOR)
                .flatMap(employee -> buildSpreadSheetRows(employee, projectsByEmployee.get(employee), hoursBundle).stream())
                .collect(Collectors.toList());
    }

    private Map<Employee, List<Project>> groupProjectsByEmployee(List<Project> projects, HoursIndexedBundle indexedHours) {
        Map<Employee, List<Project>> projectsByEmployee = new HashMap<>();
        List<Project> mergedProjects = merge(projects, projectService.fetchComplete(indexedHours.getProjects()));
        mergedProjects.stream()
                .sorted(Project.DISPLAY_CODE_COMPARATOR)
                .forEach(project ->
                        getFilteredTeamMembersUnion(project.getTeam(), indexedHours.getEmployees(project)).forEach(employee -> {
                            projectsByEmployee.putIfAbsent(employee, new LinkedList<>());
                            projectsByEmployee.get(employee).add(project);
                }));
        return projectsByEmployee;
    }

    private List<SpreadSheetRow<?>> buildSpreadSheetRows(Employee employee, Collection<Project> projects, HoursIndexedBundle indexedHours) {
        List<SpreadSheetRow<?>> result = new ArrayList<>();
        result.add(new HeadSpreadSheetRow(employee));
        List<HoursSpreadSheetRow> hourRows = new ArrayList<>();
        projects.stream()
                .sorted(Project.DISPLAY_CODE_COMPARATOR)
                .forEach(project -> hourRows.addAll(buildHoursRows(project, employee, indexedHours)));
        result.addAll(hourRows);
        result.addAll(buildTotalsRows(employee, hourRows));
        return result;
    }

    private List<HoursSpreadSheetRow> buildHoursRows(Project project, Employee employee, HoursIndexedBundle indexedHours) {
        List<HoursSpreadSheetRow> result = new LinkedList<>();
        getFilteredHourTypesUnion(employee, project, indexedHours).stream()
                .sorted(HourType.ACTUALTIME_TYPE_COMPARATOR)
                .forEach(hourType -> {
                    List<Hours> hours = indexedHours.findHours(project, employee, hourType);
                    result.add(new HoursSpreadSheetRow(project, employee, hourType, hours));
                });
        return result;
    }

    private List<TotalsSpreadSheetRow> buildTotalsRows(Employee employee, List<HoursSpreadSheetRow> hoursRows) {
        List<TotalsSpreadSheetRow> result = new LinkedList<>();
        Map<HourType, List<HoursSpreadSheetRow>> hoursRowsMap = hoursRows.stream()
                .collect(Collectors.groupingBy(SpreadSheetRow::getHourType));
        hoursRowsMap.keySet().stream()
                .sorted(HourType.ACTUALTIME_TYPE_COMPARATOR)
                .forEach(hourType -> result.add(new TotalsSpreadSheetRow(employee, hourType, hoursRowsMap.get(hourType))));
        return result;
    }

}

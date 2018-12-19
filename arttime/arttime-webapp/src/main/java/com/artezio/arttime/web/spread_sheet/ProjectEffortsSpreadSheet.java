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

import static java.util.Collections.emptyList;

public class ProjectEffortsSpreadSheet extends SpreadSheet {

    private static final long serialVersionUID = -1046744816130524270L;

    private List<Project> projectsInLowLevelRows = new LinkedList<>();

    public ProjectEffortsSpreadSheet(HoursService hoursService, ProjectService projectService,
            EmployeeService employeeService, Filter filter) {
        super(hoursService, projectService, employeeService, filter, employeeService.getEffortsEmployees());
    }

    @Override
    public boolean isHighLevel(SpreadSheetRow<?> row) {
        return !projectsInLowLevelRows.contains(row.getProject());
    }

    @Override
    protected List<SpreadSheetRow<?>> buildSpreadSheetRows() {
        HoursIndexedBundle hoursBundle = new HoursIndexedBundle(hoursService.getHours(filter));
        List<Project> availableProjects = projectService.fetchComplete(projectService.getEffortsProjects(filter));
        List<Project> mergedProjects = merge(availableProjects, projectService.fetchComplete(hoursBundle.getProjects()));
        Map<Project, List<Project>> subprojectsByMaster = groupSubprojectsByMaster(mergedProjects);
        return mergedProjects.parallelStream()
                .filter(project -> !project.isSubproject() || !availableProjects.contains(project.getMaster()))
                .filter(project -> project.getStatus() == Project.Status.ACTIVE || hoursBundle.containsHours(project)
                        || (isCustomFilter() && filter.getProjects().contains(project)))
                .sorted(Project.DISPLAY_CODE_COMPARATOR)
                .flatMap(project -> buildSpreadSheetRows(project, subprojectsByMaster.getOrDefault(project, emptyList()), hoursBundle).stream())
                .collect(Collectors.toList());
    }

    @Override
    protected List<Integer> calculateKeysOfTotalsRows(SpreadSheetRow<?> spreadSheetRow) {
        Project project = spreadSheetRow.getProject();
        return rows.stream()
                .filter(row -> row instanceof TotalsSpreadSheetRow)
                .map(TotalsSpreadSheetRow.class::cast)
                .filter(row -> row.containsTotalBy(project))
                .map(TotalsSpreadSheetRow::getKey)
                .collect(Collectors.toList());
    }

    private Map<Project, List<Project>> groupSubprojectsByMaster(List<Project> projects) {
        return projects.stream()
                .filter(project -> project.getMaster() != null)
                .collect(Collectors.groupingBy(Project::getMaster));
    }

    private List<SpreadSheetRow<?>> buildSpreadSheetRows(Project project, List<Project> subprojects,
                                                         HoursIndexedBundle hoursBundle) {
        List<SpreadSheetRow<?>> result = new ArrayList<>();
        List<SpreadSheetRow<?>> subprojectsSpreadSheetRows = new ArrayList<>();
        result.addAll(buildSpreadSheetRows(project, hoursBundle));
        subprojects.stream()
                .sorted(Project.DISPLAY_CODE_COMPARATOR)
                .filter(subproject -> subproject.getStatus() == Project.Status.ACTIVE || hoursBundle.containsHours(subproject)
                        || (isCustomFilter() && filter.getProjects().contains(subproject)))
                .forEach(subproject -> {
                    subprojectsSpreadSheetRows.addAll(buildSpreadSheetRows(subproject, hoursBundle));
                    projectsInLowLevelRows.add(subproject);
                });
        if (!subprojectsSpreadSheetRows.isEmpty()) {
            result.addAll(subprojectsSpreadSheetRows);
            result.addAll(buildTotalsRows(project, result));
        }
        return result;
    }

    private List<SpreadSheetRow<?>> buildSpreadSheetRows(Project project, HoursIndexedBundle hoursBundle) {
        List<SpreadSheetRow<?>> result = new ArrayList<>();
        List<HoursSpreadSheetRow> hourRows = buildHoursRows(project, hoursBundle);
        if (!(filerHasAnyEmployeeInfo() && hourRows.isEmpty())) {
            result.add(new HeadSpreadSheetRow(project));
            result.addAll(hourRows);
            result.addAll(buildTotalsRows(project, result));
        }
        return result;
    }

    private List<HoursSpreadSheetRow> buildHoursRows(Project project, HoursIndexedBundle hoursBundle) {
        List<HoursSpreadSheetRow> hoursRows = new ArrayList<>();
        List<Employee> employees = getFilteredTeamMembersUnion(project.getTeam(), hoursBundle.getEmployees(project));
        employees.stream()
                .sorted(Employee.NAME_COMPARATOR)
                .forEach(employee -> hoursRows.addAll(buildHoursRows(project, employee, hoursBundle)));
        return hoursRows;
    }

    private List<HoursSpreadSheetRow> buildHoursRows(Project project, Employee employee, HoursIndexedBundle hoursBundle) {
        List<HourType> hourTypes = getFilteredHourTypesUnion(employee, project, hoursBundle);
        return hourTypes.stream()
                .sorted(HourType.ACTUALTIME_TYPE_COMPARATOR)
                .map(hourType -> {
                    List<Hours> hours = hoursBundle.findHours(project, employee, hourType);
                    return new HoursSpreadSheetRow(project, employee, hourType, hours);
                })
                .collect(Collectors.toList());
    }

    private List<TotalsSpreadSheetRow> buildTotalsRows(Project project, List<SpreadSheetRow<?>> allRows) {
        Map<HourType, List<HoursSpreadSheetRow>> hoursRowsMap = allRows.stream()
                .filter(row -> row instanceof HoursSpreadSheetRow)
                .map(HoursSpreadSheetRow.class::cast)
                .collect(Collectors.groupingBy(SpreadSheetRow::getHourType));
        if (hoursRowsMap.isEmpty()) {
            project.getAccountableHours().forEach(hourType -> hoursRowsMap.put(hourType, emptyList()));
        }
        return hoursRowsMap.keySet().stream()
                .sorted(HourType.ACTUALTIME_TYPE_COMPARATOR)
                .map(hourType -> new TotalsSpreadSheetRow(project, hourType, hoursRowsMap.get(hourType)))
                .collect(Collectors.toList());
    }

}

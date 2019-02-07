package com.artezio.arttime.web.spread_sheet;

import com.artezio.arttime.datamodel.Employee;
import com.artezio.arttime.datamodel.HourType;
import com.artezio.arttime.datamodel.Hours;
import com.artezio.arttime.datamodel.Project;
import com.artezio.arttime.filter.Filter;
import com.artezio.arttime.services.EmployeeService;
import com.artezio.arttime.services.HoursService;
import com.artezio.arttime.services.ProjectService;
import com.lassitercg.faces.components.event.SheetUpdate;
import com.lassitercg.faces.components.sheet.Sheet;
import org.apache.commons.lang3.SerializationUtils;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

public abstract class SpreadSheet implements Serializable {

    private static final long serialVersionUID = 7406047486153333282L;

    private Sheet sheet;
    protected Filter filter;
    protected List<SpreadSheetRow<?>> rows;
    protected EmployeeService employeeService;
    protected ProjectService projectService;
    protected HoursService hoursService;
    protected Collection<Employee> availableEmployees = Collections.emptySet();

    protected abstract List<SpreadSheetRow<?>> buildSpreadSheetRows();
    protected abstract List<Integer> calculateKeysOfTotalsRows(SpreadSheetRow<?> updatedRow);

    public SpreadSheet() {}

    public SpreadSheet(HoursService hoursService, ProjectService projectService, EmployeeService employeeService,
                       Filter filter, Collection<Employee> availableEmployees) {
        this.hoursService = hoursService;
        this.projectService = projectService;
        this.employeeService = employeeService;
        this.filter = SerializationUtils.clone(filter);
        this.availableEmployees = availableEmployees;
    }

    public List<SpreadSheetRow<?>> getRows() {
        if (rows == null) {
            rows = buildSpreadSheetRows();
        }
        return rows;
    }

    public void cellEditEvent() {
        Set<Object> rowKeys = new HashSet<>();
        sheet.getUpdates().parallelStream()
                .map(SheetUpdate::getRowIndex)
                .distinct()
                .forEach(update -> rowKeys.addAll(calculateKeysOfTotalsRows(rows.get(update))));
        sheet.getAccumulatedUpdates().addAll(sheet.getUpdates());
        sheet.updateDirtyRows(rowKeys);
    }

    public boolean isHighLevel(SpreadSheetRow<?> row) {
        return true;
    }

    public boolean isReadOnlyCell(SpreadSheetRow<?> row, Date date) {
        return !row.getProject().isAllowEmployeeReportTime()
                || (!row.getProject().isTeamMember(row.getEmployee()) && ((Hours) row.get(date)).getQuantity() == null)
                || ((Hours) row.get(date)).isApproved();
    }

    public Object getValue(SpreadSheetRow<?> row, Date date) {
        return row.get(date);
    }

    public Sheet getSheet() {
        return sheet;
    }

    public void setSheet(Sheet sheet) {
        this.sheet = sheet;
    }

    public List<Hours> getSelectedHours() {
        List<Date> daysInPeriod = filter.getPeriod().getDays();
        String selection = sheet.getSelection();
        if (selection != null) {
            List<Hours> result = new LinkedList<>();
            Range range = new Range(selection);
            for (int i = range.getFromRow(); i <= range.getToRow(); i++) {
                for (int j = range.getFromCol(); j <= range.getToCol(); j++) {
                    int dynamicCol = j - sheet.getFixedCols();
                    if (dynamicCol >= 0) {
                        Date date = daysInPeriod.get(dynamicCol);
                        SpreadSheetRow<?> sheetRow = rows.get(i);
                        if (sheetRow instanceof HoursSpreadSheetRow) {
                            Hours hour = (Hours) sheetRow.getValuesMap().get(date);
                            if (hour != null) {
                                result.add(hour);
                            }
                        }
                    }
                }
            }
            return result;
        }
        return Collections.emptyList();
    }

    public Set<Hours> getUpdatedHours() {
        List<SheetUpdate> updates = sheet.getAccumulatedUpdates();
        List<Date> days = filter.getPeriod().getDays();
        return updates.stream()
                .filter(update -> update.getRowData() instanceof HoursSpreadSheetRow)
                .map(update ->
                        ((HoursSpreadSheetRow) update.getRowData()).get(days.get(update.getColIndex() - sheet.getFixedCols())))
                .collect(Collectors.toSet());
    }

    //TODO rename (perhaps HoursHelper or HoursWrapper are more appropriate) + try to remove this class
    public class HoursIndexedBundle {

        private Map<Project, Map<Employee, Map<HourType, List<Hours>>>> hours;

        protected HoursIndexedBundle(List<Hours> hours) {
            this.hours = hours.parallelStream()
                    .collect(Collectors.groupingBy(Hours::getProject,
                            Collectors.groupingBy(Hours::getEmployee,
                                    Collectors.groupingBy(Hours::getType, Collectors.toList()))));
        }

        public List<Hours> findHours(Project project, Employee employee, HourType hourType) {
            return (hours.containsKey(project))
                    ? findHours(hours.get(project), employee, hourType)
                    : new ArrayList<>();
        }

        public List<Hours> findHours(Project project) {
            return hours.containsKey(project)
                    ? hours.get(project).values().stream()
                    .flatMap(hoursMap -> hoursMap.values().stream())
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList())
                    : new ArrayList<>();
        }

        public List<Hours> findHours(Project project, Employee employee) {
            return hours.getOrDefault(project, Collections.emptyMap())
                    .getOrDefault(employee, Collections.emptyMap())
                    .values().stream()
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
        }

        public boolean containsHours(Project project, Employee employee) {
            return (hours.containsKey(project))
                    && ((hours.get(project).containsKey(employee))
                    && !hours.get(project).get(employee).isEmpty());
        }

        public boolean containsHours(Employee employee) {
            return hours.values().stream()
                    .anyMatch(prj -> prj.containsKey(employee));
        }

        public boolean containsHours(Project project) {
            return hours.containsKey(project);
        }

        public List<HourType> getHourTypes(Project project, Employee employee) {
            return (hours.containsKey(project))
                    ? (hours.get(project).containsKey(employee))
                    ? new ArrayList<>(hours.get(project).get(employee).keySet())
                    : new ArrayList<>()
                    : new ArrayList<>();
        }

        public List<Project> getProjects() {
            return new ArrayList<>(hours.keySet());
        }

        public List<Employee> getEmployees(Project project) {
            return hours.containsKey(project)
                    ? new ArrayList<>(hours.get(project).keySet())
                    : new ArrayList<>();
        }

        private List<Hours> findHours(Map<Employee, Map<HourType, List<Hours>>> map, Employee employee, HourType hourType) {
            return (map.containsKey(employee))
                    ? findHours(map.get(employee), hourType)
                    : new ArrayList<>();
        }

        private List<Hours> findHours(Map<HourType, List<Hours>> map, HourType hourType) {
            return (map.containsKey(hourType))
                    ? map.get(hourType)
                    : new ArrayList<>();
        }

    }

    class Range {
        private int fromRow;
        private int fromCol;
        private int toRow;
        private int toCol;

        public Range(String selectedArea) {
            String[] coordinates = selectedArea.substring(1, selectedArea.length() - 1).split(",");
            fromRow = Math.min(Integer.parseInt(coordinates[0]), Integer.parseInt(coordinates[2]));
            fromCol = Math.min(Integer.parseInt(coordinates[1]), Integer.parseInt(coordinates[3]));
            toRow = Math.max(Integer.parseInt(coordinates[0]), Integer.parseInt(coordinates[2]));
            toCol = Math.max(Integer.parseInt(coordinates[1]), Integer.parseInt(coordinates[3]));
        }

        public int getFromRow() {
            return fromRow;
        }

        public void setFromRow(int fromRow) {
            this.fromRow = fromRow;
        }

        public int getFromCol() {
            return fromCol;
        }

        public void setFromCol(int fromCol) {
            this.fromCol = fromCol;
        }

        public int getToRow() {
            return toRow;
        }

        public void setToRow(int toRow) {
            this.toRow = toRow;
        }

        public int getToCol() {
            return toCol;
        }

        public void setToCol(int toCol) {
            this.toCol = toCol;
        }
    }

    public List<Hours> getHours() {
        return rows.stream()
                .filter(row -> row instanceof HoursSpreadSheetRow)
                .map(row -> ((HoursSpreadSheetRow) row).getHours())
                .collect(ArrayList::new, ArrayList::addAll, ArrayList::addAll);
    }

    public Filter getFilter() {
        return filter;
    }

    public void updateSelectedRows() {
        Set<Object> selectedRowKeys = getSelectedRowKeys();
        sheet.updateDirtyRows(selectedRowKeys);
    }

    public void updateAllRows() {
        Set<Object> selectedRowKeys = getRowKeys();
        sheet.updateDirtyRows(selectedRowKeys);
    }

    protected List<Employee> getFilteredTeamMembersUnion(List<Employee> projectTeam, List<Employee> employeesWithHours) {
        return merge(projectTeam, employeesWithHours).stream()
                .filter(availableEmployees::contains)
                .filter(employee -> (!filter.containsAtLeastOneEmployee() || filter.getEmployees().contains(employee))
                        && (!filter.containsAtLeastOneDepartment() || filter.getDepartments().contains(employee.getDepartment())))
                .collect(Collectors.toList());
    }

    protected List<HourType> getFilteredHourTypesUnion(Employee employee, Project project, HoursIndexedBundle indexedHours) {
        return merge(indexedHours.getHourTypes(project, employee), project.getAccountableHours()).stream()
                .filter(hourType -> !filter.containsAtLeastOneHourType()
                                || (filter.containsAtLeastOneHourType() && filter.getHourTypes().contains(hourType)))
                .collect(Collectors.toList());
    }


    protected <T> List<T> merge(List<T> list1, List<T> list2) {
        Set<T> set = new HashSet<>(list1);
        set.addAll(list2);
        return new ArrayList<>(set);
    }

    protected boolean isCustomFilter() {
        return filter.getName() != null;
    }

    protected boolean filerHasAnyEmployeeInfo() {
        return filter.containsAtLeastOneDepartment() || filter.containsAtLeastOneEmployee();
    }

    private Set<Object> getSelectedRowKeys() {
        Set<Object> selectedRowKeys = new HashSet<>();
        String selection = sheet.getSelection();
        if (selection != null) {
            Range range = new Range(selection);
            for (int i = range.getFromRow(); i <= range.getToRow(); i++) {
                selectedRowKeys.add(rows.get(i).getKey());
            }
        }
        return selectedRowKeys;
    }

    private Set<Object> getRowKeys() {
        Set<Object> rowKeys = new HashSet<>();
        rows.forEach(row -> rowKeys.add(row.getKey()));
        return rowKeys;
    }

}

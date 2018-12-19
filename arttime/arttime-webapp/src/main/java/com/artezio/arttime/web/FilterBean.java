package com.artezio.arttime.web;

import com.artezio.arttime.datamodel.Employee;
import com.artezio.arttime.datamodel.HourType;
import com.artezio.arttime.datamodel.Project;
import com.artezio.arttime.filter.Filter;
import com.artezio.arttime.services.*;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.artezio.arttime.datamodel.Project.Status.ACTIVE;

@Named
@SessionScoped
public class FilterBean implements Serializable {

    private static final long serialVersionUID = -5784546097312069056L;

    private Filter currentFilter;
    @Inject
    private FilterService filterService;
    @Inject
    private ProjectService projectService;
    @Inject
    private EmployeeService employeeService;
    @Inject
    private DepartmentService departmentService;
    @Inject
    private HourTypeService hourTypeService;
    private String newFilterName;

    @PostConstruct
    public void init() {
        currentFilter = filterService.getActiveProjectsFilter();
    }

    public Filter getCurrentFilter() {
        return currentFilter;
    }

    public void setCurrentFilter(Filter filter) {
        if (filter.getId() != null) filter = filterService.getFilter(filter.getName());
        filter.setRangePeriodSelector(currentFilter.getRangePeriodSelector());
        currentFilter = filter;
        setSelectedProjects(filter.getProjects());
        newFilterName = currentFilter.getName();
    }

    public void save() {
        currentFilter.setName(newFilterName);
        setCurrentFilter(filterService.save(currentFilter));
    }

    public void remove(Filter filter) {
        filterService.remove(filter);
        if (currentFilter.equals(filter)) {
            currentFilter.setId(null);
        }
    }

    public String getNewFilterName() {
        return newFilterName;
    }

    public void setNewFilterName(String newFilterName) {
        this.newFilterName = newFilterName;
    }

    public List<Project> getSelectedProjects() {
        return currentFilter.containsAtLeastOneProject()
                ? projectService.fetchComplete(currentFilter.getProjects()).stream()
                    .filter(project -> project.isMasterProject()
                        || project.isSubproject() && project.canBeManaged(employeeService.getLoggedEmployee().orElse(null))
                        && !currentFilter.getProjects().contains(project.getMaster()))
                    .collect(Collectors.toList())
                : currentFilter.getProjects();
    }

    public void setSelectedProjects(List<Project> selectedProjects) {
        selectedProjects = selectedProjects.isEmpty() ? selectedProjects : getProjectHierarchies(selectedProjects);
        currentFilter.setProjects(selectedProjects);
    }

    public List<Employee> getEmployees() {
        return employeeService.getAll()
                .parallelStream()
                .filter(employee -> !employee.isFormer())
                .sorted(Employee.NAME_COMPARATOR)
                .collect(Collectors.toList());
    }

    public List<String> getSelectedDepartments() {
        return currentFilter.getDepartments();
    }

    public void setSelectedDepartments(List<String> departments) {
        currentFilter.setDepartments(departments);
    }

    public List<Employee> getSelectedEmployees() {
        return currentFilter.getEmployees();
    }

    public void setSelectedEmployees(List<Employee> employees) {
        currentFilter.setEmployees(employees);
    }

    public List<HourType> getSelectedHourTypes() {
        return currentFilter.getHourTypes();
    }

    public void setSelectedHourTypes(List<HourType> hourTypes) {
        currentFilter.setHourTypes(hourTypes);
    }

    public List<Project> getProjects() {
        List<Project> availableProjects = projectService.fetchComplete(projectService.getAll());
        Set<Project> projects = availableProjects.stream()
                .filter(project -> project.getStatus() == ACTIVE
                        && (project.isMasterProject()
                        || project.isSubproject() && project.canBeManaged(employeeService.getLoggedEmployee().orElse(null))
                        && !availableProjects.contains(project.getMaster())))
                .collect(Collectors.toSet());
        List<Project> projectsFromFilter = currentFilter.getProjects().stream()
                .filter(project -> project.isMasterProject()
                        || project.isSubproject() && !currentFilter.getProjects().contains(project.getMaster()))
                .collect(Collectors.toList());
        projects.addAll(projectsFromFilter);
        return projects.stream()
                .sorted(Project.DISPLAY_CODE_COMPARATOR)
                .collect(Collectors.toList());
    }

    public List<String> getDepartments() {
        List<String> departments = departmentService.getAll();
        departments.sort(Comparator.naturalOrder());
        return departments;
    }

    public List<HourType> getHourTypes() {
        List<HourType> hourTypes = hourTypeService.getAll();
        hourTypes.sort(HourType.ACTUALTIME_TYPE_COMPARATOR);
        return hourTypes;
    }

    private List<Project> getProjectHierarchies(List<Project> selectedProjects) {
        List<Project> result = new ArrayList<>();
        selectedProjects.forEach(project -> result.addAll(projectService.getProjectHierarchy(project)));
        return result;
    }

    public List<Filter> getFilters() {
        return filterService.getFilters();
    }

}

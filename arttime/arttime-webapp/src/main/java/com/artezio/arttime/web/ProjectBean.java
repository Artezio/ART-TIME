package com.artezio.arttime.web;

import com.artezio.arttime.datamodel.Employee;
import com.artezio.arttime.datamodel.Project;
import com.artezio.arttime.datamodel.Project.Status;
import com.artezio.arttime.datamodel.TeamFilter;
import com.artezio.arttime.services.EmployeeService;
import com.artezio.arttime.services.ProjectService;
import com.artezio.arttime.services.integration.EmployeeTrackingSystem;
import com.artezio.arttime.services.integration.TeamTrackingSystem;
import com.artezio.arttime.utils.NoDuplicatesList;
import com.google.common.collect.Sets;
import org.apache.commons.lang.ArrayUtils;
import org.ocpsoft.common.util.Strings;
import org.primefaces.event.FlowEvent;

import javax.annotation.PostConstruct;
import javax.faces.context.ExternalContext;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

@Named
@ViewScoped
public class ProjectBean implements Serializable {

    private static final long serialVersionUID = -5685248912502538750L;

    private String selectedTab;
    private Project project;
    @Inject
    private ProjectService projectService;
    @Inject
    private EmployeeService employeeService;
    @Inject
    private EmployeeTrackingSystem employeeTrackingSystem;
    @Inject
    private ExternalContext externalContext;
    private List<Employee> filtered;
    private Employee employee;
    private List<Project> subprojects;
    private List<Project> projects;
    private Map<Employee, Project[]> participations;

    @PostConstruct
    public void init() {
        Map<String, String> requestParams = externalContext.getRequestParameterMap();

        String projectId = requestParams.get("project");
        String masterId = requestParams.get("master");

        if (projectId != null) {
            this.project = projectService.loadProject(Long.parseLong(projectId));
        } else if (masterId != null) {
            this.project = new Project(projectService.loadProject(Long.parseLong(masterId)));
        } else {
            createNewProject();
        }
        selectedTab = calculateIndexTab(project);
    }

    protected void createNewProject() {
        project = new Project();
        Optional<Employee> loggedEmployee = Optional.ofNullable(
                employeeService
                        .getLoggedEmployee()
                        .orElse(employeeTrackingSystem.findEmployee(externalContext.getUserPrincipal().getName())));
        loggedEmployee.ifPresent(project::addManager);
    }

    public List<Employee> getManagersOrdered(Project project) {
        return project.getManagers().stream()
                .sorted(Employee.NAME_COMPARATOR)
                .collect(Collectors.toList());
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public String getSelectedTab() {
        return selectedTab;
    }

    public void setSelectedTab(String selectedTab) {
        this.selectedTab = selectedTab;
    }

    public void update() {
        modifyTeamsForChangedProjects();
        List<Project> projects = getProjectTree();
        projects.forEach(this::trimProjectCode);
        projectService.update(projects);
    }

    public void create() {
        trimProjectCode(project);
        projectService.create(project);
    }

    public void delete(Employee employee) {
        Project[] participations = getParticipations().get(employee);
        participations = (Project[]) ArrayUtils.removeElement(participations, project);
        getParticipations().put(employee, participations);
    }

    public void deleteFromAllProjects(Employee employee) {
        List<Project> projectsByEmployee = asList(getParticipations().get(employee));
        projectsByEmployee.forEach(prj -> prj.removeTeamMember(employee));
        getParticipations().remove(employee);
    }

    protected void trimProjectCode(Project project) {
        project.setCode(project.getCode().trim());
    }

    protected void modifyTeamsForChangedProjects() {
        getProjectsToDisplay().forEach(this::modifyTeamIfChanged);
    }

    protected void modifyTeamIfChanged(Project project) {
        Set<Employee> removedEmployees = project.getTeam().stream()
                .filter(teamMember -> !getParticipations().containsKey(teamMember) || !ArrayUtils.contains(getParticipations().get(teamMember), project))
                .collect(Collectors.toSet());
        Set<Employee> addedEmployees = getParticipations().entrySet().stream()
                .filter(participation -> !project.isTeamMember(participation.getKey()))
                .filter(participation -> ArrayUtils.contains(participation.getValue(), project))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
        addedEmployees.forEach(employeeService::create);
        addedEmployees.forEach(project::addTeamMember);
        project.removeTeamMembers(removedEmployees);
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public void addNewTeamMember() {
        project.addTeamMember(employee);
        getParticipations().put(employee, new Project[]{project});
        employee = null;
    }

    public List<Employee> getOrderedTeam() {
        List<Employee> team = getTeamIncludeSubprojects(project);
        team.sort(Employee.NAME_COMPARATOR);
        return withoutOutdatedEmployees(team);
    }

    public List<Employee> getFiltered() {
        return filtered;
    }

    public void setFiltered(List<Employee> filtered) {
        this.filtered = filtered;
    }

    public String onWizardFlow(FlowEvent event) {
        return selectedTab = event.getNewStep();
    }

    public Status[] getStatuses() {
        return Status.values();
    }

    public boolean isManagedMasterProject() {
        return employeeService.getLoggedEmployee()
                .map(employee -> project.getMaster().canBeManaged(employee))
                .orElse(false);
    }

    public List<Project> getSubprojects() {
        if (subprojects == null) {
            subprojects = projectService.fetchComplete(projectService.getManagedProjectHierarchy(project)).stream()
                    .filter(p -> !p.equals(project))
                    .collect(Collectors.toList());
        }
        return subprojects;
    }

    public void remove(Project project) {
        projectService.remove(project);
        subprojects = null;
    }

    public Map<Employee, Project[]> getParticipations() {
        if (participations == null) {
            participations = new HashMap<>();
            List<Project> projects = getProjectsToDisplay();
            Set<Employee> employees = getTeamForProjects(projects);
            for (Employee employee : employees) {
                List<Project> projectList = getProjectsByEmployee(projects, employee);
                participations.put(employee, projectList.toArray(new Project[0]));
            }
        }
        return participations;
    }

    public boolean isMasterAvailableForLoggedInEmployee() {
        return project != null && project.isSubproject() && canUserManageProject();
    }

    protected List<Project> getProjectTree() {
        if (projects == null) {
            projects = isNewProject()
                    ? asList(project)
                    : projectService.fetchComplete(projectService.getManagedProjectHierarchy(project));
            projects.replaceAll(p -> (p.equals(project) ? project : p));
        }
        return projects;
    }

    private boolean isNewProject() {
        return project.getId() == null;
    }

    private Set<Employee> getTeamForProjects(List<Project> projects) {
        return projects.stream()
                .flatMap(prj -> prj.getTeam().stream())
                .collect(Collectors.toSet());
    }

    public String calculateIndexTab(Project project) {
        return project.getStatus().equals(Status.ACTIVE)
                ? "0"
                : !project.isSubproject() ? "1" : "2";
    }

    private List<Employee> getTeamIncludeSubprojects(Project project) {
        List<Employee> result = new NoDuplicatesList<>();
        if (project.getStatus() == Status.ACTIVE) {
            getActiveProjects().forEach(prj -> result.addAll(prj.getTeam()));
        } else {
            result.addAll(project.getTeam());
        }
        return result;
    }

    public List<Project> getProjectsToDisplay() {
        List<Project> projects = getActiveProjects();
        if (!isNewProject() && !projects.contains(project)) {
            projects.add(project);
        }
        return projects;
    }

    public List<Project> getActiveProjects() {
        return getProjectTree().stream()
                .filter(prj -> prj.getStatus() == Status.ACTIVE)
                .sorted(Project.DISPLAY_CODE_COMPARATOR)
                .collect(Collectors.toList());
    }

    protected List<Employee> withoutOutdatedEmployees(List<Employee> team) {
        Map<Employee, Project[]> actualParticipations = getParticipations();
        return team.stream()
                .filter(actualParticipations::containsKey)
                .filter(this::hasAnyParticipation)
                .collect(Collectors.toList());
    }

    private boolean canUserManageProject() {
        return !projectService.fetchComplete(asList(project.getMaster())).isEmpty();
    }

    private boolean hasAnyParticipation(Employee employee) {
        // Workaround: Mojarra could write Object[] instead of Employee[] into participations, which then would cause ClassCastException on loadProject().length
        Set<Project> projects = Sets.newHashSet(getParticipations().get(employee));
        return !projects.isEmpty();
    }

    private List<Project> getProjectsByEmployee(List<Project> projects, Employee employee) {
        return projects.stream()
                .filter(prj -> prj.isTeamMember(employee))
                .collect(Collectors.toList());
    }

    public String[] getDepartments() {
        if (project == null
                || (project.getTeamFilter() == null)
                || (project.getTeamFilter().getFilterType() != TeamFilter.Type.DEPARTMENTS)) {
            return new String[0];
        }
        TeamFilter teamFilter = project.getTeamFilter();
        if (teamFilter.getValue() != null) {
            return teamFilter.getValue().split(",");
        } else {
            return new String[0];
        }
    }

    public void setDepartments(String[] departments) {
        if (project != null && project.getTeamFilter().getFilterType() == TeamFilter.Type.DEPARTMENTS) {
            project.getTeamFilter().setValue(Strings.join(Arrays.asList(departments), ","));
        }
    }

    public void teamFilterTypeChanged(AjaxBehaviorEvent event) {
        if (project != null && project.getTeamFilter().getValue() != null) {
            project.getTeamFilter().setValue(null);
        }
    }

}
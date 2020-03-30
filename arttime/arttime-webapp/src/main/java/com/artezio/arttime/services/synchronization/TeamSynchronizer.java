package com.artezio.arttime.services.synchronization;

import com.artezio.arttime.datamodel.Employee;
import com.artezio.arttime.datamodel.Project;
import com.artezio.arttime.datamodel.TeamFilter;
import com.artezio.arttime.repositories.ProjectRepository;
import com.artezio.arttime.services.EmployeeService;
import com.artezio.arttime.services.NotificationManagerLocal;
import com.artezio.arttime.services.integration.TeamTrackingSystem;
import com.artezio.arttime.utils.StringUtil;
import org.apache.commons.collections.CollectionUtils;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.inject.Named;
import java.text.MessageFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.artezio.arttime.datamodel.Project.Status.ACTIVE;
import static com.artezio.arttime.datamodel.TeamFilter.Type.BASED_ON_MASTER;
import static com.artezio.arttime.datamodel.TeamFilter.Type.DISABLED;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

@Named
@Stateless
public class TeamSynchronizer {

    private Logger log = Logger.getLogger(TeamSynchronizer.class.getName());

    @Inject
    private TeamTrackingSystem teamTrackingSystem;
    @Inject
    private ProjectRepository projectRepository;
    @Inject
    private NotificationManagerLocal notificationManager;
    @Inject
    private EmployeeService employeeService;

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void importTeam(Project project) {
        try {
            if (!project.isTeamFilterDisabled()
                    && !(project.getTeamFilter().getFilterType() == BASED_ON_MASTER
                        && project.getMaster().getTeamFilter().getFilterType() == DISABLED)) {
                List<Employee> actualTeamMembers = getTeam(project);
                List<Employee> currentTeamMembers = getCurrentTeamMembers(project);
                List<Employee> formerTeamMembers = substract(currentTeamMembers, actualTeamMembers);
                List<Employee> newTeamMembers = getNewTeamMembers(project, currentTeamMembers, actualTeamMembers);
                updateProject(project, actualTeamMembers, formerTeamMembers, newTeamMembers);
            } else {
                List<Employee> formerEmployees = project.getTeam().stream()
                        .filter(Employee::isFormer)
                        .collect(Collectors.toList());
                project.getTeam().removeIf(Employee::isFormer);
                updateProject(project, project.getTeam(), formerEmployees, emptyList());
            }
        } catch (Exception e) {
            String message = MessageFormat.format("Thrown exception during synchronization by {0}", project);
            log.log(Level.SEVERE, message, e);
        }
    }

    private void updateProject(Project project, List<Employee> actualTeamMembers, List<Employee> formerTeamMembers,
                               List<Employee> newTeamMembers) {
        if (!formerTeamMembers.isEmpty() || !newTeamMembers.isEmpty()) {
            newTeamMembers.forEach(employeeService::create);
            project.setTeam(actualTeamMembers);
            projectRepository.update(project);
            notificationManager.notifyAboutTeamChanges(project, formerTeamMembers, newTeamMembers);
            List<Employee> employeesWithoutCalendar = newTeamMembers.stream()
                    .filter(e -> e.getCalendar() == null)
                    .collect(Collectors.toList());
            employeeService.setCalendar(employeesWithoutCalendar);
        }
    }

    protected List<Employee> getNewTeamMembers(Project project, List<Employee> currentTeamMembers,
                                               List<Employee> actualTeamMembers) {
        TeamFilter.Type type = project.getTeamFilter().getFilterType();
        return type == BASED_ON_MASTER
                ? emptyList()
                : substract(actualTeamMembers, currentTeamMembers);
    }

    protected List<Employee> getCurrentTeamMembers(Project project) {
        boolean hasBasedOnMasterSubprojects = projectRepository.query()
                .masters(asList(project))
                .status(ACTIVE)
                .list()
                .stream()
                .anyMatch(p -> p.getTeamFilter().getFilterType() == BASED_ON_MASTER);
        return hasBasedOnMasterSubprojects
                ? getEmployeesFromProjectTree(project)
                : project.getTeam();
    }


    @SuppressWarnings("unchecked")
    private <T> List<T> substract(List<T> listToSubtractFrom, List<T> listToSubtract) {
        return (List<T>) CollectionUtils.subtract(listToSubtractFrom, listToSubtract);
    }

    protected List<Employee> getEmployeesFromProjectTree(Project project) {
        Project baseProject = getBaseProject(project);
        Set<Employee> employees = new HashSet<>();
        List<Project> projects = asList(baseProject);
        while (!projects.isEmpty()) {
            employees.addAll(projects.stream()
                    .flatMap(p -> p.getTeam().stream())
                    .collect(Collectors.toSet()));
            projects = projectRepository.query()
                    .masters(projects)
                    .status(ACTIVE)
                    .list()
                    .stream()
                    .filter(prj -> prj.getTeamFilter().getFilterType() == BASED_ON_MASTER)
                    .collect(Collectors.toList());
        }
        return new ArrayList<>(employees);
    }

    protected Project getBaseProject(Project project) {
        Project master = project;
        while (master.getTeamFilter().getFilterType() == BASED_ON_MASTER) {
            master = master.getMaster();
        }
        return master;
    }

    protected List<Employee> getTeam(Project project) {
        TeamFilter teamFilter = project.getTeamFilter();
        switch (teamFilter.getFilterType()) {
            case PROJECT_CODES:
                return getEmployeesByProjectCodes(teamFilter.getValue());
            case DEPARTMENTS:
                return getEmployeesByDepartments(teamFilter.getValue());
            case BASED_ON_MASTER:
                return getTeam(project.getMaster());
            case DISABLED:
                return Collections.emptyList();
            default:
                throw new IllegalStateException("No matching TeamFilter.Type for '" + teamFilter.getFilterType() + "' found.");
        }
    }

    protected List<Employee> getEmployeesByDepartments(String value) {
        return Optional.ofNullable(value)
                .map(StringUtil::splitByComma).orElse(Collections.emptyList())
                .stream()
                .flatMap(department -> teamTrackingSystem.getTeamByDepartment(department).stream())
                .distinct()
                .sorted(Employee.NAME_COMPARATOR)
                .collect(Collectors.toList());
    }

    protected List<Employee> getEmployeesByProjectCodes(String code) {
        return Optional.ofNullable(code)
                .map(StringUtil::splitByComma).orElse(Collections.emptyList())
                .stream()
                .flatMap(group -> teamTrackingSystem.getTeamByGroupCode(group).stream())
                .distinct()
                .sorted(Employee.NAME_COMPARATOR)
                .collect(Collectors.toList());
    }

}

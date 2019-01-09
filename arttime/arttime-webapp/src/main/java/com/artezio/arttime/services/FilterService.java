package com.artezio.arttime.services;

import com.artezio.arttime.admin_tool.cache.WebCached;
import com.artezio.arttime.datamodel.Period;
import com.artezio.arttime.datamodel.Project;
import com.artezio.arttime.filter.Filter;
import com.artezio.arttime.repositories.DepartmentRepository;
import com.artezio.arttime.repositories.FilterRepository;
import com.artezio.arttime.repositories.ProjectRepository;
import com.artezio.arttime.web.interceptors.FacesMessage;
import com.artezio.javax.jpa.abac.AbacContext;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.security.Principal;
import java.util.*;

import static com.artezio.arttime.admin_tool.cache.WebCached.Scope.SESSION_SCOPED;
import static com.artezio.arttime.datamodel.Project.Status.ACTIVE;
import static com.artezio.arttime.security.AbacContexts.VIEW_TIMESHEET;
import static com.artezio.arttime.security.auth.UserRoles.*;
import static java.util.Collections.singletonList;

@Named
@Stateless
@PermitAll
public class FilterService implements Serializable {

    public static final String MY_ACTIVE_PROJECTS_FILTER_NAME = "My active projects";

    @Inject
    private FilterRepository filterRepository;
    @Inject
    private EmployeeService employeeService;
    @Inject
    private DepartmentRepository departmentRepository;
    @Inject
    private HourTypeService hourTypeService;
    @Inject
    private ProjectRepository projectRepository;
    @Inject
    private Principal principal;

    @WebCached(scope = SESSION_SCOPED)
    public Filter getActiveProjectsFilter() {
        List<Project> managedActiveProjects = getActiveManagedProjects();
        return new Filter(MY_ACTIVE_PROJECTS_FILTER_NAME, principal.getName(), true, managedActiveProjects);
    }

    @AbacContext(VIEW_TIMESHEET)
    public Filter getTimesheetFilter() {
        Filter result = new Filter();
        result.setDepartments(departmentRepository.getDepartments());
        result.setHourTypes(hourTypeService.getAll());
        employeeService.getLoggedEmployee().ifPresent(employee -> result.setEmployees(Collections.singletonList(employee)));
        result.setProjects(getActiveAndReportedProjects(result.getPeriod()));
        return result;
    }

    @WebCached(resetCache = true)
    @FacesMessage(onCompleteMessageKey = "message.filterIsSaved")
    public Filter save(Filter filter) {
        Filter equivalentFilter = getFilter(filter.getName());
        if (!filter.isPredefined() && equivalentFilter != null) {
            filter.setId(equivalentFilter.getId());
            return filterRepository.update(filter);
        } else {
            return filterRepository.create(filter);
        }
    }
    
    private List<Project> getActiveAndReportedProjects(Period period) {
        List<Project> activeProjects = getActiveProjects();
        List<Project> reportedProjects = getReportedProjects(period);
        Set<Project> projects = new HashSet<>(activeProjects);
        projects.addAll(reportedProjects);
        return new ArrayList<>(projects);
    }

    private List<Project> getActiveManagedProjects() {
        return projectRepository.query()
                .managedBy(principal.getName())
                .status(ACTIVE)
                .list();
    }

    private List<Project> getActiveProjects() {
        return projectRepository.query()
                .teamMember(principal.getName())
                .status(ACTIVE)
                .withoutMasterOrWithMasterStatus(ACTIVE)
                .list();
    }

    private List<Project> getReportedProjects(Period period) {
        return projectRepository.query()
                .withHoursFor(principal.getName())
                .withHoursFrom(period.getStart())
                .withHoursTill(period.getFinish())
                .distinct()
                .list();
    }

    public Filter getFilter(String name) {
        return filterRepository.query()
                .name(name)
                .fetchProjects()
                .fetchDepartments()
                .fetchEmployees()
                .fetchHourTypes()
                .getSingleResultOrNull();
    }
    
    public void remove(Filter filter) {
        filterRepository.delete(filter);
    }

    public List<Filter> getFilters() {
        return filterRepository.query().list();
    }
    
}

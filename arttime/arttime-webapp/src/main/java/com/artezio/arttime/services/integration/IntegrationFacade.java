package com.artezio.arttime.services.integration;

import static com.artezio.arttime.security.auth.UserRoles.INTEGRATION_CLIENT_ROLE;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.persistence.NoResultException;
import javax.xml.bind.annotation.XmlElement;

import com.artezio.arttime.admin_tool.log.Log;
import com.artezio.arttime.datamodel.Day;
import com.artezio.arttime.datamodel.Employee;
import com.artezio.arttime.datamodel.HourType;
import com.artezio.arttime.datamodel.Hours;
import com.artezio.arttime.datamodel.Period;
import com.artezio.arttime.datamodel.Project;
import com.artezio.arttime.datamodel.WorkdaysCalendar;
import com.artezio.arttime.filter.Filter;
import com.artezio.arttime.repositories.HourTypeRepository;
import com.artezio.arttime.repositories.ProjectRepository;
import com.artezio.arttime.services.*;
import com.artezio.arttime.web.criteria.RangePeriodSelector;

@RolesAllowed(INTEGRATION_CLIENT_ROLE)
@Stateless
@WebService(serviceName = "arttime-facade-ws", name = "arttime-facade-ws")
public class IntegrationFacade {

    @Inject
    private HoursService hoursService;
    @Inject
    private ProjectService projectService;
    @Inject
    private ProjectRepository projectRepository;
    @Inject
    private HourTypeRepository hourTypeRepository;
    @Inject
    private HourTypeService hourTypeService;
    @Inject
    private WorkdaysCalendarService workdaysCalendarService;

    @WebMethod
    @WebResult(name = "project")
    public List<Project> getProjects() {
        return projectService.fetchComplete(projectService.getAll());
    }

    @WebMethod
    @WebResult(name = "hoursType")
    public List<HourType> getHourTypes() {
        return hourTypeService.getAll();
    }

    @WebMethod
    @WebResult(name = "employee")
    public List<Employee> getProjectTeam(@WebParam(name = "code") String projectCode) throws ProjectNotFoundException {
        Project project = getProjectWithTeam(projectCode).orElseThrow(ProjectNotFoundException::new);
        return project.getTeam();
    }

    @WebMethod
    @WebResult(name = "calendars")
    public List<WorkdaysCalendar> getCalendars() {
        return workdaysCalendarService.getAll();
    }

    @WebMethod
    @WebResult(name = "day")
    public List<Day> getCalendarDays(
            @WebParam(name = "calendar") WorkdaysCalendar workdaysCalendar,
            @WebParam(name = "from") Date from,
            @WebParam(name = "till") Date till) {
        Period period = new Period(from, till);
        return workdaysCalendarService.getDays(workdaysCalendar, period);
    }

    @WebMethod
    @WebResult(name = "subprojects")
    public List<Project> getAllSubprojects(@WebParam(name = "masterProjectCode") String masterProjectCode) {
        Project rootProject = getProject(masterProjectCode).orElseThrow(NoResultException::new);
        List<Project> projects = projectService.fetchComplete(projectService.getProjectHierarchy(rootProject));
        projects.remove(rootProject);
        return projects;
    }

    @WebMethod
    @WebResult(name = "hours")
    public List<Hours> getHours(@WebParam(name = "hoursSearchCriteria") @XmlElement(required = true) HoursSearchCriteria criteria) {
        Predicate<Project> projectContainedInCriteria = (project) -> criteria.getProjectCodes().contains(project.getCode());
        Predicate<Project> projectMasterContainedInCriteria = (project) -> project.getMaster() != null && criteria.isIncludeSubprojects() && criteria.getProjectCodes().contains(project.getMaster().getCode());
        List<Project> projects = projectService.getAll().stream()
                .filter(project -> projectContainedInCriteria.test(project) || projectMasterContainedInCriteria.test(project))
                .collect(Collectors.toList());
        return projects.isEmpty()
                ? Collections.emptyList()
                : hoursService.getHours(buildHoursFilter(criteria, projects));
    }

    @WebMethod
    @Log(logParams = true)
    public void modifyHours(@WebParam(name = "change") List<HoursChange> changes) throws ApplyHoursChangeException {
        hoursService.apply(changes);
    }

    private Optional<Project> getProject(String projectCode) {
        return Optional.ofNullable(
                projectRepository.query()
                        .code(projectCode)
                        .getSingleResultOrNull());
    }
    
    private Optional<Project> getProjectWithTeam(String projectCode) {
        return Optional.ofNullable(
                projectRepository.query()
                        .code(projectCode)
                        .fetchTeam()
                        .getSingleResultOrNull());
    }

    private Filter buildHoursFilter(HoursSearchCriteria criteria, List<Project> projects) {
        Filter filter = new Filter();
        Period period = new Period(criteria.getDateFrom(), criteria.getDateTo());
        filter.setRangePeriodSelector(new RangePeriodSelector(period));
        filter.setProjects(projects);
        if (criteria.isApprovedOnly()) {
            filter.setApproved(true);
        }
        return filter;
    }

}

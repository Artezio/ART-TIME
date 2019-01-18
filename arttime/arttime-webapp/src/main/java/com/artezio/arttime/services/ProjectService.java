package com.artezio.arttime.services;

import com.artezio.arttime.admin_tool.cache.WebCached;
import com.artezio.arttime.datamodel.Project;
import com.artezio.arttime.filter.Filter;
import com.artezio.arttime.repositories.ProjectRepository;
import com.artezio.javax.jpa.abac.AbacContext;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static com.artezio.arttime.admin_tool.cache.WebCached.Scope.REQUEST_SCOPED;
import static com.artezio.arttime.admin_tool.cache.WebCached.Scope.VIEW_SCOPED;
import static com.artezio.arttime.security.AbacContexts.MANAGE_PROJECTS;
import static com.artezio.arttime.security.AbacContexts.VIEW_TIMESHEET;
import static com.artezio.arttime.security.auth.UserRoles.*;
import static java.util.Arrays.asList;

@Named
@Stateless
@PermitAll
public class ProjectService implements Serializable {

    @Inject
    private ProjectRepository projectRepository;

    @RolesAllowed({EXEC_ROLE, PM_ROLE})
    @AbacContext(MANAGE_PROJECTS)
    public Project create(Project project) {
        return projectRepository.create(project);
    }

    @RolesAllowed({EXEC_ROLE, PM_ROLE})
    @AbacContext(MANAGE_PROJECTS)
    public void update(List<Project> projects) {
        projectRepository.update(projects);
    }

    @RolesAllowed({EXEC_ROLE, PM_ROLE})
    @AbacContext(MANAGE_PROJECTS)
    public void remove(Project project) {
        projectRepository.remove(project);
    }

    @RolesAllowed({EXEC_ROLE, PM_ROLE, OFFICE_MANAGER, ACCOUNTANT, INTEGRATION_CLIENT_ROLE})
    @WebCached(scope = VIEW_SCOPED)
    public List<Project> fetchComplete(List<Project> projects) {
        return projects.isEmpty()
                ? projects
                : projectRepository.query()
                    .projects(projects)
                    .fetchTeam()
                    .fetchManagers()
                    .fetchAccountableHours()
                    .distinct()
                    .list();
    }

    @PermitAll
    @AbacContext(VIEW_TIMESHEET)
    @WebCached(scope = REQUEST_SCOPED)
    public List<Project> getMyProjects() {
        return projectRepository
                .query()
                .fetchAccountableHours()
                .fetchTeam()
                .distinct()
                .list();
    }

    @RolesAllowed({EXEC_ROLE, PM_ROLE})
    @AbacContext(MANAGE_PROJECTS)
    @WebCached(scope = REQUEST_SCOPED)
    public List<Project> getManagedProjects() {
        return getAll();
    }

    @RolesAllowed({EXEC_ROLE, PM_ROLE, OFFICE_MANAGER, ACCOUNTANT, INTEGRATION_CLIENT_ROLE, SYSTEM_ROLE})
    @WebCached(scope = VIEW_SCOPED)
    public List<Project> getAll() {
        return projectRepository.query()
                .list();
    }

    @RolesAllowed({EXEC_ROLE, PM_ROLE, OFFICE_MANAGER, ACCOUNTANT})
    @WebCached(scope = REQUEST_SCOPED)
    public List<Project> getEffortsProjects(Filter filter) {
        return projectRepository.query()
                .filter(filter)
                .fetchAccountableHours()
                .distinct()
                .list();
    }

    @RolesAllowed({EXEC_ROLE, PM_ROLE})
    @AbacContext(MANAGE_PROJECTS)
    @WebCached(scope = VIEW_SCOPED)
    public Project loadProject(Long id) {
        return projectRepository.query()
                .id(id)
                .fetchManagers()
                .fetchTeam()
                .fetchAccountableHours()
                .getSingleResult();
    }

    @RolesAllowed({EXEC_ROLE, PM_ROLE, OFFICE_MANAGER, ACCOUNTANT})
    @WebCached(scope = VIEW_SCOPED)
    public List<Project> getProjects(List<Long> ids) {
        return ids.isEmpty()
                ? new ArrayList<>()
                : projectRepository.query()
                    .projectIds(ids)
                    .list();
    }

    @RolesAllowed({EXEC_ROLE, PM_ROLE})
    @AbacContext(MANAGE_PROJECTS)
    public List<Project> getManagedProjectHierarchy(Project master) {
        return getProjectHierarchy(master);
    }

    @RolesAllowed({EXEC_ROLE, PM_ROLE, OFFICE_MANAGER, ACCOUNTANT, INTEGRATION_CLIENT_ROLE})
    @WebCached(scope = REQUEST_SCOPED)
    public List<Project> getProjectHierarchy(Project master) {
        List<Project> projects = asList(master);
        List<Project> result = new LinkedList<>();
        while (!projects.isEmpty()) {
            result.addAll(projects);
            projects = projectRepository.query()
                    .masters(projects)
                    .distinct()
                    .list();
        }
        return result;
    }

}

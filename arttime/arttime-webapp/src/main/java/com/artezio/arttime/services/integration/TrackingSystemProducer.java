package com.artezio.arttime.services.integration;

import com.artezio.arttime.admin_tool.cache.WebCached;
import com.artezio.arttime.admin_tool.cache.WebCached.Scope;
import com.artezio.arttime.config.ApplicationSettings;
import com.artezio.arttime.config.Settings;

import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Singleton
@Startup
@Named
public class TrackingSystemProducer {
    @Inject
    @ApplicationSettings
    private Settings settings;

    @Inject
    @TrackingSystemImplementation
    private Instance<EmployeeTrackingSystem> employeeTrackingSystems;

    @Inject
    @TrackingSystemImplementation
    private Instance<TeamTrackingSystem> teamTrackingSystems;

    @Inject
    @TrackingSystemImplementation
    private Instance<DepartmentTrackingSystem> departmentTrackingSystems;

    @Produces
    @RequestScoped
    @WebCached(scope = Scope.REQUEST_SCOPED)
    @Named("employeeTrackingSystem")
    public EmployeeTrackingSystem getEmployeeTrackingSystem() {
        String name = settings.getEmployeeTrackingSystemName();
        return findImplementationByName(employeeTrackingSystems, name);
    }

    @Produces
    @RequestScoped
    @WebCached(scope = Scope.REQUEST_SCOPED)
    @Named("teamTrackingSystem")
    public TeamTrackingSystem getTeamTrackingSystem() {
        String name = settings.getTeamTrackingSystemName();
        return findImplementationByName(teamTrackingSystems, name);
    }

    @Produces
    @RequestScoped
    @WebCached(scope = Scope.REQUEST_SCOPED)
    @Named("departmentTrackingSystem")
    public DepartmentTrackingSystem getDepartmentTrackingSystem() {
        String name = settings.getDepartmentTrackingSystemName();
        return findImplementationByName(departmentTrackingSystems, name);
    }

    @Produces
    @Named("employeeTrackingSystemNames")
    public Set<String> getEmployeeTrackingSystemNames() {
        return getTrackingSystemsNames(employeeTrackingSystems);
    }

    @Produces
    @Named("teamTrackingSystemNames")
    public Set<String> getTeamTrackingSystemNames() {
        return getTrackingSystemsNames(teamTrackingSystems);
    }

    @Produces
    @Named("departmentTrackingSystemNames")
    public Set<String> getDepartmentTrackingSystemNames() {
        return getTrackingSystemsNames(departmentTrackingSystems);
    }

    protected <T extends TrackingSystem> T findImplementationByName(Instance<T> trackingSystems, String name) {
        for (T trackingSystem : trackingSystems) {
            if (trackingSystem.getName().equals(name)) {
                return trackingSystem;
            }
        }
        return null;
    }

    protected Set<String> getTrackingSystemsNames(Instance<? extends TrackingSystem> trackingSystems) {
        return StreamSupport.stream(trackingSystems.spliterator(), false)
                .map(TrackingSystem::getName)
                .collect(Collectors.toSet());
    }

}

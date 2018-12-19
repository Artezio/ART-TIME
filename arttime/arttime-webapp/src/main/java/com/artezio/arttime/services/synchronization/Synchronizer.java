package com.artezio.arttime.services.synchronization;

import com.artezio.arttime.admin_tool.log.Log;
import com.artezio.arttime.config.ApplicationSettings;
import com.artezio.arttime.config.Settings;
import com.artezio.arttime.datamodel.Project;
import com.artezio.arttime.services.ProjectService;
import com.artezio.arttime.services.SettingsService;

import javax.annotation.security.RolesAllowed;
import javax.annotation.security.RunAs;
import javax.ejb.Asynchronous;
import javax.ejb.Singleton;
import javax.inject.Inject;
import javax.inject.Named;

import static com.artezio.arttime.security.auth.UserRoles.ADMIN_ROLE;
import static com.artezio.arttime.security.auth.UserRoles.SYSTEM_ROLE;

@Named
@Singleton
@RunAs(SYSTEM_ROLE)
public class Synchronizer {
    @Inject
    @ApplicationSettings
    private Settings settings;
    @Inject
    private SettingsService settingsService;
    @Inject
    private EmployeeSynchronizerLocal employeeSynchronizer;
    @Inject
    private TeamSynchronizer teamSynchronizer;
    @Inject
    private ProjectService projectService;

    @Asynchronous
    @RolesAllowed({ADMIN_ROLE, SYSTEM_ROLE})
    @Log(beforeExecuteMessage = "Synchronization started.", afterExecuteMessage = "Synchronization completed.")
    public void synchronize() {
        settings = settingsService.getSettings();
        if (settings != null) {
            trySynchronizeEmployees();
            trySynchronizeTeam();
        }
    }

    protected void trySynchronizeEmployees() {
        if (settings.isEmployeesSynchronizationEnabled()) {
            employeeSynchronizer.synchronizeEmployees();
        }
    }

    protected void trySynchronizeTeam() {
        if (settings.isTeamSynchronizationEnabled()) {
            for (Project project : projectService.getAll()) {
                if (project.getStatus() == Project.Status.ACTIVE) {
                    teamSynchronizer.importTeam(project);
                }
            }
        }
    }

}

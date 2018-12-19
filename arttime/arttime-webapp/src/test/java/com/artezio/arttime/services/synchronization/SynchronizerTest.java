package com.artezio.arttime.services.synchronization;

import com.artezio.arttime.config.Setting;
import com.artezio.arttime.config.Settings;
import com.artezio.arttime.datamodel.Project;
import com.artezio.arttime.datamodel.TeamFilter;
import com.artezio.arttime.services.ProjectService;
import com.artezio.arttime.services.SettingsService;
import com.artezio.arttime.repositories.ProjectRepository;
import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.EnumMap;

import static junitx.util.PrivateAccessor.setField;

public class SynchronizerTest {
    private Synchronizer synchronizer;
    private IMocksControl mockControl;
    private EmployeeSynchronizerLocal employeeSynchronizer;
    private TeamSynchronizer teamSynchronizer;
    private SettingsService settingsService;
    private Settings settings;
    private ProjectService projectService;

    @Before
    public void setUp() throws NoSuchFieldException {
        synchronizer = new Synchronizer();
        settings = new Settings(new EnumMap<>(Setting.Name.class));
        mockControl = EasyMock.createControl();
        employeeSynchronizer = mockControl.createMock(EmployeeSynchronizerLocal.class);
        teamSynchronizer = mockControl.createMock(TeamSynchronizer.class);
        projectService = mockControl.createMock(ProjectService.class);
        settingsService = mockControl.createMock(SettingsService.class);
        setField(synchronizer, "employeeSynchronizer", employeeSynchronizer);
        setField(synchronizer, "teamSynchronizer", teamSynchronizer);
        setField(synchronizer, "projectService", projectService);
        setField(synchronizer, "settingsService", settingsService);
        setField(synchronizer, "settings", settings);
    }

    @Test
    public void testSynchronize_ensureEmployeesFirstTeamSecond() throws NoSuchFieldException {
        synchronizer = EasyMock.createMockBuilder(Synchronizer.class)
                .addMockedMethod("trySynchronizeEmployees")
                .addMockedMethod("trySynchronizeTeam")
                .createMock();
        setField(synchronizer, "settingsService", settingsService);
        EasyMock.expect(settingsService.getSettings()).andReturn(settings).anyTimes();
        mockControl.replay();

        synchronizer.synchronize();

        mockControl.verify();
    }

    @Test
    public void testTrySynchronizeEmployees() {
        settings.setEmployeesSynchronizationEnabled(true);
        employeeSynchronizer.synchronizeEmployees();
        mockControl.replay();

        synchronizer.trySynchronizeEmployees();

        mockControl.verify();
    }

    @Test
    public void testTrySynchronizeEmployees_ImportDisabled() {
        settings.setEmployeesSynchronizationEnabled(false);
        mockControl.replay();

        synchronizer.trySynchronizeEmployees();

        mockControl.verify();
    }

    @Test
    public void testTrySynchronizeTeam() throws NoSuchFieldException {
        settings.setTeamSynchronizationEnabled(true);
        Project project = new Project();
        project.getTeamFilter().setFilterType(TeamFilter.Type.PROJECT_CODES);
        project.getTeamFilter().setValue("project_code");
        setField(project, "id", 1L);
        ProjectRepository.ProjectQuery projectQuery = Mockito.mock(ProjectRepository.ProjectQuery.class);

        EasyMock.expect(projectService.getAll()).andReturn(Arrays.asList(project));
        teamSynchronizer.importTeam(project);
        mockControl.replay();

        synchronizer.trySynchronizeTeam();

        mockControl.verify();
    }

    @Test
    public void testTrySynchronizeTeam_ImportDisabled() {
        settings.setTeamSynchronizationEnabled(false);
        mockControl.replay();

        synchronizer.trySynchronizeTeam();

        mockControl.verify();
    }

    @Test
    public void testTrySynchronizeTeam_ifProjectNotActive() throws NoSuchFieldException {
        settings.setTeamSynchronizationEnabled(true);
        Project project = new Project();
        project.getTeamFilter().setFilterType(TeamFilter.Type.PROJECT_CODES);
        project.getTeamFilter().setValue("project_code");
        project.setStatus(Project.Status.CLOSED);
        setField(project, "id", 1L);
        ProjectRepository.ProjectQuery projectQuery = Mockito.mock(ProjectRepository.ProjectQuery.class);

        EasyMock.expect(projectService.getAll()).andReturn(Arrays.asList(project));
        mockControl.replay();

        synchronizer.trySynchronizeTeam();

        mockControl.verify();
    }

    @Test
    public void testStartSynchronization() throws NoSuchFieldException {
        settings.setEmployeesSynchronizationEnabled(true);
        settings.setTeamSynchronizationEnabled(true);
        mockControl.checkOrder(true);
        Project project = new Project();
        project.getTeamFilter().setFilterType(TeamFilter.Type.PROJECT_CODES);
        project.getTeamFilter().setValue("project_code");
        setField(project, "id", 1L);

        EasyMock.expect(settingsService.getSettings()).andReturn(settings);
        employeeSynchronizer.synchronizeEmployees();
        EasyMock.expectLastCall();
        EasyMock.expect(projectService.getAll()).andReturn(Arrays.asList(project));
        teamSynchronizer.importTeam(project);
        EasyMock.expectLastCall();
        mockControl.replay();

        synchronizer.synchronize();

        mockControl.verify();
    }

    @Test
    public void testStartSynchronization_ifSettingsIsNull() {
        EasyMock.expect(settingsService.getSettings()).andReturn(null);
        mockControl.replay();

        synchronizer.synchronize();

        mockControl.verify();
    }

}

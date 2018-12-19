package com.artezio.arttime.services.synchronization;

import static com.artezio.arttime.datamodel.Project.Status.ACTIVE;
import static com.artezio.arttime.datamodel.TeamFilter.Type.*;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static junitx.util.PrivateAccessor.setField;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;
import static org.powermock.api.easymock.PowerMock.replay;

import com.artezio.arttime.datamodel.Employee;
import com.artezio.arttime.datamodel.Project;
import com.artezio.arttime.datamodel.TeamFilter;
import com.artezio.arttime.datamodel.TeamFilter.Type;
import com.artezio.arttime.repositories.ProjectRepository;
import com.artezio.arttime.services.EmployeeService;
import com.artezio.arttime.services.NotificationManagerLocal;
import com.artezio.arttime.services.integration.TeamTrackingSystem;
import com.artezio.arttime.services.integration.spi.UserInfo;
import junitx.framework.ArrayAssert;
import junitx.framework.ListAssert;
import org.easymock.EasyMock;
import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import java.util.*;

@RunWith(EasyMockRunner.class)
public class TeamSynchronizerTest {

    @TestSubject
    private TeamSynchronizer teamSynchronizer = new TeamSynchronizer();
    @Mock
    private TeamTrackingSystem teamTrackingSystem;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private EmployeeService employeeService;
    @Mock
    private NotificationManagerLocal notificationManager;

    @Test
    public void testImportTeam() throws NoSuchFieldException {
        Project project = new Project();
        Employee formerTeamMember = new Employee("employee1");
        Employee currentTeamMember2 = new Employee("employee2");
        Employee newTeamMember = new Employee("employee3");
        project.setTeam(asList(formerTeamMember, currentTeamMember2));
        List<Employee> expected = asList(currentTeamMember2, newTeamMember);

        expect(projectRepository.update(project)).andReturn(project);
        expectLastCall();
        employeeService.create(newTeamMember);
        expectLastCall();
        employeeService.setCalendar(asList(newTeamMember));
        expectLastCall();
        notificationManager.notifyAboutTeamChanges(project, asList(formerTeamMember), asList(newTeamMember));

        teamSynchronizer = createMockBuilder(TeamSynchronizer.class)
                .addMockedMethod("getCurrentTeamMembers", Project.class)
                .addMockedMethod("getNewTeamMembers", Project.class, List.class, List.class)
                .addMockedMethod("getTeam", Project.class)
                .createMock();
        setField(teamSynchronizer, "teamTrackingSystem", teamTrackingSystem);
        setField(teamSynchronizer, "projectRepository", projectRepository);
        setField(teamSynchronizer, "notificationManager", notificationManager);
        setField(teamSynchronizer, "employeeService", employeeService);
        expect(teamSynchronizer.getCurrentTeamMembers(project)).andReturn(project.getTeam());
        expect(teamSynchronizer.getNewTeamMembers(anyObject(), anyObject(), anyObject())).andReturn(asList(newTeamMember));
        expect(teamSynchronizer.getTeam(project)).andReturn(expected);
        replay(teamTrackingSystem, notificationManager, projectRepository, employeeService, teamSynchronizer);

        teamSynchronizer.importTeam(project);

        verify(teamTrackingSystem, notificationManager, projectRepository, employeeService, teamSynchronizer);
        ArrayAssert.assertEquivalenceArrays(expected.toArray(), project.getTeam().toArray());
    }

    @Test
    public void testImportTeam_projectWithDisabledFilterType() {
        Project project = new Project();
        project.setTeamFilter(new TeamFilter(DISABLED));
        Employee employee1 = new Employee("employee1");
        employee1.setFormer(true);
        Employee employee2 = new Employee("employee2");
        project.setTeam(asList(employee1, employee2));

        EasyMock.expect(projectRepository.update(project)).andReturn(project);
        notificationManager.notifyAboutTeamChanges(project, asList(employee1), emptyList());
        EasyMock.expectLastCall();
        employeeService.setCalendar(emptyList());
        EasyMock.expectLastCall();
        replay(employeeService, projectRepository, teamTrackingSystem, notificationManager);

        teamSynchronizer.importTeam(project);

        verify(employeeService, projectRepository, teamTrackingSystem, notificationManager);
        ListAssert.assertEquals(asList(employee2), new ArrayList<>(project.getTeam()));
    }

    @Test
    public void testImportTeam_subprojectWithBasedOnMasterFilterType_MasterWithDisabledFilterType() {
        Project master = new Project();
        master.setTeamFilter(new TeamFilter(DISABLED));
        Project subproject = new Project(master);
        subproject.setTeamFilter(new TeamFilter(BASED_ON_MASTER));
        Employee employee1 = new Employee("employee1");
        employee1.setFormer(true);
        Employee employee2 = new Employee("employee2");
        List<Employee> teamMembers = asList(employee1, employee2);
        master.setTeam(teamMembers);
        subproject.setTeam(teamMembers);

        EasyMock.expect(projectRepository.update(subproject)).andReturn(subproject);
        notificationManager.notifyAboutTeamChanges(subproject, asList(employee1), emptyList());
        EasyMock.expectLastCall();
        employeeService.setCalendar(emptyList());
        EasyMock.expectLastCall();
        replay(employeeService, projectRepository, teamTrackingSystem, notificationManager);

        teamSynchronizer.importTeam(subproject);

        verify(employeeService, projectRepository, teamTrackingSystem, notificationManager);
        ListAssert.assertEquals(asList(employee2), new ArrayList<>(subproject.getTeam()));
    }

    @Test
    public void testImportTeam_subprojectWithProjectCodeFilterType_MasterWithDisabledFilterType() throws NoSuchFieldException {
        Project master = new Project();
        master.setTeamFilter(new TeamFilter(DISABLED));
        Project subproject = new Project(master);
        subproject.setTeamFilter(new TeamFilter(PROJECT_CODES));
        Employee employee1 = new Employee("employee1");
        employee1.setFormer(true);
        Employee employee2 = new Employee("employee2");
        List<Employee> teamMembers = asList(employee1, employee2);
        master.setTeam(teamMembers);
        subproject.setTeam(teamMembers);
        ProjectRepository.ProjectQuery projectQuery = Mockito.mock(ProjectRepository.ProjectQuery.class, Mockito.RETURNS_DEEP_STUBS);

        teamSynchronizer = createMockBuilder(TeamSynchronizer.class)
                .addMockedMethod("getTeam", Project.class)
                .createMock();
        setField(teamSynchronizer, "teamTrackingSystem", teamTrackingSystem);
        setField(teamSynchronizer, "projectRepository", projectRepository);
        setField(teamSynchronizer, "notificationManager", notificationManager);
        setField(teamSynchronizer, "employeeService", employeeService);
        EasyMock.expect(teamSynchronizer.getTeam(subproject)).andReturn(asList(employee2));
        EasyMock.expect(projectRepository.query()).andReturn(projectQuery);
        Mockito.when(projectQuery
                .masters(asList(subproject))
                .status(ACTIVE)
                .list()
        ).thenReturn(emptyList());
        EasyMock.expect(projectRepository.update(subproject)).andReturn(subproject);
        notificationManager.notifyAboutTeamChanges(subproject, asList(employee1), emptyList());
        EasyMock.expectLastCall();
        employeeService.setCalendar(emptyList());
        EasyMock.expectLastCall();
        replay(employeeService, projectRepository, teamTrackingSystem, notificationManager, teamSynchronizer);

        teamSynchronizer.importTeam(subproject);

        EasyMock.verify(employeeService, projectRepository, teamTrackingSystem, notificationManager);
        Mockito.verify(projectQuery
                .masters(asList(subproject))
                .status(ACTIVE)
        ).list();

        ListAssert.assertEquals(asList(employee2), new ArrayList<>(subproject.getTeam()));
    }

    @Test
    public void testImportTeam_noChangesInTeam() throws NoSuchFieldException {
        Project project = new Project();
        setField(project, "id", 1L);
        Employee employee1 = new Employee("employee1");
        Employee employee2 = new Employee("employee2");
        project.setTeam(asList(employee1, employee2));
        List<Employee> expected = asList(employee1, employee2);

        teamSynchronizer = createMockBuilder(TeamSynchronizer.class)
                .addMockedMethod("getCurrentTeamMembers", Project.class)
                .addMockedMethod("getNewTeamMembers", Project.class, List.class, List.class)
                .addMockedMethod("getTeam", Project.class)
                .createMock();
        setField(teamSynchronizer, "teamTrackingSystem", teamTrackingSystem);
        setField(teamSynchronizer, "projectRepository", projectRepository);
        setField(teamSynchronizer, "notificationManager", notificationManager);
        setField(teamSynchronizer, "employeeService", employeeService);
        expect(teamSynchronizer.getCurrentTeamMembers(project)).andReturn(project.getTeam());
        expect(teamSynchronizer.getNewTeamMembers(anyObject(), anyObject(), anyObject())).andReturn(emptyList());
        expect(teamSynchronizer.getTeam(project)).andReturn(expected);
        replay(teamTrackingSystem, teamSynchronizer, notificationManager, projectRepository);

        teamSynchronizer.importTeam(project);

        verify(teamTrackingSystem, teamSynchronizer, notificationManager, projectRepository);
        ArrayAssert.assertEquivalenceArrays(expected.toArray(), project.getTeam().toArray());
    }

    @Test
    public void testImportTeam_noNewTeamMembers() throws NoSuchFieldException {
        Project project = new Project();
        Employee employee1 = new Employee("employee1");
        Employee employee2 = new Employee("employee2");
        project.setTeam(asList(employee1, employee2));
        List<Employee> expected = asList(employee2);

        teamSynchronizer = createMockBuilder(TeamSynchronizer.class)
                .addMockedMethod("getCurrentTeamMembers", Project.class)
                .addMockedMethod("getNewTeamMembers", Project.class, List.class, List.class)
                .addMockedMethod("getTeam", Project.class)
                .createMock();
        setField(teamSynchronizer, "teamTrackingSystem", teamTrackingSystem);
        setField(teamSynchronizer, "projectRepository", projectRepository);
        setField(teamSynchronizer, "notificationManager", notificationManager);
        setField(teamSynchronizer, "employeeService", employeeService);
        expect(teamSynchronizer.getTeam(project)).andReturn(expected);
        expect(teamSynchronizer.getCurrentTeamMembers(project)).andReturn(project.getTeam());
        expect(teamSynchronizer.getNewTeamMembers(anyObject(), anyObject(), anyObject())).andReturn(emptyList());

        expect(projectRepository.update(project)).andReturn(project);
        notificationManager.notifyAboutTeamChanges(project, asList(employee1), emptyList());
        replay(teamTrackingSystem, teamSynchronizer, notificationManager, projectRepository);

        teamSynchronizer.importTeam(project);

        verify(teamTrackingSystem, teamSynchronizer, notificationManager, projectRepository);
        ArrayAssert.assertEquivalenceArrays(expected.toArray(), project.getTeam().toArray());
    }

    @Test
    public void testImportTeam_noFormerTeamMembers() throws NoSuchFieldException {
        Project project = new Project();
        Employee currentTeamMember = new Employee("employee1");
        Employee newTeamMember = new Employee("employee2");
        project.setTeam(asList(currentTeamMember));
        List<Employee> expected = asList(currentTeamMember, newTeamMember);

        employeeService.create(newTeamMember);
        expectLastCall();
        employeeService.setCalendar(asList(newTeamMember));
        expectLastCall();

        teamSynchronizer = createMockBuilder(TeamSynchronizer.class)
                .addMockedMethod("getCurrentTeamMembers", Project.class)
                .addMockedMethod("getNewTeamMembers", Project.class, List.class, List.class)
                .addMockedMethod("getTeam", Project.class)
                .createMock();
        setField(teamSynchronizer, "teamTrackingSystem", teamTrackingSystem);
        setField(teamSynchronizer, "projectRepository", projectRepository);
        setField(teamSynchronizer, "employeeService", employeeService);
        setField(teamSynchronizer, "notificationManager", notificationManager);
        expect(teamSynchronizer.getTeam(project)).andReturn(expected);
        expect(teamSynchronizer.getCurrentTeamMembers(project)).andReturn(project.getTeam());
        expect(teamSynchronizer.getNewTeamMembers(anyObject(), anyObject(), anyObject())).andReturn(asList(newTeamMember));

        expect(projectRepository.update(project)).andReturn(project);
        notificationManager.notifyAboutTeamChanges(project, emptyList(), asList(newTeamMember));
        replay(teamTrackingSystem, notificationManager, projectRepository, employeeService, teamSynchronizer);

        teamSynchronizer.importTeam(project);

        verify(teamTrackingSystem, notificationManager, projectRepository, employeeService, teamSynchronizer);
        ArrayAssert.assertEquivalenceArrays(expected.toArray(), project.getTeam().toArray());
    }

    @Test
    public void testImportTeam_subprojectBasedOnMaster() throws NoSuchFieldException {
        Project master = new Project();
        master.setCode("master");
        master.setTeamFilter(new TeamFilter(Type.PROJECT_CODES));
        Project subProjectBasedOnMaster = new Project(master);
        subProjectBasedOnMaster.setTeamFilter(new TeamFilter(BASED_ON_MASTER));
        subProjectBasedOnMaster.setCode("subBased");
        Project subProjectNotBasedOnMaster = new Project(master);
        subProjectNotBasedOnMaster.setTeamFilter(new TeamFilter(Type.PROJECT_CODES));
        subProjectNotBasedOnMaster.setCode("subNotBased");
        Employee employee1 = new Employee("emp1");
        Employee employee2 = new Employee("emp2");
        Employee employee3 = new Employee("emp3");
        Employee employee4 = new Employee("emp4");
        List<Employee> employeesFromTrackingSystem = asList(employee1, employee2, employee3, employee4);

        master.addTeamMember(employee1);
        master.addTeamMember(employee2);
        subProjectBasedOnMaster.addTeamMember(employee3);
        subProjectNotBasedOnMaster.addTeamMember(employee1);

        teamSynchronizer = createMockBuilder(TeamSynchronizer.class)
                .addMockedMethod("getCurrentTeamMembers", Project.class)
                .addMockedMethod("getNewTeamMembers", Project.class, List.class, List.class)
                .addMockedMethod("getTeam", Project.class)
                .createMock();
        setField(teamSynchronizer, "teamTrackingSystem", teamTrackingSystem);
        setField(teamSynchronizer, "employeeService", employeeService);
        setField(teamSynchronizer, "notificationManager", notificationManager);
        expect(teamSynchronizer.getTeam(subProjectBasedOnMaster)).andReturn(employeesFromTrackingSystem);
        expect(teamSynchronizer.getCurrentTeamMembers(subProjectBasedOnMaster)).andReturn(asList(employee1, employee2, employee3));
        expect(teamSynchronizer.getNewTeamMembers(anyObject(), anyObject(), anyObject())).andReturn(emptyList());
        replay(teamTrackingSystem, teamSynchronizer);

        teamSynchronizer.importTeam(subProjectBasedOnMaster);

        verify(teamTrackingSystem, teamSynchronizer);
        assertTrue(subProjectBasedOnMaster.isTeamMember(employee3));
        assertFalse(subProjectBasedOnMaster.isTeamMember(employee1));
        assertFalse(subProjectBasedOnMaster.isTeamMember(employee2));
        assertFalse(subProjectBasedOnMaster.isTeamMember(employee4));
    }

    @Test
    public void testGetEmployeesFromProjectTree() {
        Employee employee1 = new Employee("emp1");
        Employee employee2 = new Employee("emp2");
        Employee employee3 = new Employee("emp3");
        Employee employee4 = new Employee("emp4");
        Employee employee5 = new Employee("emp5");
        Project master = new Project();
        master.setCode("master");
        master.setTeamFilter(new TeamFilter(Type.PROJECT_CODES, "abc"));
        master.addTeamMember(employee1);
        Project subProjectBasedOnMaster1 = new Project(master);
        subProjectBasedOnMaster1.setCode("subBased1");
        subProjectBasedOnMaster1.setTeamFilter(new TeamFilter(BASED_ON_MASTER));
        subProjectBasedOnMaster1.addTeamMember(employee2);
        subProjectBasedOnMaster1.addTeamMember(employee3);
        Project subProjectBasedOnMaster2 = new Project(master);
        subProjectBasedOnMaster2.setCode("subBased2");
        subProjectBasedOnMaster2.setTeamFilter(new TeamFilter(BASED_ON_MASTER));
        subProjectBasedOnMaster2.addTeamMember(employee4);
        Project subProjectNotBasedOnMaster = new Project(master);
        subProjectNotBasedOnMaster.setCode("subNotBased");
        subProjectNotBasedOnMaster.setTeamFilter(new TeamFilter(Type.PROJECT_CODES));
        subProjectNotBasedOnMaster.addTeamMember(employee5);

        ProjectRepository.ProjectQuery query = mock(ProjectRepository.ProjectQuery.class);
        ProjectRepository.ProjectQuery query1 = mock(ProjectRepository.ProjectQuery.class);
        ProjectRepository.ProjectQuery query2 = mock(ProjectRepository.ProjectQuery.class);
        expect(projectRepository.query()).andReturn(query).times(2);
        expect(query.masters(asList(master))).andReturn(query1);
        expect(query.masters(asList(subProjectBasedOnMaster1, subProjectBasedOnMaster2))).andReturn(query2);
        expect(query1.status(ACTIVE)).andReturn(query1);
        expect(query1.list()).andReturn(asList(subProjectBasedOnMaster1, subProjectBasedOnMaster2, subProjectNotBasedOnMaster));
        expect(query2.status(ACTIVE)).andReturn(query2);
        expect(query2.list()).andReturn(emptyList());
        replay(projectRepository, query, query1, query2);

        List<Employee> teamFromMaster = teamSynchronizer.getEmployeesFromProjectTree(master);

        verify(projectRepository, query);
        assertTrue(teamFromMaster.contains(employee1));
        assertTrue(teamFromMaster.contains(employee2));
        assertTrue(teamFromMaster.contains(employee3));
        assertTrue(teamFromMaster.contains(employee4));
        assertFalse(teamFromMaster.contains(employee5));
    }

    @Test
    public void testGetEmployeesFromProjectTree_startingFromSubproject() {
        Employee employee1 = new Employee("emp1");
        Employee employee2 = new Employee("emp2");
        Employee employee3 = new Employee("emp3");
        Employee employee4 = new Employee("emp4");
        Employee employee5 = new Employee("emp5");
        Project master = new Project();
        master.setCode("master");
        master.setTeamFilter(new TeamFilter(Type.PROJECT_CODES, "abc"));
        master.addTeamMember(employee1);
        Project subProjectBasedOnMaster1 = new Project(master);
        subProjectBasedOnMaster1.setCode("subBased1");
        subProjectBasedOnMaster1.setTeamFilter(new TeamFilter(BASED_ON_MASTER));
        subProjectBasedOnMaster1.addTeamMember(employee2);
        subProjectBasedOnMaster1.addTeamMember(employee3);
        Project subProjectBasedOnMaster2 = new Project(master);
        subProjectBasedOnMaster2.setCode("subBased2");
        subProjectBasedOnMaster2.setTeamFilter(new TeamFilter(BASED_ON_MASTER));
        subProjectBasedOnMaster2.addTeamMember(employee4);
        Project subProjectNotBasedOnMaster = new Project(master);
        subProjectNotBasedOnMaster.setCode("subNotBased");
        subProjectNotBasedOnMaster.setTeamFilter(new TeamFilter(Type.PROJECT_CODES));
        subProjectNotBasedOnMaster.addTeamMember(employee5);

        ProjectRepository.ProjectQuery query = mock(ProjectRepository.ProjectQuery.class);
        ProjectRepository.ProjectQuery query1 = mock(ProjectRepository.ProjectQuery.class);
        ProjectRepository.ProjectQuery query2 = mock(ProjectRepository.ProjectQuery.class);
        expect(projectRepository.query()).andReturn(query).times(2);
        expect(query.masters(asList(master))).andReturn(query1);
        expect(query.masters(asList(subProjectBasedOnMaster1, subProjectBasedOnMaster2))).andReturn(query2);
        expect(query1.status(ACTIVE)).andReturn(query1);
        expect(query1.list()).andReturn(asList(subProjectBasedOnMaster1, subProjectBasedOnMaster2, subProjectNotBasedOnMaster));
        expect(query2.status(ACTIVE)).andReturn(query2);
        expect(query2.list()).andReturn(emptyList());
        replay(projectRepository, query, query1, query2);

        List<Employee> teamFromSubproject = teamSynchronizer.getEmployeesFromProjectTree(subProjectBasedOnMaster1);

        verify(projectRepository, query);
        assertTrue(teamFromSubproject.contains(employee1));
        assertTrue(teamFromSubproject.contains(employee2));
        assertTrue(teamFromSubproject.contains(employee3));
        assertTrue(teamFromSubproject.contains(employee4));
        assertFalse(teamFromSubproject.contains(employee5));
    }

    @Test
    public void testGetCurrentTeamMembers() {
        Project project = new Project();
        project.setTeamFilter(new TeamFilter(Type.PROJECT_CODES, "abc"));
        Project subproject = new Project(project);
        subproject.setTeamFilter(new TeamFilter(DISABLED));
        Employee employee1 = new Employee("emp1");
        Employee employee2 = new Employee("emp2");
        Employee employee3 = new Employee("emp3");
        project.addTeamMember(employee1);
        project.addTeamMember(employee2);
        subproject.addTeamMember(employee2);
        subproject.addTeamMember(employee3);

        ProjectRepository.ProjectQuery query = mock(ProjectRepository.ProjectQuery.class);
        expect(projectRepository.query()).andReturn(query);
        expect(query.masters(asList(project))).andReturn(query);
        expect(query.status(ACTIVE)).andReturn(query);
        expect(query.list()).andReturn(asList(subproject));
        replay(projectRepository, query);

        List<Employee> currentTeamMembers = teamSynchronizer.getCurrentTeamMembers(project);

        verify(projectRepository, query);
        assertTrue(currentTeamMembers.containsAll(project.getTeam()));
        assertFalse(currentTeamMembers.contains(employee3));
    }

    @Test
    public void testGetCurrentTeamMembers_existSubprojectsBasedOnMaster() throws NoSuchFieldException {
        Project project = new Project();
        project.setTeamFilter(new TeamFilter(Type.PROJECT_CODES, "abc"));
        Project subproject = new Project(project);
        subproject.setTeamFilter(new TeamFilter(BASED_ON_MASTER));
        Project subprojectNotBasedOnMaster = new Project(project);
        subprojectNotBasedOnMaster.setTeamFilter(new TeamFilter(Type.PROJECT_CODES));
        Employee employee1 = new Employee("emp1");
        Employee employee2 = new Employee("emp2");
        Employee employee3 = new Employee("emp3");
        Employee employee4 = new Employee("emp4");
        project.addTeamMember(employee1);
        project.addTeamMember(employee2);
        subproject.addTeamMember(employee2);
        subproject.addTeamMember(employee3);
        subprojectNotBasedOnMaster.addTeamMember(employee4);

        teamSynchronizer = createMockBuilder(TeamSynchronizer.class)
                .addMockedMethod("getEmployeesFromProjectTree", Project.class)
                .createMock();
        setField(teamSynchronizer, "projectRepository", projectRepository);
        ProjectRepository.ProjectQuery query = mock(ProjectRepository.ProjectQuery.class);
        expect(teamSynchronizer.getEmployeesFromProjectTree(project)).andReturn(asList(employee1, employee2, employee3));
        expect(projectRepository.query()).andReturn(query);
        expect(query.masters(asList(project))).andReturn(query);
        expect(query.status(ACTIVE)).andReturn(query);
        expect(query.list()).andReturn(asList(subproject, subprojectNotBasedOnMaster));
        replay(projectRepository, teamSynchronizer, query);

        List<Employee> currentTeamMembers = teamSynchronizer.getCurrentTeamMembers(project);

        verify(projectRepository, teamSynchronizer, query);
        assertTrue(currentTeamMembers.containsAll(project.getTeam()));
        assertTrue(currentTeamMembers.containsAll(subproject.getTeam()));
        assertFalse(currentTeamMembers.contains(employee4));
    }

    @Test
    public void testGetEmployeesFromProjectTree_expectNoEmployeesFromClosedSubprojects() {
        Employee employee1 = new Employee("emp1");
        Employee employee2 = new Employee("emp2");
        Employee employee3 = new Employee("emp3");
        Employee employee4 = new Employee("emp4");
        Project master = new Project();
        master.setCode("master");
        master.setTeamFilter(new TeamFilter(Type.PROJECT_CODES, "abc"));
        master.addTeamMember(employee1);
        Project activeSubproject = new Project(master);
        activeSubproject.setStatus(ACTIVE);
        activeSubproject.setCode("activeSubBased");
        activeSubproject.setTeamFilter(new TeamFilter(BASED_ON_MASTER));
        activeSubproject.addTeamMember(employee2);
        activeSubproject.addTeamMember(employee3);
        Project closedSubproject = new Project(master);
        closedSubproject.setStatus(Project.Status.CLOSED);
        closedSubproject.setCode("subBased2");
        closedSubproject.setTeamFilter(new TeamFilter(BASED_ON_MASTER));
        closedSubproject.addTeamMember(employee3);
        closedSubproject.addTeamMember(employee4);

        ProjectRepository.ProjectQuery query = mock(ProjectRepository.ProjectQuery.class);
        ProjectRepository.ProjectQuery query1 = mock(ProjectRepository.ProjectQuery.class);
        ProjectRepository.ProjectQuery query2 = mock(ProjectRepository.ProjectQuery.class);
        expect(projectRepository.query()).andReturn(query).times(2);
        expect(query.masters(asList(master))).andReturn(query1);
        expect(query.masters(asList(activeSubproject))).andReturn(query2);
        expect(query1.status(ACTIVE)).andReturn(query1);
        expect(query1.list()).andReturn(asList(activeSubproject));
        expect(query2.status(ACTIVE)).andReturn(query2);
        expect(query2.list()).andReturn(emptyList());
        replay(projectRepository, query, query1, query2);

        List<Employee> teamFromSubproject = teamSynchronizer.getEmployeesFromProjectTree(activeSubproject);

        verify(projectRepository, query);
        assertTrue(teamFromSubproject.contains(employee1));
        assertTrue(teamFromSubproject.contains(employee2));
        assertTrue(teamFromSubproject.contains(employee3));
        assertFalse(teamFromSubproject.contains(employee4));
    }

    @Test
    public void testGetTeam() {
        Project project = new Project();
        project.setTeamFilter(new TeamFilter(TeamFilter.Type.DISABLED));
        List<Employee> actual = teamSynchronizer.getTeam(project);
        assertTrue(actual.isEmpty());
    }

    @Test
    public void testGetTeam_projectCodesFilterType() {
        Project project = new Project();
        String projectCodes = "code1, code2";
        project.setTeamFilter(new TeamFilter(TeamFilter.Type.PROJECT_CODES, projectCodes));
        Employee employee1 = new Employee("uname1");
        Employee employee2 = new Employee("uname2");
        expect(teamTrackingSystem.getTeamByGroupCode("code1")).andReturn(asList(employee1));
        expect(teamTrackingSystem.getTeamByGroupCode("code2")).andReturn(asList(employee2));
        replay(teamTrackingSystem);

        List<Employee> actual = teamSynchronizer.getTeam(project);

        verify(teamTrackingSystem);
        assertNotNull(actual);
        assertEquals(2, actual.size());
        assertTrue(actual.contains(employee1));
        assertTrue(actual.contains(employee2));
    }

    @Test
    public void testGetTeam_basedOnMasterFilterType() {
        Project master = new Project();
        master.setTeamFilter(new TeamFilter(TeamFilter.Type.DISABLED));
        Project project = new Project(master);
        project.setTeamFilter(new TeamFilter(TeamFilter.Type.BASED_ON_MASTER));
        List<Employee> actual = teamSynchronizer.getTeam(project);
        assertTrue(actual.isEmpty());
    }

}


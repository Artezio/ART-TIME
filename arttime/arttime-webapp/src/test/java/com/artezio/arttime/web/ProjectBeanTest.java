package com.artezio.arttime.web;

import com.artezio.arttime.datamodel.Employee;
import com.artezio.arttime.datamodel.Project;
import com.artezio.arttime.datamodel.Project.Status;
import com.artezio.arttime.datamodel.WorkdaysCalendar;
import com.artezio.arttime.repositories.ProjectRepository;
import com.artezio.arttime.repositories.WorkdaysCalendarRepository;
import com.artezio.arttime.services.EmployeeService;
import com.artezio.arttime.services.ProjectService;
import com.artezio.arttime.services.integration.EmployeeTrackingSystem;
import junitx.framework.ListAssert;
import org.easymock.EasyMock;
import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.primefaces.component.selectcheckboxmenu.SelectCheckboxMenu;
import org.primefaces.event.FlowEvent;

import javax.faces.context.ExternalContext;
import javax.faces.event.ValueChangeEvent;
import java.security.Principal;
import java.util.*;

import static junitx.util.PrivateAccessor.getField;
import static junitx.util.PrivateAccessor.setField;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

@RunWith(EasyMockRunner.class)
public class ProjectBeanTest {

    @TestSubject
    private ProjectBean bean = new ProjectBean();
    @Mock
    private ProjectService projectService;
    @Mock
    private ExternalContext externalContext;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private EmployeeService employeeService;
    @Mock
    private WorkdaysCalendarRepository workdaysCalendarRepository;
    @Mock
    private ValueChangeEvent valueChangeEvent;
    @Mock
    private SelectCheckboxMenu selectCheckboxMenu;
    @Mock
    private EmployeeTrackingSystem employeeTrackingSystem;

    @Test
    public void testCreate() throws NoSuchFieldException {
        Project project = createProject(1L, "code", Status.ACTIVE);
        setField(bean, "project", project);

        EasyMock.expect(projectService.create(project)).andReturn(project);
        EasyMock.replay(projectService);

        bean.create();

        EasyMock.verify(projectService);
    }

    @Test
    public void testCreate_codeIsTrimmed() throws NoSuchFieldException {
        Project project = createProject(1L, "  \t code \t ", Status.ACTIVE);
        setField(bean, "project", project);

        EasyMock.expect(projectService.create(project)).andReturn(project);
        EasyMock.replay(projectService);

        bean.create();

        EasyMock.verify(projectService);
        assertEquals("code", project.getCode());
    }

    @Test
    public void testUpdate() throws NoSuchFieldException {
        Project project = new Project();
        setField(bean, "project", project);

        projectService.update(bean.getActiveProjects());
        EasyMock.replay(projectService);

        bean.update();

        EasyMock.verify(projectService);
    }

    @Test
    public void testUpdate_codeIsTrimmed() throws NoSuchFieldException {
        Project project = new Project();
        project.setCode("  \t code\t ");
        setField(bean, "project", project);

        projectService.update(bean.getActiveProjects());
        EasyMock.replay(projectService);

        bean.update();

        EasyMock.verify(projectService);
        assertEquals("code", project.getCode());
    }

    @Test
    public void testAddNewTeamMember() throws NoSuchFieldException {
        Project project = new Project();
        Employee employee = new Employee("employee");
        WorkdaysCalendar calendar = new WorkdaysCalendar();
        employee.setCalendar(calendar);
        Map<Employee, Project[]> participations = new HashMap<>();
        setField(bean, "participations", participations);
        setField(bean, "project", project);
        setField(bean, "employee", employee);

        bean.addNewTeamMember();

        assertEquals(1, participations.size());
        assertEquals(1, participations.get(employee).length);
        assertSame(project, participations.get(employee)[0]);
        assertTrue(participations.containsKey(employee));
        assertNull(bean.getEmployee());
    }

    @Test
    public void testGetOrderedTeam_ifStatusActive() throws NoSuchFieldException {
        bean = createMockBuilder(ProjectBean.class).addMockedMethods("getActiveProjects", "getParticipations").createMock();
        Employee employee1 = new Employee("employee1");
        Employee employee2 = new Employee("employee2");
        Employee employee3 = new Employee("employee3");
        Project project1 = createProject(1L, "project1", Status.ACTIVE);
        project1.addTeamMember(employee3);
        Project project2 = createProject(2L, "project2", Status.CLOSED);
        project2.addTeamMember(employee1);
        Project project3 = createProject(3L, "project3", Status.ACTIVE);
        project3.addTeamMember(employee2);
        Map<Employee, Project[]> participations = new HashMap<>();
        participations.put(employee3, new Project[]{project1});
        participations.put(employee2, new Project[]{project3});

        setField(bean, "project", project1);
        expect(bean.getActiveProjects()).andReturn(Arrays.asList(project1, project3));
        expect(bean.getParticipations()).andReturn(participations).anyTimes();
        replay(bean);

        List<Employee> actual = bean.getOrderedTeam();

        verify(bean);
        ListAssert.assertEquals(Arrays.asList(employee2, employee3), actual);
    }

    @Test
    public void testGetOrderedTeam_ifStatusNotActive() throws NoSuchFieldException {
        bean = createMockBuilder(ProjectBean.class).addMockedMethod("getParticipations").createMock();
        Employee employee1 = new Employee("employee1");
        Employee employee2 = new Employee("employee2");
        Project project = new Project();
        project.setStatus(Status.FROZEN);
        project.addTeamMember(employee1);
        project.addTeamMember(employee2);
        Map<Employee, Project[]> participations = new HashMap<>();
        participations.put(employee1, new Project[]{project});
        participations.put(employee2, new Project[]{project});

        List<Employee> expected = Arrays.asList(employee1, employee2);

        setField(bean, "project", project);
        expect(bean.getParticipations()).andReturn(participations).anyTimes();
        replay(bean);

        List<Employee> actual = bean.getOrderedTeam();

        verify(bean);
        ListAssert.assertEquals(expected, actual);
    }

    @Test
    public void testGetActiveProjects() throws NoSuchFieldException {
        bean = createMockBuilder(ProjectBean.class).addMockedMethod("getProjectTree").createMock();
        Project project1 = createProject(1L, "proj", Status.ACTIVE);
        Project project2 = createProject(2L, "proj_", Status.FROZEN);
        Project project3 = createProject(3L, "_proj", Status.CLOSED);
        Project project4 = createProject(4L, "_prj_", Status.ACTIVE);

        expect(bean.getProjectTree()).andReturn(Arrays.asList(project1, project2, project3, project4));
        replay(bean);

        List<Project> actual = bean.getActiveProjects();

        verify(bean);

        ListAssert.assertEquals(Arrays.asList(project4, project1), actual);
    }

    private Project createProject(Long id, String code, Status status) throws NoSuchFieldException {
        Project project = new Project();
        project.setCode(code);
        project.setStatus(status);
        setField(project, "id", id);
        return project;
    }

    @Test
    public void testInit_ifProjectPassedAsAParam() throws Exception {
        setField(bean, "externalContext", externalContext);
        setField(bean, "projectService", projectService);
        Project project = new Project();
        Map<String, String> requestParams = new HashMap<>();
        requestParams.put("project", "1");
        expect(projectService.loadProject(1L)).andReturn(project);
        expect(externalContext.getRequestParameterMap()).andReturn(requestParams);
        replay(externalContext, projectService);

        bean.init();

        verify(externalContext, projectService);

        assertSame(project, getField(bean, "project"));
    }

    @Test
    public void testInit_ifMasterPassAsParam() throws Exception {
        setField(bean, "externalContext", externalContext);
        setField(bean, "projectService", projectService);
        Project project = new Project();
        Map<String, String> requestParams = new HashMap<>();
        requestParams.put("master", "1");
        expect(projectService.loadProject(1L)).andReturn(project);
        expect(externalContext.getRequestParameterMap()).andReturn(requestParams);
        replay(externalContext, projectService);

        bean.init();
        Project actual = (Project) getField(bean, "project");

        verify(externalContext, projectService);
        assertNotNull(actual);
        assertSame(project, actual.getMaster());
    }

    @Test
    public void testInit_ifNoneParams() throws Exception {
        Employee loggedEmployee = new Employee("user name");
        setField(bean, "externalContext", externalContext);
        Map<String, String> requestParams = new HashMap<>();

        expect(externalContext.getRequestParameterMap()).andReturn(requestParams);
        Principal principal = mock(Principal.class);
        expect(externalContext.getUserPrincipal()).andReturn(principal);
        expect(principal.getName()).andReturn("username");
        expect(employeeService.getLoggedEmployee()).andReturn(Optional.of(loggedEmployee));
        replay(externalContext, employeeService, principal);

        bean.init();
        Project actual = (Project) getField(bean, "project");

        verify(externalContext, employeeService, principal);
        assertNotNull(actual);
        assertTrue(actual.getManagers().contains(loggedEmployee));
    }

    @Test
    public void testCreateNewProject() throws NoSuchFieldException {
        final String username = "username";
        Employee loggedEmployee = new Employee(username);
        expect(employeeService.getLoggedEmployee()).andReturn(Optional.of(loggedEmployee));
        Principal principal = mock(Principal.class);
        expect(externalContext.getUserPrincipal()).andReturn(principal);
        expect(principal.getName()).andReturn(username);
        replay(employeeService, principal, externalContext);

        bean.createNewProject();

        verify(employeeService, principal, externalContext);
        Project actual = (Project)getField(bean, "project");
        assertTrue(actual.getManagers().contains(loggedEmployee));
    }

    @Test
    public void testCreateNewProject_loggedEmployeeNotFound_externalEmployeeFound() throws NoSuchFieldException {
        final String username = "username";
        Employee loggedEmployee = new Employee(username);
        expect(employeeService.getLoggedEmployee()).andReturn(Optional.empty());
        expect(employeeTrackingSystem.findEmployee(username)).andReturn(loggedEmployee);
        Principal principal = mock(Principal.class);
        expect(externalContext.getUserPrincipal()).andReturn(principal);
        expect(principal.getName()).andReturn(username);
        replay(employeeService, employeeTrackingSystem, principal, externalContext);

        bean.createNewProject();

        verify(employeeService, employeeTrackingSystem, principal, externalContext);
        Project actual = (Project)getField(bean, "project");
        assertTrue(actual.getManagers().contains(loggedEmployee));
    }

    @Test
    public void testCreateNewProject_loggedEmployeeNotFound_externalEmployeeNotFound() throws NoSuchFieldException {
        final String username = "username";
        expect(employeeService.getLoggedEmployee()).andReturn(Optional.empty());
        expect(employeeTrackingSystem.findEmployee(username)).andReturn(null);
        Principal principal = mock(Principal.class);
        expect(externalContext.getUserPrincipal()).andReturn(principal);
        expect(principal.getName()).andReturn(username);
        replay(employeeService, employeeTrackingSystem, principal, externalContext);

        bean.createNewProject();

        verify(employeeService, employeeTrackingSystem, principal, externalContext);
        Project actual = (Project)getField(bean, "project");
        assertTrue(actual.getManagers().isEmpty());
    }

    @Test
    public void testIsManagedMasterProject() throws Exception {
        Project master = new Project();
        Project project = new Project(master);
        Employee employee = new Employee();
        master.addManager(employee);

        setField(bean, "project", project);
        setField(bean, "projectService", projectService);
        expect(employeeService.getLoggedEmployee()).andReturn(Optional.of(employee));

        replay(projectService, employeeService);

        boolean actual = bean.isManagedMasterProject();

        verify(projectService, employeeService);
        assertTrue(actual);
    }

    @Test
    public void testGetSubprojects_ifNotNull() throws Exception {
        List<Project> subprojects = new ArrayList<>();
        setField(bean, "subprojects", subprojects);

        List<Project> actual = bean.getSubprojects();

        assertSame(subprojects, actual);
    }

    @Test
    public void testGetSubprojects_ifNull() throws Exception {
        Project project = new Project();
        setField(bean, "project", project);
        setField(bean, "subprojects", null);
        setField(bean, "projectService", projectService);
        List<Project> expected = new ArrayList<>();

        EasyMock.expect(projectService.getManagedProjectHierarchy(project)).andReturn(expected);
        EasyMock.expect(projectService.fetchComplete(expected)).andReturn(expected);
        EasyMock.replay(projectService);

        List<Project> actual = bean.getSubprojects();

        EasyMock.verify(projectService);
        assertEquals(expected, actual);
    }

    @Test
    public void testGetParticipations_participationListIsNull() throws NoSuchFieldException {
        bean = createMockBuilder(ProjectBean.class).addMockedMethods("getActiveProjects").createMock();
        List<Project> projects = new LinkedList<>();
        List<Employee> employees = new LinkedList<>();
        Project project1 = createProject(1L, "1", Status.ACTIVE),
                project2 = createProject(2L, "2", Status.ACTIVE);
        Employee employee1 = new Employee("emp1"),
                employee2 = new Employee("emp2");
        Collections.addAll(employees, employee1, employee2);
        project1.setTeam(employees);
        project2.setTeam(employees);
        Collections.addAll(projects, project1, project2);
        Map<Employee, Project[]> expectedResult = new HashMap<>();
        expectedResult.put(employee1, projects.toArray(new Project[0]));
        expectedResult.put(employee2, projects.toArray(new Project[0]));
        setField(bean, "project", project1);
        expect(bean.getActiveProjects()).andReturn(projects);
        replay(bean);

        Map<Employee, Project[]> actualResult = bean.getParticipations();

        verify(bean);
        assertTrue(actualResult.containsKey(employee1));
        assertTrue(actualResult.containsKey(employee2));
        assertTrue(Arrays.equals(expectedResult.get(employee1), actualResult.get(employee1)));
        assertTrue(Arrays.equals(expectedResult.get(employee2), actualResult.get(employee2)));
    }

    @Test
    public void testGetParticipations_participationListIsNotNull() throws NoSuchFieldException {
        bean = createMockBuilder(ProjectBean.class).addMockedMethods("getActiveProjects").createMock();
        List<Project> projects = new LinkedList<>();
        List<Employee> employees = new LinkedList<>();
        Project project1 = createProject(1L, "1", Status.ACTIVE),
                project2 = createProject(2L, "2", Status.ACTIVE);
        Employee employee1 = new Employee("emp1"),
                employee2 = new Employee("emp2");
        Collections.addAll(employees, employee1, employee2);
        project1.setTeam(employees);
        project2.setTeam(employees);
        Collections.addAll(projects, project1, project2);
        Map<Employee, Project[]> expectedResult = new HashMap<>();
        expectedResult.put(employee1, projects.toArray(new Project[0]));
        expectedResult.put(employee2, projects.toArray(new Project[0]));

        setField(bean, "participations", expectedResult);

        Map<Employee, Project[]> actualResult = bean.getParticipations();

        assertEquals(expectedResult, actualResult);
    }

    @Test
    public void testRemoveProject() throws Exception {
        Project project = new Project();
        setField(bean, "project", project);
        setField(bean, "subprojects", new ArrayList<Project>());

        projectService.remove(project);
        EasyMock.replay(projectService);

        bean.remove(project);

        EasyMock.verify(projectService);
        assertNull(getField(bean, "subprojects"));
    }

    @Test
    public void testOnWizardFlow() throws Exception {
        FlowEvent flowEvent = createMock(FlowEvent.class);
        expect(flowEvent.getNewStep()).andReturn("new step");
        replay(flowEvent);

        String actual = bean.onWizardFlow(flowEvent);

        verify(flowEvent);
        assertEquals("new step", actual);
        assertEquals("new step", getField(bean, "selectedTab"));
    }

    @Test
    public void testGetStatuses() {
        List<Status> expected = Arrays.asList(Status.ACTIVE, Status.FROZEN, Status.CLOSED);

        Status[] actual = bean.getStatuses();

        ListAssert.assertEquals(expected, Arrays.asList(actual));
    }

    @Test
    public void testDeleteFromAllProjects() throws NoSuchFieldException {
        bean = createMockBuilder(ProjectBean.class).addMockedMethod("getParticipations").createMock();

        Employee employee = new Employee("employee");
        Project project1 = createProject(1L, "code1", Status.ACTIVE);
        project1.getTeam().add(employee);
        Project project2 = createProject(2L, "code2", Status.ACTIVE);
        project2.getTeam().add(employee);
        Map<Employee, Project[]> participations = new HashMap<>();
        participations.put(employee, new Project[]{project1, project2});
        setField(bean, "participations", participations);
        setField(bean, "project", project1);
        expect(bean.getParticipations()).andReturn(participations).anyTimes();

        replay(bean);

        bean.deleteFromAllProjects(employee);

        verify(bean);
        assertFalse(project1.isTeamMember(employee));
        assertFalse(project2.isTeamMember(employee));
    }

    @Test
    public void testBuildProjectsList_prjIdIsNull() throws NoSuchFieldException {
        Project project = new Project();
        setField(bean, "project", project);
        List<Project> actual = bean.getProjectTree();

        assertEquals(1, actual.size());
        assertSame(project, actual.get(0));
    }

    @Test
    public void testBuildProjectsList_prjIdIsNotNull() throws NoSuchFieldException {
        Project project = createProject(1L, "prj1", Status.ACTIVE);
        Project project2 = createProject(2L, "prj2", Status.CLOSED);
        List<Project> projects = new ArrayList<>();
        projects.add(project);
        projects.add(project2);

        setField(bean, "project", project);
        setField(bean, "projectService", projectService);

        expect(projectService.getManagedProjectHierarchy(project)).andReturn(projects);
        EasyMock.expect(projectService.fetchComplete(projects)).andReturn(projects);
        replay(projectService);

        List<Project> actual = bean.getProjectTree();

        verify(projectService);

        assertSame(projects, actual);
    }

    @Test
    public void testGetProjectsTree_hasSubprojectsFromMaster() throws NoSuchFieldException {
        Project master = createProject(1L, "master", Status.ACTIVE);
        Project subProject = createProject(2L, "sub", Status.ACTIVE);
        List<Project> expected = Arrays.asList(master, subProject);
        setField(bean, "project", master);
        setField(bean, "projectService", projectService);

        EasyMock.expect(projectService.getManagedProjectHierarchy(master)).andReturn(expected);
        EasyMock.expect(projectService.fetchComplete(expected)).andReturn(expected);
        EasyMock.replay(projectService);

        List<Project> actual = bean.getProjectTree();

        EasyMock.verify(projectService);
        ListAssert.assertEquals(expected, actual);
    }

    @Test
    public void testWithoutOutdatedEmployees() throws NoSuchFieldException {
        Map<Employee, Project[]> participations = new HashMap<>();
        Employee employee1 = new Employee("uname1");
        Project[] projects1 = new Project[]{new Project()};
        participations.put(employee1, projects1);
        Employee outdatedEmployee1 = new Employee("outdateduname1");
        participations.put(outdatedEmployee1, new Project[]{});
        Employee outdatedEmployee2 = new Employee("outdateduname2");
        setField(bean, "participations", participations);
        List<Employee> allEmployees = Arrays.asList(employee1, outdatedEmployee1, outdatedEmployee2);

        List<Employee> actual = bean.withoutOutdatedEmployees(allEmployees);

        assertTrue(actual.contains(employee1));
        assertFalse(actual.contains(outdatedEmployee1));
        assertFalse(actual.contains(outdatedEmployee2));
    }

    @Test
    public void testModifyTeamIfChanged_teamWasEmpty_addedNewMembers() throws NoSuchFieldException {
        Map<Employee, Project[]> participations = new HashMap<>();
        Project project = new Project();
        Employee addedEmployee1 = new Employee("uname1");
        Employee addedEmployee2 = new Employee("uname2");
        participations.put(addedEmployee1, new Project[]{project});
        participations.put(addedEmployee2, new Project[]{project});
        setField(bean, "participations", participations);

        bean.modifyTeamIfChanged(project);

        assertTrue(project.isTeamMember(addedEmployee1));
        assertTrue(project.isTeamMember(addedEmployee2));
    }

    @Test
    public void testModifyTeamIfChanged_teamWasNotEmpty_removedAndAddedMembers() throws NoSuchFieldException {
        Map<Employee, Project[]> participations = new HashMap<>();
        Project project = new Project();
        Employee addedEmployee = new Employee("uname1");
        Employee formerEmployee = new Employee("uname3");
        project.addTeamMember(formerEmployee);
        participations.put(addedEmployee, new Project[]{project});
        setField(bean, "participations", participations);

        bean.modifyTeamIfChanged(project);

        assertTrue(project.isTeamMember(addedEmployee));
        assertFalse(project.isTeamMember(formerEmployee));
    }

    @Test
    public void testModifyTeamsForChangedProjects_masterIsActive_expectParticipationsSavedOnlyForActiveProjects() throws NoSuchFieldException {
        Employee employee1 = new Employee("employee1");
        Employee employee2 = new Employee("employee2");
        Employee employee3 = new Employee("employee3");
        Project project1 = createProject(1L, "project1", Status.ACTIVE);
        Project subActive = createProject(2L, "sub1", Status.ACTIVE);
        subActive.addTeamMember(employee2);
        Project subClosed = createProject(3L, "sub2", Status.CLOSED);
        subClosed.addTeamMember(employee3);
        Map<Employee, Project[]> participations = new HashMap<>();
        participations.put(employee1, new Project[]{project1, subClosed});
        participations.put(employee2, new Project[]{subActive, subClosed});
        projectService = createMock(ProjectService.class);
        setField(bean, "participations", participations);
        setField(bean, "project", project1);
        setField(bean, "projectService", projectService);
        List<Project> hierarchy = Arrays.asList(project1, subActive, subClosed);

        EasyMock.expect(projectService.getManagedProjectHierarchy(project1)).andReturn(hierarchy).anyTimes();
        EasyMock.expect(projectService.fetchComplete(hierarchy)).andReturn(hierarchy);
        EasyMock.replay(projectService);

        bean.modifyTeamsForChangedProjects();

        EasyMock.verify(projectService);
        assertTrue(project1.isTeamMember(employee1));
        assertFalse(project1.isTeamMember(employee2));
        assertFalse(project1.isTeamMember(employee3));
        assertFalse(subActive.isTeamMember(employee1));
        assertTrue(subActive.isTeamMember(employee2));
        assertFalse(subActive.isTeamMember(employee3));
        assertFalse(subClosed.isTeamMember(employee1));
        assertFalse(subClosed.isTeamMember(employee2));
        assertTrue(subClosed.isTeamMember(employee3));

    }

}

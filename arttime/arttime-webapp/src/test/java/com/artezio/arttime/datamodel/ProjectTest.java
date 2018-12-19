package com.artezio.arttime.datamodel;

import com.artezio.arttime.datamodel.Project.Status;
import com.artezio.arttime.datamodel.TeamFilter.Type;
import junitx.framework.ListAssert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static junitx.util.PrivateAccessor.setField;
import static org.junit.Assert.*;

public class ProjectTest {

    @Test
    public void testIsTeamMember(){
        Project project = new Project();
        Employee employee = new Employee("not team member");
        Employee teamMember = new Employee("team member");
        project.addTeamMember(teamMember);

        assertTrue(project.isTeamMember(teamMember));
        assertFalse(project.isTeamMember(employee));
    }

    @Test
    public void testAddAccountableHours(){
        Project project = new Project();
        assertEquals(0, project.getAccountableHours().size());
        HourType expected = new HourType("type");
        project.addAccountableHours(expected);

        assertTrue(project.getAccountableHours().contains(expected));
    }

    @Test
    public void testAddManager() {
        Project project = new Project();
        Employee manager = new Employee("manager");
        List<Employee> expected = Arrays.asList(manager);

        project.addManager(manager);

        List<Employee> actual = new ArrayList<>(project.getManagers());
        ListAssert.assertEquals(expected, actual);
    }

    @Test
    public void testCanBeManaged_ifEmployeeIsPm() {
        Project project = new Project();
        Employee manager = new Employee("manager");
        project.addManager(manager);

        boolean actual = project.canBeManaged(manager);

        assertTrue(actual);
    }

    @Test
    public void testCanBeManaged_ifEmployeeIsMasterProjectPm() {
        Employee manager = new Employee("manager");
        Project master = new Project();
        master.addManager(manager);
        Project project = new Project(master);

        boolean actual = project.canBeManaged(manager);

        assertTrue(actual);
    }

    @Test
    public void testCanBeManaged_ifEmployeeIsNotPm() {
        Employee employee = new Employee("employee");
        Project master = new Project();
        Project project = new Project(master);

        boolean actual = project.canBeManaged(employee);

        assertFalse(actual);
    }

    @Test
    public void testGetRootProject_ifHimselfRootProject() {
        Project project = new Project();

        Project actual = project.getRootProject();

        assertSame(project, actual);
    }

    @Test
    public void testGetRootProject() {
        Project project1 = new Project();
        Project project2 = new Project(project1);
        Project project3 = new Project(project2);

        Project actual = project3.getRootProject();

        assertSame(project1, actual);
    }

    @Test
    public void testGetTeamFilter() throws NoSuchFieldException {
        Project project = new Project();
        TeamFilter teamFilter = new TeamFilter();
        setField(project, "teamFilter", teamFilter);

        TeamFilter actual = project.getTeamFilter();

        assertSame(teamFilter, actual);
    }

    @Test
    public void testGetTeamFilter_ifTeamFilterIsNull_AndMaster() throws NoSuchFieldException {
        Project project = new Project();
        setField(project, "teamFilter", null);

        TeamFilter actual = project.getTeamFilter();

        assertNotNull(actual);
        assertEquals(TeamFilter.Type.PROJECT_CODES, actual.getFilterType());
    }

    @Test
    public void testGetTeamFilter_ifTeamFilterIsNull_AndSubproject() throws NoSuchFieldException {
        Project master = new Project();
        Project project = new Project(master);
        setField(project, "teamFilter", null);

        TeamFilter actual = project.getTeamFilter();

        assertNotNull(actual);
        assertEquals(Type.BASED_ON_MASTER, actual.getFilterType());
    }

    @Test
    public void testIsValidTeamFilter_ifMaster() throws NoSuchFieldException {
        Project project = new Project();
        TeamFilter teamFilter = new TeamFilter(TeamFilter.Type.BASED_ON_MASTER);
        setField(project, "teamFilter", teamFilter);

        boolean actual = project.isValidTeamFilter();

        assertFalse(actual);
    }

    @Test
    public void testIsValidTeamFilter_ifSubproject() throws NoSuchFieldException {
        Project master = new Project();
        Project project = new Project(master);
        TeamFilter teamFilter = new TeamFilter(Type.BASED_ON_MASTER);
        setField(project, "teamFilter", teamFilter);

        boolean actual = project.isValidTeamFilter();

        assertTrue(actual);
    }

    @Test
    public void testBasedOnImportSettingsOf_IfSame() {
        Project project = new Project();

        boolean actual = project.basedOnImportSettingsOf(project);

        assertTrue(actual);
    }

    @Test
    public void testBasedOnImportSettingsOf_IfSubproject_DisabledImport() throws NoSuchFieldException {
        Project master = createProject(1L, null, TeamFilter.Type.PROJECT_CODES);
        Project subproject = createProject(2L, master, Type.DISABLED);

        boolean actual = subproject.basedOnImportSettingsOf(master);

        assertFalse(actual);
    }

    @Test
    public void testBasedOnImportSettingsOf_IfSubproject_BasedOnMasterImport() throws NoSuchFieldException {
        Project master = createProject(1L, null, Type.PROJECT_CODES);
        Project subproject = createProject(2L, master, Type.BASED_ON_MASTER);

        boolean actual = subproject.basedOnImportSettingsOf(master);

        assertTrue(actual);
    }

    @Test
    public void testIsTeamFilterDisabled_whenNotDisabled_notBasedOnMaster() throws NoSuchFieldException {
        Project project = createProject(1L, null, Type.PROJECT_CODES);
        assertFalse(project.isTeamFilterDisabled());
    }

    @Test
    public void testIsTeamFilterDisabled_whenNotDisabled_basedOnMaster_masterIsNotDisabled() throws NoSuchFieldException {
        Project master = createProject(1L, null, Type.PROJECT_CODES);
        Project project = createProject(2L, master, TeamFilter.Type.BASED_ON_MASTER);
        assertFalse(project.isTeamFilterDisabled());
    }

    @Test
    public void testIsTeamFilterDisabled_whenNotDisabled_basedOnMaster_masterIsDisabled() throws NoSuchFieldException {
        Project master = createProject(1L, null, Type.DISABLED);
        Project project = createProject(2L, master, Type.BASED_ON_MASTER);
        assertTrue(project.isTeamFilterDisabled());
    }

    @Test
    public void testIsTeamFilterDisabled_whenDisabled() throws NoSuchFieldException {
        Project project = createProject(1L, null, Type.DISABLED);
        assertTrue(project.isTeamFilterDisabled());
    }

    private Project createProject(Long id, Project master, Type type) throws NoSuchFieldException {
        Project project = new Project(master);
        setField(project, "id", id);
        TeamFilter teamFilter = new TeamFilter(type);
        project.setTeamFilter(teamFilter);
        return project;
    }

    @Test
    public void testGetStatus_ifNotHasMaster() {
        Project project = new Project();
        project.setStatus(Status.ACTIVE);

        Status actual = project.getStatus();

        assertEquals(Status.ACTIVE, actual);
    }

    @Test
    public void testGetStatus_ifOneOfMastersInStatuseClosed() {
        Project master1 = new Project();
        master1.setStatus(Status.CLOSED);
        Project master2 = new Project(master1);
        master2.setStatus(Status.FROZEN);
        Project project = new Project(master2);
        project.setStatus(Status.ACTIVE);

        Status actual = project.getStatus();

        assertEquals(Status.CLOSED, actual);
    }

    @Test
    public void testGetStatus_ifMastersIsActive_butProjectClosed() {
        Project master = new Project();
        master.setStatus(Status.ACTIVE);
        Project project = new Project(master);
        project.setStatus(Status.CLOSED);

        Status actual = project.getStatus();

        assertEquals(Status.CLOSED, actual);
    }

    @Test
    public void test() {
        Project project = new Project();
        System.out.println(project.getStatus());
    }


}



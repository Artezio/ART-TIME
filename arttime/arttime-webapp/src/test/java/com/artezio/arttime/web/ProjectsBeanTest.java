package com.artezio.arttime.web;

import com.artezio.arttime.datamodel.Employee;
import com.artezio.arttime.datamodel.Project;
import com.artezio.arttime.services.ProjectService;
import com.artezio.arttime.repositories.ProjectRepository;
import org.easymock.EasyMock;
import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.primefaces.component.datatable.DataTable;

import javax.faces.context.FacesContext;
import javax.faces.event.ComponentSystemEvent;
import javax.faces.event.PreRenderComponentEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static junitx.util.PrivateAccessor.setField;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

@RunWith(EasyMockRunner.class)
public class ProjectsBeanTest {
    private ProjectsBean projectBean;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private ProjectService projectService;
    @Mock
    private FacesContext facesContext;

    @Before
    public void setUp() {
        projectBean = new ProjectsBean();
    }

    @Test
    public void testRemove() throws NoSuchFieldException {
        Project project = createMock(Project.class);
        setField(projectBean, "projectService", projectService);

        projectService.remove(project);
        EasyMock.replay(projectService);

        projectBean.remove(project);
        EasyMock.verify(projectService);
    }

    @Test
    public void testFilterByManager() {
        Employee manager1 = new Employee("userName1", "John", "Smith", "e1@mail.com");
        Employee manager2 = new Employee("userName2", "Ivan", "Ivanov", "e2@mail.com");

        List<Employee> managers = Arrays.asList(manager1, manager2);
        String filterValue = "vAn";
        boolean actual = projectBean.filterByManager(managers, filterValue, null);
        assertTrue(actual);

        managers = Arrays.asList(manager1);
        actual = projectBean.filterByManager(managers, filterValue, null);
        assertFalse(actual);
    }

    @Test
    public void testGetProjects_ifProjectsNotNull() throws NoSuchFieldException {
        List<Project> projects = new ArrayList<>();
        setField(projectBean, "projects", projects);

        List<Project> actual = projectBean.getProjects();

        assertSame(projects, actual);
    }

    @Test
    public void testGetProjects_ifProjectsNull() throws NoSuchFieldException {
        List<Project> expected = new ArrayList<Project>();
        Employee loggedEmployee = new Employee("uname", "fname", "lname", "email");
        setField(projectBean, "projects", null);
        setField(projectBean, "projectService", projectService);

        EasyMock.expect(projectService.getManagedProjects()).andReturn(expected);
        EasyMock.expect(projectService.fetchComplete(expected)).andReturn(expected);
        replay(projectService);

        List<Project> actual = projectBean.getProjects();

        verify(projectService);
        assertSame(expected, actual);
    }

    @Test
    public void testFilterBySubprojects() {
        Project masterProject = new Project();
        Project subproject = new Project(masterProject);

        boolean actual = projectBean.filterBySubprojects(masterProject.isSubproject(), false, null);
        assertTrue(actual);

        actual = projectBean.filterBySubprojects(subproject.isSubproject(), false, null);
        assertFalse(actual);
    }

    @Test
    public void testFilterBySubprojects_showSubprojects() {
        Project masterProject = new Project();
        Project subproject = new Project(masterProject);

        boolean actual = projectBean.filterBySubprojects(masterProject.isSubproject(), true, null);
        assertTrue(actual);

        actual = projectBean.filterBySubprojects(subproject.isSubproject(), true, null);
        assertTrue(actual);
    }

    @Test
    public void testSetInitialFilteredValue_postback() throws NoSuchFieldException {
        setField(projectBean, "facesContext", facesContext);
        setField(projectBean, "projectRepository", projectRepository);
        ComponentSystemEvent event = createMock(ComponentSystemEvent.class);
        expect(facesContext.isPostback()).andReturn(true);
        replay(facesContext, event, projectRepository);

        projectBean.setInitialFilteredValue(event);

        verify(facesContext, event, projectRepository);
    }

    @Test
    public void testSetInitialFilteredValue_notPostback() throws NoSuchFieldException {
        Employee loggedEmployee = new Employee();
        setField(projectBean, "facesContext", facesContext);
        setField(projectBean, "projectService", projectService);
        DataTable table = createMock(DataTable.class);
        ComponentSystemEvent event = new PreRenderComponentEvent(table);
        ArrayList<Project> projects = new ArrayList<>();

        EasyMock.expect(projectService.getManagedProjects()).andReturn(projects);
        EasyMock.expect(projectService.fetchComplete(projects)).andReturn(projects);
        expect(facesContext.isPostback()).andReturn(false);
        table.updateValue(projects);
        replay(facesContext, table, projectService);

        projectBean.setInitialFilteredValue(event);

        verify(facesContext, table, projectService);
    }

}

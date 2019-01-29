package com.artezio.arttime.web;

import com.artezio.arttime.datamodel.Employee;
import com.artezio.arttime.datamodel.Project;
import com.artezio.arttime.filter.Filter;
import com.artezio.arttime.services.EmployeeService;
import com.artezio.arttime.services.FilterService;
import com.artezio.arttime.services.ProjectService;
import junitx.framework.ListAssert;
import org.easymock.EasyMock;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.primefaces.context.RequestContext;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.artezio.arttime.datamodel.Project.Status.CLOSED;
import static com.artezio.arttime.datamodel.Project.Status.FROZEN;
import static java.util.Arrays.asList;
import static junitx.util.PrivateAccessor.getField;
import static junitx.util.PrivateAccessor.setField;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({RequestContext.class})
public class FilterBeanTest {

    @TestSubject
    private FilterBean filterBean = new FilterBean();
    @Mock
    private FilterService filterService;
    @Mock
    private ProjectService projectService;
    @Mock
    private EmployeeService employeeService;
    private Employee loggedEmployee = new Employee();

    @Test
    public void testInit() {
        Filter activeProjectsFilter = mock(Filter.class);
        expect(filterService.getActiveProjectsFilter()).andReturn(activeProjectsFilter);
        replay(filterService);

        filterBean.init();

        verify(filterService);
        assertNotSame(activeProjectsFilter, filterBean.getCurrentFilter());
    }

    @Test
    public final void testGetCurrentFilter() throws NoSuchFieldException {
        Filter expected = createMock(Filter.class);
        setField(filterBean, "currentFilter", expected);

        Filter actual = filterBean.getCurrentFilter();

        assertSame(expected, actual);
    }

    @Test
    public final void testSetCurrentFilter() throws NoSuchFieldException {
        Employee employee = new Employee("ownerName");
        Project project = new Project();
        project.setTeam(Collections.singletonList(employee));
        List<Project> projects = Collections.singletonList(project);
        Filter expected = new Filter();
        expected.setName("filterName");
        expected.setOwner("ownerName");
        expected.setProjects(projects);
        setField(filterBean, "currentFilter", new Filter());

        EasyMock.expect(projectService.getProjectHierarchy(project)).andReturn(projects);
        EasyMock.replay(filterService, projectService);

        filterBean.setCurrentFilter(expected);

        EasyMock.verify(filterService, projectService);

        assertSame(expected, filterBean.getCurrentFilter());
    }

    @Test
    public void testSetCurrentFilter_CurrentFilterIsNotNull() throws NoSuchFieldException {
        Employee employee = new Employee("ownerName");
        Project project = new Project();
        project.setTeam(Collections.singletonList(employee));
        List<Project> projects = Collections.singletonList(project);
        Filter currentFilter = new Filter();
        currentFilter.setOwner("ownerName");
        currentFilter.setProjects(projects);
        setField(filterBean, "currentFilter", currentFilter);
        Filter expected = new Filter();
        expected.setOwner("ownerName");
        expected.setProjects(projects);

        EasyMock.expect(projectService.getProjectHierarchy(project)).andReturn(projects);
        EasyMock.replay(filterService, projectService);

        filterBean.setCurrentFilter(expected);

        EasyMock.verify(filterService, projectService);

        assertSame(expected, filterBean.getCurrentFilter());
        assertSame(currentFilter.getRangePeriodSelector(), expected.getRangePeriodSelector());
    }

    @Test
    public void testGetProjects_MasterAvailable() throws NoSuchFieldException {
        Project project1 = createProject(1L, null, "XXYY");
        Project project2 = createProject(2L, null, "AABB");
        List<Project> projects = asList(project1, project2);
        projects.forEach(project -> project.addManager(loggedEmployee));
        Filter filter = new Filter();
        setField(filterBean, "currentFilter", filter);

        EasyMock.expect(projectService.getAll()).andReturn(projects);
        EasyMock.expect(projectService.fetchComplete(projects)).andReturn(projects);
        EasyMock.replay(projectService);

        List<Project> actual = filterBean.getProjects();

        EasyMock.verify(projectService);
        ListAssert.assertEquals(projects, actual);
    }

    @Test
    public void testGetProjects_MasterAndSubprojectAvailable() throws NoSuchFieldException {
        Project project1 = createProject(1L, null, "XXYY");
        Project subproject1 = createProject(55L, project1, "123");
        Project project2 = createProject(2L, null, "AABB");
        List<Project> projects = asList(project1, project2, subproject1);
        List<Project> expected = asList(project1, project2);
        projects.forEach(project -> project.addManager(loggedEmployee));
        Filter filter = new Filter();
        setField(filterBean, "currentFilter", filter);

        EasyMock.expect(projectService.getAll()).andReturn(projects);
        EasyMock.expect(projectService.fetchComplete(projects)).andReturn(projects);
        EasyMock.replay(projectService);

        List<Project> actual = filterBean.getProjects();

        EasyMock.verify(projectService);
        ListAssert.assertEquals(expected, actual);
    }

    @Test
    public void testGetProjects_SelectedCustomFilter_FilterContainsActiveProjects() throws NoSuchFieldException {
        Project project1 = createProject(1L, null, "XXYY");
        Project subproject1 = createProject(55L, null, "123");
        Project project2 = createProject(2L, null, "AABB");
        List<Project> projects = asList(project1, project2, subproject1);
        projects.forEach(project -> project.addManager(loggedEmployee));
        Filter filter = new Filter();
        filter.setProjects(projects);
        setField(filterBean, "currentFilter", filter);

        EasyMock.expect(projectService.getAll()).andReturn(projects);
        EasyMock.expect(projectService.fetchComplete(projects)).andReturn(projects);
        EasyMock.replay(projectService);

        List<Project> actual = filterBean.getProjects();

        EasyMock.verify(projectService);
        ListAssert.assertEquals(projects, actual);
    }

    @Test
    public void testGetProjects_SelectedCustomFilter_FilterContainsClosedProjects() throws NoSuchFieldException {
        Project project1 = createProject(1L, null, "XXYY");
        Project subproject1 = createProject(55L, null, "123");
        Project project2 = createProject(2L, null, "AABB");
        project2.setStatus(CLOSED);
        project2.addManager(loggedEmployee);
        List<Project> projects = asList(project1, subproject1);
        List<Project> expected = asList(project1, subproject1, project2);
        projects.forEach(project -> project.addManager(loggedEmployee));
        Filter filter = new Filter();
        filter.getProjects().add(project2);
        setField(filterBean, "currentFilter", filter);

        EasyMock.expect(projectService.getAll()).andReturn(projects);
        EasyMock.expect(projectService.fetchComplete(projects)).andReturn(projects);
        EasyMock.replay(projectService);

        List<Project> actual = filterBean.getProjects();

        EasyMock.verify(projectService);
        ListAssert.assertEquals(expected, actual);
    }

    @Test
    public void testGetProjects_SelectedCustomFilter_FilterContainsClosedProjectsWithSubprojects() throws NoSuchFieldException {
        Project project1 = createProject(1L, null, "XXYY");
        project1.setStatus(CLOSED);
        project1.addManager(loggedEmployee);
        Project subproject1 = createProject(55L, project1, "123");
        subproject1.setStatus(CLOSED);
        subproject1.addManager(loggedEmployee);
        Project project2 = createProject(2L, null, "AABB");
        project2.addManager(loggedEmployee);
        List<Project> projects = asList(project2);
        List<Project> expected = asList(project1, project2);
        Filter filter = new Filter();
        filter.setProjects(asList(project1, subproject1));
        setField(filterBean, "currentFilter", filter);

        EasyMock.expect(projectService.getAll()).andReturn(projects);
        EasyMock.expect(projectService.fetchComplete(projects)).andReturn(projects);
        EasyMock.replay(projectService);

        List<Project> actual = filterBean.getProjects();

        EasyMock.verify(projectService);
        ListAssert.assertEquals(expected, actual);
    }

    @Test
    public void testGetSelectedProjects() throws NoSuchFieldException {
        Project project1 = createProject(1L, null, "project1");
        project1.setStatus(FROZEN);
        Project project2 = createProject(5L, null, "project2");
        List<Project> expected = asList(project2, project1);
        Filter filter = new Filter();
        filter.setProjects(expected);
        setField(filterBean, "currentFilter", filter);

        EasyMock.expect(projectService.fetchComplete(anyObject(List.class))).andReturn(expected);
        EasyMock.replay(projectService);

        List<Project> actual = filterBean.getSelectedProjects();

        EasyMock.verify(projectService);
        ListAssert.assertEquals(expected, actual);
    }

    @Test
    public void testGetSelectedProjects_noneSelected() throws NoSuchFieldException {
        Filter filter = new Filter();
        filter.setProjects(Collections.emptyList());
        setField(filterBean, "currentFilter", filter);

        List<Project> actual = filterBean.getSelectedProjects();

        assertTrue(actual.isEmpty());
    }

    @Test
    public void testGetSelectedProjects_MasterAndSubprojectAvailable() throws NoSuchFieldException {
        Project project1 = createProject(1L, null, "project1");
        Project subproject = createProject(5L, project1, "subproject");
        project1.addManager(loggedEmployee);
        subproject.addManager(loggedEmployee);
        List<Project> expected = asList(project1);
        Filter filter = new Filter();
        filter.setProjects(asList(project1, subproject));
        setField(filterBean, "currentFilter", filter);

        EasyMock.expect(projectService.fetchComplete(anyObject(List.class))).andReturn(expected);
        EasyMock.replay(projectService);

        List<Project> actual = filterBean.getSelectedProjects();

        EasyMock.verify(projectService);
        ListAssert.assertEquals(expected, actual);
    }

    @Test
    public void testGetSelectedProjects_OnlySubprojectAvailable() throws NoSuchFieldException {
        Project project1 = createProject(1L, null, "project1");
        Project subproject = createProject(5L, project1, "subproject");
        subproject.addManager(loggedEmployee);
        List<Project> expected = asList(subproject);
        Filter filter = new Filter();
        filter.setProjects(expected);
        setField(filterBean, "currentFilter", filter);

        EasyMock.expect(projectService.fetchComplete(anyObject(List.class))).andReturn(expected);
        expect(employeeService.getLoggedEmployee()).andReturn(Optional.of(loggedEmployee));
        EasyMock.replay(projectService, employeeService);

        List<Project> actual = filterBean.getSelectedProjects();

        EasyMock.verify(projectService, employeeService);
        ListAssert.assertEquals(expected, actual);
    }

    @Test
    public void testRemove() throws NoSuchFieldException {
        Filter filter = new Filter("filter", "user", false);
        Filter currentFilter = new Filter("currentFilter", "user", false);
        setField(filterBean, "currentFilter", currentFilter);

        filterService.remove(filter);

        EasyMock.replay(filterService);

        filterBean.remove(filter);

        EasyMock.verify(filterService);
    }

    @Test
    public void testRemove_ifCurrentFilter() throws NoSuchFieldException {
        Filter currentFilter = new Filter("currentFilter", "user", false);
        setField(currentFilter, "id", 1L);
        setField(filterBean, "currentFilter", currentFilter);
        filterService.remove(currentFilter);
        EasyMock.replay(filterService);

        filterBean.remove(currentFilter);

        Filter actual = (Filter) getField(filterBean, "currentFilter");

        EasyMock.verify(filterService);

        assertNull(actual.getId());
    }

    @Test
    public void testGetEmployees() throws NoSuchFieldException {
        EmployeeService employeeService = mock(EmployeeService.class);
        Employee currentEmployee = new Employee("current");
        currentEmployee.setFormer(false);
        Employee formerEmployee = new Employee("former");
        formerEmployee.setFormer(true);
        setField(filterBean, "employeeService", employeeService);

        EasyMock.expect(employeeService.getAll()).andReturn(asList(currentEmployee, formerEmployee));
        EasyMock.replay(employeeService);

        List<Employee> actual = filterBean.getEmployees();

        EasyMock.verify(employeeService);

        assertTrue(actual.contains(currentEmployee));
        assertFalse(actual.contains(formerEmployee));
    }

    private Project createProject(Long id, Project master, String code) throws NoSuchFieldException {
        Project project = new Project(master);
        project.setCode(code);
        setField(project, "id", id);
        return project;
    }

}

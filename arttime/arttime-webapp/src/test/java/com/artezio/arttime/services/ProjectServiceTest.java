package com.artezio.arttime.services;

import com.artezio.arttime.datamodel.Project;
import com.artezio.arttime.repositories.ProjectRepository;
import junitx.framework.ListAssert;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.easymock.annotation.Mock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.persistence.NoResultException;
import java.util.Arrays;
import java.util.List;

import static com.artezio.arttime.datamodel.Project.Status.ACTIVE;
import static com.artezio.arttime.datamodel.Project.Status.CLOSED;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static junit.framework.TestCase.assertEquals;
import static junitx.util.PrivateAccessor.setField;
import static org.easymock.EasyMock.replay;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore(value = "javax.security.auth.*")
public class ProjectServiceTest {

    private ProjectService projectService = new ProjectService();
    @Mock
    private ProjectRepository projectRepository;

    @Before
    public void setUp() throws Exception {
        setField(projectService, "projectRepository", projectRepository);
    }

    @Test
    public void testGetProjectHierarchy_ProjectHasNoSubprojects() {
        Project master = createProject(1L, null);
        List<Project> expected = asList(master);
        ProjectRepository.ProjectQuery projectQuery = Mockito.mock(ProjectRepository.ProjectQuery.class, Mockito.RETURNS_DEEP_STUBS);

        EasyMock.expect(projectRepository.query()).andReturn(projectQuery).once();
        Mockito.when(projectQuery
                .masters(asList(master))
                .status(ACTIVE)
                .distinct()
                .list())
                .thenReturn(emptyList());
        replay(projectRepository);

        List<Project> actual = projectService.getProjectHierarchy(master);

        EasyMock.verify(projectRepository);
        ListAssert.assertEquals(expected, actual);
    }

    @Test
    public void testGetProjectHierarchy_ProjectHasSubprojects_SubprojectsAreActive() {
        Project master = createProject(1L, null);
        Project subproject01 = createProject(2L, master);
        Project subproject02 = createProject(3L, master);
        Project subproject11 = createProject(4L, subproject01);
        List<Project> expected = asList(master, subproject01, subproject11, subproject02);
        ProjectRepository.ProjectQuery projectQuery = Mockito.mock(ProjectRepository.ProjectQuery.class, Mockito.RETURNS_DEEP_STUBS);

        EasyMock.expect(projectRepository.query()).andReturn(projectQuery).times(3);
        Mockito.when(projectQuery
                .masters(asList(master))
                .status(ACTIVE)
                .distinct()
                .list())
                .thenReturn(asList(subproject01, subproject02));
        Mockito.when(projectQuery
                .masters(asList(subproject01, subproject02))
                .status(ACTIVE)
                .distinct()
                .list())
                .thenReturn(asList(subproject11));
        Mockito.when(projectQuery
                .masters(asList(subproject11))
                .status(ACTIVE)
                .distinct()
                .list())
                .thenReturn(emptyList());
        replay(projectRepository);

        List<Project> actual = projectService.getProjectHierarchy(master);

        EasyMock.verify(projectRepository);
        ListAssert.assertEquals(expected, actual);
    }

    @Test
    public void testGetProjectHierarchy_ProjectHasSubprojects_SubprojectsAreClosed() {
        Project master = createProject(1L, null);
        Project subproject01 = createProject(2L, master);
        subproject01.setStatus(CLOSED);
        Project subproject02 = createProject(3L, master);
        subproject02.setStatus(CLOSED);
        Project subproject11 = createProject(4L, subproject01);
        subproject11.setStatus(CLOSED);
        List<Project> expected = asList(master);
        ProjectRepository.ProjectQuery projectQuery = Mockito.mock(ProjectRepository.ProjectQuery.class, Mockito.RETURNS_DEEP_STUBS);

        EasyMock.expect(projectRepository.query()).andReturn(projectQuery);

        Mockito.when(projectQuery
                .masters(expected)
                .status(ACTIVE)
                .distinct()
                .list())
                .thenReturn(emptyList());
        replay(projectRepository);

        List<Project> actual = projectService.getProjectHierarchy(master);

        EasyMock.verify(projectRepository);
        ListAssert.assertEquals(expected, actual);
    }

    @Test
    public void testGetManagedProjectHierarchy_ProjectHasSubprojects_SubprojectsAreActive() {
        Project master = createProject(1L, null);
        Project subproject01 = createProject(2L, master);
        Project subproject02 = createProject(3L, master);
        Project subproject11 = createProject(4L, subproject01);
        List<Project> expected = asList(master, subproject01, subproject11, subproject02);
        ProjectRepository.ProjectQuery projectQuery = Mockito.mock(ProjectRepository.ProjectQuery.class, Mockito.RETURNS_DEEP_STUBS);

        EasyMock.expect(projectRepository.query()).andReturn(projectQuery).times(3);
        Mockito.when(projectQuery
                .masters(asList(master))
                .distinct()
                .list())
                .thenReturn(asList(subproject01, subproject02));
        Mockito.when(projectQuery
                .masters(asList(subproject01, subproject02))
                .distinct()
                .list())
                .thenReturn(asList(subproject11));
        Mockito.when(projectQuery
                .masters(asList(subproject11))
                .distinct()
                .list())
                .thenReturn(emptyList());
        replay(projectRepository);

        List<Project> actual = projectService.getManagedProjectHierarchy(master);

        EasyMock.verify(projectRepository);
        ListAssert.assertEquals(expected, actual);
    }

    @Test
    public void testGetManagedProjectHierarchy_ProjectHasSubprojects_SubprojectsAreClosed() {
        Project master = createProject(1L, null);
        Project subproject01 = createProject(2L, master);
        Project subproject02 = createProject(3L, master);
        Project subproject11 = createProject(4L, subproject01);
        List<Project> expected = asList(master, subproject01, subproject11, subproject02);
        ProjectRepository.ProjectQuery projectQuery = Mockito.mock(ProjectRepository.ProjectQuery.class, Mockito.RETURNS_DEEP_STUBS);

        EasyMock.expect(projectRepository.query()).andReturn(projectQuery).times(3);
        Mockito.when(projectQuery
                .masters(asList(master))
                .distinct()
                .list())
                .thenReturn(asList(subproject01, subproject02));
        Mockito.when(projectQuery
                .masters(asList(subproject01, subproject02))
                .distinct()
                .list())
                .thenReturn(asList(subproject11));
        Mockito.when(projectQuery
                .masters(asList(subproject11))
                .distinct()
                .list())
                .thenReturn(emptyList());
        replay(projectRepository);

        List<Project> actual = projectService.getManagedProjectHierarchy(master);

        EasyMock.verify(projectRepository);
        ListAssert.assertEquals(expected, actual);
    }

    @Test
    public void testGetById() {
        Long id = 5L;
        ProjectRepository.ProjectQuery projectQuery = Mockito.mock(ProjectRepository.ProjectQuery.class, Mockito.RETURNS_DEEP_STUBS);
        Project project = createProject(id, null);
        EasyMock.expect(projectRepository.query()).andReturn(projectQuery);
        Mockito.when(projectQuery
                .id(id)
                .fetchManagers()
                .fetchTeam()
                .fetchAccountableHours()
                .getSingleResult()).thenReturn(project);
        replay(projectRepository);

        Project actual = projectService.loadProject(5L);

        assertEquals(actual, project);
    }

    @Test(expected = NoResultException.class)
    public void testGetById_noResult() {
        Long id = 5L;
        ProjectRepository.ProjectQuery projectQuery = Mockito.mock(ProjectRepository.ProjectQuery.class, Mockito.RETURNS_DEEP_STUBS);
        EasyMock.expect(projectRepository.query()).andReturn(projectQuery);
        Mockito.when(projectQuery
                .id(id)
                .fetchManagers()
                .fetchTeam()
                .fetchAccountableHours()
                .getSingleResult()).thenThrow(new NoResultException());
        replay(projectRepository);

        projectService.loadProject(5L);
    }

    private Project createProject(Long id, Project master) {
        try {
            Project project = new Project(master);
            setField(project, "id", id);
            return project;
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("Error during creating test project", e);
        }
    }
    
    @Test
    public void testGetProjects() {
        List<Long> ids = Arrays.asList(1L, 2L);
        List<Project> expected = Arrays.asList(new Project());
        ProjectRepository.ProjectQuery projectQuery = Mockito.mock(ProjectRepository.ProjectQuery.class, Mockito.RETURNS_DEEP_STUBS);

        EasyMock.expect(projectRepository.query()).andReturn(projectQuery);
        Mockito.when(projectQuery
                .projectIds(ids)
                .list()).thenReturn(expected);
        EasyMock.replay(projectRepository);

        List<Project> actual = projectService.getProjects(ids);

        EasyMock.verify(projectRepository);
        ListAssert.assertEquals(expected, actual);
    }

}

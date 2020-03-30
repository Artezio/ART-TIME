package com.artezio.arttime.repositories;

import static java.util.Arrays.asList;
import static junitx.util.PrivateAccessor.setField;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

import com.artezio.arttime.datamodel.Employee;
import com.artezio.arttime.datamodel.HourType;
import com.artezio.arttime.datamodel.Hours;
import com.artezio.arttime.datamodel.Project;
import com.artezio.arttime.datamodel.TeamFilter;
import com.artezio.arttime.datamodel.WorkdaysCalendar;

@RunWith(PowerMockRunner.class)
public class ProjectRepositoryTest {

    private EntityManager entityManager;
    private EntityManagerFactory entityManagerFactory;
    private ProjectRepository projectRepository;
    private EmployeeRepository employeeRepository;

    @Before
    public void setUp() throws Exception {
        projectRepository = new ProjectRepository();
        Map<String, String> properties = new HashMap<>();
        properties.put("javax.persistence.validation.mode", "none");
        entityManagerFactory = Persistence.createEntityManagerFactory("test-pu", properties);
        entityManager = entityManagerFactory.createEntityManager();
        setField(projectRepository, "entityManager", entityManager);
        entityManager.getTransaction().begin();
        employeeRepository = createMock(EmployeeRepository.class);
    }

    @After
    public void tearDown() throws Exception {
        if (entityManager.getTransaction().isActive()) {
            entityManager.getTransaction().rollback();
        }
        entityManagerFactory.close();
    }

    @Test
    public void testCreateProject() throws NoSuchFieldException {
        HourType hourType = new HourType("type");
        WorkdaysCalendar calendar = new WorkdaysCalendar("calendar");
        entityManager.persist(hourType);
        entityManager.persist(calendar);
        entityManager.flush();
        entityManager.clear();

        Employee manager = new Employee("manager");
        Employee resource = new Employee("slave");
        resource.setCalendar(calendar);
        Project project = new Project();
        project.addManager(manager);
        project.getAccountableHours().add(hourType);
        project.addTeamMember(resource);
        assertNull(project.getId());
        setField(projectRepository, "employeeRepository", employeeRepository);
        expect(employeeRepository.create(resource)).andReturn(getPersisted(resource));
        expect(employeeRepository.create(manager)).andReturn(getPersisted(manager));
        replay(employeeRepository);

        Project actual = projectRepository.create(project);
        entityManager.flush();

        verify(employeeRepository);
        assertNotNull(actual.getId());
    }

    @Test
    public void testCreateProjectWithExistsManager() throws NoSuchFieldException {
        Employee manager = new Employee("manager");
        entityManager.persist(manager);
        entityManager.flush();
        entityManager.clear();

        Project project = new Project();
        project.addManager(manager);
        setField(projectRepository, "employeeRepository", employeeRepository);
        expect(employeeRepository.create(manager)).andReturn(manager);
        replay(employeeRepository);

        project = projectRepository.create(project);

        verify(employeeRepository);
        entityManager.flush();
    }

    @Test
    public void testCreateProjectWithSameEmployee() throws NoSuchFieldException {
        Employee employee = new Employee("slave");
        WorkdaysCalendar calendar = new WorkdaysCalendar("calendar");
        entityManager.merge(employee);
        entityManager.persist(calendar);
        entityManager.flush();
        entityManager.clear();

        Employee manager = new Employee("manager");
        employee.setCalendar(calendar);
        Project project = new Project();
        project.addManager(manager);
        project.addTeamMember(employee);
        setField(projectRepository, "employeeRepository", employeeRepository);
        expect(employeeRepository.create(employee)).andReturn(employee);
        expect(employeeRepository.create(manager)).andReturn(getPersisted(manager));
        replay(employeeRepository);

        projectRepository.create(project);

        verify(employeeRepository);
    }

    private <T> T getPersisted(T t) {
        entityManager.persist(t);
        return t;
    }

    @Test
    public void testRemoveProject() {
        Project project = new Project();
        entityManager.persist(project);
        Long id = project.getId();

        projectRepository.remove(project);
        Project actual = entityManager.find(Project.class, id);
        assertNull(actual);
    }

    @Test(expected = PersistenceException.class)
    public void testRemoveProject_RemoveForbidden() {
        Project project = new Project();
        Hours hours = new Hours();
        hours.setProject(project);
        entityManager.persist(project);
        entityManager.persist(hours);
        entityManager.flush();

        projectRepository.remove(project);
        entityManager.flush();
    }

    @Test
    public void testUpdateProject() throws NoSuchFieldException {
        Employee manager = new Employee("manager");
        Project project = new Project();
        project.setStatus(Project.Status.ACTIVE);
        project.addManager(manager);
        entityManager.persist(manager);
        entityManager.persist(project);
        entityManager.flush();
        entityManager.clear();

        project.setStatus(Project.Status.CLOSED);
        setField(projectRepository, "employeeRepository", employeeRepository);
        expect(employeeRepository.create(manager)).andReturn(manager);
        replay(employeeRepository);

        project = projectRepository.update(project);

        verify(employeeRepository);
        assertEquals(Project.Status.CLOSED, project.getStatus());
    }

    @Test
    public void testUpdateProject_masterIsChanged() throws NoSuchFieldException{
        projectRepository = new ProjectRepository();
        Map<String, String> properties = new HashMap<String, String>();
        properties.put("javax.persistence.validation.mode", "none");
        entityManagerFactory = Persistence.createEntityManagerFactory("test-pu", properties);
        entityManager = entityManagerFactory.createEntityManager();
        setField(projectRepository, "entityManager", entityManager);
        setField(projectRepository, "employeeRepository", employeeRepository);
        entityManager.getTransaction().begin();

        Employee manager = new Employee("userName", "firstName", "lastName", "e@mail.com");
        manager.setDepartment("department");
        HourType hourType = new HourType("type");
        TeamFilter teamFilter = new TeamFilter(TeamFilter.Type.DISABLED,"");

        Project master = createProject("master", manager, hourType, teamFilter);
        Project newMaster = createProject("newMaster", manager, hourType, teamFilter);
        Project subProject = createProject("subProject", master, manager, hourType, teamFilter);

        entityManager.persist(hourType);
        entityManager.persist(manager);
        entityManager.persist(master);
        entityManager.persist(newMaster);
        entityManager.persist(subProject);
        entityManager.flush();
        entityManager.clear();

        setField(subProject, "master", newMaster);

        Project actual = projectRepository.update(subProject);
        entityManager.clear();
        actual = entityManager.find(Project.class, actual.getId());

        assertEquals(master.getCode(), actual.getMaster().getCode());
    }

    private Project createProject(String code, Employee manager, HourType hourType, TeamFilter teamFilter){
        return createProject(code, null, manager, hourType, teamFilter);
    }

    private Project createProject(String code, Project master, Employee manager, HourType hourType, TeamFilter teamFilter){
        Project result = new Project(master);
        result.setCode(code);
        result.setManagers(asList(new Employee[]{manager}));
        result.setAccountableHours(asList(new HourType[]{hourType}));
        result.setTeamFilter(teamFilter);
        return result;
    }

    @Test
    public void testUpdateCollectionOfProject() {
        projectRepository = createMockBuilder(ProjectRepository.class)
                .addMockedMethod("update", Project.class)
                .createMock();
        Project project1 = new Project();
        Project project2 = new Project();
        Project project3 = new Project();
        List<Project> projects = asList(project1, project2, project3);

        expect(projectRepository.update(anyObject(Project.class))).andReturn(new Project()).times(3);

        replay(projectRepository);

        projectRepository.update(projects);

        verify(projectRepository);
    }

}

package com.artezio.arttime.repositories;

import com.artezio.arttime.datamodel.*;
import static com.artezio.arttime.datamodel.Project.Status.ACTIVE;
import static com.artezio.arttime.datamodel.Project.Status.CLOSED;
import static com.artezio.arttime.test.utils.CalendarUtils.getOffsetDate;
import static java.util.Arrays.asList;

import java.util.*;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;

import static junitx.util.PrivateAccessor.getField;
import static junitx.util.PrivateAccessor.setField;
import static org.easymock.EasyMock.*;

import org.hibernate.Hibernate;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
public class ProjectQueryTest {

    private EntityManager entityManager;
    private EntityManagerFactory entityManagerFactory;
    private ProjectRepository.ProjectQuery projectQuery;
    private ProjectRepository projectRepository;

    @Before
    public void setUp() throws Exception {
        Map<String, String> properties = new HashMap<>();
        properties.put("javax.persistence.validation.mode", "none");
        entityManagerFactory = Persistence.createEntityManagerFactory("test-pu", properties);
        entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        projectRepository = new ProjectRepository();
        setField(projectRepository, "entityManager", entityManager);
        projectQuery = newProjectQuery();
    }

    @After
    public void tearDown() {
        if (entityManager.getTransaction().isActive()) {
            if (entityManager.getTransaction().getRollbackOnly()) {
                entityManager.getTransaction().rollback();
            } else {
                entityManager.getTransaction().commit();
            }
            entityManagerFactory.close();
        }
    }

    @Test
    public void testListWithHoursFor_GivenUsername() {
        Employee expectedEmployee = new Employee("username", "first", "last", "email");
        Employee dummyEmployee = new Employee("dummyusername", "first2", "last2", "email2");
        Project dummyProject = new Project();
        Project expectedProject = new Project();
        dummyProject.setTeam(asList(dummyEmployee));
        expectedProject.setTeam(asList(expectedEmployee));
        HourType hourType = new HourType("type1");
        Hours dummyHours = new Hours(dummyProject, getOffsetDate(5), dummyEmployee, hourType);
        Hours expectedHours = new Hours(expectedProject, getOffsetDate(1), expectedEmployee, hourType);
        entityManager.persist(hourType);
        entityManager.persist(expectedEmployee);
        entityManager.persist(dummyEmployee);
        entityManager.persist(dummyProject);
        entityManager.persist(expectedProject);
        entityManager.persist(dummyHours);
        entityManager.persist(expectedHours);
        entityManager.flush();

        List<Project> actual = projectQuery
                .withHoursFor("username")
                .list();

        assertEquals(asList(expectedProject), actual);
    }

    @Test
    public void testList_expectAll() {
        Project expected1 = new Project();
        expected1.setCode("ABC");
        Project expected2 = new Project();
        expected2.setCode("DEF");
        entityManager.persist(expected1);
        entityManager.persist(expected2);
        entityManager.flush();

        List<Project> actual = projectQuery.list();

        assertEquals(actual, asList(expected1, expected2));
    }

    @Test
    public void testGetById_whenExists() {
        Project expected = new Project();
        Project dummy = new Project();
        entityManager.persist(expected);
        entityManager.persist(dummy);
        entityManager.flush();

        Project actual = projectQuery.id(expected.getId()).getSingleResult();

        assertEquals(actual, expected);
    }

    @Test(expected = NoResultException.class)
    public void testGetById_whenNotExists() {
        projectQuery.id(100L).getSingleResult();
    }

    @Test
    public void testGetByCodes() {
        Project expected = new Project();
        expected.setCode("ABC_");
        Project dummy = new Project();
        dummy.setCode("DEFEF");
        entityManager.persist(expected);
        entityManager.persist(dummy);
        entityManager.flush();

        Project actual = projectQuery.code("ABC_").getSingleResult();
        assertEquals(actual, expected);

        Project actualByCodes = projectQuery.codes(Collections.singletonList("ABC_"))
                .getSingleResult();

        assertEquals(actualByCodes, expected);
    }

    @Test
    public void testListByMasterProjects() {
        Project master = new Project();
        master.setCode("MASTER");
        Project subproject1 = new Project(master);
        subproject1.setCode("SUB1");
        Project subproject2 = new Project(master);
        subproject2.setCode("SUB2");
        Project dummy = new Project();
        dummy.setCode("DUMMY");
        entityManager.persist(master);
        entityManager.persist(subproject1);
        entityManager.persist(subproject2);
        entityManager.persist(dummy);
        entityManager.flush();

        List<Project> actual = projectQuery.masters(Collections.singletonList(master)).list();

        assertEquals(asList(subproject1, subproject2), actual);
    }

    @Test
    public void testListByDepartment() {
        Employee dummyEmployee = new Employee("username", "firstDummy", "lastDummy", "email1",
                "dummyDept");
        Project dummyProject = new Project();
        dummyProject.setTeam(asList(dummyEmployee));
        dummyProject.setCode("CODE");
        Employee employee = new Employee("username2", "first", "last", "email2", "expectedDept");
        Project expectedProject = new Project();
        expectedProject.setTeam(asList(employee));
        expectedProject.setCode("EXPECTED_CODE");
        entityManager.persist(dummyEmployee);
        entityManager.persist(dummyProject);
        entityManager.persist(employee);
        entityManager.persist(expectedProject);
        entityManager.flush();

        List<Project> actual = projectQuery.withTeamMembersIn(employee.getDepartment()).list();

        assertEquals(asList(expectedProject), actual);
    }

    @Test
    public void testListByManagers() {
        Employee dummyManager = new Employee("dummmy", "firstDummy", "lastDummy", "email1",
                "dummyDept");
        Project dummyProject = new Project();
        dummyProject.setManagers(asList(dummyManager));
        dummyProject.setCode("CODE");
        Employee manager = new Employee("manager", "first", "last", "email2");
        Project expectedProject1 = new Project();
        expectedProject1.setManagers(asList(manager));
        expectedProject1.setCode("EXPECTED_CODE_1");
        Project expectedProject2 = new Project();
        expectedProject2.setManagers(asList(manager));
        expectedProject2.setCode("EXPECTED_CODE_2");
        entityManager.persist(dummyManager);
        entityManager.persist(dummyProject);
        entityManager.persist(manager);
        entityManager.persist(expectedProject1);
        entityManager.persist(expectedProject2);
        entityManager.flush();

        List<Project> actual = projectQuery.managedBy(manager.getUserName()).list();

        assertEquals(asList(expectedProject1, expectedProject2), actual);
    }

    @Test
    public void testManagedByEmployee() {
        projectQuery = createMockBuilder(ProjectRepository.ProjectQuery.class)
                .addMockedMethod("managedBy", String.class)
                .createMock();
        expect(projectQuery.managedBy(anyString())).andReturn(projectQuery);
        replay(projectQuery);

        projectQuery.managedBy(new Employee());

        verify(projectQuery);
    }

    @Test
    public void testStatusPredicate() {
        Project activeProject = new Project();
        activeProject.setStatus(ACTIVE);
        Project closedProject = new Project();
        closedProject.setStatus(CLOSED);
        Project frozenProject = new Project();
        frozenProject.setStatus(Project.Status.FROZEN);
        entityManager.persist(activeProject);
        entityManager.persist(closedProject);
        entityManager.persist(frozenProject);
        entityManager.flush();

        List<Project> actualActiveProjects = projectQuery.status(ACTIVE).list();

        assertEquals(asList(activeProject), actualActiveProjects);
    }

    @Test
    public void testStatusPredicate_MasterActive_SubprojectActive() {
        Project master = new Project();
        master.setStatus(ACTIVE);
        Project subproject = new Project(master);
        subproject.setStatus(ACTIVE);
        List<Project> expected = asList(master, subproject);
        entityManager.persist(master);
        entityManager.persist(subproject);
        entityManager.flush();

        List<Project> actual = projectQuery.status(ACTIVE).list();

        assertEquals(expected, actual);
    }

    @Test
    public void testStatusPredicate_MasterActive_SubprojectClosed() {
        Project master = new Project();
        master.setStatus(ACTIVE);
        Project subproject = new Project(master);
        subproject.setStatus(CLOSED);
        List<Project> expected = asList(master);
        entityManager.persist(master);
        entityManager.persist(subproject);
        entityManager.flush();

        List<Project> actual = projectQuery.status(ACTIVE).list();

        assertEquals(expected, actual);
    }

    @Test
    public void testStatusPredicate_MasterClosed_SubprojectActive() {
        Project master = new Project();
        master.setStatus(CLOSED);
        Project subproject = new Project(master);
        subproject.setStatus(ACTIVE);
        entityManager.persist(master);
        entityManager.persist(subproject);
        entityManager.flush();

        List<Project> actual = projectQuery.status(ACTIVE).list();

        assertTrue(actual.isEmpty());
    }

    @Test
    public void testStatusPredicate_MasterClosed_SubprojectClosed() {
        Project master = new Project();
        master.setStatus(CLOSED);
        Project subproject = new Project(master);
        subproject.setStatus(CLOSED);
        entityManager.persist(master);
        entityManager.persist(subproject);
        entityManager.flush();

        List<Project> actual = projectQuery.status(ACTIVE).list();

        assertTrue(actual.isEmpty());
    }

    @Test
    public void testStatusPredicate_FindClosedProjects_MasterClosed_SubprojectActive() {
        Project master = new Project();
        master.setStatus(CLOSED);
        Project subproject = new Project(master);
        subproject.setStatus(ACTIVE);
        List<Project> expected = asList(master, subproject);
        entityManager.persist(master);
        entityManager.persist(subproject);
        entityManager.flush();

        List<Project> actual = projectQuery.status(CLOSED).list();

        assertEquals(expected, actual);
    }

    @Test
    public void testListByMasterStatus_singleJoin() {
        Project activeProject = new Project();
        activeProject.setStatus(ACTIVE);
        Project closedProject = new Project();
        closedProject.setStatus(CLOSED);
        Project frozenProject = new Project(activeProject);
        frozenProject.setStatus(Project.Status.FROZEN);
        entityManager.persist(activeProject);
        entityManager.persist(closedProject);
        entityManager.persist(frozenProject);
        entityManager.flush();

        List<Project> actual = projectQuery.withMasterStatus(ACTIVE).list();

        assertEquals(asList(frozenProject), actual);
    }

    @Test
    public void testListByMasterStatus_multiJoin() {
        Project activeProject = new Project();
        activeProject.setStatus(ACTIVE);
        Project closedProject = new Project();
        closedProject.setStatus(CLOSED);
        Project frozenProject = new Project(activeProject);
        frozenProject.setStatus(Project.Status.FROZEN);
        entityManager.persist(activeProject);
        entityManager.persist(closedProject);
        entityManager.persist(frozenProject);
        entityManager.flush();

        List<Project> actual = projectQuery
                .withMasterStatus(ACTIVE)
                .withMasterStatus(ACTIVE)
                .list();

        assertEquals(asList(frozenProject), actual);
    }

    @Test
    public void testListWithMembers() {
        Employee dummyEmployee = new Employee("username", "firstDummy", "lastDummy", "email1");
        Project dummyProject = new Project();
        dummyProject.setTeam(asList(dummyEmployee));
        Employee employee1 = new Employee("username2", "first", "last", "email2");
        Employee employee2 = new Employee("username3", "first3", "last3", "email3");
        Project expectedProject = new Project();
        expectedProject.setTeam(asList(employee1, employee2));
        Project expectedProject2 = new Project();
        expectedProject2.setTeam(asList(employee1, employee2));
        entityManager.persist(dummyEmployee);
        entityManager.persist(dummyProject);
        entityManager.persist(employee1);
        entityManager.persist(employee2);
        entityManager.persist(expectedProject);
        entityManager.persist(expectedProject2);
        entityManager.flush();

        List<Project> actual = projectQuery
                .teamMember(employee1)
                .teamMember(employee2)
                .list();

        assertEquals(asList(expectedProject, expectedProject2), actual);
    }

    @Test
    public void testListWithTeamMembers_partialMatch() throws Exception {
        Employee emp1 = new Employee("u1", "f1", "l1", "e1", "d1");
        Employee emp2 = new Employee("u2", "f2", "l2", "e2", "d2");
        Employee emp3 = new Employee("u3", "f3", "l3", "e3", "d3");
        Employee emp4 = new Employee("u4", "f4", "l4", "e4", "d4");
        Project project1 = new Project();
        project1.setCode("P1");
        project1.addTeamMember(emp1);
        Project project2 = new Project();
        project2.setCode("P2");
        project2.addTeamMember(emp1);
        project2.addTeamMember(emp2);
        project2.addTeamMember(emp4);
        Project project3 = new Project();
        project3.setCode("P3");
        entityManager.joinTransaction();
        entityManager.persist(emp1);
        entityManager.persist(emp2);
        entityManager.persist(emp3);
        entityManager.persist(emp4);
        entityManager.persist(project1);
        entityManager.persist(project2);
        entityManager.persist(project3);
        entityManager.flush();
        entityManager.clear();

        List<Project> projects = projectRepository.query()
                .teamMembers(asList(emp1, emp3, emp4))
                .list();
        assertTrue(projects.contains(project1));
        assertTrue(projects.contains(project2));
        assertFalse(projects.contains(project3));
    }

    @Test
    public void testListWithHoursFrom() {
        Employee employee = new Employee("username", "first", "last", "email");
        Project earlyProject = new Project();
        Project expectedProject = new Project();
        earlyProject.setTeam(asList(employee));
        expectedProject.setTeam(asList(employee));
        HourType hourType = new HourType("type1");
        Hours earlyHours = new Hours(earlyProject, getOffsetDate(-1), employee, hourType);
        Hours expectedHours = new Hours(expectedProject, getOffsetDate(1), employee, hourType);
        entityManager.persist(hourType);
        entityManager.persist(employee);
        entityManager.persist(earlyProject);
        entityManager.persist(earlyHours);
        entityManager.persist(expectedProject);
        entityManager.persist(expectedHours);
        entityManager.flush();

        List<Project> actual = projectQuery
                .withHoursFrom(getOffsetDate(1))
                .list();

        assertEquals(asList(expectedProject), actual);
    }

    @Test
    public void testListWithHoursTill() {
        Employee employee = new Employee("username", "first", "last", "email");
        Project lateProject = new Project();
        Project expectedProject = new Project();
        lateProject.setTeam(asList(employee));
        expectedProject.setTeam(asList(employee));
        HourType hourType = new HourType("type1");
        Hours lateHours = new Hours(lateProject, getOffsetDate(5), employee, hourType);
        Hours expectedHours = new Hours(expectedProject, getOffsetDate(1), employee, hourType);
        entityManager.persist(hourType);
        entityManager.persist(employee);
        entityManager.persist(lateProject);
        entityManager.persist(lateHours);
        entityManager.persist(expectedProject);
        entityManager.persist(expectedHours);
        entityManager.flush();

        List<Project> actual = projectQuery
                .withHoursTill(getOffsetDate(1))
                .list();

        assertEquals(asList(expectedProject), actual);
    }

    @Test
    public void testListWithHoursFor() {
        Employee expectedEmployee = new Employee("username", "first", "last", "email");
        Employee dummyEmployee = new Employee("dummyusername", "first2", "last2", "email2");
        Project dummyProject = new Project();
        Project expectedProject = new Project();
        dummyProject.setTeam(asList(dummyEmployee));
        expectedProject.setTeam(asList(expectedEmployee));
        HourType hourType = new HourType("type1");
        Hours dummyHours = new Hours(dummyProject, getOffsetDate(5), dummyEmployee, hourType);
        Hours expectedHours = new Hours(expectedProject, getOffsetDate(1), expectedEmployee,
                hourType);
        entityManager.persist(hourType);
        entityManager.persist(expectedEmployee);
        entityManager.persist(dummyEmployee);
        entityManager.persist(dummyProject);
        entityManager.persist(expectedProject);
        entityManager.persist(dummyHours);
        entityManager.persist(expectedHours);
        entityManager.flush();

        List<Project> actual = projectQuery
                .withHoursFor(expectedEmployee)
                .list();

        assertEquals(asList(expectedProject), actual);
    }

    @Test
    public void testFetchManagers() throws NoSuchFieldException {
        Employee manager = new Employee("man", "fname", "lname", "email", "dept");
        Project expected = new Project();
        expected.addManager(manager);

        entityManager.persist(manager);
        entityManager.persist(expected);
        entityManager.flush();
        entityManager.clear();

        Project actual = projectQuery.id(expected.getId())
                .fetchManagers()
                .getSingleResult();

        Set<Employee> managers = (Set<Employee>) getField(actual, "managers");
        assertTrue(Hibernate.isInitialized(managers));
        assertTrue(managers.contains(manager));
    }

    @Test
    public void testFetchAccountableHours() throws NoSuchFieldException {
        HourType accountableHour = new HourType("typ");
        Project expected = new Project();
        expected.addAccountableHours(accountableHour);

        entityManager.persist(accountableHour);
        entityManager.persist(expected);
        entityManager.flush();
        entityManager.clear();

        Project actual = projectQuery.id(expected.getId())
                .fetchAccountableHours()
                .getSingleResult();

        Set<HourType> accountableHours = (Set<HourType>) getField(actual, "accountableHours");
        assertTrue(Hibernate.isInitialized(accountableHour));
        assertTrue(accountableHours.contains(accountableHour));
    }

    private ProjectRepository.ProjectQuery newProjectQuery() {
        return projectRepository.new ProjectQuery();
    }

}

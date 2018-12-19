package com.artezio.arttime.datamodel;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;
import java.util.HashMap;
import java.util.Map;

import static java.util.Arrays.asList;

public class ProjectUniqueConstraintTest {

    private EntityManager entityManager;
    private EntityManagerFactory entityManagerFactory;

    @Before
    public void setUp() throws Exception {
        Map<String, String> properties = new HashMap<>();
        properties.put("javax.persistence.validation.mode", "none");
        entityManagerFactory = Persistence.createEntityManagerFactory("test-pu", properties);
        entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
    }

    @After
    public void tearDown() throws Exception {
        if (entityManager.getTransaction().isActive()) {
            entityManager.getTransaction().rollback();
        }
        entityManagerFactory.close();
    }

    @Test
    public void testPersistProjects_uniqueCodes() {
        String code1 = "CODE1";
        String code2 = "CODE2";
        Employee manager = new Employee("manager", "fname", "lname", "email", "dept");
        HourType hourType = new HourType("ht");
        entityManager.persist(hourType);
        entityManager.persist(manager);
        Project project1 = createProject(code1, manager, hourType, new TeamFilter(TeamFilter.Type.DISABLED));
        Project project2 = createProject(code2, manager, hourType, new TeamFilter(TeamFilter.Type.DISABLED));
        entityManager.persist(project1);
        entityManager.persist(project2);
        entityManager.flush();
    }

    @Test(expected = PersistenceException.class)
    public void testPersistProjects_nonUniqueCodes_withoutMaster() {
        String nonUniqueCode = "CODE";
        Employee manager = new Employee("manager", "fname", "lname", "email", "dept");
        HourType hourType = new HourType("ht");
        entityManager.persist(hourType);
        entityManager.persist(manager);
        Project project1 = createProject(nonUniqueCode, manager, hourType, new TeamFilter(TeamFilter.Type.DISABLED));
        Project project2 = createProject(nonUniqueCode, manager, hourType, new TeamFilter(TeamFilter.Type.DISABLED));
        entityManager.persist(project1);
        entityManager.persist(project2);
        entityManager.flush();
    }

    @Test(expected = PersistenceException.class)
    public void testPersistProjects_nonUniqueCodes_sameMaster() {
        String nonUniqueCode = "CODE";
        String masterCode = "Master";
        Employee manager = new Employee("manager", "fname", "lname", "email", "dept");
        HourType hourType = new HourType("ht");
        entityManager.persist(hourType);
        entityManager.persist(manager);
        Project master = createProject(masterCode, manager, hourType, new TeamFilter(TeamFilter.Type.DISABLED));
        entityManager.persist(master);
        Project nonUnique1 = createProject(nonUniqueCode, master, manager, hourType, new TeamFilter(TeamFilter.Type.DISABLED));
        Project nonUnique2 = createProject(nonUniqueCode, master, manager, hourType, new TeamFilter(TeamFilter.Type.DISABLED));
        entityManager.flush();
        entityManager.persist(nonUnique1);
        entityManager.persist(nonUnique2);
        entityManager.flush();
    }

    @Test
    public void testPersistProjects_nonUniqueCodes_differentMasters() {
        String nonUniqueCode = "CODE";
        String master1Code = "Master1";
        String master2Code = "Master2";
        Employee manager = new Employee("manager", "fname", "lname", "email", "dept");
        HourType hourType = new HourType("ht");
        entityManager.persist(hourType);
        entityManager.persist(manager);
        Project master1 = createProject(master1Code, manager, hourType, new TeamFilter(TeamFilter.Type.DISABLED));
        Project master2 = createProject(master2Code, manager, hourType, new TeamFilter(TeamFilter.Type.DISABLED));
        entityManager.persist(master1);
        entityManager.persist(master2);
        entityManager.flush();
        Project nonUnique1 = createProject(nonUniqueCode, master1, manager, hourType, new TeamFilter(TeamFilter.Type.DISABLED));
        entityManager.persist(nonUnique1);
        entityManager.flush();
        Project nonUnique2 = createProject(nonUniqueCode, master2, manager, hourType, new TeamFilter(TeamFilter.Type.DISABLED));
        entityManager.persist(nonUnique2);
        entityManager.flush();
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


}

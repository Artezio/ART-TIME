package com.artezio.arttime.repositories;

import com.artezio.arttime.datamodel.Employee;
import com.artezio.arttime.datamodel.HourType;
import com.artezio.arttime.datamodel.Project;
import static com.artezio.arttime.datamodel.Project.Status.ACTIVE;
import com.artezio.arttime.datamodel.TeamFilter;
import com.artezio.arttime.filter.Filter;
import com.artezio.arttime.markers.IntegrationTest;
import com.artezio.arttime.services.ApplyHoursChangeException;
import com.artezio.arttime.services.HoursChange;
import java.util.Arrays;
import java.util.List;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.UserTransaction;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
@Category(IntegrationTest.class)
public class ProjectRepositoryIntegrationTest {

    @Inject
    private ProjectRepository projectRepository;
    @Inject
    private HoursRepository hoursRepository;
    @PersistenceContext
    private EntityManager entityManager;
    @Inject
    private UserTransaction utx;

    @Deployment
    public static Archive deploy() {
        return ShrinkWrap.create(WebArchive.class, "test.war")
                .addPackage("com.artezio.arttime.datamodel")
                .addPackage("com.artezio.arttime.web.criteria")
                .addPackage("com.artezio.arttime.utils")
                .addPackage("org.apache.commons.lang.time")
                .addPackage("org.apache.commons.lang.exception")
                .addPackage("org.apache.commons.lang")
                .addPackage("com.artezio.arttime.repositories")
                .addPackage("com.artezio.arttime.exceptions")
                .addPackages(true, "com.artezio.javax")
                .addClasses(Filter.class, ApplyHoursChangeException.class, HoursChange.class, IntegrationTest.class)
                .addAsResource("META-INF/arquillian-persistence.xml", "META-INF/persistence.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    private HourType testHourType = new HourType("Test");
    private Employee testEmployee = new Employee("manager", "test", "test", "test", "test");

    @Before
    public void setUp() throws Exception {
        utx.begin();
        entityManager.joinTransaction();
        entityManager.persist(testHourType);
        utx.commit();
    }

    @After
    public void tearDown() throws Exception {
        utx.begin();
        entityManager.joinTransaction();
        entityManager.createQuery("DELETE FROM Project").executeUpdate();
        entityManager.createQuery("DELETE FROM Employee").executeUpdate();
        entityManager.createQuery("DELETE FROM HourType").executeUpdate();
        utx.commit();
    }

    @Test
    @Ignore
    //TODO correct
    public void testCreateProjectWithSubprojects() {
        Project project = createTestProject("Test", false, null);
        Project subProject1 = createTestProject("TestSubPrj1", false, null);
        Project subProject2 = createTestProject("TestSubPrj2", false, null);
        List<Project> expected = Arrays.asList(subProject1, subProject2);
        Long projectId = projectRepository.create(project).getId();

//        List<Project> actual = projectRepository.findProject(projectId);
//        actual.forEach(subproject -> assertEquals(project, subproject.getMaster()));
    }

    private Project createTestProject(String code, boolean isSubproject, Project master) {
        Project project = new Project();
        project.setCode(code);
        project.setStatus(ACTIVE);
        project.setAccountableHours(Arrays.asList(testHourType));
        project.setManagers(Arrays.asList(testEmployee));
        project.setAllowEmployeeReportTime(true);
        TeamFilter teamFilter;
        if (isSubproject) {
            project.setMaster(master);
            teamFilter = new TeamFilter(TeamFilter.Type.BASED_ON_MASTER);
        } else {
            teamFilter = new TeamFilter(TeamFilter.Type.DISABLED);
        }
        project.setTeamFilter(teamFilter);
        return project;
    }

}

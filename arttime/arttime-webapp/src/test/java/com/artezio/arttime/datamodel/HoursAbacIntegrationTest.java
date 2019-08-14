package com.artezio.arttime.datamodel;

import com.artezio.arttime.filter.Filter;
import com.artezio.arttime.markers.IntegrationTest;
import com.artezio.arttime.services.ApplyHoursChangeException;
import com.artezio.arttime.services.HoursChange;
import com.artezio.arttime.test.utils.CalendarUtils;
import com.artezio.arttime.test.utils.security.SessionContextProducer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(Arquillian.class)
@Category(IntegrationTest.class)
public class HoursAbacIntegrationTest extends AbacIntegrationTest {
    @PersistenceContext(unitName = "secured-test-pu")
    private EntityManager abacEntityManager;

    @Deployment
    public static WebArchive deploy() {
        return ShrinkWrap.create(WebArchive.class, "test.war")
                .addPackage("com.artezio.arttime.datamodel")
                .addPackage("com.artezio.arttime.web.criteria")
                .addPackage("com.artezio.arttime.utils")
                .addPackage("com.artezio.arttime.repositories")
                .addPackages(true, "org.apache.commons")
                .addPackage("com.artezio.arttime.exceptions")
                .addAsLibraries(Maven.resolver().loadPomFromFile("pom.xml").resolve("com.artezio.javax.jpa:abac").withoutTransitivity().asSingleFile())
                .addClass(IntegrationTest.class)
                .addClasses(Filter.class, ApplyHoursChangeException.class, HoursChange.class, SessionContextProducer.class)
                .addPackages(true, "com.artezio.arttime.test.utils")
                .addAsResource("META-INF/arquillian-persistence.xml", "META-INF/persistence.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    private String department1 = "Dep1";
    private String department2 = "Dep2";
    private String department3 = "Dep3";

    private Project masterProject1;
    private Project masterProject2;
    private Project subProject1;
    private Project subProject2;
    private HourType hourType;

    private Employee projectManagerOfAllProjects;

    @Before
    public void setUp() throws Exception {
        runInNewTx.run(() -> {
            unsecuredEntityManager.joinTransaction();
            hourType = new HourType("HType");
            hourType.setActualTime(true);
            masterProject1 = createProject("Master1", hourType);
            subProject1 = createProject("Sub1", hourType);
            subProject1.setMaster(masterProject1);
            masterProject2 = createProject("Master2", hourType);
            subProject2 = createProject("Sub2", hourType);
            subProject2.setMaster(masterProject2);
            projectManagerOfAllProjects = new Employee("superPm", "superPmFirst", "last", "superemail", department3);
            masterProject1.addManager(projectManagerOfAllProjects);
            masterProject2.addManager(projectManagerOfAllProjects);
            subProject1.addManager(projectManagerOfAllProjects);
            subProject2.addManager(projectManagerOfAllProjects);
            unsecuredEntityManager.persist(hourType);
            unsecuredEntityManager.persist(projectManagerOfAllProjects);
            unsecuredEntityManager.persist(masterProject1);
            unsecuredEntityManager.persist(masterProject2);
            unsecuredEntityManager.persist(subProject1);
            unsecuredEntityManager.persist(subProject2);
        });
    }

    @After
    public void tearDown() throws Exception {
        runInNewTx.run(() -> {
            unsecuredEntityManager.joinTransaction();
            unsecuredEntityManager.createQuery("DELETE FROM Hours").executeUpdate();
            unsecuredEntityManager.createQuery("DELETE FROM Project").executeUpdate();
            unsecuredEntityManager.createQuery("DELETE FROM HourType").executeUpdate();
            unsecuredEntityManager
                    .createQuery("SELECT e FROM Employee e", Employee.class)
                    .getResultList()
                    .forEach(unsecuredEntityManager::remove);
        });
    }

    @Test
    public void testGetAll_viewTimesheetContext() throws Exception {
        Employee caller = createAnonymousEmployee(department1);
        Employee anotherEmployee = new Employee("another", "empx", "lx", "emx", department1);
        Hours hours1 = new Hours(masterProject1, CalendarUtils.getOffsetDate(0), caller, hourType);
        Hours hours2 = new Hours(subProject2, CalendarUtils.getOffsetDate(1), anotherEmployee, hourType);
        Hours hours3 = new Hours(masterProject2, CalendarUtils.getOffsetDate(3), caller, hourType);
        masterProject2.setAllowEmployeeReportTime(false);
        runInNewTx.run(() -> {
            unsecuredEntityManager.joinTransaction();
            masterProject2 = unsecuredEntityManager.merge(masterProject2);
            unsecuredEntityManager.persist(anotherEmployee);
            unsecuredEntityManager.persist(hours1);
            unsecuredEntityManager.persist(hours2);
            unsecuredEntityManager.persist(hours3);
        });
        List<Hours> actualList = callUsingAbacContext.callInViewTimesheetContext(() ->
                abacEntityManager.createQuery("SELECT h FROM Hours h", Hours.class).getResultList());
        assertFalse(actualList.isEmpty());
        assertTrue(actualList.contains(hours1));
        assertFalse(actualList.contains(hours2));
        assertTrue(actualList.contains(hours3));
    }

    @Test
    public void testGetAll_viewTimesheetContext_projectIsClosed_existReportedHours() throws Exception {
        Employee caller = createAnonymousEmployee(department1);
        Project closedProject = createProject("closedProject", hourType);
        closedProject.addManager(projectManagerOfAllProjects);
        closedProject.setStatus(Project.Status.CLOSED);
        Hours hours1 = new Hours(closedProject, CalendarUtils.getOffsetDate(0), caller, hourType);
        Hours hours2 = new Hours(closedProject, CalendarUtils.getOffsetDate(1), caller, hourType);
        runInNewTx.run(() -> {
            unsecuredEntityManager.joinTransaction();
            unsecuredEntityManager.persist(closedProject);
            unsecuredEntityManager.persist(hours1);
            unsecuredEntityManager.persist(hours2);
        });
        List<Hours> actualList = callUsingAbacContext.callInViewTimesheetContext(() ->
                abacEntityManager.createQuery("SELECT h FROM Hours h", Hours.class).getResultList());
        assertFalse(actualList.isEmpty());
        assertTrue(actualList.contains(hours1));
        assertTrue(actualList.contains(hours2));
    }

    @Test
    public void testGetAll_viewTimesheetContext_masterIsClosed_existReportedHours() throws Exception {
        Employee caller = createAnonymousEmployee(department1);
        Project closedMasterProject = createProject("closedProject", hourType);
        closedMasterProject.setStatus(Project.Status.CLOSED);
        closedMasterProject.addManager(projectManagerOfAllProjects);
        Project activeSubproject = createProject("subprojectt", hourType);
        activeSubproject.setStatus(Project.Status.ACTIVE);
        activeSubproject.addManager(projectManagerOfAllProjects);
        Hours hours1 = new Hours(closedMasterProject, CalendarUtils.getOffsetDate(0), caller, hourType);
        Hours hours2 = new Hours(activeSubproject, CalendarUtils.getOffsetDate(1), caller, hourType);
        runInNewTx.run(() -> {
            unsecuredEntityManager.joinTransaction();
            unsecuredEntityManager.persist(closedMasterProject);
            unsecuredEntityManager.persist(activeSubproject);
            unsecuredEntityManager.persist(hours1);
            unsecuredEntityManager.persist(hours2);
        });
        List<Hours> actualList = callUsingAbacContext.callInViewTimesheetContext(() ->
                abacEntityManager.createQuery("SELECT h FROM Hours h", Hours.class).getResultList());
        assertFalse(actualList.isEmpty());
        assertTrue(actualList.contains(hours1));
        assertTrue(actualList.contains(hours2));
    }

    @Test
    public void testGetAll_viewTimesheetContext_masterIsClosed_notExistReportedHours() throws Exception {
        Employee caller = createAnonymousEmployee(department1);
        Project closedMasterProject = createProject("closedProject", hourType);
        closedMasterProject.setStatus(Project.Status.CLOSED);
        closedMasterProject.addManager(projectManagerOfAllProjects);
        Project activeSubproject = createProject("subprojectt", hourType);
        activeSubproject.setStatus(Project.Status.ACTIVE);
        activeSubproject.addManager(projectManagerOfAllProjects);
        Project otherSubproject = createProject("othersubprojectt", hourType);
        otherSubproject.setStatus(Project.Status.ACTIVE);
        otherSubproject.addManager(projectManagerOfAllProjects);
        Hours hours1 = new Hours(otherSubproject, CalendarUtils.getOffsetDate(0), projectManagerOfAllProjects, hourType);
        runInNewTx.run(() -> {
            unsecuredEntityManager.joinTransaction();
            unsecuredEntityManager.persist(closedMasterProject);
            unsecuredEntityManager.persist(activeSubproject);
            unsecuredEntityManager.persist(otherSubproject);
            unsecuredEntityManager.persist(hours1);
        });
        List<Hours> actualList = callUsingAbacContext.callInViewTimesheetContext(() ->
                abacEntityManager.createQuery("SELECT h FROM Hours h", Hours.class).getResultList());
        assertTrue(actualList.isEmpty());
    }

    @Test
    public void testGetAll_viewTimesheetContext_projectIsClosed_masterIsActive_existReportedHours() throws Exception {
        Employee caller = createAnonymousEmployee(department1);
        Project activeMasterProject = createProject("activeProject", hourType);
        activeMasterProject.setStatus(Project.Status.ACTIVE);
        activeMasterProject.addManager(projectManagerOfAllProjects);
        Project closedSubproject = createProject("closedSubprojects", hourType);
        closedSubproject.addManager(projectManagerOfAllProjects);
        closedSubproject.setStatus(Project.Status.CLOSED);
        Hours hours1 = new Hours(activeMasterProject, CalendarUtils.getOffsetDate(0), caller, hourType);
        Hours hours2 = new Hours(closedSubproject, CalendarUtils.getOffsetDate(1), caller, hourType);
        runInNewTx.run(() -> {
            unsecuredEntityManager.joinTransaction();
            unsecuredEntityManager.persist(activeMasterProject);
            unsecuredEntityManager.persist(closedSubproject);
            unsecuredEntityManager.persist(hours1);
            unsecuredEntityManager.persist(hours2);
        });
        List<Hours> actualList = callUsingAbacContext.callInViewTimesheetContext(() ->
                abacEntityManager.createQuery("SELECT h FROM Hours h", Hours.class).getResultList());
        assertFalse(actualList.isEmpty());
        assertTrue(actualList.contains(hours1));
        assertTrue(actualList.contains(hours2));
    }

    @Test
    public void testGetAll_viewTimesheetContext_callerIsProjectManager() throws Exception {
        Employee caller = createAnonymousEmployee(department1);
        masterProject1.addManager(caller);
        Employee anotherEmployee = new Employee("another", "empx", "lx", "emx", department1);
        Hours hours1 = new Hours(masterProject1, CalendarUtils.getOffsetDate(0), anotherEmployee, hourType);
        Hours hours2 = new Hours(masterProject2, CalendarUtils.getOffsetDate(3), caller, hourType);
        runInNewTx.run(() -> {
            unsecuredEntityManager.joinTransaction();
            masterProject1 = unsecuredEntityManager.merge(masterProject1);
            unsecuredEntityManager.persist(anotherEmployee);
            unsecuredEntityManager.persist(hours1);
            unsecuredEntityManager.persist(hours2);
        });
        List<Hours> actualList = runAsProjectManager.call(() -> callUsingAbacContext.callInViewTimesheetContext(() ->
                abacEntityManager.createQuery("SELECT h FROM Hours h", Hours.class).getResultList()));
        assertFalse(actualList.isEmpty());
        assertFalse(actualList.contains(hours1));
        assertTrue(actualList.contains(hours2));
    }

    @Test
    public void testSave() throws Exception {
        Employee caller = createAnonymousEmployee(department1);
        Project disabledProject = createProject("disabled", hourType);
        Hours unapprovedHours = new Hours(masterProject1, CalendarUtils.getOffsetDate(0), caller, hourType);
        unapprovedHours.setQuantity(BigDecimal.ONE);
        Hours unapprovedHoursDisabledProject = new Hours(disabledProject, CalendarUtils.getOffsetDate(1), caller, hourType);
        unapprovedHoursDisabledProject.setQuantity(BigDecimal.ONE);
        Hours approvedHours = new Hours(subProject2, CalendarUtils.getOffsetDate(2), caller, hourType);
        approvedHours.setQuantity(BigDecimal.ONE);
        approvedHours.setApproved(true);
        disabledProject.addManager(projectManagerOfAllProjects);
        disabledProject.setAllowEmployeeReportTime(false);
        runInNewTx.run(() -> {
            unsecuredEntityManager.joinTransaction();
            unsecuredEntityManager.persist(disabledProject);
            unsecuredEntityManager.persist(approvedHours);
            unsecuredEntityManager.persist(unapprovedHours);
            unsecuredEntityManager.persist(unapprovedHoursDisabledProject);
        });
        runInNewTx.call(() -> {
            abacEntityManager.joinTransaction();
            abacEntityManager.createQuery("UPDATE Hours h SET h.comment = :comment")
                    .setParameter("comment", "comment")
                    .executeUpdate();
            return true;
        });
        List<Hours> changedHours = runInNewTx.call(() -> {
            unsecuredEntityManager.joinTransaction();
            return unsecuredEntityManager.createQuery("SELECT h FROM Hours h", Hours.class).getResultList();
        });
        assertFalse(changedHours.stream().anyMatch(h -> h.getComment() != null));
    }

    @Test
    public void testSave_reportTimeContext() throws Exception {
        Employee caller = createAnonymousEmployee(department1);
        Project disabledProject = createProject("disabled", hourType);
        Hours unapprovedHours = new Hours(masterProject1, CalendarUtils.getOffsetDate(0), caller, hourType);
        unapprovedHours.setQuantity(BigDecimal.ONE);
        Hours unapprovedHoursDisabledProject = new Hours(disabledProject, CalendarUtils.getOffsetDate(1), caller, hourType);
        unapprovedHoursDisabledProject.setQuantity(BigDecimal.ONE);
        Hours approvedHours = new Hours(subProject2, CalendarUtils.getOffsetDate(2), caller, hourType);
        approvedHours.setQuantity(BigDecimal.ONE);
        Hours unapprovedNotOwnedHours = new Hours(masterProject1, CalendarUtils.getOffsetDate(3), projectManagerOfAllProjects, hourType);
        unapprovedHours.setQuantity(BigDecimal.ONE);
        approvedHours.setApproved(true);
        disabledProject.addManager(projectManagerOfAllProjects);
        disabledProject.setAllowEmployeeReportTime(false);
        runInNewTx.run(() -> {
            unsecuredEntityManager.joinTransaction();
            unsecuredEntityManager.persist(disabledProject);
            unsecuredEntityManager.persist(approvedHours);
            unsecuredEntityManager.persist(unapprovedHours);
            unsecuredEntityManager.persist(unapprovedHoursDisabledProject);
            unsecuredEntityManager.persist(unapprovedNotOwnedHours);
        });
        callUsingAbacContext.callInReportTimeContext(() -> {
            runInNewTx.run(() -> {
                abacEntityManager.joinTransaction();
                abacEntityManager.createQuery("UPDATE Hours h SET h.comment = :comment")
                        .setParameter("comment", "comment")
                        .executeUpdate();
            });
            return true;
        });
        Hours changedUnapprovedHours = runInNewTx.call(() -> {
            unsecuredEntityManager.joinTransaction();
            return unsecuredEntityManager.find(Hours.class, unapprovedHours.getId());
        });
        Hours changedApprovedHours = runInNewTx.call(() -> {
            unsecuredEntityManager.joinTransaction();
            return unsecuredEntityManager.find(Hours.class, approvedHours.getId());
        });
        Hours changedUnapprovedHoursDisabledProject = runInNewTx.call(() -> {
            unsecuredEntityManager.joinTransaction();
            return unsecuredEntityManager.find(Hours.class, unapprovedHoursDisabledProject.getId());
        });
        Hours changedUnapprovedNotOwnedHours = runInNewTx.call(() -> {
            unsecuredEntityManager.joinTransaction();
            return unsecuredEntityManager.find(Hours.class, unapprovedNotOwnedHours.getId());
        });
        assertFalse(changedUnapprovedHours.getComment().isEmpty());
        assertNull(changedApprovedHours.getComment());
        assertNull(changedUnapprovedHoursDisabledProject.getComment());
        assertNull(changedUnapprovedNotOwnedHours.getComment());
    }

    @Test
    public void testGetAll() throws Exception {
        Employee caller = createAnonymousEmployee(department1);
        Hours hours = new Hours(masterProject1, CalendarUtils.getOffsetDate(0), caller, hourType);
        runInNewTx.run(() -> {
            unsecuredEntityManager.joinTransaction();
            unsecuredEntityManager.persist(hours);
        });
        List<Hours> actualHours = runInNewTx.call(() -> {
            abacEntityManager.joinTransaction();
            return abacEntityManager.createQuery("SELECT h FROM Hours h", Hours.class).getResultList();
        });
        assertTrue(actualHours.isEmpty());
    }

    @Test
    public void testGetAll_callerIsExec() throws Exception {
        Employee caller = createAnonymousEmployee(department1);
        Hours hours1 = new Hours(masterProject1, CalendarUtils.getOffsetDate(0), caller, hourType);
        Hours hours2 = new Hours(subProject2, CalendarUtils.getOffsetDate(1), projectManagerOfAllProjects, hourType);
        runInNewTx.run(() -> {
            unsecuredEntityManager.joinTransaction();
            unsecuredEntityManager.persist(hours1);
            unsecuredEntityManager.persist(hours2);
        });
        List<Hours> actualHours = runAsExec.call(() ->
                runInNewTx.call(() -> {
                    abacEntityManager.joinTransaction();
                    return abacEntityManager.createQuery("SELECT h FROM Hours h", Hours.class).getResultList();
                }));
        assertFalse(actualHours.isEmpty());
        assertTrue(actualHours.contains(hours1));
        assertTrue(actualHours.contains(hours2));
    }

    @Test
    public void testGetAll_callerIsProjectManagerOfMaster() throws Exception {
        Employee caller = createAnonymousEmployee(department1);
        masterProject1.addManager(caller);
        Hours masterHours = new Hours(masterProject1, CalendarUtils.getOffsetDate(0), projectManagerOfAllProjects, hourType);
        Hours subProjectHours = new Hours(subProject1, CalendarUtils.getOffsetDate(1), projectManagerOfAllProjects, hourType);
        Hours unexpectedHours1 = new Hours(masterProject2, CalendarUtils.getOffsetDate(2), projectManagerOfAllProjects, hourType);
        Hours unexpectedHours2 = new Hours(subProject2, CalendarUtils.getOffsetDate(3), projectManagerOfAllProjects, hourType);
        runInNewTx.run(() -> {
            unsecuredEntityManager.joinTransaction();
            masterProject1 = unsecuredEntityManager.merge(masterProject1);
            unsecuredEntityManager.persist(masterHours);
            unsecuredEntityManager.persist(subProjectHours);
            unsecuredEntityManager.persist(unexpectedHours1);
            unsecuredEntityManager.persist(unexpectedHours2);
        });
        List<Hours> actualHours = runAsProjectManager.call(() ->
                runInNewTx.call(() -> {
                    abacEntityManager.joinTransaction();
                    return abacEntityManager.createQuery("SELECT h FROM Hours h", Hours.class).getResultList();
                }));
        assertFalse(actualHours.isEmpty());
        assertTrue(actualHours.contains(masterHours));
        assertTrue(actualHours.contains(subProjectHours));
        assertFalse(actualHours.contains(unexpectedHours1));
        assertFalse(actualHours.contains(unexpectedHours2));
    }

    @Test
    public void testGetAll_callerIsProjectManagerOfSubproject() throws Exception {
        Employee caller = createAnonymousEmployee(department1);
        subProject1.addManager(caller);
        Hours unexpectedHours1 = new Hours(masterProject1, CalendarUtils.getOffsetDate(0), projectManagerOfAllProjects, hourType);
        Hours subProjectHours = new Hours(subProject1, CalendarUtils.getOffsetDate(1), projectManagerOfAllProjects, hourType);
        Hours unexpectedHours2 = new Hours(masterProject2, CalendarUtils.getOffsetDate(2), projectManagerOfAllProjects, hourType);
        Hours unexpectedHours3 = new Hours(subProject2, CalendarUtils.getOffsetDate(3), projectManagerOfAllProjects, hourType);
        runInNewTx.run(() -> {
            unsecuredEntityManager.joinTransaction();
            subProject1 = unsecuredEntityManager.merge(subProject1);
            unsecuredEntityManager.persist(subProjectHours);
            unsecuredEntityManager.persist(unexpectedHours1);
            unsecuredEntityManager.persist(unexpectedHours2);
            unsecuredEntityManager.persist(unexpectedHours3);
        });
        List<Hours> actualHours = runAsProjectManager.call(() ->
                runInNewTx.call(() -> {
                    abacEntityManager.joinTransaction();
                    return abacEntityManager.createQuery("SELECT h FROM Hours h", Hours.class).getResultList();
                }));
        assertFalse(actualHours.isEmpty());
        assertTrue(actualHours.contains(subProjectHours));
        assertFalse(actualHours.contains(unexpectedHours1));
        assertFalse(actualHours.contains(unexpectedHours2));
        assertFalse(actualHours.contains(unexpectedHours3));
    }

    @Test
    public void testGetAll_callerIsOfficeManager() throws Exception {
        Employee caller = createAnonymousEmployee(department1, new String[]{department3});
        Employee managedEmployee = createEmployee("managedEmployee", department3, new String[]{});
        Employee unmanagedEmployee = createEmployee("unManagedEmployee", department2, new String[]{});
        Hours expectedHours = new Hours(masterProject1, CalendarUtils.getOffsetDate(0), managedEmployee, hourType);
        Hours unexpectedHours = new Hours(masterProject1, CalendarUtils.getOffsetDate(0), unmanagedEmployee, hourType);
        runInNewTx.run(() -> {
            unsecuredEntityManager.joinTransaction();
            unsecuredEntityManager.persist(expectedHours);
            unsecuredEntityManager.persist(unexpectedHours);
        });
        List<Hours> actualHours = runAsOfficeManager.call(() ->
                runInNewTx.call(() -> {
                    abacEntityManager.joinTransaction();
                    return abacEntityManager.createQuery("SELECT h FROM Hours h", Hours.class).getResultList();
                }));
        assertFalse(actualHours.isEmpty());
        assertTrue(actualHours.contains(expectedHours));
        assertFalse(actualHours.contains(unexpectedHours));
    }

    @Test
    public void testGetAll_callerIsAccountant() throws Exception {
        Employee caller = createAnonymousEmployee(department1, new String[]{department3});
        Employee managedEmployee = createEmployee("managedEmployee", department3, new String[]{});
        Employee unmanagedEmployee = createEmployee("unManagedEmployee", department2, new String[]{});
        Hours expectedHours = new Hours(masterProject1, CalendarUtils.getOffsetDate(0), managedEmployee, hourType);
        Hours unexpectedHours = new Hours(masterProject1, CalendarUtils.getOffsetDate(0), unmanagedEmployee, hourType);
        runInNewTx.run(() -> {
            unsecuredEntityManager.joinTransaction();
            unsecuredEntityManager.persist(expectedHours);
            unsecuredEntityManager.persist(unexpectedHours);
        });
        List<Hours> actualHours = runAsAccountant.call(() ->
                runInNewTx.call(() -> {
                    abacEntityManager.joinTransaction();
                    return abacEntityManager.createQuery("SELECT h FROM Hours h", Hours.class).getResultList();
                }));
        assertFalse(actualHours.isEmpty());
        assertTrue(actualHours.contains(expectedHours));
        assertFalse(actualHours.contains(unexpectedHours));
    }

    protected Project createProject(String code, HourType... accountableHours) {
        Project project = new Project();
        project.setCode(code);
        project.setTeamFilter(new TeamFilter(TeamFilter.Type.DISABLED));
        project.setAccountableHours(Arrays.asList(accountableHours));
        project.setStatus(Project.Status.ACTIVE);
        return project;
    }

}

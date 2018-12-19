package com.artezio.arttime.datamodel;

import com.artezio.arttime.filter.Filter;
import com.artezio.arttime.markers.IntegrationTest;
import com.artezio.arttime.services.ApplyHoursChangeException;
import com.artezio.arttime.services.HoursChange;
import com.artezio.arttime.test.utils.security.SessionContextProducer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.PomEquippedResolveStage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
@Category(IntegrationTest.class)
public class WorkdaysCalendarAbacIntegrationTest extends AbacIntegrationTest {
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
                .addClasses(Filter.class, ApplyHoursChangeException.class, HoursChange.class, SessionContextProducer.class,
                        IntegrationTest.class)
                .addPackages(true, "com.artezio.arttime.test.utils")
                .addAsResource("META-INF/arquillian-persistence.xml", "META-INF/persistence.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    private String department1 = "Depart1";
    private String department2 = "Depart2";
    private WorkdaysCalendar notAssignedCalendar;
    private WorkdaysCalendar calendarAssignedToDepartment1;
    private WorkdaysCalendar calendarAssignedToDepartment2;

    @Before
    public void setUp() throws Exception {
        runInNewTx.run(() -> {
            unsecuredEntityManager.joinTransaction();
            notAssignedCalendar = new WorkdaysCalendar("Calendar1");
            calendarAssignedToDepartment1 = new WorkdaysCalendar("Calendar2");
            calendarAssignedToDepartment1.setDepartments(new HashSet<>(Arrays.asList(department1)));
            calendarAssignedToDepartment2 = new WorkdaysCalendar("Calendar3");
            calendarAssignedToDepartment2.setDepartments(new HashSet<>(Arrays.asList(department2)));
            unsecuredEntityManager.persist(notAssignedCalendar);
            unsecuredEntityManager.persist(calendarAssignedToDepartment1);
            unsecuredEntityManager.persist(calendarAssignedToDepartment2);
            unsecuredEntityManager.flush();
        });
    }

    @After
    public void tearDown() throws Exception {
        runInNewTx.run(() -> {
            unsecuredEntityManager.joinTransaction();
            unsecuredEntityManager
                    .createQuery("SELECT e FROM Employee e", Employee.class)
                    .getResultList()
                    .forEach(unsecuredEntityManager::remove);
            unsecuredEntityManager
                    .createQuery("SELECT c FROM WorkdaysCalendar c", WorkdaysCalendar.class)
                    .getResultList()
                    .forEach(unsecuredEntityManager::remove);
        });
    }

    @Test
    public void testGetAll() throws Exception {
        Employee caller = createAnonymousEmployee(department1);
        List<WorkdaysCalendar> actualList = abacEntityManager.createQuery("SELECT c FROM WorkdaysCalendar c", WorkdaysCalendar.class).getResultList();
        assertTrue(actualList.contains(notAssignedCalendar));
        assertTrue(actualList.contains(calendarAssignedToDepartment1));
        assertTrue(actualList.contains(calendarAssignedToDepartment2));
    }

    @Test
    public void testGetAll_manageCalendarsContext() throws Exception {
        Employee caller = createAnonymousEmployee(department1);
        List<WorkdaysCalendar> actualList = runInNewTx.call(() ->
                callUsingAbacContext.callInManageCalendarsContext(() ->
                        abacEntityManager.createQuery("SELECT c FROM WorkdaysCalendar c", WorkdaysCalendar.class).getResultList()));
        assertTrue(actualList.isEmpty());
    }

    @Test
    public void testGetAll_manageCalendarsContext_callerIsExec() throws Exception {
        Employee caller = createAnonymousEmployee(department1);
        List<WorkdaysCalendar> actualList = runAsExec.call(() ->
                runInNewTx.call(() ->
                        callUsingAbacContext.callInManageCalendarsContext(() ->
                                abacEntityManager.createQuery("SELECT c FROM WorkdaysCalendar c", WorkdaysCalendar.class).getResultList())));
        assertFalse(actualList.isEmpty());
        assertTrue(actualList.contains(notAssignedCalendar));
        assertTrue(actualList.contains(calendarAssignedToDepartment1));
        assertTrue(actualList.contains(calendarAssignedToDepartment2));
    }

    @Test
    public void testGetAll_manageCalendarsContext_callerIsOfficeManager() throws Exception {
        Employee caller = createAnonymousEmployee(department1, new String[]{department1});
        WorkdaysCalendar calendarOnlyForDepartment1 = new WorkdaysCalendar("wc_only1");
        WorkdaysCalendar calendarForBothDepartments = new WorkdaysCalendar("wc_1and2");
        WorkdaysCalendar calendarOnlyForDepartment2 = new WorkdaysCalendar("wc_only2");
        calendarOnlyForDepartment1.setDepartments(new HashSet<>(Arrays.asList(department1)));
        calendarOnlyForDepartment2.setDepartments(new HashSet<>(Arrays.asList(department2)));
        calendarForBothDepartments.setDepartments(new HashSet<>(Arrays.asList(department1, department2)));
        runInNewTx.run(() -> {
            unsecuredEntityManager.joinTransaction();
            unsecuredEntityManager.persist(calendarOnlyForDepartment1);
            unsecuredEntityManager.persist(calendarForBothDepartments);
            unsecuredEntityManager.flush();
        });
        List<WorkdaysCalendar> actualList = runInNewTx.call(() ->
                runAsOfficeManager.call(() ->
                        callUsingAbacContext.callInManageCalendarsContext(() ->
                                abacEntityManager.createQuery("SELECT c FROM WorkdaysCalendar c", WorkdaysCalendar.class).getResultList())));
        System.out.println("Actual calendars:");
        actualList.forEach(calendar -> System.out.format("Cal %s deps %s%n", calendar.getName(), calendar.getDepartments()));

        Employee loadedCaller = runInNewTx.call(() -> {
            unsecuredEntityManager.joinTransaction();
            return unsecuredEntityManager.createQuery("SELECT e FROM Employee e LEFT JOIN FETCH e.accessibleDepartments ac WHERE e.userName=:userName",Employee.class).setParameter("userName", caller.getUserName()).getSingleResult();
        });
        System.out.format("Caller was username = %s, access = %s%n", loadedCaller.getUserName(), loadedCaller.getAccessibleDepartments());

        assertFalse(actualList.isEmpty());
        assertTrue(actualList.contains(notAssignedCalendar));
        assertTrue(actualList.contains(calendarOnlyForDepartment1));
        assertTrue(actualList.contains(calendarForBothDepartments));
        assertFalse(actualList.contains(calendarOnlyForDepartment2));
    }

}

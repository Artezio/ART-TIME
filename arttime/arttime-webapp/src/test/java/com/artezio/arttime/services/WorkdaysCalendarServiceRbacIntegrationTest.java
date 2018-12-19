package com.artezio.arttime.services;

import com.artezio.arttime.config.ApplicationSettings;
import com.artezio.arttime.config.Setting;
import com.artezio.arttime.config.Settings;
import com.artezio.arttime.datamodel.Employee;
import com.artezio.arttime.datamodel.WorkdaysCalendar;
import com.artezio.arttime.filter.Filter;
import com.artezio.arttime.markers.IntegrationTest;
import com.artezio.arttime.services.integration.DepartmentTrackingSystem;
import com.artezio.arttime.services.integration.EmployeeTrackingSystem;
import com.artezio.arttime.services.integration.TrackingSystem;
import com.artezio.arttime.test.utils.RunInNewTx;
import com.artezio.arttime.test.utils.security.SessionContextProducer;
import com.artezio.arttime.test.utils.security.runas.*;
import com.artezio.arttime.utils.CalendarUtils;
import com.artezio.arttime.web.criteria.RangePeriodSelector;
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
import org.mockito.Mockito;

import javax.ejb.EJBAccessException;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Arrays;

@RunWith(Arquillian.class)
@Category(IntegrationTest.class)
public class WorkdaysCalendarServiceRbacIntegrationTest {
    @Inject
    private RunAsProjectManager runAsProjectManager;
    @Inject
    private RunAsAccountant runAsAccountant;
    @Inject
    private RunAsOfficeManager runAsOfficeManager;
    @Inject
    private RunAsIntegrationClient runAsIntegrationClient;
    @Inject
    private RunAsSystem runAsSystem;
    @Inject
    private RunAsAdmin runAsAdmin;
    @Inject
    private RunAsExec runAsExec;
    @Inject
    private RunInNewTx runInNewTx;
    @PersistenceContext(unitName = "test-pu")
    private EntityManager entityManager;
    @Inject
    private WorkdaysCalendarService workdaysCalendarService;

    private WorkdaysCalendar workdaysCalendar;

    @Deployment
    public static WebArchive deploy() {
        PomEquippedResolveStage resolveStage = Maven.configureResolver().workOffline(false).loadPomFromFile("pom.xml");
        return ShrinkWrap.create(WebArchive.class, "test.war")
                .addClass(RangePeriodSelector.class)
                .addClass(CalendarUtils.class)
                .addClass(Settings.class)
                .addClass(Setting.class)
                .addClass(ApplicationSettings.class)
                .addPackages(true, "com.artezio.arttime.repositories")
                .addClass(EmployeeService.class)
                .addClass(WorkdaysCalendarService.class)
                .addClass(SettingsService.class)
                .addClass(TrackingSystem.class)
                .addClass(DepartmentTrackingSystem.class)
                .addClass(EmployeeTrackingSystem.class)
                .addClass(IntegrationTest.class)
                .addPackage("com.artezio.arttime.datamodel")
                .addPackages(true, "org.apache.commons")
                .addPackage("com.artezio.arttime.exceptions")
                .addPackages(true, "com.artezio.arttime.test.utils")
                .addClasses(Filter.class, ApplyHoursChangeException.class, HoursChange.class, SessionContextProducer.class)
                .addAsLibraries(resolveStage.resolve("com.artezio.javax.jpa:abac").withoutTransitivity().asSingleFile())
                .addAsLibraries(resolveStage.resolve("org.mockito:mockito-core:1.10.19").withTransitivity().asFile())
                .addAsLibraries(resolveStage.resolve("junit-addons:junit-addons:1.4").withTransitivity().asFile())
                .addAsResource("META-INF/arquillian-persistence.xml", "META-INF/persistence.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Produces
    public DepartmentTrackingSystem departmentTrackingSystem() {
        return Mockito.mock(DepartmentTrackingSystem.class);
    }

    @Before
    public void setUp() throws Exception {
        workdaysCalendar = new WorkdaysCalendar("Calendar");
        runInNewTx.run(() -> {
            entityManager.joinTransaction();
            entityManager.persist(workdaysCalendar);
        });
    }

    @After
    public void tearDown() throws Exception {
        runInNewTx.run(() -> {
            entityManager.joinTransaction();
            entityManager.createQuery("DELETE from WorkdaysCalendar ").executeUpdate();
        });
    }

    @Test(expected = EJBAccessException.class)
    public void testUpdate() throws Exception {
        workdaysCalendarService.update(workdaysCalendar);
    }

    @Test(expected = EJBAccessException.class)
    public void testUpdate_userIsAdmin() throws Exception {
        runAsAdmin.call(() -> workdaysCalendarService.update(workdaysCalendar));
    }

    @Test
    public void testUpdate_userIsExec() throws Exception {
        runAsExec.call(() -> workdaysCalendarService.update(workdaysCalendar));
    }

    @Test(expected = EJBAccessException.class)
    public void testUpdate_userIsPM() throws Exception {
        runAsProjectManager.call(() -> workdaysCalendarService.update(workdaysCalendar));
    }

    @Test(expected = EJBAccessException.class)
    public void testUpdate_userIsIntegrationClient() throws Exception {
        runAsIntegrationClient.call(() -> workdaysCalendarService.update(workdaysCalendar));
    }

    @Test(expected = EJBAccessException.class)
    public void testUpdate_userIsAccountant() throws Exception {
        runAsAccountant.call(() -> workdaysCalendarService.update(workdaysCalendar));
    }

    @Test(expected = EJBAccessException.class)
    public void testUpdate_userIsSystem() throws Exception {
        runAsSystem.call(() -> workdaysCalendarService.update(workdaysCalendar));
    }

    @Test
    public void testUpdate_userIsOfficeManager() throws Exception {
        runAsOfficeManager.call(() -> workdaysCalendarService.update(workdaysCalendar));
    }

    @Test(expected = EJBAccessException.class)
    public void testCreate() throws Exception {
        create();
    }

    @Test(expected = EJBAccessException.class)
    public void testCreate_userIsAdmin() throws Exception {
        runAsAdmin.call(this::create);
    }

    @Test
    public void testCreate_userIsExec() throws Exception {
        runAsExec.call(this::create);
    }

    @Test(expected = EJBAccessException.class)
    public void testCreate_userIsPM() throws Exception {
        runAsProjectManager.call(this::create);
    }

    @Test(expected = EJBAccessException.class)
    public void testCreate_userIsIntegrationClient() throws Exception {
        runAsIntegrationClient.call(this::create);
    }

    @Test(expected = EJBAccessException.class)
    public void testCreate_userIsAccountant() throws Exception {
        runAsAccountant.call(this::create);
    }

    @Test(expected = EJBAccessException.class)
    public void testCreate_userIsSystem() throws Exception {
        runAsSystem.call(this::create);
    }

    @Test
    public void testCreate_userIsOfficeManager() throws Exception {
        runAsOfficeManager.call(this::create);
    }

    private WorkdaysCalendar create() {
        return workdaysCalendarService.create(new WorkdaysCalendar("NewCalendar"));
    }

    @Test(expected = EJBAccessException.class)
    public void testRemove() throws Exception {
        remove();
    }

    @Test(expected = EJBAccessException.class)
    public void testRemove_userIsAdmin() throws Exception {
        runAsAdmin.call(this::remove);
    }

    @Test
    public void testRemove_userIsExec() throws Exception {
        runAsExec.call(this::remove);
    }

    @Test(expected = EJBAccessException.class)
    public void testRemove_userIsPM() throws Exception {
        runAsProjectManager.call(this::remove);
    }

    @Test(expected = EJBAccessException.class)
    public void testRemove_userIsIntegrationClient() throws Exception {
        runAsIntegrationClient.call(this::remove);
    }

    @Test(expected = EJBAccessException.class)
    public void testRemove_userIsAccountant() throws Exception {
        runAsAccountant.call(this::remove);
    }

    @Test(expected = EJBAccessException.class)
    public void testRemove_userIsSystem() throws Exception {
        runAsSystem.call(this::remove);
    }

    @Test
    public void testRemove_userIsOfficeManager() throws Exception {
        runAsOfficeManager.call(this::remove);
    }

    private boolean remove() {
        workdaysCalendarService.remove(workdaysCalendar);
        return true;
    }

    @Test(expected = EJBAccessException.class)
    public void testGetAll() throws Exception {
        getAll();
    }

    @Test(expected = EJBAccessException.class)
    public void testGetAll_userIsAdmin() throws Exception {
        runAsAdmin.call(this::getAll);
    }

    @Test(expected = EJBAccessException.class)
    public void testGetAll_userIsExec() throws Exception {
        runAsExec.call(this::getAll);
    }

    @Test(expected = EJBAccessException.class)
    public void testGetAll_userIsPM() throws Exception {
        runAsProjectManager.call(this::getAll);
    }

    @Test
    public void testGetAll_userIsIntegrationClient() throws Exception {
        runAsIntegrationClient.call(this::getAll);
    }

    @Test(expected = EJBAccessException.class)
    public void testGetAll_userIsAccountant() throws Exception {
        runAsAccountant.call(this::getAll);
    }

    @Test(expected = EJBAccessException.class)
    public void testGetAll_userIsSystem() throws Exception {
        runAsSystem.call(this::getAll);
    }

    @Test(expected = EJBAccessException.class)
    public void testGetAll_userIsOfficeManager() throws Exception {
        runAsOfficeManager.call(this::getAll);
    }

    private Object getAll() {
        return workdaysCalendarService.getAll();
    }

    @Test(expected = EJBAccessException.class)
    public void testGetCalendarsForManaging() throws Exception {
        getCalendarsForManaging();
    }

    @Test(expected = EJBAccessException.class)
    public void testGetCalendarsForManaging_userIsAdmin() throws Exception {
        runAsAdmin.call(this::getCalendarsForManaging);
    }

    @Test
    public void testGetCalendarsForManaging_userIsExec() throws Exception {
        runAsExec.call(this::getCalendarsForManaging);
    }

    @Test(expected = EJBAccessException.class)
    public void testGetCalendarsForManaging_userIsPM() throws Exception {
        runAsProjectManager.call(this::getCalendarsForManaging);
    }

    @Test(expected = EJBAccessException.class)
    public void testGetCalendarsForManaging_userIsIntegrationClient() throws Exception {
        runAsIntegrationClient.call(this::getCalendarsForManaging);
    }

    @Test(expected = EJBAccessException.class)
    public void testGetCalendarsForManaging_userIsAccountant() throws Exception {
        runAsAccountant.call(this::getCalendarsForManaging);
    }

    @Test(expected = EJBAccessException.class)
    public void testGetCalendarsForManaging_userIsSystem() throws Exception {
        runAsSystem.call(this::getCalendarsForManaging);
    }

    @Test
    public void testGetCalendarsForManaging_userIsOfficeManager() throws Exception {
        runAsOfficeManager.call(this::getCalendarsForManaging);
    }

    private Object getCalendarsForManaging() {
        return workdaysCalendarService.getCalendarsForManaging();
    }

    @Test(expected = EJBAccessException.class)
    public void testGetCalendarsByDepartments() {
        getCalendarsByDepartments();
    }

    @Test(expected = EJBAccessException.class)
    public void testGetCalendarsByDepartments_userIsAdmin() throws Exception {
        runAsAdmin.call(this::getCalendarsByDepartments);
    }

    @Test
    public void testGetCalendarsByDepartments_userIsExec() throws Exception {
        runAsExec.call(this::getCalendarsByDepartments);
    }

    @Test(expected = EJBAccessException.class)
    public void testGetCalendarsByDepartments_userIsPM() throws Exception {
        runAsProjectManager.call(this::getCalendarsByDepartments);
    }

    @Test(expected = EJBAccessException.class)
    public void testGetCalendarsByDepartments_userIsIntegrationClient() throws Exception {
        runAsIntegrationClient.call(this::getCalendarsByDepartments);
    }

    @Test(expected = EJBAccessException.class)
    public void testGetCalendarsByDepartments_userIsAccountant() throws Exception {
        runAsAccountant.call(this::getCalendarsByDepartments);
    }

    @Test
    public void testGetCalendarsByDepartments_userIsSystem() throws Exception {
        runAsSystem.call(this::getCalendarsByDepartments);
    }

    @Test
    public void testGetCalendarsByDepartments_userIsOfficeManager() throws Exception {
        runAsOfficeManager.call(this::getCalendarsByDepartments);
    }

    private Object getCalendarsByDepartments() {
        return workdaysCalendarService.getCalendarsByDepartments();
    }

    @Test
    public void testGetDays() throws Exception {
        getDays();
    }

    @Test
    public void testGetDays_userIsAdmin() throws Exception {
        runAsAdmin.call(this::getDays);
    }

    @Test
    public void testGetDays_userIsExec() throws Exception {
        runAsExec.call(this::getDays);
    }

    @Test
    public void testGetDays_userIsPM() throws Exception {
        runAsProjectManager.call(this::getDays);
    }

    @Test
    public void testGetDays_userIsIntegrationClient() throws Exception {
        runAsIntegrationClient.call(this::getDays);
    }

    @Test
    public void testGetDays_userIsAccountant() throws Exception {
        runAsAccountant.call(this::getDays);
    }

    @Test
    public void testGetDays_userIsSystem() throws Exception {
        runAsSystem.call(this::getDays);
    }

    @Test
    public void testGetDays_userIsOfficeManager() throws Exception {
        runAsOfficeManager.call(this::getDays);
    }

    private Object getDays() {
        return workdaysCalendarService.getDays(workdaysCalendar, com.artezio.arttime.test.utils.CalendarUtils.createPeriod(1, 3));
    }

    @Test
    public void testFindById() throws Exception {
        findById();
    }

    @Test
    public void testFindById_userIsAdmin() throws Exception {
        runAsAdmin.call(this::findById);
    }

    @Test
    public void testFindById_userIsExec() throws Exception {
        runAsExec.call(this::findById);
    }

    @Test
    public void testFindById_userIsPM() throws Exception {
        runAsProjectManager.call(this::findById);
    }

    @Test
    public void testFindById_userIsIntegrationClient() throws Exception {
        runAsIntegrationClient.call(this::findById);
    }

    @Test
    public void testFindById_userIsAccountant() throws Exception {
        runAsAccountant.call(this::findById);
    }

    @Test
    public void testFindById_userIsSystem() throws Exception {
        runAsSystem.call(this::findById);
    }

    @Test
    public void testFindById_userIsOfficeManager() throws Exception {
        runAsOfficeManager.call(this::findById);
    }

    private Object findById() {
        return workdaysCalendarService.findById(workdaysCalendar.getId());
    }
}

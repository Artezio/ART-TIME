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
import com.artezio.arttime.services.integration.TeamTrackingSystem;
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

@RunWith(Arquillian.class)
@Category(IntegrationTest.class)
public class DepartmentServiceRbacIntegrationTest {
    @Inject
    private RunAsProjectManager runAsProjectManager;
    @Inject
    private RunAsAccountant runAsAccountant;
    @Inject
    private RunAsOfficeManager runAsOfficeManager;
    @Inject
    private RunAsIntegrationClient runAsIntegrationClient;
    @Inject
    private RunAsAdmin runAsAdmin;
    @Inject
    private RunAsExec runAsExec;
    @Inject
    private RunAsSystem runAsSystem;
    @Inject
    private RunInNewTx runInNewTx;
    @PersistenceContext(unitName = "test-pu")
    private EntityManager entityManager;
    @Inject
    private DepartmentService departmentService;

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
                .addClass(DepartmentService.class)
                .addClass(EmployeeService.class)
                .addClass(WorkdaysCalendarService.class)
                .addClass(SettingsService.class)
                .addClass(EmployeeTrackingSystem.class)
                .addClass(TeamTrackingSystem.class)
                .addClass(DepartmentTrackingSystem.class)
                .addClass(TrackingSystem.class)
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
            entityManager.remove(entityManager.find(WorkdaysCalendar.class, workdaysCalendar.getId()));
        });
    }

    @Test(expected = EJBAccessException.class)
    public void testGetAll() {
        departmentService.getAll();
    }

    @Test(expected = EJBAccessException.class)
    public void testGetAll_userIsAdmin_disallowInvocation() throws Exception {
        runAsAdmin.call(() -> departmentService.getAll());
    }

    @Test
    public void testGetAll_userIsExec_allowInvocation() throws Exception {
        runAsExec.call(() -> departmentService.getAll());
    }

    @Test
    public void testGetAll_userIsPM_allowInvocation() throws Exception {
        runAsProjectManager.call(() -> departmentService.getAll());
    }

    @Test(expected = EJBAccessException.class)
    public void testGetAll_userIsIntegrationClient_disallowInvocation() throws Exception {
        runAsIntegrationClient.call(() -> departmentService.getAll());
    }

    @Test
    public void testGetAll_userIsAccountant_allowInvocation() throws Exception {
        runAsAccountant.call(() -> departmentService.getAll());
    }

    @Test
    public void testGetAll_userIsOfficeManager_allowInvocation() throws Exception {
        Employee officeManager = new Employee("anonymous", "om1", "om2", "email", "dept");
        runInNewTx.run(() -> {
            entityManager.joinTransaction();
            entityManager.persist(officeManager);
        });
        runAsOfficeManager.call(() -> departmentService.getAll());
    }

    @Test(expected = EJBAccessException.class)
    public void testGetAll_userIsSystem_disallowInvocation() throws Exception {
        runAsSystem.call(() -> departmentService.getAll());
    }

    private boolean setCalendarToDepartment() {
        departmentService.setCalendarToDepartment("departmn1", workdaysCalendar);
        return true;
    }

    @Test(expected = EJBAccessException.class)
    public void testSetCalendarToDepartment() {
        departmentService.setCalendarToDepartment("depart1", new WorkdaysCalendar());
    }

    @Test(expected = EJBAccessException.class)
    public void testSetCalendarToDepartment_userIsAdmin_disallowInvocation() throws Exception {
        runAsAdmin.call(this::setCalendarToDepartment);
    }

    @Test
    public void testSetCalendarToDepartment_userIsExec_allowInvocation() throws Exception {
        runAsExec.call(this::setCalendarToDepartment);
    }

    @Test(expected = EJBAccessException.class)
    public void testSetCalendarToDepartment_userIsPM_disallowInvocation() throws Exception {
        runAsProjectManager.call(this::setCalendarToDepartment);
    }

    @Test(expected = EJBAccessException.class)
    public void testSetCalendarToDepartment_userIsIntegrationClient_disallowInvocation() throws Exception {
        runAsIntegrationClient.call(this::setCalendarToDepartment);
    }

    @Test(expected = EJBAccessException.class)
    public void testSetCalendarToDepartment_userIsAccountant_disallowInvocation() throws Exception {
        runAsAccountant.call(this::setCalendarToDepartment);
    }

    @Test
    public void testSetCalendarToDepartment_userIsOfficeManager_disallowInvocation() throws Exception {
        runAsOfficeManager.call(this::setCalendarToDepartment);
    }

    @Test(expected = EJBAccessException.class)
    public void testSetCalendarToDepartment_userIsSystem_disallowInvocation() throws Exception {
        runAsSystem.call(this::setCalendarToDepartment);
    }

}

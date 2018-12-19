package com.artezio.arttime.services;

import com.artezio.arttime.config.ApplicationSettings;
import com.artezio.arttime.config.Setting;
import com.artezio.arttime.config.Settings;
import com.artezio.arttime.datamodel.*;
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
import java.math.BigDecimal;
import java.util.Arrays;

@RunWith(Arquillian.class)
@Category(IntegrationTest.class)
public class HoursServiceRbacIntegrationTest {
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
    private HoursService hoursService;

    private HourType hourType;
    private Project project;
    private Employee employee;
    private Hours hours;

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
                .addClass(HoursService.class)
                .addClass(HourTypeService.class)
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
        hourType = new HourType("Typ");
        employee = new Employee("uname", "fname", "lname", "email", "dep1");
        project = new Project();
        project.setCode("CODE");
        project.addManager(employee);
        project.setTeamFilter(new TeamFilter(TeamFilter.Type.DISABLED));
        project.addAccountableHours(hourType);
        hours = new Hours(project, com.artezio.arttime.test.utils.CalendarUtils.getOffsetDate(0), employee, hourType);
        runInNewTx.run(() -> {
            entityManager.joinTransaction();
            entityManager.persist(hourType);
            entityManager.persist(employee);
            entityManager.persist(project);
            entityManager.persist(hours);
        });
    }

    @After
    public void tearDown() throws Exception {
        runInNewTx.run(() -> {
            entityManager.joinTransaction();
            Hours persistedHours = entityManager.find(Hours.class, hours.getId());
            if (persistedHours != null && persistedHours.getId() != null) {
                entityManager.remove(persistedHours);
            }
            entityManager.remove(entityManager.find(Project.class, project.getId()));
            entityManager.remove(entityManager.find(Employee.class, employee.getUserName()));
            entityManager.remove(entityManager.find(HourType.class, hourType.getId()));
        });
    }

    private boolean manageHours() throws ReflectiveOperationException {
        hoursService.manageHours(Arrays.asList(hours));
        return true;
    }

    @Test(expected = EJBAccessException.class)
    public void testManageHours() throws Exception {
        manageHours();
    }

    @Test(expected = EJBAccessException.class)
    public void testManageHours_userIsAdmin() throws Exception {
        runAsAdmin.call(this::manageHours);
    }

    @Test
    public void testManageHours_userIsExec() throws Exception {
        runAsExec.call(this::manageHours);
    }

    @Test
    public void testManageHours_userIsPM() throws Exception {
        runAsProjectManager.call(this::manageHours);
    }

    @Test(expected = EJBAccessException.class)
    public void testManageHours_userIsIntegrationClient() throws Exception {
        runAsIntegrationClient.call(this::manageHours);
    }

    @Test(expected = EJBAccessException.class)
    public void testManageHours_userIsAccountant() throws Exception {
        runAsAccountant.call(this::manageHours);
    }

    @Test
    public void testManageHours_userIsOfficeManager() throws Exception {
        runAsOfficeManager.call(this::manageHours);
    }

    @Test(expected = EJBAccessException.class)
    public void testManageHours_userIsSystem() throws Exception {
        runAsSystem.call(this::manageHours);
    }

    private boolean applyChange() throws ApplyHoursChangeException {
        HoursChange change = new HoursChange(project.getCode(), hours.getDate(), employee.getUserName(), hourType.getId());
        change.setQuantityDelta(BigDecimal.ONE);
        hoursService.apply(Arrays.asList(change));
        return true;
    }

    @Test(expected = EJBAccessException.class)
    public void testApply() throws Exception {
        applyChange();
    }

    @Test(expected = EJBAccessException.class)
    public void testApply_userIsAdmin() throws Exception {
        runAsAdmin.call(this::applyChange);
    }

    @Test(expected = EJBAccessException.class)
    public void testApply_userIsExec() throws Exception {
        runAsExec.call(this::applyChange);
    }

    @Test(expected = EJBAccessException.class)
    public void testApply_userIsPM() throws Exception {
        runAsProjectManager.call(this::applyChange);
    }

    @Test
    public void testApply_userIsIntegrationClient() throws Exception {
        runAsIntegrationClient.call(this::applyChange);
    }

    @Test(expected = EJBAccessException.class)
    public void testApply_userIsAccountant() throws Exception {
        runAsAccountant.call(this::applyChange);
    }

    @Test(expected = EJBAccessException.class)
    public void testApply_userIsOfficeManager() throws Exception {
        runAsOfficeManager.call(this::applyChange);
    }

    @Test(expected = EJBAccessException.class)
    public void testApply_userIsSystem() throws Exception {
        runAsSystem.call(this::applyChange);
    }

}

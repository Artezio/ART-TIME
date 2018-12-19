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

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

@RunWith(Arquillian.class)
@Category(IntegrationTest.class)
public class EmployeeServiceRbacIntegrationTest {
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
    private EmployeeService employeeService;

    private WorkdaysCalendar workdaysCalendar;
    private Employee employee;

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
                .addPackages(true, "com.artezio.arttime.test.utils")
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
        employee = new Employee("uname", "fname", "lname", "email", "department1");
        employee.setCalendar(workdaysCalendar);
        runInNewTx.run(() -> {
            entityManager.joinTransaction();
            entityManager.persist(workdaysCalendar);
            entityManager.persist(employee);
        });
    }

    @After
    public void tearDown() throws Exception {
        runInNewTx.run(() -> {
            entityManager.joinTransaction();
            entityManager.remove(entityManager.find(Employee.class, employee.getUserName()));
            entityManager.remove(entityManager.find(WorkdaysCalendar.class, workdaysCalendar.getId()));
        });
    }

    @Test(expected = EJBAccessException.class)
    public void testFind() {
        employeeService.find(employee.getUserName());
    }

    @Test(expected = EJBAccessException.class)
    public void testFind_userIsAdmin() throws Exception {
        runAsAdmin.call(() -> {
            employeeService.find(employee.getUserName());
            return null;
        });
    }

    @Test
    public void testFind_userIsExec() throws Exception {
        runAsExec.call(() -> {
            employeeService.find(employee.getUserName());
            return null;
        });
    }

    @Test(expected = EJBAccessException.class)
    public void testFind_userIsProjectManager() throws Exception {
        runAsProjectManager.call(() -> {
            employeeService.find(employee.getUserName());
            return null;
        });
    }

    @Test(expected = EJBAccessException.class)
    public void testFind_userIsAccountant() throws Exception {
        runAsAccountant.call(() -> {
            employeeService.find(employee.getUserName());
            return null;
        });
    }

    @Test
    public void testFind_userIsOfficeManager() throws Exception {
        runAsOfficeManager.call(() -> {
            employeeService.find(employee.getUserName());
            return null;
        });
    }

    @Test(expected = EJBAccessException.class)
    public void testFind_userIsIntegrationClient() throws Exception {
        runAsIntegrationClient.call(() -> {
            employeeService.find(employee.getUserName());
            return null;
        });
    }

    @Test(expected = EJBAccessException.class)
    public void testFind_userIsSystem() throws Exception {
        runAsSystem.call(() -> {
            employeeService.find(employee.getUserName());
            return null;
        });
    }
    
    @Test(expected = EJBAccessException.class)
    public void testUpdateList_userIsAdmin() throws Exception {
        runAsAdmin.call(() -> {
            employeeService.update(singletonList(employee));
            return null;
        });
    }

    @Test
    public void testUpdateList_userIsExec() throws Exception {
        runAsExec.call(() -> {
            employeeService.update(singletonList(employee));
            return null;
        });
    }

    @Test(expected = EJBAccessException.class)
    public void testUpdateList_userIsProjectManager() throws Exception {
        runAsProjectManager.call(() -> {
            employeeService.update(singletonList(employee));
            return null;
        });
    }

    @Test(expected = EJBAccessException.class)
    public void testUpdateList_userIsAccountant() throws Exception {
        runAsAccountant.call(() -> {
            employeeService.update(singletonList(employee));
            return null;
        });
    }

    @Test
    public void testUpdateList_userIsOfficeManager() throws Exception {
        runAsOfficeManager.call(() -> {
            employeeService.update(singletonList(employee));
            return null;
        });
    }

    @Test(expected = EJBAccessException.class)
    public void testUpdateList_userIsIntegrationClient() throws Exception {
        runAsIntegrationClient.call(() -> {
            employeeService.update(singletonList(employee));
            return null;
        });
    }

    @Test
    public void testUpdateList_userIsSystem() throws Exception {
        runAsSystem.call(() -> {
            employeeService.update(singletonList(employee));
            return null;
        });
    }

    @Test(expected = EJBAccessException.class)
    public void testUpdate() {
        employeeService.update(employee);
    }

    @Test(expected = EJBAccessException.class)
    public void testUpdate_userIsAdmin() throws Exception {
        runAsAdmin.call(() -> {
            employeeService.update(employee);
            return null;
        });
    }

    @Test
    public void testUpdate_userIsExec() throws Exception {
        runAsExec.call(() -> {
            employeeService.update(employee);
            return null;
        });
    }

    @Test(expected = EJBAccessException.class)
    public void testUpdate_userIsProjectManager() throws Exception {
        runAsProjectManager.call(() -> {
            employeeService.update(employee);
            return null;
        });
    }

    @Test(expected = EJBAccessException.class)
    public void testUpdate_userIsAccountant() throws Exception {
        runAsAccountant.call(() -> {
            employeeService.update(employee);
            return null;
        });
    }

    @Test
    public void testUpdate_userIsOfficeManager() throws Exception {
        runAsOfficeManager.call(() -> {
            employeeService.update(employee);
            return null;
        });
    }

    @Test(expected = EJBAccessException.class)
    public void testUpdate_userIsIntegrationClient() throws Exception {
        runAsIntegrationClient.call(() -> {
            employeeService.update(employee);
            return null;
        });
    }

    @Test
    public void testUpdate_userIsSystem() throws Exception {
        runAsSystem.call(() -> {
            employeeService.update(employee);
            return null;
        });
    }

    @Test(expected = EJBAccessException.class)
    public void testGetEmployeesHavingAccessToAnyDepartment() {
        employeeService.getEmployeesHavingAccessToAnyDepartment();
    }

    @Test(expected = EJBAccessException.class)
    public void testGetEmployeesHavingAccessToAnyDepartment_userIsAdmin() throws Exception {
        runAsAdmin.call(() -> employeeService.getEmployeesHavingAccessToAnyDepartment());
    }

    @Test
    public void testGetEmployeesHavingAccessToAnyDepartment_userIsExec() throws Exception {
        runAsExec.call(() -> employeeService.getEmployeesHavingAccessToAnyDepartment());
    }

    @Test(expected = EJBAccessException.class)
    public void testGetEmployeesHavingAccessToAnyDepartment_userIsPM() throws Exception {
        runAsProjectManager.call(() -> employeeService.getEmployeesHavingAccessToAnyDepartment());
    }

    @Test(expected = EJBAccessException.class)
    public void testGetEmployeesHavingAccessToAnyDepartment_userIsAccountant() throws Exception {
        runAsAccountant.call(() -> employeeService.getEmployeesHavingAccessToAnyDepartment());
    }

    @Test(expected = EJBAccessException.class)
    public void testGetEmployeesHavingAccessToAnyDepartment_userIsOfficeManager() throws Exception {
        runAsOfficeManager.call(() -> employeeService.getEmployeesHavingAccessToAnyDepartment());
    }

    @Test(expected = EJBAccessException.class)
    public void testGetEmployeesHavingAccessToAnyDepartment_userIsIntegrationClient() throws Exception {
        runAsIntegrationClient.call(() -> employeeService.getEmployeesHavingAccessToAnyDepartment());
    }

    @Test(expected = EJBAccessException.class)
    public void testGetEmployeesHavingAccessToAnyDepartment_userIsSystem() throws Exception {
        runAsSystem.call(() -> employeeService.getEmployeesHavingAccessToAnyDepartment());
    }

    @Test(expected = EJBAccessException.class)
    public void testSetCalendar() {
        setCalendar();
    }

    @Test(expected = EJBAccessException.class)
    public void testSetCalendar_userIsAdmin() throws Exception {
        runAsAdmin.call(this::setCalendar);
    }

    @Test
    public void testSetCalendar_userIsExec() throws Exception {
        runAsExec.call(this::setCalendar);
    }

    @Test(expected = EJBAccessException.class)
    public void testSetCalendar_userIsPM() throws Exception {
        runAsProjectManager.call(this::setCalendar);
    }

    @Test
    public void testSetCalendar_userIsSystem() throws Exception {
        runAsSystem.call(this::setCalendar);
    }

    @Test(expected = EJBAccessException.class)
    public void testSetCalendar_userIsIntegrationClient() throws Exception {
        runAsIntegrationClient.call(this::setCalendar);
    }

    @Test(expected = EJBAccessException.class)
    public void testSetCalendar_userIsAccountant() throws Exception {
        runAsAccountant.call(this::setCalendar);
    }

    @Test
    public void testSetCalendar_userIsOfficeManager() throws Exception {
        runAsOfficeManager.call(this::setCalendar);
    }

    @Test(expected = EJBAccessException.class)
    public void testGetAll() {
        employeeService.getAll();
    }

    @Test(expected = EJBAccessException.class)
    public void testGetAll_userIsAdmin() throws Exception {
        runAsAdmin.call(employeeService::getAll);
    }

    @Test
    public void testGetAll_userIsExec() throws Exception {
        runAsExec.call(employeeService::getAll);
    }

    @Test
    public void testGetAll_userIsPM() throws Exception {
        runAsProjectManager.call(employeeService::getAll);
    }

    @Test
    public void testGetAll_userIsSystem() throws Exception {
        runAsSystem.call(employeeService::getAll);
    }

    @Test(expected = EJBAccessException.class)
    public void testGetAll_userIsIntegrationClient() throws Exception {
        runAsIntegrationClient.call(employeeService::getAll);
    }

    @Test
    public void testGetAll_userIsAccountant() throws Exception {
        runAsAccountant.call(employeeService::getAll);
    }

    @Test
    public void testGetAll_userIsOfficeManager() throws Exception {
        runAsOfficeManager.call(employeeService::getAll);
    }

    @Test(expected = EJBAccessException.class)
    public void testGetEffortsEmployees() {
        employeeService.getEffortsEmployees();
    }

    @Test(expected = EJBAccessException.class)
    public void testGetEffortsEmployees_userIsAdmin() throws Exception {
        runAsAdmin.call(employeeService::getEffortsEmployees);
    }

    @Test
    public void testGetEffortsEmployees_userIsExec() throws Exception {
        runAsExec.call(employeeService::getEffortsEmployees);
    }

    @Test
    public void testGetEffortsEmployees_userIsPM() throws Exception {
        runAsProjectManager.call(employeeService::getEffortsEmployees);
    }

    @Test(expected = EJBAccessException.class)
    public void testGetEffortsEmployees_userIsSystem() throws Exception {
        runAsSystem.call(employeeService::getEffortsEmployees);
    }

    @Test(expected = EJBAccessException.class)
    public void testGetEffortsEmployees_userIsIntegrationClient() throws Exception {
        runAsIntegrationClient.call(employeeService::getEffortsEmployees);
    }

    @Test(expected = EJBAccessException.class)
    public void testGetEffortsEmployees_userIsAccountant() throws Exception {
        runAsAccountant.call(employeeService::getEffortsEmployees);
    }

    @Test
    public void testGetEffortsEmployees_userIsOfficeManager() throws Exception {
        runAsOfficeManager.call(employeeService::getEffortsEmployees);
    }

    private boolean setCalendar() {
        employeeService.setCalendar(asList(employee));
        return true;
    }

}

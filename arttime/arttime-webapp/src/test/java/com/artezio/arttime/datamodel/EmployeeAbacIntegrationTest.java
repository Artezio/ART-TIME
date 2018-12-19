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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
@Category(IntegrationTest.class)
public class EmployeeAbacIntegrationTest extends AbacIntegrationTest {
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

    private Employee employee1FromDepartment1;
    private Employee employee2FromDepartment1;
    private Employee employeeFromDepartment2;
    private String department1 = "depart1";
    private String department2 = "depart2";

    @Before
    public void setUp() throws Exception {
        runInNewTx.run(() -> {
            unsecuredEntityManager.joinTransaction();
            employee1FromDepartment1 = new Employee("emp_username", "emp_firstname", "emp_lastname", "emp_email", department1);
            employee2FromDepartment1 = new Employee("emp_username2", "emp_firstname2", "emp_lastname2", "emp_email2", department1);
            employeeFromDepartment2 = new Employee("emp1_username3", "emp1_firstname3", "emp1_lastname3", "emp1_email3", department2);
            unsecuredEntityManager.persist(employee1FromDepartment1);
            unsecuredEntityManager.persist(employee2FromDepartment1);
            unsecuredEntityManager.persist(employeeFromDepartment2);
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
        });
    }

    @Test
    public void testGetAll() throws Exception {
        Employee caller = createAnonymousEmployee(department1);
        List<Employee> actualList = runInNewTx.call(() ->
                abacEntityManager.createQuery("SELECT e FROM Employee e", Employee.class).getResultList());
        assertTrue(actualList.isEmpty());
    }

    @Test
    public void testGetAll_callerIsExec() throws Exception {
        Employee caller = createAnonymousEmployee(department1);
        List<Employee> actualList = runAsExec.call(() ->
                runInNewTx.call(() ->
                        abacEntityManager.createQuery("SELECT e FROM Employee e", Employee.class).getResultList()));
        assertContainsAllEmployees(actualList, caller);
    }

    @Test
    public void testGetAll_callerIsProjectManager() throws Exception {
        Employee caller = createAnonymousEmployee(department1);
        List<Employee> actualList = runAsProjectManager.call(() ->
                runInNewTx.call(() ->
                        abacEntityManager.createQuery("SELECT e FROM Employee e", Employee.class).getResultList()));
        assertContainsAllEmployees(actualList, caller);
    }

    @Test
    public void testGetAll_callerIsOfficeManager() throws Exception {
        Employee caller = createAnonymousEmployee(department2, new String[]{department1});
        List<Employee> actualList = runAsOfficeManager.call(() ->
                runInNewTx.call(() ->
                        abacEntityManager.createQuery("SELECT e FROM Employee e", Employee.class).getResultList()));
        assertFalse(actualList.isEmpty());
        assertTrue(actualList.contains(employee1FromDepartment1));
        assertTrue(actualList.contains(employee2FromDepartment1));
        assertFalse(actualList.contains(employeeFromDepartment2));
        assertFalse(actualList.contains(caller));
    }

    @Test
    public void testGetAll_callerIsAccountant() throws Exception {
        Employee caller = createAnonymousEmployee(department2, new String[]{department1});
        List<Employee> actualList = runAsAccountant.call(() ->
                runInNewTx.call(() ->
                        abacEntityManager.createQuery("SELECT e FROM Employee e", Employee.class).getResultList()));
        assertFalse(actualList.isEmpty());
        assertTrue(actualList.contains(employee1FromDepartment1));
        assertTrue(actualList.contains(employee2FromDepartment1));
        assertFalse(actualList.contains(employeeFromDepartment2));
        assertFalse(actualList.contains(caller));
    }

    @Test
    public void testGetAll_manageEmployeesContext_callerIsExec() throws Exception {
        Employee caller = createAnonymousEmployee(department1);
        List<Employee> actualList = runAsExec.call(() ->
                runInNewTx.call(() ->
                        callUsingAbacContext.callInManageEmployeesContext(() ->
                                abacEntityManager.createQuery("SELECT e FROM Employee e", Employee.class).getResultList())));
        assertContainsAllEmployees(actualList, caller);
    }

    @Test
    public void testGetAll_manageEmployeesContext_callerIsOfficeManager() throws Exception {
        Employee caller = createAnonymousEmployee(department2, new String[]{department1});
        List<Employee> actualList = runAsOfficeManager.call(() ->
                runInNewTx.call(() ->
                        callUsingAbacContext.callInManageEmployeesContext(() ->
                                abacEntityManager.createQuery("SELECT e FROM Employee e", Employee.class).getResultList())));
        assertFalse(actualList.isEmpty());
        assertTrue(actualList.contains(employee1FromDepartment1));
        assertTrue(actualList.contains(employee2FromDepartment1));
        assertFalse(actualList.contains(employeeFromDepartment2));
        assertFalse(actualList.contains(caller));
    }

    @Test
    public void testGetAll_reportWorktimeProblemsContext() throws Exception {
        Employee caller = createAnonymousEmployee(department1);
        List<Employee> actualList = runInNewTx.call(() ->
                callUsingAbacContext.callInReportWorktimeProblemsContext(() ->
                        abacEntityManager.createQuery("SELECT e FROM Employee e", Employee.class).getResultList()));
        assertTrue(actualList.isEmpty());
    }

    @Test
    public void testGetAll_reportWorktimeProblemsContext_callerIsExec() throws Exception {
        Employee caller = createAnonymousEmployee(department1);
        List<Employee> actualList = runInNewTx.call(() ->
                runAsExec.call(() ->
                        callUsingAbacContext.callInReportWorktimeProblemsContext(() ->
                                abacEntityManager.createQuery("SELECT e FROM Employee e", Employee.class).getResultList())));
        assertContainsAllEmployees(actualList, caller);
    }

    @Test
    public void testGetAll_reportWorktimeProblemsContext_callerIsOfficeManager() throws Exception {
        Employee caller = createAnonymousEmployee(department2, new String[]{department1});
        List<Employee> actualList = runAsOfficeManager.call(() ->
                runInNewTx.call(() ->
                        callUsingAbacContext.callInReportWorktimeProblemsContext(() ->
                                abacEntityManager.createQuery("SELECT e FROM Employee e", Employee.class).getResultList())));
        assertFalse(actualList.isEmpty());
        assertTrue(actualList.contains(employee1FromDepartment1));
        assertTrue(actualList.contains(employee2FromDepartment1));
        assertFalse(actualList.contains(employeeFromDepartment2));
        assertFalse(actualList.contains(caller));
    }

    protected void assertContainsAllEmployees(Collection<Employee> actualList, Employee... additionalEmployees) {
        assertFalse(actualList.isEmpty());
        assertTrue(actualList.contains(employee1FromDepartment1));
        assertTrue(actualList.contains(employee2FromDepartment1));
        assertTrue(actualList.contains(employeeFromDepartment2));
        assertTrue(actualList.containsAll(Arrays.asList(additionalEmployees)));
    }

}

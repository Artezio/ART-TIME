package com.artezio.arttime.filter;

import com.artezio.arttime.datamodel.AbacIntegrationTest;
import com.artezio.arttime.datamodel.Employee;
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

import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
@Category(IntegrationTest.class)
public class FilterAbacIntegrationTest extends AbacIntegrationTest {
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

    private Employee anotherEmployee;
    private Filter anotherEmployeeFilter;

    @Before
    public void setUp() throws Exception {
        runInNewTx.run(()-> {
            unsecuredEntityManager.joinTransaction();
            anotherEmployee = new Employee("another_emp_username", "emp_firstname", "emp_lastname", "emp_email", "emp_dept");
            anotherEmployeeFilter = new Filter("anemfilter", anotherEmployee.getUserName(), false);
            unsecuredEntityManager.persist(anotherEmployee);
            unsecuredEntityManager.persist(anotherEmployeeFilter);
        });
    }
    
    @After
    public void tearDown() throws Exception {
        runInNewTx.run(() -> {
            unsecuredEntityManager.joinTransaction();
            unsecuredEntityManager.createQuery("DELETE FROM Filter").executeUpdate();
            unsecuredEntityManager.createQuery("DELETE FROM Employee ").executeUpdate();
        });
    }
    
    @Test
    public void testGetAll() throws Exception {
        Employee caller = createAnonymousEmployee("dep1");
        Filter employeeFilter = new Filter("emfilter", caller.getUserName(), false);
        runInNewTx.run(() -> {
            unsecuredEntityManager.joinTransaction();
            unsecuredEntityManager.persist(employeeFilter);
        });
        List<Filter> actualList = abacEntityManager.createQuery("SELECT f FROM Filter f", Filter.class).getResultList();
        assertFalse(actualList.isEmpty());
        assertTrue(actualList.contains(employeeFilter));
        assertFalse(actualList.contains(anotherEmployeeFilter));
    }

    @Test
    public void testGetOwned() throws Exception {
        Employee caller = createAnonymousEmployee("dep1");
        Filter employeeFilter = new Filter(anotherEmployeeFilter.getName(), caller.getUserName(), false);
        runInNewTx.run(() -> {
            unsecuredEntityManager.joinTransaction();
            unsecuredEntityManager.persist(employeeFilter);
        });
        List<Filter> actualList = abacEntityManager.createQuery("SELECT f FROM Filter f WHERE f.name=:name", Filter.class)
                .setParameter("name", employeeFilter.getName())
                .getResultList();
        assertFalse(actualList.isEmpty());
        assertTrue(actualList.contains(employeeFilter));
        assertFalse(actualList.contains(anotherEmployeeFilter));
    }

    @Test
    public void testGetNonOwned() throws Exception {
        createAnonymousEmployee("dep1");
        List<Filter> actualList = abacEntityManager.createQuery("SELECT f FROM Filter f WHERE f.name=:name", Filter.class)
                .setParameter("name", anotherEmployeeFilter.getName())
                .getResultList();
        assertTrue(actualList.isEmpty());
    }

}

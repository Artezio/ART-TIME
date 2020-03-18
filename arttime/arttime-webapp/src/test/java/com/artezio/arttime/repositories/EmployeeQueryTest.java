package com.artezio.arttime.repositories;

import static junitx.util.PrivateAccessor.setField;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

import com.artezio.arttime.datamodel.Employee;

@RunWith(PowerMockRunner.class)
public class EmployeeQueryTest {

    private EntityManager entityManager;
    private EntityManagerFactory entityManagerFactory;
    private EmployeeRepository.EmployeeQuery employeeQuery;
    private EmployeeRepository employeeRepository;

    @Before
    public void setUp() throws Exception {
        Map<String, String> properties = new HashMap<>();
        properties.put("javax.persistence.validation.mode", "none");
        entityManagerFactory = Persistence.createEntityManagerFactory("test-pu", properties);
        entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        employeeRepository = new EmployeeRepository();
        setField(employeeRepository, "entityManager", entityManager);
        employeeQuery = employeeRepository.query();
    }

    @After
    public void tearDown() {
        if (entityManager.getTransaction().isActive()) {
            if (entityManager.getTransaction().getRollbackOnly()) {
                entityManager.getTransaction().rollback();
            } else {
                entityManager.getTransaction().commit();
            }
            entityManagerFactory.close();
        }
    }

    @Test
    public void testWithAccessToDepartment() {
        String expectedDepartment = "dep_exp";
        String unexpectedDepartment = "dep_notexp";
        Employee expected1 = new Employee("uname", "fname", "lname", "email");
        Employee expected2 = new Employee("uname2", "fname2", "lname2", "email2");
        Employee unexpected = new Employee("unexname", "unexfname", "unexlname", "unexemail");
        Set<String> access1 = new HashSet<>(Arrays.asList(expectedDepartment));
        Set<String> access2 = new HashSet<>(Arrays.asList(expectedDepartment, unexpectedDepartment));
        Set<String> access3 = new HashSet<>(Arrays.asList(unexpectedDepartment));
        expected1.setAccessibleDepartments(access1);
        expected2.setAccessibleDepartments(access2);
        unexpected.setAccessibleDepartments(access3);
        entityManager.persist(unexpected);
        entityManager.persist(expected1);
        entityManager.persist(expected2);
        entityManager.flush();
        entityManager.clear();

        List<Employee> actual = employeeQuery.withAccessToDepartment(expectedDepartment).list();

        assertTrue(actual.contains(expected1));
        assertTrue(actual.contains(expected2));
        assertFalse(actual.contains(unexpected));
    }

}

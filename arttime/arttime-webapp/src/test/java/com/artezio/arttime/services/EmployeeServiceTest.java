package com.artezio.arttime.services;

import static junitx.util.PrivateAccessor.setField;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.inject.spi.CDI;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.easymock.internal.LastControl;
import org.hibernate.Hibernate;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.artezio.arttime.datamodel.Employee;
import com.artezio.arttime.repositories.EmployeeRepository;
import com.google.common.collect.Sets;

@RunWith(PowerMockRunner.class)
@PrepareForTest(CDI.class)
public class EmployeeServiceTest {

    private EmployeeRepository employeeRepository;
    private EmployeeService employeeService = new EmployeeService();
    private EntityManager entityManager;
    private EntityManagerFactory entityManagerFactory;

    @Before
    public void setUp() throws NoSuchFieldException {
        Map<String, String> properties = new HashMap<>();
        properties.put("javax.persistence.validation.mode", "none");
        entityManagerFactory = Persistence.createEntityManagerFactory("test-pu", properties);
        entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        LastControl.pullMatchers();
        employeeRepository = createMock(EmployeeRepository.class);
        setField(employeeService, "employeeRepository", employeeRepository);
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
        LastControl.pullMatchers();
    }

    @Test
    public void testUpdate() {
        Employee employee = new Employee("Emp");
        expect(employeeRepository.update(employee)).andReturn(employee);
        replay(employeeRepository);
        
        employeeService.update(employee);

        verify(employeeRepository);
    }

    @Test
    public void testGetEmployeesHavingAccessToAnyDepartment() throws NoSuchFieldException {
        employeeRepository = new EmployeeRepository();
        setField(employeeRepository, "entityManager", entityManager);
        setField(employeeService, "employeeRepository", employeeRepository);

        String department1 = "dep1";
        String department2 = "dep2";
        Employee expected1 = new Employee("uname1", "fname1", "lname1", "email1");
        expected1.setAccessibleDepartments(Sets.newHashSet(department1));
        Employee expected2 = new Employee("uname2", "fname2", "lname2", "email2");
        expected2.setAccessibleDepartments(Sets.newHashSet(department2));
        Employee unexpected = new Employee("uxname3", "fname3", "lname3", "email3");
        entityManager.persist(expected1);
        entityManager.persist(expected2);
        entityManager.persist(unexpected);
        entityManager.flush();

        List<Employee> actual = employeeService.getEmployeesHavingAccessToAnyDepartment();

        assertTrue(actual.contains(expected1));
        assertTrue(actual.contains(expected2));
        assertFalse(actual.contains(unexpected));
        Employee actual1 = actual.get(actual.indexOf(expected1));
        Employee actual2 = actual.get(actual.indexOf(expected2));
        assertTrue(Hibernate.isInitialized(actual1.getAccessibleDepartments()));
        assertTrue(Hibernate.isInitialized(actual2.getAccessibleDepartments()));
    }
    
    @Test
    public void testGetCurrent() throws Exception {
        
        Employee currentEmployee1 = new Employee("uname1", "fname1", "lname1", "email1");
        currentEmployee1.setFormer(false);
        Employee currentEmployee2 = new Employee("uname2", "fname2", "lname2", "email2");
        currentEmployee2.setFormer(false);
        Employee formerEmployee = new Employee("uxname3", "fname3", "lname3", "email3");
        formerEmployee.setFormer(true);
        List<Employee> expected = Arrays.asList(currentEmployee1, currentEmployee2);
        
        employeeRepository = new EmployeeRepository();
        setField(employeeRepository, "entityManager", entityManager);
        setField(employeeService, "employeeRepository", employeeRepository);
        entityManager.persist(currentEmployee1);
        entityManager.persist(currentEmployee2);
        entityManager.persist(formerEmployee);
        entityManager.flush();
        
        List<Employee> actual = employeeService.getCurrent();
        
        assertEquals(expected, actual);
    }

}

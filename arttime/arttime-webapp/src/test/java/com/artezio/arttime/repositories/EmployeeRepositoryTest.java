package com.artezio.arttime.repositories;

import com.artezio.arttime.datamodel.Employee;
import com.artezio.arttime.datamodel.Project;
import com.artezio.arttime.filter.Filter;
import com.google.common.collect.Sets;
import java.util.*;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import junitx.framework.ListAssert;
import static junitx.util.PrivateAccessor.setField;
import org.hibernate.Hibernate;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
public class EmployeeRepositoryTest {
    private EntityManager entityManager;
    private EntityManagerFactory entityManagerFactory;
    private EmployeeRepository employeeRepository;

    @Before
    public void setUp() throws Exception {
        employeeRepository = new EmployeeRepository();
        Map<String, String> properties = new HashMap<>();
        properties.put("javax.persistence.validation.mode", "none");
        entityManagerFactory = Persistence.createEntityManagerFactory("test-pu", properties);
        entityManager = entityManagerFactory.createEntityManager();
        setField(employeeRepository, "entityManager", entityManager);
        entityManager.getTransaction().begin();
    }

    @After
    public void tearDown() throws Exception {
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
    public void testFind_ByUsername() {
        Employee expectedEmployee = new Employee("expected employee");
        Employee unexpectedEmployee = new Employee("unexpected employee");
        entityManager.persist(expectedEmployee);
        entityManager.persist(unexpectedEmployee);

        Employee actual = employeeRepository.find("expected employee");

        assertNotNull(actual);
        assertEquals(expectedEmployee, actual);
    }

    @Test
    public void testFind_ByUsername_EmployeeNotExist() {
        Employee unexpectedEmployee = new Employee("unexpected employee");
        entityManager.persist(unexpectedEmployee);

        Employee actual = employeeRepository.find("expected employee");

        assertNull(actual);
    }

    @Test
    public void testCreateEmployee() {
        Employee employee = new Employee("employee");

        employeeRepository.create(employee);

        Employee actual = entityManager.find(Employee.class, employee.getUserName());
        assertNotNull(actual);
    }

    @Test
    public void testGetAll() {
    	Employee employee1 = new Employee("employee1");
    	Employee employee2 = new Employee("employee2");        
        entityManager.persist(employee1);
        entityManager.persist(employee2);
        List<Employee> expected = Arrays.asList(employee1, employee2);

        List<Employee> actual = employeeRepository.getAll();

        ListAssert.assertEquals(expected, actual);
    }
    
    @Test
    public void testGetAllInitialized() {
        String department = "dep1";
    	Employee employee1 = new Employee("employee1");
    	employee1.setAccessibleDepartments(Sets.newHashSet(department));
    	Employee employee2 = new Employee("employee2");
        entityManager.persist(employee1);
        entityManager.persist(employee2);
        List<Employee> expected = Arrays.asList(employee1, employee2);

        List<Employee> actual = employeeRepository.getAllInitialized();

        assertTrue(actual.contains(employee1));
        Employee actual1 = actual.get(actual.indexOf(employee1));
        assertTrue(Hibernate.isInitialized(actual1.getAccessibleDepartments()));
        ListAssert.assertEquals(expected, actual);
    }

    @Test
    public void testUpdate() {    	
    	Employee employee = new Employee("iivanov", "Ivan", "Ivanov", "iivanov@mail.com");
    	entityManager.persist(employee);
    	employee.setEmail("ivan.ivanov@mail.com");
    	
    	Employee actual = employeeRepository.update(employee);
    	
    	assertEquals("ivan.ivanov@mail.com", actual.getEmail());
    	
    }
    
    @Test
    public void testPersisted() {
    	Employee employee1 = new Employee("employee1");
    	Employee employee2 = new Employee("employee2");        
        entityManager.persist(employee1);
    	
    	assertTrue(employeeRepository.persisted(employee1));
    	assertFalse(employeeRepository.persisted(employee2));
    }
    
    @Test
    public void testCreate() {
    	Employee employee = new Employee("employee");
    	
    	Employee actual = employeeRepository.create(employee);
    	
    	actual = entityManager.find(Employee.class, actual.getUserName());
    	assertNotNull(actual);
    }
    
    @Test
    public void testCreate_ifPersisted() {
    	Employee employee = new Employee("employee");
    	entityManager.persist(employee);
    	
    	Employee actual = employeeRepository.create(employee);
    	
    	actual = entityManager.find(Employee.class, actual.getUserName());
    	assertNotNull(actual);
    }
    
    @Test
    public void testGetEmployees() {
    	Employee employee1 = new Employee("employee1");
    	Employee employee2 = new Employee("employee2");
    	Employee employee3 = new Employee("employee3");
        entityManager.persist(employee1);
        entityManager.persist(employee2);
        entityManager.persist(employee3);
        List<Employee> expected = Arrays.asList(employee1, employee2);
        List<String> userNames = Arrays.asList("employee1", "employee2");
        
        List<Employee> actual = employeeRepository.getEmployees(userNames);
        
        ListAssert.assertEquals(expected, actual);
    }
    
    @Test
    public void testGetEmployees_ifUserNamesIsNull() {
        List<Employee> expected = new ArrayList<>();
        List<String> employees = null;        
        
        List<Employee> actual = employeeRepository.getEmployees(employees);
        
        ListAssert.assertEquals(expected, actual);
    }
    
    @Test
    public void testGetEmployees_ifUserNamesIsEmpty() {
        List<Employee> expected = new ArrayList<>();
        List<String> userNames = new ArrayList<>();
        
        List<Employee> actual = employeeRepository.getEmployees(userNames);
        
        ListAssert.assertEquals(expected, actual);
    }
    
    @Test
    public void testGetEmployeesByDepartments() {
    	Employee employee1 = new Employee("employee1");
    	employee1.setDepartment("department1");
    	Employee employee2 = new Employee("employee2");
    	employee2.setDepartment("department2");
    	Employee employee3 = new Employee("employee3");
    	employee3.setDepartment("department3");
    	Project project1 = new Project();
    	project1.addTeamMember(employee1);
    	project1.addTeamMember(employee2);
    	Project project2 = new Project();
    	project2.addTeamMember(employee1);
    	project2.addTeamMember(employee3);

    	entityManager.persist(project1);
    	entityManager.persist(project2);
    	entityManager.persist(employee1);
    	entityManager.persist(employee2);
    	entityManager.persist(employee3);
    	
    	Filter filter = new Filter();
    	filter.setProjects(Arrays.asList(project1, project2));
    	filter.setEmployees(Arrays.asList(employee1, employee2, employee3));
    	filter.setDepartments(Arrays.asList("department1", "department2", "department3"));
    	List<Employee> expected = Arrays.asList(employee1, employee2, employee3);
    	
    	List<Employee> actual = employeeRepository.getEmployeesByDepartments(filter);
    	
    	ListAssert.assertEquals(expected, actual);
    }
    
    @Test
    public void testGetEmployeesByDepartments_ifEmployeesAreEmpty() {
        Employee employee1 = new Employee("employee1", "test1", "test1", "department1");
        Employee employee2 = new Employee("employee2", "test2", "test2", "department2");

        Filter filter = new Filter();
        filter.setEmployees(Collections.emptyList());
        filter.setDepartments(Arrays.asList("department1", "department2"));

    	entityManager.persist(employee1);
    	entityManager.persist(employee2);

    	List<Employee> expected = filter.getEmployees();
    	List<Employee> actual = employeeRepository.getEmployeesByDepartments(filter);
    	
    	ListAssert.assertEquals(expected, actual);
    }

    @Test
    public void testGetEmployeesByDepartments_ifEmployeesAreEmpty_FilterHasNotAllDepartments() {
        Employee employee1 = new Employee("employee1");
        employee1.setDepartment("dep1");
        Employee employee2 = new Employee("employee2");
        employee2.setDepartment("dep2");
        Employee employee3 = new Employee("employee3");
        employee3.setDepartment("dep3");
        Filter filter = new Filter();
        filter.setEmployees(Arrays.asList(employee1, employee2));
        filter.setDepartments(Arrays.asList("dep1", "dep2"));

        entityManager.persist(employee1);
        entityManager.persist(employee2);
        entityManager.persist(employee3);

        List<Employee> expected = new ArrayList<>(filter.getEmployees());
        List<Employee> actual = employeeRepository.getEmployeesByDepartments(filter);

        ListAssert.assertEquals(expected, actual);
    }
    
    @Test
    public void testGetEmployeesByDepartments_ifDepartmentsAreEmpty() {
    	Employee employee1 = new Employee("employee1");
    	Employee employee2 = new Employee("employee2");
    	Employee employee3 = new Employee("employee3");
        Filter filter = new Filter();
        filter.setEmployees(Arrays.asList(employee1, employee2, employee3));
        filter.setDepartments(Collections.emptyList());

    	entityManager.persist(employee1);
    	entityManager.persist(employee2);
    	entityManager.persist(employee3);
        
    	List<Employee> expected = new ArrayList<>(filter.getEmployees());
    	List<Employee> actual = employeeRepository.getEmployeesByDepartments(filter);
    	
    	ListAssert.assertEquals(expected, actual);
    }

    @Test
    public void testGetEmployeesByDepartments_ifDepartmentsAreEmpty_FilterHasNotAllEmployees() {
        Employee employee1 = new Employee("employee1");
        Employee employee2 = new Employee("employee2");
        Employee employee3 = new Employee("employee3");
        Filter filter = new Filter();
        filter.setEmployees(Arrays.asList(employee1, employee2));
        filter.setDepartments(Collections.emptyList());

        entityManager.persist(employee1);
        entityManager.persist(employee2);
        entityManager.persist(employee3);

        List<Employee> expected = new ArrayList<>(filter.getEmployees());
        List<Employee> actual = employeeRepository.getEmployeesByDepartments(filter);

        ListAssert.assertEquals(expected, actual);
    }

    @Test
    public void testGetEmployeesByDepartments_ifEmployeesDepartmentsAreEmpty() {
        Employee employee1 = new Employee("employee1");
        employee1.setDepartment("department1");
        Employee employee2 = new Employee("employee2");
        employee2.setDepartment("department2");
        Employee employee3 = new Employee("employee3");
        employee3.setDepartment("department3");
        Project project1 = new Project();
        project1.addTeamMember(employee1);
        project1.addTeamMember(employee2);
        Project project2 = new Project();
        project2.addTeamMember(employee1);
        project2.addTeamMember(employee3);
        Filter filter = new Filter();
        filter.setEmployees(Collections.emptyList());
        filter.setDepartments(Collections.emptyList());
        
        entityManager.persist(project1);
        entityManager.persist(project2);
        entityManager.persist(employee1);
        entityManager.persist(employee2);
        entityManager.persist(employee3);
        entityManager.flush();

        List<Employee> expected = Arrays.asList(employee1, employee2, employee3);
        List<Employee> actual = employeeRepository.getEmployeesByDepartments(filter);

        ListAssert.assertEquals(expected, actual);
    }

}

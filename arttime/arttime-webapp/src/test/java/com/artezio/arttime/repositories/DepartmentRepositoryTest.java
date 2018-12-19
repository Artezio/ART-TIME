package com.artezio.arttime.repositories;

import static junitx.util.PrivateAccessor.setField;
import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.artezio.arttime.datamodel.Employee;

public class DepartmentRepositoryTest {
	private EntityManager entityManager;
	private EntityManagerFactory entityManagerFactory;
	private DepartmentRepository departmentRepository;

	@Before
	public void setUp() throws Exception {
		departmentRepository = new DepartmentRepository();
		Map<String, String> properties = new HashMap<String, String>();
		properties.put("javax.persistence.validation.mode", "none");
		entityManagerFactory = Persistence.createEntityManagerFactory("test-pu", properties);
		entityManager = entityManagerFactory.createEntityManager();
		setField(departmentRepository, "entityManager", entityManager);
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
	public void testGetDepartments() {
		Employee employee1 = new Employee("emp1");
		Employee employee2 = new Employee("emp2");
		Employee employee3 = new Employee("emp3");
		Employee employee4 = new Employee("emp4");
		Employee employee5 = new Employee("emp5");
		employee1.setDepartment("dep1");
		employee2.setDepartment("dep2");
		employee3.setDepartment("dep1");
		employee4.setDepartment("");
		employee5.setDepartment(null);
		entityManager.persist(employee1);
		entityManager.persist(employee2);
		entityManager.persist(employee3);
		entityManager.persist(employee4);
		entityManager.persist(employee5);

		List<String> actual = departmentRepository.getDepartments();
		
		assertEquals(3, actual.size());
		assertTrue(actual.contains(employee1.getDepartment()));
		assertTrue(actual.contains(employee2.getDepartment()));
		assertTrue(actual.contains(employee4.getDepartment()));
	}
}

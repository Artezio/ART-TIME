package com.artezio.arttime.repositories;

import com.artezio.arttime.datamodel.HourType;
import com.artezio.arttime.exceptions.ActualTimeRemovalException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static junitx.util.PrivateAccessor.setField;
import static org.junit.Assert.*;

public class HourTypeRepositoryTest {

	private EntityManager entityManager;
	private EntityManagerFactory entityManagerFactory;
	private HourTypeRepository hourTypeRepository;

	@Before
	public void setUp() throws Exception {
		hourTypeRepository = new HourTypeRepository();
		Map<String, String> properties = new HashMap<>();
		properties.put("javax.persistence.validation.mode", "none");
		entityManagerFactory = Persistence.createEntityManagerFactory("test-pu", properties);
		entityManager = entityManagerFactory.createEntityManager();
		setField(hourTypeRepository, "entityManager", entityManager);
		entityManager.getTransaction().begin();
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
	public void testCreateHourType() {
		HourType hourType = new HourType();
		
		hourTypeRepository.create(hourType);

		Long id = hourType.getId();
		assertNotNull(id);
		HourType actual = entityManager.find(HourType.class, id);
		assertEquals(hourType, actual);
	}

	@Test
	public void testRemoveHourType() throws ActualTimeRemovalException {
		HourType hourType = new HourType();
		entityManager.persist(hourType);
		Long id = hourType.getId();

		hourTypeRepository.remove(hourType);

		HourType actual = entityManager.find(HourType.class, id);
		assertNull(actual);
	}

	@Test(expected = ActualTimeRemovalException.class)
	public void testRemoveHourType_ifActualTime() throws ActualTimeRemovalException {
		HourType actualTime = new HourType("actual time");
		actualTime.setActualTime(true);
		entityManager.persist(actualTime);

		hourTypeRepository.remove(actualTime);
	}

	@Test
	public void testUpdateHourType() {
		HourType hourType = new HourType();
		hourType.setType("day off");
		entityManager.persist(hourType);
		String newType = "over time";
		hourType.setType(newType);
		entityManager.flush();

		HourType actual = hourTypeRepository.update(hourType);

		assertEquals(newType, actual.getType());
	}

	@Test
	public void testResetActualTime() {
		HourType notActualTime = new HourType();
		HourType actualTime = new HourType();
		actualTime.setActualTime(true);

		entityManager.persist(notActualTime);
		entityManager.persist(actualTime);
		entityManager.flush();
		entityManager.clear();

		entityManager.createQuery("UPDATE HourType h SET h.actualTime = false")
				.executeUpdate();

		List<HourType> actual = entityManager.createQuery("SELECT ht FROM HourType ht", HourType.class)
				.getResultList();
		assertFalse(actual.get(0).isActualTime());
		assertFalse(actual.get(1).isActualTime());
	}

}

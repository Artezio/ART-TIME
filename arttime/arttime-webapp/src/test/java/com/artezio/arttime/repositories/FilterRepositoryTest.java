package com.artezio.arttime.repositories;

import static junitx.util.PrivateAccessor.setField;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.util.HashMap;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.artezio.arttime.filter.Filter;

public class FilterRepositoryTest {
    
    private EntityManager entityManager;
    private EntityManagerFactory entityManagerFactory;
    private FilterRepository filterRepository;
    
    @Before
    public void setUp() throws Exception {
        Map<String, String> properties = new HashMap<>();
        properties.put("javax.persistence.validation.mode", "none");
        entityManagerFactory = Persistence.createEntityManagerFactory("test-pu", properties);
        entityManager = entityManagerFactory.createEntityManager();
        
        filterRepository = new FilterRepository();
        setField(filterRepository, "entityManager", entityManager);
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
    public void testCreate() {
        Filter expected = new Filter();
        
        Filter actual = filterRepository.create(expected);
        
        assertSame(expected, actual);
        assertNotNull(actual.getId());
    }
    

    @Test
    public void testUpdate() {
        Filter expected = new Filter();
        expected.setName("old filter name");
        entityManager.persist(expected);
        entityManager.flush();
        entityManager.clear();
        
        expected.setName("new filter name");

        filterRepository.update(expected);

        Filter actual = entityManager.find(Filter.class, expected.getId());
        assertEquals(expected.getName(), actual.getName());
    }

    @Test
    public void testDelete() {
        Filter filter = new Filter();
        entityManager.persist(filter);
        entityManager.flush();
        entityManager.clear();

        filterRepository.delete(filter);

        Filter actual = entityManager.find(Filter.class, filter.getId());
        assertNull(actual);
    }
    
}

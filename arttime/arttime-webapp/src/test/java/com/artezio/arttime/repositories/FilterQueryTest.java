package com.artezio.arttime.repositories;

import static junitx.util.PrivateAccessor.setField;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.artezio.arttime.filter.Filter;

import junitx.framework.ListAssert;

public class FilterQueryTest {
    
    private EntityManager entityManager;
    private EntityManagerFactory entityManagerFactory;
    private FilterRepository filterRepository;
    private FilterRepository.FilterQuery filterQuery;
    
    @Before
    public void setUp() throws Exception {
        Map<String, String> properties = new HashMap<>();
        properties.put("javax.persistence.validation.mode", "none");
        entityManagerFactory = Persistence.createEntityManagerFactory("test-pu", properties);
        entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        filterRepository = new FilterRepository();
        setField(filterRepository, "entityManager", entityManager);
        filterQuery = filterRepository.new FilterQuery();
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
    public void testGetFilters() {
        Filter filter1 = new Filter("filter1", "owner1", true);
        Filter filter2 = new Filter("filter2", "owner2", false);
        entityManager.persist(filter1);
        entityManager.persist(filter2);
        entityManager.flush();
        entityManager.clear();
        
        List<Filter> actual = filterQuery.list();
        
        ListAssert.assertEquals(Arrays.asList(filter1, filter2), actual);
    }

}

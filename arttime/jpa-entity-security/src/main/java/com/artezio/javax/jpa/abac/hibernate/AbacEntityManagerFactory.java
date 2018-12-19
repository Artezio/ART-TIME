package com.artezio.javax.jpa.abac.hibernate;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.metamodel.Metamodel;
import java.util.Map;

public class AbacEntityManagerFactory implements EntityManagerFactory {

    private EntityManagerFactory entityManagerFactory;

    public AbacEntityManagerFactory(EntityManagerFactory originalFactory) {
        this.entityManagerFactory = originalFactory;
    }

    @Override
    public EntityManager createEntityManager() {
        return new AbacEntityManager(entityManagerFactory.createEntityManager());
    }

    @Override
    public EntityManager createEntityManager(Map map) {
        return new AbacEntityManager(entityManagerFactory.createEntityManager(map));
    }

    @Override
    public EntityManager createEntityManager(SynchronizationType synchronizationType) {
        return new AbacEntityManager(entityManagerFactory.createEntityManager(synchronizationType));
    }

    @Override
    public EntityManager createEntityManager(SynchronizationType synchronizationType, Map map) {
        return new AbacEntityManager(entityManagerFactory.createEntityManager(synchronizationType, map));
    }

    @Override
    public CriteriaBuilder getCriteriaBuilder() {
        return entityManagerFactory.getCriteriaBuilder();
    }

    @Override
    public Metamodel getMetamodel() {
        return entityManagerFactory.getMetamodel();
    }

    @Override
    public boolean isOpen() {
        return entityManagerFactory.isOpen();
    }

    @Override
    public void close() {
        entityManagerFactory.close();
    }

    @Override
    public Map<String, Object> getProperties() {
        return entityManagerFactory.getProperties();
    }

    @Override
    public Cache getCache() {
        return entityManagerFactory.getCache();
    }

    @Override
    public PersistenceUnitUtil getPersistenceUnitUtil() {
        return entityManagerFactory.getPersistenceUnitUtil();
    }

    @Override
    public void addNamedQuery(String name, Query query) {
        entityManagerFactory.addNamedQuery(name, query);
    }

    @Override
    public <T> T unwrap(Class<T> cls) {
        return entityManagerFactory.unwrap(cls);
    }

    @Override
    public <T> void addNamedEntityGraph(String graphName, EntityGraph<T> entityGraph) {
        entityManagerFactory.addNamedEntityGraph(graphName, entityGraph);
    }
}

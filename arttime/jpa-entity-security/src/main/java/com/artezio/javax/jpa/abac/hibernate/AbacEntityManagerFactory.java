package com.artezio.javax.jpa.abac.hibernate;

import org.hibernate.Session;
import org.hibernate.SessionBuilder;
import org.hibernate.engine.spi.SessionFactoryImplementor;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.metamodel.Metamodel;
import java.util.Collections;
import java.util.Map;

public class AbacEntityManagerFactory implements EntityManagerFactory {

    private EntityManagerFactory entityManagerFactory;

    public AbacEntityManagerFactory(EntityManagerFactory originalFactory) {
        this.entityManagerFactory = originalFactory;
    }

    @Override
    public EntityManager createEntityManager() {
        return createAbacEntityManager(SynchronizationType.SYNCHRONIZED, Collections.emptyMap());
    }

    @Override
    public EntityManager createEntityManager(Map map) {
        return createAbacEntityManager(SynchronizationType.SYNCHRONIZED, map);
    }

    @Override
    public EntityManager createEntityManager(SynchronizationType synchronizationType) {
        return createAbacEntityManager(synchronizationType, Collections.emptyMap());
    }

    @Override
    public EntityManager createEntityManager(SynchronizationType synchronizationType, Map map) {
        return createAbacEntityManager(synchronizationType, map);
    }

    private AbacEntityManager createAbacEntityManager(SynchronizationType synchronizationType, Map map) {
        PostFlushInterceptor interceptor = new PostFlushInterceptor();
        SessionFactoryImplementor sessionFactory = entityManagerFactory.unwrap(SessionFactoryImplementor.class);
        SessionBuilder sessionBuilder = sessionFactory
                .withOptions()
                .interceptor(interceptor);
        if (synchronizationType == SynchronizationType.SYNCHRONIZED) {
            sessionBuilder.autoJoinTransactions(true);
        } else {
            sessionBuilder.autoJoinTransactions(false);
        }
        Session session = sessionBuilder.openSession();
        if (map != null) {
            map.keySet().forEach(key -> {
                if (key instanceof String) {
                    session.setProperty((String) key, map.get(key));
                }
            });
        }
        AbacEntityManager abacEntityManager = new AbacEntityManager(session);
        interceptor.onPostFlush(abacEntityManager::postFlush);
        return abacEntityManager;
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

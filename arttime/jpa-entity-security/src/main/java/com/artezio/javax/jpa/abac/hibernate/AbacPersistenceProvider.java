package com.artezio.javax.jpa.abac.hibernate;

import org.hibernate.boot.registry.classloading.spi.ClassLoaderService;
import org.hibernate.jpa.boot.spi.EntityManagerFactoryBuilder;
import org.hibernate.jpa.boot.spi.PersistenceUnitDescriptor;

import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.ProviderUtil;
import java.util.Map;

public class AbacPersistenceProvider extends org.hibernate.jpa.HibernatePersistenceProvider {

    @Override
    public EntityManagerFactory createContainerEntityManagerFactory(PersistenceUnitInfo info, Map properties) {
        return new AbacEntityManagerFactory(super.createContainerEntityManagerFactory(info, properties));
    }

    @Override
    public EntityManagerFactory createEntityManagerFactory(String persistenceUnitName, Map properties) {
        return new AbacEntityManagerFactory(super.createEntityManagerFactory(persistenceUnitName, properties));
    }

    @Override
    protected EntityManagerFactoryBuilder getEntityManagerFactoryBuilderOrNull(String persistenceUnitName, Map properties) {
        return super.getEntityManagerFactoryBuilderOrNull(persistenceUnitName, properties);
    }

    @Override
    protected EntityManagerFactoryBuilder getEntityManagerFactoryBuilderOrNull(String persistenceUnitName, Map properties, ClassLoader providedClassLoader) {
        return super.getEntityManagerFactoryBuilderOrNull(persistenceUnitName, properties, providedClassLoader);
    }

    @Override
    protected EntityManagerFactoryBuilder getEntityManagerFactoryBuilderOrNull(String persistenceUnitName, Map properties, ClassLoaderService providedClassLoaderService) {
        return super.getEntityManagerFactoryBuilderOrNull(persistenceUnitName, properties, providedClassLoaderService);
    }

    @Override
    public void generateSchema(PersistenceUnitInfo info, Map map) {
        super.generateSchema(info, map);
    }

    @Override
    public boolean generateSchema(String persistenceUnitName, Map map) {
        return super.generateSchema(persistenceUnitName, map);
    }

    @Override
    protected EntityManagerFactoryBuilder getEntityManagerFactoryBuilder(PersistenceUnitInfo info, Map integration) {
        return super.getEntityManagerFactoryBuilder(info, integration);
    }

    @Override
    protected EntityManagerFactoryBuilder getEntityManagerFactoryBuilder(PersistenceUnitDescriptor persistenceUnitDescriptor, Map integration, ClassLoader providedClassLoader) {
        return super.getEntityManagerFactoryBuilder(persistenceUnitDescriptor, integration, providedClassLoader);
    }

    @Override
    protected EntityManagerFactoryBuilder getEntityManagerFactoryBuilder(PersistenceUnitDescriptor persistenceUnitDescriptor, Map integration, ClassLoaderService providedClassLoaderService) {
        return super.getEntityManagerFactoryBuilder(persistenceUnitDescriptor, integration, providedClassLoaderService);
    }

    @Override
    public ProviderUtil getProviderUtil() {
        return super.getProviderUtil();
    }
}

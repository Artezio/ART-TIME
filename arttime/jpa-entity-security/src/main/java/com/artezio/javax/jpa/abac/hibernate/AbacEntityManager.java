package com.artezio.javax.jpa.abac.hibernate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.enterprise.inject.spi.CDI;
import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.StoredProcedureQuery;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.Type;

import org.hibernate.*;
import org.jboss.logging.Logger;

import com.artezio.javax.el.ElEvaluator;
import com.artezio.javax.jpa.abac.AbacRule;
import com.artezio.javax.jpa.abac.ActiveAbacContext;
import com.artezio.javax.jpa.abac.EntityAccessDeniedException;
import com.artezio.javax.jpa.abac.ParamValue;

public class AbacEntityManager implements EntityManager {

    private final static String DEFAULT_CONTEXT_NAME = "";

    private ActiveAbacContext activeAbacContext;
    private Session session;
    private EntityManager entityManager;
    private final Logger log = Logger.getLogger(AbacEntityManager.class.getName());
    private Set<Class<?>> securedEntities;
    private Map<String, ParamValue[]> filters;
    private Map<String, Set<String>> filtersInContext;
    private ElEvaluator elEvaluator;
    private Map<String, Map<String, Object>> evaluatedFilterParametersCache = new ConcurrentHashMap<>();

    public AbacEntityManager(EntityManager entityManager) {
        this.session = entityManager.unwrap(Session.class);
        elEvaluator = CDI.current().select(ElEvaluator.class).get();
        this.entityManager = entityManager;


        initAbacFilters();
    }

    // TODO: Move the initialization to factory class
    private void initAbacFilters() {
        Set<Class<?>> securedEntities = listSecuredEntities();
        filters = listAbacFilters(securedEntities);
        filtersInContext = listFiltersInContext(securedEntities);
    }

    protected Map<String, Set<String>> listFiltersInContext(Set<Class<?>> securedEntities) {
        Map<String, Set<String>> result = new HashMap<>();
        for (Class<?> securedEntity : securedEntities) {
            for (AbacRule annotation : listAbacAnnotations(securedEntity)) {
                Set<String> contexts = new HashSet<>(Arrays.asList(annotation.contexts()));
                if (contexts.isEmpty()) {
                    contexts.add(DEFAULT_CONTEXT_NAME);
                }
                Set<String> filters = new HashSet<>(Arrays.asList(annotation.filtersUsed()));
                contexts.stream()
                        .forEach((context) -> {
                            Set<String> filtersAlreadyAttached = result.computeIfAbsent(context,
                                    (c) -> new HashSet<>());
                            filtersAlreadyAttached.addAll(filters);
                        });
            }
        }
        return result;
    }

    protected Map<String, ParamValue[]> listAbacFilters(Set<Class<?>> securedEntities) {
        Map<String, ParamValue[]> filterParamValues = new HashMap<>();
        for (Class<?> securedEntity : securedEntities) {
            listAbacAnnotations(securedEntity).stream()
                    .forEach(annotation -> {
                        Stream.of(annotation.filtersUsed())
                                .forEach(filterName -> filterParamValues.put(filterName, annotation.paramValues()));
                    });
        }

        return securedEntities.stream()
                .flatMap(entityClass -> listAbacAnnotations(entityClass).stream())
                .map(annotation -> annotation.filtersUsed())
                .flatMap(Stream::of)
                .distinct()
                .collect(Collectors.toMap(filterName -> filterName, filterParamValues::get));
    }

    protected Set<Class<?>> listSecuredEntities() {
        if (securedEntities == null) {
            securedEntities = listAllManagedEntities().stream()
                    .filter(entityClass -> !listAbacAnnotations(entityClass).isEmpty())
                    .collect(Collectors.toSet());
        }
        return securedEntities;
    }

    private Set<Class<?>> listAllManagedEntities() {
        return entityManager.getMetamodel().getEntities().stream()
                .map(Type::getJavaType)
                .collect(Collectors.toSet());
    }

    protected List<AbacRule> listAbacAnnotations(Class<?> entityClass) {
        AbacRule[] result = entityClass.getAnnotationsByType(AbacRule.class);
        return Arrays.asList(result);
    }

    protected void updateAbacState() {
        String context = getActiveAbacContextName();
        filters.keySet().stream()
                .forEach((filterName) -> updateFilter(filterName, context));
    }

    private String getActiveAbacContextName() {
        if (activeAbacContext == null) {
            activeAbacContext = CDI.current().select(ActiveAbacContext.class).get();
        }
        return activeAbacContext.getName();
    }

    private void updateFilter(String filterName, String context) {
        boolean filterNeedsUpdating = isContextPresent(context)
                ? isFilterHasToBeUpdated(filterName, context)
                : isFilterHasToBeUpdated(filterName, DEFAULT_CONTEXT_NAME);
        if (filterNeedsUpdating) {
            Filter filter = session.enableFilter(filterName);
            List<ParamValue> params = Arrays.asList(filters.get(filterName));
            Set<String> filterParamNames = filter.getFilterDefinition().getParameterNames();
            params.stream()
                    .filter(paramValue -> filterParamNames.contains(paramValue.paramName()))
                    .forEach(paramValue -> filter.setParameter(paramValue.paramName(), evaluateFilterParam(filterName, paramValue)));
        } else {
            session.disableFilter(filterName);
        }
    }

    private Object evaluateFilterParam(String filterName, ParamValue paramValue) {
        Map<String, Object> evaluatedFilterParams = evaluatedFilterParametersCache.computeIfAbsent(filterName, (e) -> new ConcurrentHashMap<>());
        return evaluatedFilterParams.computeIfAbsent(paramValue.paramName(), (paramName) -> elEvaluator.evaluate(paramValue.elExpression()));
    }

    private boolean isFilterHasToBeUpdated(String filterName, String context) {
        return isContextPresent(context) && filtersInContext.get(context).contains(filterName);
    }

    private boolean isContextPresent(String context) {
        return filtersInContext.containsKey(context);
    }

    private <T> void checkEntityAccessRights(T entity) {
        checkEntityAccessRights(entity, entity.getClass());
    }

    private <T> void checkEntityAccessRights(T entity, Class<?> entityClass) {
        if (isAbacSecured(entityClass)) {
            String entityName = entityClass.getSimpleName();
            Query checkQuery = entityManager
                    .createQuery("SELECT 1 FROM " + entityName + " entity WHERE entity = :entity")
                    .setParameter("entity", entity);
            try {
                updateAbacState();
                checkQuery.getSingleResult();
            } catch (NoResultException e) {
                throw new EntityAccessDeniedException(entity);
            }
        }
    }

    private boolean isAbacSecured(Class<?> entityClass) {
        return securedEntities.contains(entityClass);
    }

/////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void persist(Object entity) {
        entityManager.persist(entity);
        checkEntityAccessRights(entity);
    }

    @Override
    public <T> T merge(T entity) {
        T result = (T) entityManager.merge(entity);
        checkEntityAccessRights(result);
        return result;
    }

    public void remove(Object entity) {
        if (!entityManager.contains(entity)) {
            checkEntityAccessRights(entity);
        }
        entityManager.remove(entity);
    }

    public <T> T find(Class<T> entityClass, Object primaryKey) {
        T result = entityManager.find(entityClass, primaryKey);
        if (result != null) {
            checkEntityAccessRights(result, entityClass);
        }
        return result;
    }

    public <T> T find(Class<T> entityClass, Object primaryKey, Map<String, Object> properties) {
        T result = entityManager.find(entityClass, primaryKey, properties);
        if (result != null) {
            checkEntityAccessRights(result, entityClass);
        }
        return result;
    }

    public <T> T find(Class<T> entityClass, Object primaryKey, LockModeType lockMode) {
        T result = entityManager.find(entityClass, primaryKey, lockMode);
        if (result != null) {
            checkEntityAccessRights(result, entityClass);
        }
        return result;
    }

    public <T> T find(Class<T> entityClass, Object primaryKey, LockModeType lockMode, Map<String, Object> properties) {
        T result = entityManager.find(entityClass, primaryKey, lockMode, properties);
        if (result != null) {
            checkEntityAccessRights(result, entityClass);
        }
        return result;
    }

    public Query createQuery(String qlString) {
        updateAbacState();
        return entityManager.createQuery(qlString);
    }

    public <T> TypedQuery<T> createQuery(CriteriaQuery<T> criteriaQuery) {
        updateAbacState();
        return entityManager.createQuery(criteriaQuery);
    }

    public Query createQuery(CriteriaUpdate updateQuery) {
        // TODO: Check the ABAC criteria behaviour
        updateAbacState();
        return entityManager.createQuery(updateQuery);
    }

    public Query createQuery(CriteriaDelete deleteQuery) {
        // TODO: Check the ABAC criteria behaviour
        updateAbacState();
        return entityManager.createQuery(deleteQuery);
    }

    public <T> TypedQuery<T> createQuery(String qlString, Class<T> resultClass) {
        updateAbacState();
        return entityManager.createQuery(qlString, resultClass);
    }

    public Query createNamedQuery(String name) {
        updateAbacState();
        return entityManager.createNamedQuery(name);
    }

    public <T> TypedQuery<T> createNamedQuery(String name, Class<T> resultClass) {
        updateAbacState();
        return entityManager.createNamedQuery(name, resultClass);
    }

    public void joinTransaction() {
        entityManager.joinTransaction();
        initAbacFilters();
    }

    // The following operations are not implemented with ABAC security support

    public CriteriaBuilder getCriteriaBuilder() {
        return entityManager.getCriteriaBuilder();
    }

    public <T> T getReference(Class<T> entityClass, Object primaryKey) {
        return entityManager.getReference(entityClass, primaryKey);
    }

    public void flush() {
        entityManager.flush();
    }

    public void setFlushMode(FlushModeType flushMode) {
        entityManager.setFlushMode(flushMode);
    }

    public FlushModeType getFlushMode() {
        return entityManager.getFlushMode();
    }

    public void lock(Object entity, LockModeType lockMode) {
        entityManager.lock(entity, lockMode);
    }

    public void lock(Object entity, LockModeType lockMode, Map<String, Object> properties) {
        entityManager.lock(entity, lockMode, properties);
    }

    public void refresh(Object entity) {
        entityManager.refresh(entity);
    }

    public void refresh(Object entity, Map<String, Object> properties) {
        entityManager.refresh(entity, properties);
    }

    public void refresh(Object entity, LockModeType lockMode) {
        entityManager.refresh(entity, lockMode);
    }

    public void refresh(Object entity, LockModeType lockMode, Map<String, Object> properties) {
        entityManager.refresh(entity, lockMode, properties);
    }

    public void clear() {
        entityManager.clear();
    }

    public void detach(Object entity) {
        entityManager.detach(entity);
    }

    public boolean contains(Object entity) {
        return entityManager.contains(entity);
    }

    public LockModeType getLockMode(Object entity) {
        return entityManager.getLockMode(entity);
    }

    public void setProperty(String propertyName, Object value) {
        entityManager.setProperty(propertyName, value);
    }

    public Map<String, Object> getProperties() {
        return entityManager.getProperties();
    }

    public Query createNativeQuery(String sqlString) {
        return entityManager.createNativeQuery(sqlString);
    }

    public Query createNativeQuery(String sqlString, Class resultClass) {
        return entityManager.createNativeQuery(sqlString, resultClass);
    }

    public Query createNativeQuery(String sqlString, String resultSetMapping) {
        return entityManager.createNativeQuery(sqlString, resultSetMapping);
    }

    public StoredProcedureQuery createNamedStoredProcedureQuery(String name) {
        return entityManager.createNamedStoredProcedureQuery(name);
    }

    public StoredProcedureQuery createStoredProcedureQuery(String procedureName) {
        return entityManager.createStoredProcedureQuery(procedureName);
    }

    public StoredProcedureQuery createStoredProcedureQuery(String procedureName, Class... resultClasses) {
        return entityManager.createStoredProcedureQuery(procedureName, resultClasses);
    }

    public StoredProcedureQuery createStoredProcedureQuery(String procedureName, String... resultSetMappings) {
        return entityManager.createStoredProcedureQuery(procedureName, resultSetMappings);
    }

    public boolean isJoinedToTransaction() {
        return entityManager.isJoinedToTransaction();
    }

    public <T> T unwrap(Class<T> cls) {
        return entityManager.unwrap(cls);
    }

    public Object getDelegate() {
        return entityManager.getDelegate();
    }

    public void close() {
        entityManager.close();
    }

    public boolean isOpen() {
        return entityManager.isOpen();
    }

    public EntityTransaction getTransaction() {
        return entityManager.getTransaction();
    }

    public EntityManagerFactory getEntityManagerFactory() {
        return entityManager.getEntityManagerFactory();
    }

    public Metamodel getMetamodel() {
        return entityManager.getMetamodel();
    }

    public <T> EntityGraph<T> createEntityGraph(Class<T> rootType) {
        return entityManager.createEntityGraph(rootType);
    }

    public EntityGraph<?> createEntityGraph(String graphName) {
        return entityManager.createEntityGraph(graphName);
    }

    public EntityGraph<?> getEntityGraph(String graphName) {
        return entityManager.getEntityGraph(graphName);
    }

    public <T> List<EntityGraph<? super T>> getEntityGraphs(Class<T> entityClass) {
        return entityManager.getEntityGraphs(entityClass);
    }

}

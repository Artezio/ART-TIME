package com.artezio.arttime.repositories;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.persistence.*;
import javax.persistence.criteria.*;
import org.hibernate.annotations.QueryHints;

public class Query<T> {

    private Class<T> type;

    private EntityManager entityManager;
    private CriteriaBuilder criteriaBuilder;
    private CriteriaQuery<T> criteriaQuery;
    private Root<T> root;
    protected EntityGraph<?> entityGraph;
    private CriteriaQuery<Tuple> tupleCriteriaQuery;

    private List<Predicate> predicates = new ArrayList<>();
    private boolean distinct = false;
    private boolean cacheable = true;

    public Query(EntityManager entityManager) {
        this(entityManager, false);
    }
    
    public Query(EntityManager entityManager, boolean tupleResult) {
        type = getType();
        this.entityManager = entityManager;
        criteriaBuilder = entityManager.getCriteriaBuilder();
        if (tupleResult) {
            tupleCriteriaQuery = getCriteriaBuilder().createTupleQuery();
            root = tupleCriteriaQuery.from(type);
        } else {
            criteriaQuery = criteriaBuilder.createQuery(type);
            root = criteriaQuery.from(type);
        }
    }

    @SuppressWarnings("unchecked")
    private Class<T> getType() {
        return (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    public Query<T> distinct() {
        distinct = true;
        return this;
    }

    public Query<T> uncached() {
        cacheable = false;
        return this;
    }

    public List<T> list() {
        return buildQuery().getResultList();
    }

    public T getSingleResult() {
        return buildQuery().getSingleResult();
    }
    
    public T getSingleResultOrNull() {
        try {
            return getSingleResult();
        } catch (NoResultException ignored) {
            return null;
        }
    }
    
    protected void add(Predicate predicate) {
        predicates.add(predicate);
    }

    protected TypedQuery<T> buildQuery() {
        CriteriaQuery<T> selectQuery = criteriaQuery.select(root).distinct(distinct);
        addWhereClause(selectQuery);
        return createQuery(selectQuery);
    }

    protected <K> TypedQuery<K> createQuery(CriteriaQuery<K> selectQuery) {
        return createQuery(selectQuery, cacheable);
    }
    
    protected <K> TypedQuery<K> createQuery(CriteriaQuery<K> selectQuery, boolean cacheable) {
        return (entityGraph != null
                ? entityManager.createQuery(selectQuery).setHint("javax.persistence.loadgraph", entityGraph)
                : entityManager.createQuery(selectQuery))
                .setHint(QueryHints.CACHEABLE, cacheable);
    }

    protected <K> void addWhereClause(CriteriaQuery<K> selectQuery) {
        predicates.stream().reduce(criteriaBuilder::and).ifPresent(selectQuery::where);
    }

    protected Root<T> getRoot() {
        if (root == null) root = criteriaQuery.from(type);
        return root;
    }

    protected CriteriaBuilder getCriteriaBuilder() {
        return criteriaBuilder;
    }

    protected CriteriaQuery<T> getCriteriaQuery() {
        return criteriaQuery;
    }

    // Most databases don't allow "WHERE .. IN ()" clause with empty () parenthesis
    protected void addInPredicate(Expression<?> path, Collection<?> values) {
        if (!values.isEmpty()) {
            add(path.in(values));
        }
    }
    
    protected CriteriaQuery<Tuple> getTupleCriteriaQuery() {
        return tupleCriteriaQuery;
    }
    
}

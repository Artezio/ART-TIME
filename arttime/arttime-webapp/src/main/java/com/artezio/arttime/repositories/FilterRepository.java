package com.artezio.arttime.repositories;

import com.artezio.arttime.admin_tool.cache.WebCached;
import com.artezio.arttime.filter.Filter;
import com.artezio.arttime.filter.Filter_;
import com.artezio.arttime.web.interceptors.FacesMessage;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.JoinType;

@Stateless
public class FilterRepository {
    
    @PersistenceContext
    private EntityManager entityManager;

    @WebCached(resetCache = true)
    public Filter create(Filter filter) {
        filter.setId(null);
        entityManager.persist(filter);
        return filter;
    }

    @WebCached(resetCache = true)
    public Filter update(Filter filter) {
        return entityManager.merge(filter);
    }

    @FacesMessage(onCompleteMessageKey = "message.filterIsDeleted")
    @WebCached(resetCache = true)
    public void delete(Filter filter) {
        filter = entityManager.find(Filter.class, filter.getId());
        entityManager.remove(filter);
    }
    
    @WebCached(resetCache = true)
    public FilterQuery query() {
        return new FilterQuery();
    }

    public class FilterQuery extends Query<Filter> {

        public FilterQuery() {
            super(entityManager);
        }
        
        public FilterQuery(EntityManager entityManager) {
            super(entityManager);
        }
        
        public FilterQuery name(String name) {
            add(getCriteriaBuilder().equal(getRoot().get(Filter_.name), name));
            return this;
        }
        
        public FilterQuery fetchProjects() {
            getRoot().fetch(Filter_.projects, JoinType.LEFT);
            return this;
        }
        
        public FilterQuery fetchDepartments() {
            getRoot().fetch(Filter_.departments, JoinType.LEFT);
            return this;
        }
        
        public FilterQuery fetchEmployees() {
            getRoot().fetch(Filter_.employees, JoinType.LEFT);
            return this;
        }
        
        public FilterQuery fetchHourTypes() {
            getRoot().fetch(Filter_.hourTypes, JoinType.LEFT);
            return this;
        }
        
    }

}

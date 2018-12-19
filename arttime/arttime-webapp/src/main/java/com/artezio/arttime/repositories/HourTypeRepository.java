package com.artezio.arttime.repositories;

import com.artezio.arttime.admin_tool.cache.WebCached;
import com.artezio.arttime.admin_tool.cache.WebCached.Scope;
import com.artezio.arttime.admin_tool.log.DetailedLogged;
import com.artezio.arttime.admin_tool.log.Log;
import com.artezio.arttime.datamodel.HourType;
import com.artezio.arttime.datamodel.HourType_;
import com.artezio.arttime.exceptions.ActualTimeRemovalException;
import com.artezio.arttime.web.interceptors.FacesMessage;
import org.hibernate.annotations.QueryHints;

import javax.ejb.Stateless;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.List;

@Stateless
public class HourTypeRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public HourType find(Long id) {
        return query()
                .id(id)
                .getSingleResultOrNull();
    }

    @Log(logParams = true)
    @FacesMessage(onCompleteMessageKey = "message.hourTypeIsSaved")
    public HourType create(@DetailedLogged HourType hourType) {
        entityManager.persist(hourType);
        return hourType;
    }

    @FacesMessage(onCompleteMessageKey = "message.hourTypeIsDeleted")
    @WebCached(resetCache = true)
    @Log(logParams = true)
    public void remove(@DetailedLogged HourType hourType) throws ActualTimeRemovalException {
        if (hourType.isActualTime()) {
            throw new ActualTimeRemovalException("Attempt to delete an actual time.");
        }
        HourType type = entityManager.find(HourType.class, hourType.getId());
        entityManager.remove(type);
    }

    @Log(logParams = true)
    @FacesMessage(onCompleteMessageKey = "message.hourTypeIsSaved")
    public HourType update(@DetailedLogged HourType hourType) {
        return entityManager.merge(hourType);
    }

    public void resetActualTime() {
        entityManager.createQuery("UPDATE HourType h SET h.actualTime = false").executeUpdate();
    }

    public HourTypeQuery query() {
        return new HourTypeQuery();
    }

    public class HourTypeQuery extends Query<HourType> {

        HourTypeQuery() {
            super(entityManager);
        }

        public HourTypeQuery id(Long id) {
            add(getCriteriaBuilder().equal(getRoot().get(HourType_.id), id));
            return this;
        }

        public HourTypeQuery actualTime() {
            add(getCriteriaBuilder().isTrue(getRoot().get(HourType_.actualTime)));
            return this;
        }

    }

}

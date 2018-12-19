package com.artezio.arttime.repositories;

import com.artezio.arttime.admin_tool.cache.WebCached;
import com.artezio.arttime.admin_tool.cache.WebCached.Scope;
import com.artezio.arttime.admin_tool.log.DetailedLogged;
import com.artezio.arttime.admin_tool.log.Log;
import com.artezio.arttime.datamodel.Day;
import com.artezio.arttime.datamodel.Employee;
import com.artezio.arttime.datamodel.Period;
import com.artezio.arttime.datamodel.WorkdaysCalendar;
import org.hibernate.annotations.QueryHints;

import javax.ejb.Stateless;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TemporalType;
import java.util.List;

@Named
@Stateless
public class WorkdaysCalendarRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public WorkdaysCalendar findWorkdaysCalendar(Long id) {
        return entityManager.find(WorkdaysCalendar.class, id);
    }

    @Log(logParams = true)
    public WorkdaysCalendar create(@DetailedLogged WorkdaysCalendar workdaysCalendar) {
        entityManager.persist(workdaysCalendar);
        return workdaysCalendar;
    }

    @WebCached(scope = Scope.REQUEST_SCOPED)
    public List<WorkdaysCalendar> getWorkdaysCalendars() {
        return entityManager.createQuery("SELECT w FROM WorkdaysCalendar w", WorkdaysCalendar.class)
                .setHint(QueryHints.CACHEABLE, "true").getResultList();
    }

    public WorkdaysCalendar findByDepartment(String department) {
        try {
            return entityManager.createQuery(
                    "SELECT w FROM WorkdaysCalendar w " +
                    "WHERE :department member of w.departments", WorkdaysCalendar.class)
                    .setParameter("department", department)
                    .setHint(QueryHints.CACHEABLE, "true")
                    .getSingleResult();
        } catch (NoResultException ignored) {
            return null;
        }
    }

    @Log(logParams = true)
    @WebCached(scope = Scope.REQUEST_SCOPED, resetCache = true)
    public void remove(@DetailedLogged WorkdaysCalendar workdaysCalendar) {
        workdaysCalendar = findWorkdaysCalendar(workdaysCalendar.getId());
        entityManager.createQuery("DELETE FROM Day d WHERE d.workdaysCalendar = :calendar")
                .setParameter("calendar", workdaysCalendar).executeUpdate();
        entityManager.remove(workdaysCalendar);
    }

    public void update(List<Day> days) {
        for (Day day : days) {
            if (day.getId() == null) {
                entityManager.persist(day);
            } else {
                entityManager.merge(day);
            }
        }
    }

    @WebCached(scope = Scope.REQUEST_SCOPED)
    public List<Day> getSpecialDays(WorkdaysCalendar workdaysCalendar, Period period) {
        String query = "SELECT d FROM Day d " +
                "WHERE d.workdaysCalendar = :calendar " +
                "AND d.date >= :start AND d.date <= :finish";
        return entityManager.createQuery(query, Day.class)
                .setParameter("calendar", workdaysCalendar)
                .setParameter("start", period.getStart(), TemporalType.DATE)
                .setParameter("finish", period.getFinish(), TemporalType.DATE)
                .setHint(QueryHints.CACHEABLE, "true")
                .getResultList();
    }

    @Log(logParams = true)
    public void update(@DetailedLogged WorkdaysCalendar workdaysCalendar, List<Day> days) {
        entityManager.merge(workdaysCalendar);
        update(days);
    }

    @Log(logParams = true)
    public WorkdaysCalendar update(@DetailedLogged WorkdaysCalendar workdaysCalendar) {
        return entityManager.merge(workdaysCalendar);
    }

    @WebCached
    public WorkdaysCalendar findDefaultCalendar(String department) {
        try {
            return entityManager
                    .createQuery("SELECT wc FROM WorkdaysCalendar wc "
                            + "LEFT JOIN wc.departments d "
                            + "WHERE d = :dapartment", WorkdaysCalendar.class)
                    .setParameter("dapartment", department)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public WorkdaysCalendar attachAndRefresh(WorkdaysCalendar detachedEntity) {
        return entityManager.createQuery("SELECT c FROM WorkdaysCalendar c WHERE c = :detachedEntity", WorkdaysCalendar.class)
                .setParameter("detachedEntity", detachedEntity)
                .getSingleResult();
    }

}

package com.artezio.arttime.repositories;

import com.artezio.arttime.admin_tool.cache.WebCached;
import com.artezio.arttime.admin_tool.cache.WebCached.Scope;
import com.artezio.arttime.admin_tool.log.DetailedLogged;
import com.artezio.arttime.admin_tool.log.Log;
import com.artezio.arttime.datamodel.*;
import com.artezio.arttime.utils.CalendarUtils;
import com.artezio.javax.jpa.abac.hibernate.AbacEntityManager;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;
import javax.persistence.Tuple;
import javax.persistence.criteria.*;

@Named
@Stateless
@WebCached(scope = Scope.REQUEST_SCOPED)
public class HoursRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public List<Hours> getActualHours(List<Employee> employees, Period period, boolean onlyApproved) {
        List<Hours> result = new ArrayList<>();
        if (!employees.isEmpty())
            result = new HoursQuery()
                    .period(period)
                    .employees(employees)
                    .actualTime()
                    .onlyApproved(onlyApproved)
                    .uncached()
                    .list();
        return result;
    }

    public void lock(Employee employee) {
        Employee entityToLock = entityManager.find(Employee.class, employee.getUserName());
        entityManager.lock(entityToLock, LockModeType.PESSIMISTIC_WRITE);
    }

    @Log(logParams = true)
    public Hours create(@DetailedLogged Hours hours) {
        entityManager.persist(hours);
        return hours;
    }

    @Log(logParams = true)
    public Hours update(@DetailedLogged Hours hours) {
        return entityManager.merge(hours);
    }

    @Log(logParams = true)
    public void remove(@DetailedLogged Hours hours) {
        entityManager.remove(hours);
    }
    
    public Map<Employee, Map<Boolean, BigDecimal>> getActualTimeByEmployeeAndApproval(List<Employee> employees,
            Period period) {
        return new HoursQuery(true)
                .period(period)
                .employees(employees)
                .actualTime()
                .withQuantity()
                .getTimeByEmployeeAndApproval();
    }

    public Map<Employee, List<Employee>> getManagersForApproveByEmployee(List<Employee> employees, Period period) {
        List<Tuple> managersAndEmployees = new HoursQuery(true)
                .period(period)
                .employees(employees)
                .actualTime()
                .withQuantity()
                .unapproved()
                .getManagers();
        return collectManagersByEmployee(managersAndEmployees);
    }

    public Map<Employee, List<Employee>> getManagersByEmployee(List<Employee> employees, Period period) {
        List<Tuple> managersAndEmployees = new HoursQuery(true)
                .period(period)
                .employees(employees)
                .actualTime()
                .withQuantity()
                .getManagers();
        return collectManagersByEmployee(managersAndEmployees);
    }

    private Map<Employee, List<Employee>> collectManagersByEmployee(List<Tuple> managerAndEmployee) {
        return managerAndEmployee.stream().collect(Collectors.groupingBy(t -> t.get(0, Employee.class),
                Collectors.mapping(t -> t.get(1, Employee.class), Collectors.toList())));
    }

    @WebCached(resetCache = true)
    public HoursQuery query() {
        return new HoursQuery();
    }

    @WebCached(resetCache = true)
    public HoursQuery tupleQuery() {
        return new HoursQuery(true);
    }

    public class HoursQuery extends Query<Hours> {

        private Fetch<Hours, Project> projectFetch;

        public HoursQuery() {
            super(entityManager);
        }

        public HoursQuery(boolean tupleResult) {
            super(entityManager, tupleResult);
        }

        public HoursQuery date(Date date) {
            add(getCriteriaBuilder().equal(getRoot().get(Hours_.date), CalendarUtils.convertToSqlDate(date)));
            return this;
        }

        public HoursQuery hourType(Long id) {
            add(getCriteriaBuilder().equal(getRoot().get(Hours_.type).get(HourType_.id), id));
            return this;
        }

        public HoursQuery project(String code) {
            add(getCriteriaBuilder().equal(getRoot().get(Hours_.project).get(Project_.code), code));
            return this;
        }

        public HoursQuery employee(String username) {
            add(getCriteriaBuilder().equal(getRoot().get(Hours_.employee).get(Employee_.userName), username));
            return this;
        }

        public BigDecimal getTime() {
            Expression<BigDecimal> sumSelector = getCriteriaBuilder().sum(getRoot().get(Hours_.quantity));
            getTupleCriteriaQuery().multiselect(sumSelector);
            addWhereClause(getTupleCriteriaQuery());
            BigDecimal result = createQuery(getTupleCriteriaQuery(), false).getSingleResult().get(0, BigDecimal.class);
            return Optional.ofNullable(result).orElse(BigDecimal.ZERO);
        }

        public HoursQuery actualTime() {
            add(getCriteriaBuilder().isTrue(getRoot().get(Hours_.type).get(HourType_.actualTime)));
            return this;
        }

        public HoursQuery onlyApproved(boolean onlyApproved) {
            return onlyApproved ? approved(true) : approved(null);
        }

        public HoursQuery unapproved() {
            return approved(false);
        }

        public HoursQuery approved(Boolean approved) {
            Optional.ofNullable(approved)
                    .ifPresent(a -> add(getCriteriaBuilder().equal(getRoot().get(Hours_.approved), a)));
            return this;
        }

        public HoursQuery projects(Collection<Project> projects) {
            addInPredicate(getRoot().get(Hours_.project), projects);
            return this;
        }

        public HoursQuery types(List<HourType> hourTypes) {
            addInPredicate(getRoot().get(Hours_.type), hourTypes);
            return this;
        }

        public HoursQuery period(Period period) {
            java.sql.Date start = CalendarUtils.convertToSqlDate(period.getStart());
            java.sql.Date finish = CalendarUtils.convertToSqlDate(period.getFinish());

            add(getCriteriaBuilder().greaterThanOrEqualTo(getRoot().get(Hours_.date), start));
            add(getCriteriaBuilder().lessThanOrEqualTo(getRoot().get(Hours_.date), finish));
            return this;
        }

        public HoursQuery employees(Collection<Employee> employees) {
            addInPredicate(getRoot().get(Hours_.employee), employees);
            return this;
        }

        public HoursQuery employee(Employee employee) {
            add(getCriteriaBuilder().equal(getRoot().get(Hours_.employee), employee));
            return this;
        }

        public Map<Employee, Map<Boolean, BigDecimal>> getTimeByEmployeeAndApproval() {
            Join<Hours, Employee> employeeSelector = getRoot().join(Hours_.employee, JoinType.LEFT);
            Path<Boolean> approvedSelector = getRoot().get(Hours_.approved);
            Expression<BigDecimal> sumSelector = getCriteriaBuilder().sum(getRoot().get(Hours_.quantity));

            getTupleCriteriaQuery().multiselect(employeeSelector, approvedSelector, sumSelector);
            addWhereClause(getTupleCriteriaQuery());
            getTupleCriteriaQuery().groupBy(employeeSelector, approvedSelector);
            return createQuery(getTupleCriteriaQuery(), false).getResultList().stream().collect(collectTimeByEmployeeAndApproval());
        }

        private Collector<Tuple, ?, Map<Employee, Map<Boolean, BigDecimal>>> collectTimeByEmployeeAndApproval() {
            return Collectors.toMap(t -> t.get(0, Employee.class), t -> createApprovedTimeMap(t),
                    (oldValue, newValue) -> {
                        oldValue.putAll(newValue);
                        return oldValue;
                    });
        }

        private Map<Boolean, BigDecimal> createApprovedTimeMap(Tuple t) {
            HashMap<Boolean, BigDecimal> result = new HashMap<>();
            result.put(t.get(1, Boolean.class), t.get(2, BigDecimal.class));
            return result;
        }

        public HoursQuery withQuantity() {
            add(getRoot().get(Hours_.quantity).isNotNull());
            return this;
        }

        public List<Tuple> getManagers() {
            Path<Employee> employeeSelector = getRoot().get(Hours_.employee);
            Expression<Set<Employee>> managersSelector = getRoot().get(Hours_.project).get(Project_.managers);

            getTupleCriteriaQuery().multiselect(employeeSelector, managersSelector).distinct(true);
            addWhereClause(getTupleCriteriaQuery());
            return createQuery(getTupleCriteriaQuery(), false).getResultList();
        }

        public HoursQuery approved() {
            return approved(true);
        }

        public HoursQuery employeeUserNames(List<String> employeeUserNames) {
            addInPredicate(getRoot().get(Hours_.employee).get(Employee_.userName), employeeUserNames);
            return this;
        }

        public HoursQuery typeIds(List<Long> hourTypeIds) {
            addInPredicate(getRoot().get(Hours_.type).get(HourType_.id), hourTypeIds);
            return this;
        }

        public HoursQuery projectIds(List<Long> projectIds) {
            fetchProject();
            addInPredicate(((Path<Project>)projectFetch).get(Project_.id), projectIds);
            return this;
        }

        public HoursQuery departments(List<String> departments) {
            addInPredicate(getRoot().get(Hours_.employee).get(Employee_.department), departments);
            return this;
        }

        public HoursQuery fetchProject() {
            if (projectFetch == null) {
                projectFetch = getRoot().fetch(Hours_.project, JoinType.LEFT);
            }
            return this;
        }

        public HoursQuery fetchProjectAccountableHours() {
            fetchProject();
            projectFetch.fetch(Project_.accountableHours, JoinType.LEFT);
            return this;
        }

        public HoursQuery fetchProjectTeam() {
            fetchProject();
            projectFetch.fetch(Project_.team, JoinType.LEFT);
            return this;
        }
    }

}

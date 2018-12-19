package com.artezio.arttime.repositories;

import com.artezio.arttime.admin_tool.cache.WebCached;
import com.artezio.arttime.admin_tool.cache.WebCached.Scope;
import com.artezio.arttime.admin_tool.log.DetailedLogged;
import com.artezio.arttime.datamodel.Employee;
import com.artezio.arttime.datamodel.Employee_;
import com.artezio.arttime.datamodel.Project_;
import com.artezio.arttime.filter.Filter;
import com.artezio.javax.jpa.abac.hibernate.AbacEntityManager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;

import org.hibernate.Hibernate;

@Named
@Stateless
public class EmployeeRepository implements Serializable {

    @PersistenceContext
    private EntityManager entityManager;

    @WebCached(scope = Scope.REQUEST_SCOPED)
    public Employee find(String userName) {
        try {
            return get(userName);
        } catch (NoResultException e) {
            return null;
        }
    }

    @WebCached(scope = Scope.REQUEST_SCOPED)
    public Employee get(String userName) {
        return new EmployeeQuery()
                .userName(userName)
                .fetchAccessibleDepartments()
                .getSingleResult();
    }

    public List<Employee> getAll() {
        List<Employee> result = new EmployeeQuery().list();
        result.sort(Employee.NAME_COMPARATOR);
        return result;
    }

    @WebCached(scope = Scope.VIEW_SCOPED)
    public List<Employee> getAllInitialized() {
        List<Employee> result = new EmployeeQuery()
                .fetchAccessibleDepartments()
                .list();
        result.sort(Employee.NAME_COMPARATOR);
        return result;
    }

    public Employee update(@DetailedLogged Employee employee) {
        entityManager.merge(employee);
        return employee;
    }

    public void update(Collection<Employee> employees) {
        employees.forEach(this::update);
    }

    @WebCached(scope = Scope.REQUEST_SCOPED)
    public List<Employee> findByDepartment(String department) {
        return new EmployeeQuery()
                .department(department)
                .list();
    }

    @WebCached(scope = Scope.REQUEST_SCOPED)
    public List<Employee> getEmployeesByDepartments(Filter filter) {
        return new EmployeeQuery()
                .filter(filter)
                .list();
    }

    @WebCached(scope = Scope.REQUEST_SCOPED)
    public List<Employee> getEmployees(List<String> userNames) {
        if (userNames != null && !userNames.isEmpty()) {
            return new EmployeeQuery()
                    .userNames(userNames)
                    .list();
        } else {
            return new ArrayList<>();
        }
    }

    public Employee create(Employee employee) {
        if (!persisted(employee)) {
            entityManager.persist(employee);
        }
        return employee;
    }

    public Employee attachAndRefresh(Employee detachedEntity) {
        return entityManager.createQuery("SELECT e FROM Employee e WHERE e = :detachedEntity", Employee.class)
                .setParameter("detachedEntity", detachedEntity)
                .getSingleResult();
    }

    public List<Employee> fetchComplete(List<Employee> employees) {
        return employees.stream()
                .map(this::fetchComplete)
                .collect(Collectors.toList());
    }

    public Employee fetchComplete(Employee employee) {
        employee = attachAndRefresh(employee);
        Hibernate.initialize(employee.getAccessibleDepartments());
        return employee;
    }

    public EmployeeQuery query() {
        return new EmployeeQuery();
    }

    protected boolean persisted(Employee employee) {
    	return entityManager.find(Employee.class, employee.getUserName()) != null;
    }

    public class EmployeeQuery extends Query<Employee> {

        private Path<String> accessToDepartmentsPath;
        private boolean eagerLoadAccessibleDepartments = false;

        private EmployeeQuery() {
            super(entityManager);
        }

        public EmployeeQuery employees(List<Employee> employees) {
            addInPredicate(getRoot(), employees);
            return this;
        }

        public EmployeeQuery userName(String userName) {
            add(getCriteriaBuilder().equal(getRoot().get(Employee_.userName),  userName));
            return this;
        }

        public EmployeeQuery userNames(List<String> userNames) {
            addInPredicate(getRoot().get(Employee_.userName), userNames);
            return this;
        }

        public EmployeeQuery department(String department) {
            add(getCriteriaBuilder().equal(getRoot().get(Employee_.department), department));
            return this;
        }

        public EmployeeQuery departments(Collection<String> departments) {
            addInPredicate(getRoot().get(Employee_.department), departments);
            return this;
        }

        public EmployeeQuery filter(Filter filter) {
            if (filter.containsAtLeastOneEmployee()) {
                employees(filter.getEmployees());
            }
            if (filter.containsAtLeastOneDepartment()) {
                departments(filter.getDepartments());
            }
            return this;
        }

        public EmployeeQuery notFormer() {
            add(getCriteriaBuilder().isFalse(getRoot().get(Employee_.former)));
            return this;
        }

        /**
         * @implNote Hibernate bug https://hibernate.atlassian.net/browse/HHH-6686 makes use of "notEmpty" impossible
         */
        public EmployeeQuery withAccessToAnyDepartment() {
            fetchAccessibleDepartments();
            add(getCriteriaBuilder().isNotNull(accessToDepartmentsPath));
            return this;
        }

        public EmployeeQuery withAccessToDepartment(String department) {
            fetchAccessibleDepartments();
            add(accessToDepartmentsPath.in(department));
            return this;
        }

        public EmployeeQuery fetchAccessibleDepartments() {
            if (accessToDepartmentsPath == null) {
                accessToDepartmentsPath = (Path<String>)getRoot().fetch(Employee_.accessibleDepartments, JoinType.LEFT);
            }
            return this;
        }

        @Override
        public List<Employee> list() {
            return getWithCheckForEagerLoading(super.list());
        }

        private List<Employee> getWithCheckForEagerLoading(List<Employee> employees) {
            if (eagerLoadAccessibleDepartments) {
                employees.forEach(employee -> Hibernate.initialize(employee.getAccessibleDepartments()));
            }
            return employees;
        }

    }
}

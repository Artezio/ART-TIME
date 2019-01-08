package com.artezio.arttime.services;

import static com.artezio.arttime.admin_tool.cache.WebCached.Scope.REQUEST_SCOPED;
import static com.artezio.arttime.admin_tool.cache.WebCached.Scope.VIEW_SCOPED;
import static com.artezio.arttime.security.AbacContexts.MANAGE_EMPLOYEES;
import static com.artezio.arttime.security.AbacContexts.VIEW_TIMESHEET;
import static com.artezio.arttime.security.auth.UserRoles.*;

import java.io.Serializable;
import java.security.Principal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.NoResultException;

import com.artezio.arttime.admin_tool.cache.WebCached;
import com.artezio.arttime.datamodel.Employee;
import com.artezio.arttime.datamodel.WorkdaysCalendar;
import com.artezio.arttime.filter.Filter;
import com.artezio.arttime.repositories.EmployeeRepository;
import com.artezio.arttime.web.interceptors.FacesMessage;
import com.artezio.javax.jpa.abac.AbacContext;

@Named
@Stateless
@RolesAllowed(EXEC_ROLE)
public class EmployeeService implements Serializable {

    private static final long serialVersionUID = -4811148727200120422L;
    
    @Inject
    private EmployeeRepository employeeRepository;
    @Inject
    private WorkdaysCalendarService workdaysCalendarService;
    @Inject
    private Principal principal;

    @RolesAllowed({EXEC_ROLE, OFFICE_MANAGER})
    @AbacContext(MANAGE_EMPLOYEES)
    public Employee find(String username) {
        try {
            return employeeRepository.query()
                    .userName(username)
                    .fetchAccessibleDepartments()
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @RolesAllowed({EXEC_ROLE, PM_ROLE, OFFICE_MANAGER, SYSTEM_ROLE})
    public void create(Employee employee) {
        employeeRepository.create(employee);
    }

    @RolesAllowed({EXEC_ROLE, OFFICE_MANAGER, SYSTEM_ROLE})
    @AbacContext(MANAGE_EMPLOYEES)
    @FacesMessage(onCompleteMessageKey = "message.employeeSettingsAreSaved")
    public Employee update(Employee employee) {
        return employeeRepository.update(employee);
    }

    @RolesAllowed({EXEC_ROLE, OFFICE_MANAGER, SYSTEM_ROLE})
    @AbacContext(MANAGE_EMPLOYEES)
    public void update(Collection<Employee> employees) {
        employees.forEach(this::update);
    }

    @RolesAllowed({EXEC_ROLE, OFFICE_MANAGER, SYSTEM_ROLE})
    @AbacContext(MANAGE_EMPLOYEES)
    public void setCalendar(Collection<Employee> employees) {
        Map<String, WorkdaysCalendar> calendars = workdaysCalendarService.getCalendarsByDepartments();
        employees.forEach(employee -> employee.setCalendar(calendars.get(employee.getDepartment())));
    }

    @RolesAllowed(EXEC_ROLE)
    @AbacContext(MANAGE_EMPLOYEES)
    public List<Employee> getEmployeesHavingAccessToAnyDepartment() {
        return employeeRepository.query()
                .withAccessToAnyDepartment()
                .list();
    }

    @RolesAllowed({EXEC_ROLE, OFFICE_MANAGER, PM_ROLE})
    @WebCached(scope = REQUEST_SCOPED)
    public List<Employee> getEffortsEmployees() {
        return getAll();
    }

    @RolesAllowed({EXEC_ROLE, OFFICE_MANAGER, PM_ROLE, ACCOUNTANT})
    @WebCached(scope = REQUEST_SCOPED)
    public List<Employee> getEffortsEmployees(Filter filter) {
        return employeeRepository.query()
                .filter(filter)
                .list();
    }

    @RolesAllowed({EXEC_ROLE, OFFICE_MANAGER, SYSTEM_ROLE})
    @AbacContext(MANAGE_EMPLOYEES)
    @WebCached(scope = VIEW_SCOPED)
    public List<Employee> getManagedEmployees() {
        return getAll();
    }

    @RolesAllowed({EXEC_ROLE, PM_ROLE, OFFICE_MANAGER, ACCOUNTANT, SYSTEM_ROLE})
    public List<Employee> getAll() {
        return employeeRepository.query()
                .list();
    }

    @PermitAll
    @AbacContext(VIEW_TIMESHEET)
    @WebCached(scope = REQUEST_SCOPED)
    public Optional<Employee> getLoggedEmployee() {
        return Optional.ofNullable(employeeRepository.query()
                .userName(principal.getName())
                .fetchAccessibleDepartments()
                .getSingleResultOrNull());
    }

}

package com.artezio.arttime.services;

import com.artezio.arttime.admin_tool.cache.WebCached;
import com.artezio.arttime.admin_tool.cache.WebCached.Scope;
import com.artezio.arttime.datamodel.Employee;
import com.artezio.arttime.datamodel.WorkdaysCalendar;
import com.artezio.arttime.repositories.DepartmentRepository;
import com.artezio.arttime.repositories.EmployeeRepository;
import com.artezio.arttime.repositories.WorkdaysCalendarRepository;
import com.artezio.arttime.services.integration.DepartmentTrackingSystem;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.*;

import static com.artezio.arttime.admin_tool.cache.WebCached.Scope.VIEW_SCOPED;
import static com.artezio.arttime.security.auth.UserRoles.*;

@Named
@Stateless
@PermitAll
public class DepartmentService implements Serializable {

    @Inject
    private DepartmentTrackingSystem departmentTrackingSystem;
    @Inject
    private DepartmentRepository departmentRepository;
    @Inject
    private WorkdaysCalendarRepository workdaysCalendarRepository;
    @Inject
    private EmployeeRepository employeeRepository;
    @Inject
    private EmployeeService employeeService;
    @Resource
    private SessionContext sessionContext;

    @WebCached(scope = VIEW_SCOPED)
    @RolesAllowed({EXEC_ROLE, OFFICE_MANAGER, PM_ROLE, ACCOUNTANT})
    public List<String> getAll() {
        if (sessionContext.isCallerInRole(EXEC_ROLE) || sessionContext.isCallerInRole(PM_ROLE)) {
            Set<String> departments = new HashSet<>(departmentTrackingSystem.getDepartments());
            List<String> storedDepartments = departmentRepository.getDepartments();
            departments.addAll(storedDepartments);
            return new ArrayList<>(departments);
        } else {
            return employeeService.getLoggedEmployee()
                    .map(Employee::getAccessibleDepartments)
                    .map(ArrayList::new)
                    .orElse(new ArrayList<>());
        }
    }

    @RolesAllowed({EXEC_ROLE, OFFICE_MANAGER})
    public void setCalendarToDepartment(String department, WorkdaysCalendar newCalendar) {
        WorkdaysCalendar currentCalendar = workdaysCalendarRepository.findByDepartment(department);
        changeDepartmentCalendar(department, newCalendar, currentCalendar);
        changeEmployeesCalendar(department, newCalendar, currentCalendar);
    }

    private void changeDepartmentCalendar(String department, WorkdaysCalendar newCalendar, WorkdaysCalendar currentCalendar) {
        Optional.ofNullable(currentCalendar).ifPresent(calendar -> calendar.getDepartments().remove(department));
        Optional.ofNullable(newCalendar).ifPresent(calendar ->
                workdaysCalendarRepository.attachAndRefresh(calendar).getDepartments().add(department));
    }

    private void changeEmployeesCalendar(String department, WorkdaysCalendar newCalendar, WorkdaysCalendar currentCalendar) {
        employeeRepository.query()
                .department(department)
                .list().stream()
                .filter(employee -> employee.getCalendar() == null || employee.getCalendar().equals(currentCalendar))
                .forEach(employee -> employee.setCalendar(newCalendar));
    }

}

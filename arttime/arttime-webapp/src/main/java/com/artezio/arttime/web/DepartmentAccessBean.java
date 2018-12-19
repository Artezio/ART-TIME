package com.artezio.arttime.web;

import com.artezio.arttime.datamodel.Employee;
import com.artezio.arttime.repositories.EmployeeRepository;
import com.artezio.arttime.services.DepartmentService;
import com.artezio.arttime.services.EmployeeService;
import com.google.common.collect.Sets;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Named
@ViewScoped
public class DepartmentAccessBean implements Serializable {
    private static final long serialVersionUID = 1L;
    @Inject
    private DepartmentService departmentService;
    @Inject
    private EmployeeService employeeService;
    private Map<String, List<Employee>> accessToDepartments;

    public Map<String, List<Employee>> getAccessToDepartments() {
        if (accessToDepartments == null) {
            accessToDepartments = departmentService.getAll().stream()
                    .collect(Collectors.toMap(
                            department -> department,
                            this::getOrderedEmployeesByAccess)
                    );
        }
        return accessToDepartments;
    }

    protected List<Employee> getOrderedEmployeesByAccess(String department) {
        return getEmployeesByAccessTo(department).stream()
                .sorted(Employee.NAME_COMPARATOR)
                .collect(Collectors.toList());
    }

    public String save() {
        Set<Employee> modifiedEmployees = modifyAccessForChangedEmployees();
        employeeService.update(modifiedEmployees);
        return "";
    }

    private Set<Employee> modifyAccessForChangedEmployees() {
        return departmentService.getAll().stream()
                .flatMap(this::modifyAccessForChangedEmployees)
                .collect(Collectors.toSet());
    }

    private Stream<Employee> modifyAccessForChangedEmployees(String department) {
        Set<Employee> employeesWithRevokedAccess = revokeAccessForChangedEmployees(department);
        Set<Employee> employeesWithGrantedAccess = grantAccessForChangedEmployees(department);
        return Sets.union(employeesWithGrantedAccess, employeesWithRevokedAccess).stream();
    }

    protected Set<Employee> revokeAccessForChangedEmployees(String department) {
        Set<Employee> employeesWithAccess = getEmployeesByAccessTo(department);
        Set<Employee> selectedEmployees = Sets.newHashSet(Optional.ofNullable(getAccessToDepartments().get(department)).orElse(new ArrayList<>()));
        Set<Employee> employeesToRevokeAccess = Sets.difference(employeesWithAccess, selectedEmployees);
        employeesToRevokeAccess.forEach(employee -> employee.revokeAccessToDepartment(department));
        return employeesToRevokeAccess;
    }

    protected Set<Employee> grantAccessForChangedEmployees(String department) {
        Set<Employee> employeesWithAccess = getEmployeesByAccessTo(department);
        Set<Employee> selectedEmployees = Sets.newHashSet(Optional.ofNullable(getAccessToDepartments().get(department)).orElse(new ArrayList<>()));
        Set<Employee> employeesToGrantAccess = Sets.difference(selectedEmployees, employeesWithAccess);
        employeesToGrantAccess.forEach(employee -> employee.grantAccessToDepartment(department));
        return employeesToGrantAccess;
    }

    private Set<Employee> getEmployeesByAccessTo(String department) {
        return employeeService.getEmployeesHavingAccessToAnyDepartment().stream()
                .filter(employee -> employee.hasAccessTo(department))
                .collect(Collectors.toSet());
    }
}

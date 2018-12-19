package com.artezio.arttime.services.integration;

import com.artezio.arttime.datamodel.Employee;

import java.util.List;
import java.util.stream.Collectors;

public interface EmployeeTrackingSystem extends TrackingSystem {
    List<Employee> getEmployees();
    Employee findEmployee(String userName);

    default List<Employee> findEmployeesByFullName(String fullName) {
        List<Employee> employees = getEmployees();
        String preparedFullName = fullName.trim().toLowerCase();
        return employees.parallelStream()
                .filter(employee -> employee.getFullName().toLowerCase().contains(preparedFullName))
                .collect(Collectors.toList());
    }
}

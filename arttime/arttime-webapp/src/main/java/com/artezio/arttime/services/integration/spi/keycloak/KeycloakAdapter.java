package com.artezio.arttime.services.integration.spi.keycloak;

import com.artezio.arttime.datamodel.Employee;
import com.artezio.arttime.datamodel.WorkdaysCalendar;
import com.artezio.arttime.repositories.WorkdaysCalendarRepository;
import com.artezio.arttime.services.integration.DepartmentTrackingSystem;
import com.artezio.arttime.services.integration.EmployeeTrackingSystem;
import com.artezio.arttime.services.integration.TeamTrackingSystem;
import com.artezio.arttime.services.integration.TrackingSystemImplementation;
import com.artezio.arttime.services.integration.spi.UserInfo;
import org.apache.commons.lang.WordUtils;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@TrackingSystemImplementation
public class KeycloakAdapter implements EmployeeTrackingSystem, TeamTrackingSystem, DepartmentTrackingSystem {

    private static final String TRACKING_SYSTEM_NAME = "Keycloak";

    @Inject
    private KeycloakClient keycloakClient;
    @Inject
    private WorkdaysCalendarRepository workdaysCalendarRepository;

    @Override
    public String getName() {
        return TRACKING_SYSTEM_NAME;
    }

    @Override
    public Collection<String> getDepartments() {
        return keycloakClient.listDepartments().stream()
                .map(Optional::ofNullable)
                .map(department -> department.orElse(Employee.NO_DEPARTMENT))
                .map(department -> WordUtils.capitalizeFully(department, new char[]{'-', ' '}))
                .collect(Collectors.toList());
    }

    @Override
    public List<Employee> getEmployees() {
        return keycloakClient.listUsers()
                .parallelStream()
                .map(this::createEmployee)
                .sorted(Employee.NAME_COMPARATOR)
                .collect(Collectors.toList());
    }

    @Override
    public @Nullable
    Employee findEmployee(String userName) {
        return keycloakClient.listUsers().parallelStream()
                .filter(user -> user.getUsername().equals(userName))
                .findAny()
                .map(this::createEmployee)
                .orElse(null);
    }

    @Override
    public List<Employee> getTeamByGroupCode(String groupCode) {
        return keycloakClient.listUsers(groupCode).parallelStream()
                .map(this::createEmployee)
                .sorted(Employee.NAME_COMPARATOR)
                .collect(Collectors.toList());
    }

    @Override
    public List<Employee> getTeamByDepartment(String department) {
        return keycloakClient.listUsers().parallelStream()
                .filter(userInfo -> (department == null && userInfo.getDepartment() == null) || (department != null && department.equalsIgnoreCase(userInfo.getDepartment())))
                .map(this::createEmployee)
                .distinct()
                .sorted(Employee.NAME_COMPARATOR)
                .collect(Collectors.toList());
    }

    Employee createEmployee(UserInfo userInfo) {
        Employee employee = new Employee(userInfo.getUsername(), userInfo.getFirstName(), userInfo.getLastName(),
                userInfo.getEmail(), userInfo.getDepartment());
        WorkdaysCalendar calendar = workdaysCalendarRepository.findDefaultCalendar(employee.getDepartment());
        employee.setCalendar(calendar);
        return employee;
    }
}

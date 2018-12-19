package com.artezio.arttime.services.integration.spi.ldap;

import com.artezio.arttime.datamodel.Employee;
import com.artezio.arttime.datamodel.WorkdaysCalendar;
import com.artezio.arttime.repositories.WorkdaysCalendarRepository;
import com.artezio.arttime.services.integration.DepartmentTrackingSystem;
import com.artezio.arttime.services.integration.EmployeeTrackingSystem;
import com.artezio.arttime.services.integration.TeamTrackingSystem;
import com.artezio.arttime.services.integration.TrackingSystemImplementation;
import com.artezio.arttime.services.integration.spi.UserInfo;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@TrackingSystemImplementation
public class LdapAdapter implements EmployeeTrackingSystem, TeamTrackingSystem, DepartmentTrackingSystem {

    private static final String TRACKING_SYSTEM_NAME = "LDAP";

    @Inject
    private WorkdaysCalendarRepository workdaysCalendarRepository;
    @Inject
    private LdapClient ldapClient;

    @Override
    public List<Employee> getEmployees() {
        return ldapClient.listUsers().parallelStream()
                .map(this::createEmployee)
                .sorted(Employee.NAME_COMPARATOR)
                .collect(Collectors.toList());
    }

    @Override
    public Employee findEmployee(String userName) {
        return Optional.ofNullable(ldapClient.findUser(userName))
                .map(this::createEmployee)
                .orElse(null);
    }

    @Override
    public List<Employee> getTeamByGroupCode(String groupCode) {
        return ldapClient.listUsers(groupCode).stream()
                .map(this::createEmployee)
                .sorted(Employee.NAME_COMPARATOR)
                .collect(Collectors.toList());
    }

    @Override
    public List<Employee> getTeamByDepartment(String department) {
        return ldapClient.listUsers().parallelStream()
                .filter(userInfo -> (department == null && userInfo.getDepartment() == null) || (department != null && department.equalsIgnoreCase(userInfo.getDepartment())))
                .map(this::createEmployee)
                .distinct()
                .sorted(Employee.NAME_COMPARATOR)
                .collect(Collectors.toList());
    }

    @Override
    public String getName() {
        return TRACKING_SYSTEM_NAME;
    }

    @Override
    public Set<String> getDepartments() {
        return ldapClient.listDepartments();
    }

    Employee createEmployee(UserInfo userInfo) {
        Employee employee = new Employee(userInfo.getUsername(), userInfo.getFirstName(), userInfo.getLastName(),
                userInfo.getEmail(), userInfo.getDepartment());
        WorkdaysCalendar calendar = workdaysCalendarRepository.findDefaultCalendar(employee.getDepartment());
        employee.setCalendar(calendar);
        return employee;
    }
}

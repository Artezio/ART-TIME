package com.artezio.arttime.web;

import com.artezio.arttime.datamodel.Employee;
import com.artezio.arttime.datamodel.WorkdaysCalendar;
import com.artezio.arttime.services.EmployeeService;

import javax.annotation.PostConstruct;
import javax.faces.context.ExternalContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

@Named
@ViewScoped
public class EmployeesBean implements Serializable {

    private static final long serialVersionUID = 2L;
    private Employee employee;
    @Inject
    private ExternalContext externalContext;
    @Inject
    private EmployeeService employeeService;
    private List<Employee> filteredEmployees;
    private List<?> filteredCalendars = new ArrayList<>();
    private List<Employee> employees;

    @PostConstruct
    public void init() {
        Map<String, String> requestParams = externalContext.getRequestParameterMap();
        String username = requestParams.get("employee");
        if (username != null) {
            this.employee = employeeService.find(username);
        }
    }

    public List<Employee> getEmployees() {
        if (employees == null) {
            employees = employeeService.getManagedEmployees();
        }
        return employees;
    }

    public List<Employee> findByFullName(String fullName) {
        String preparedFullName = fullName.trim().toLowerCase();
        return getEmployees().stream()
                .filter(employee -> employee.getFullName().toLowerCase().contains(preparedFullName))
                .collect(Collectors.toList());
    }

    public void editWorkload(Employee employee) {
        this.employee = employee;
    }

    public void save() {
        employeeService.update(employee);
    }

    public Employee getEmployee() {
        return employee;
    }

	public List<Employee> getFilteredEmployees() {
		return filteredEmployees;
	}

	public void setFilteredEmployees(List<Employee> filteredEmployees) {
		this.filteredEmployees = filteredEmployees;
	}
    
	public boolean filterByFormer(Object value, Object filterValue, Locale locale){
		boolean onlyCurrent = (boolean) filterValue;
		boolean former = (boolean) value;
		return !onlyCurrent || !former;
	}

    public boolean filterByCalendar(Object value, Object filter, Locale locale) {
        if (filter == null) {
            return true;
        }
        if (value == null) {
            return false;
        }
        WorkdaysCalendar currentCalendarElement = (WorkdaysCalendar) value;
        Collection<WorkdaysCalendar> filterCalendars = (Collection<WorkdaysCalendar>) filter;

        return filterCalendars.isEmpty()
                || filterCalendars.stream().anyMatch(currentCalendarElement::equals);
    }

    public List<?> getFilteredCalendars() {
        return filteredCalendars;
    }

    public void setFilteredCalendars(List<?> filteredCalendars) {
        this.filteredCalendars = filteredCalendars;
    }

}

package com.artezio.arttime.services.synchronization;

import com.artezio.arttime.datamodel.Employee;
import com.artezio.arttime.services.EmployeeService;
import com.artezio.arttime.services.integration.EmployeeTrackingSystem;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

@Named
@Stateless
public class EmployeeSynchronizer implements EmployeeSynchronizerLocal {

    @Inject
    private EmployeeTrackingSystem employeeTrackingSystem;
    @Inject
    private EmployeeService employeeService;

    public void synchronizeEmployees() {
        List<Employee> employees = employeeService.getAll();
        for (Employee employee : employees) {
            String userName = employee.getUserName();
            Employee externalEmployee = employeeTrackingSystem.findEmployee(userName);
            if (externalEmployee != null) {
                employee.setFirstName(externalEmployee.getFirstName());
                employee.setLastName(externalEmployee.getLastName());
                employee.setEmail(externalEmployee.getEmail());
                employee.setDepartment(externalEmployee.getDepartment());
                employee.setFormer(false);
            } else {
                employee.setFormer(true);
            }
            employeeService.update(employee);
        }
    }

}

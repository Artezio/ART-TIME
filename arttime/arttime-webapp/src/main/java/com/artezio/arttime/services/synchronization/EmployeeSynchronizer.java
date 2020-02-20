package com.artezio.arttime.services.synchronization;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.inject.Inject;
import javax.transaction.UserTransaction;

import com.artezio.arttime.datamodel.Employee;
import com.artezio.arttime.services.EmployeeService;
import com.artezio.arttime.services.integration.EmployeeTrackingSystem;

@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class EmployeeSynchronizer implements EmployeeSynchronizerLocal {

    private static final String SYNCHRONIZATION_ERROR_MSG = "User {0} synchronization error";
    private static final String SYNCHRONIZATION_ERROR_DETAILS_MSG = "User synchronization error details";

    private Logger log = Logger.getLogger(EmployeeSynchronizer.class.getName());

    @Inject
    private EmployeeTrackingSystem employeeTrackingSystem;
    @Inject
    private EmployeeService employeeService;
    @Resource
    private UserTransaction transaction;

    public void synchronizeEmployees() {
        employeeService.getAll().forEach(this::synchronize);
    }

    private void synchronize(Employee employee) {
        try {
            String userName = employee.getUserName();
            Employee externalEmployee = employeeTrackingSystem.findEmployee(userName);
            update(employee, externalEmployee);
        } catch (Exception e) {
            log.log(Level.WARNING, SYNCHRONIZATION_ERROR_MSG, employee.getUserName());
            log.log(Level.FINE, SYNCHRONIZATION_ERROR_DETAILS_MSG, e);
        }
    }

    private void update(Employee target, Employee source) throws Exception {
        if (source != null) {
            target.setFirstName(source.getFirstName());
            target.setLastName(source.getLastName());
            target.setEmail(source.getEmail());
            target.setDepartment(source.getDepartment());
            target.setFormer(false);
        } else {
            target.setFormer(true);
        }
        
        save(target);
    }

    private void save(Employee employee) throws Exception {
        try {
            transaction.begin();
            employeeService.update(employee);
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
            throw e;
        }
    }
    

}

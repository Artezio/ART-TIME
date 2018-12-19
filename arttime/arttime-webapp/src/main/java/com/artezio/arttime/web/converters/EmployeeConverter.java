package com.artezio.arttime.web.converters;

import com.artezio.arttime.datamodel.Employee;
import com.artezio.arttime.repositories.EmployeeRepository;
import com.artezio.arttime.services.integration.EmployeeTrackingSystem;
import org.apache.commons.lang.StringUtils;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.inject.Inject;

@FacesConverter(value = "employeeConverter", forClass = Employee.class)
public class EmployeeConverter implements Converter {

    @Inject
    private EmployeeRepository employeeRepository;
    @Inject
    private EmployeeTrackingSystem employeeTrackingSystem;

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        Employee employee = employeeRepository.find(value);
        return (employee == null)
                ? employeeTrackingSystem.findEmployee(value)
                : employeeRepository.fetchComplete(employee);
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        if (value instanceof Employee) {
            return ((Employee) value).getUserName();
        }
        return null;
    }
}

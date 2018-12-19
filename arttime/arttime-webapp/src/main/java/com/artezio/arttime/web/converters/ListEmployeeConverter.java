package com.artezio.arttime.web.converters;

import com.artezio.arttime.datamodel.Employee;
import com.artezio.arttime.repositories.EmployeeRepository;
import org.apache.commons.lang.StringUtils;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@FacesConverter(value = "listEmployeeConverter")
public class ListEmployeeConverter implements Converter {

    @Inject
    private EmployeeRepository employeeRepository;

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        List<String> userNames = Arrays.asList(value.split(","));
        return employeeRepository.getEmployees(userNames);
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        if (value == null) {
            return null;
        }
        return getAsString(value);
    }

    static public String getAsString(Object value) {
        return ((List<Employee>) value)
                .stream()
                .map(Employee::getUserName)
                .collect(Collectors.joining(","));
    }

}

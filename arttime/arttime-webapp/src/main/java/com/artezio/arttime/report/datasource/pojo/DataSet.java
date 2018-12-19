package com.artezio.arttime.report.datasource.pojo;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public abstract class DataSet {

    public static final String REPORT_PARAM_NAME_START_DATE = "startDate";
    public static final String REPORT_PARAM_NAME_END_DATE = "endDate";
    public static final String REPORT_PARAM_NAME_EMPLOYEE_USERNAMES = "employeeUserNames";
    public static final String REPORT_PARAM_NAME_DEPARTMENTS = "departments";
    public static final String REPORT_PARAM_NAME_PROJECT_IDS = "projectIds";
    public static final String REPORT_PARAM_NAME_HOUR_TYPE_IDS = "hourTypeIds";

    private Map<String, Object> parameters;
    private Iterator<?> dataIterator;

    public void open(Object appContext, Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    public Object next() {
        if (dataIterator.hasNext())
            return dataIterator.next();
        return null;
    };

    public void close() {
    };
    
    public void setDataIterator(Iterator<?> iterator) {
        this.dataIterator = iterator;
    }

    public List<Long> getProjectIdsParam() {
        return getLongParams(REPORT_PARAM_NAME_PROJECT_IDS);
    }
    
    public List<Long> getHourTypeIdsParam() {
        return getLongParams(REPORT_PARAM_NAME_HOUR_TYPE_IDS);
    }

    public List<String> getDepartmentsParam() {
        return getStringParams(REPORT_PARAM_NAME_DEPARTMENTS);
    }
    
    public List<String> getEmployeeUserNamesParam() {
        return getStringParams(REPORT_PARAM_NAME_EMPLOYEE_USERNAMES);
    }

    public Date getStartDateParam() {
        return (Date) parameters.get(REPORT_PARAM_NAME_START_DATE);
    }

    public Date getEndDateParam() {
        return (Date) parameters.get(REPORT_PARAM_NAME_END_DATE);
    }
    
    private List<Long> getLongParams(String paramName) {
        Object[] objectArray = (Object[]) parameters.get(paramName);
        return Arrays.asList(objectArray).stream()
                .map(obj -> (obj instanceof Long)? (Long)obj : ((BigDecimal)obj).longValue())
                .collect(Collectors.toList());
    }
    
    private List<String> getStringParams(String paramName) {
        Object[] objectArray = (Object[]) parameters.get(paramName);
        return Arrays.asList(objectArray).stream()
                .map(object -> Objects.toString(object, null))
                .collect(Collectors.toList());
    }
    

}

package com.artezio.arttime.report.datasource.pojo;

import static com.artezio.arttime.report.datasource.pojo.DataSet.*;
import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.artezio.arttime.report.datasource.pojo.DataSet;

import junitx.framework.ListAssert;

public class DataSetTest {
    
    private DataSet dataSet;
    private Map<String, Object> parameters;
    
    @Before
    public void setUp() throws Exception {
        dataSet = new DataSet() {};
        parameters = new HashMap<>();
        dataSet.open(null, parameters);
    }

    @Test
    public void testGetProjectIdsParam_runtime() {
        Object paramValue = new Long[] {1L, 2L};
        parameters.put(REPORT_PARAM_NAME_PROJECT_IDS, paramValue);
        
        List<Long> actual = dataSet.getProjectIdsParam();
        
        ListAssert.assertEquals(Arrays.asList(1L, 2L), actual);
    }
    
    @Test
    public void testGetProjectIdsParam_designtime() {
        Object paramValue = new Object[] {BigDecimal.valueOf(1L), BigDecimal.valueOf(2)};
        parameters.put(REPORT_PARAM_NAME_PROJECT_IDS, paramValue);
        
        List<Long> actual = dataSet.getProjectIdsParam();
        
        ListAssert.assertEquals(Arrays.asList(1L, 2L), actual);
    }
    
    @Test
    public void testGetHourTypeIdsParam_runtime() {
        Object paramValue = new Long[] {1L, 2L};
        parameters.put(REPORT_PARAM_NAME_HOUR_TYPE_IDS, paramValue);
        
        List<Long> actual = dataSet.getHourTypeIdsParam();
        
        ListAssert.assertEquals(Arrays.asList(1L, 2L), actual);
    }
    
    @Test
    public void testGetHourTypeIdsParam_designtime() {
        Object paramValue = new Object[] {BigDecimal.valueOf(1L), BigDecimal.valueOf(2)};
        parameters.put(REPORT_PARAM_NAME_HOUR_TYPE_IDS, paramValue);
        
        List<Long> actual = dataSet.getHourTypeIdsParam();
        
        ListAssert.assertEquals(Arrays.asList(1L, 2L), actual);
    }
    
    @Test
    public void testGetDepartmentsParam_runtime() {
        Object paramValue = new String[] {"dep1", "dep2"};
        parameters.put(REPORT_PARAM_NAME_DEPARTMENTS, paramValue);
        
        List<String> actual = dataSet.getDepartmentsParam();
        
        ListAssert.assertEquals(Arrays.asList("dep1", "dep2"), actual);
    }
    
    @Test
    public void testGetDepartmentsParam_designtime() {
        Object paramValue = new Object[] {"dep1", "dep2"};
        parameters.put(REPORT_PARAM_NAME_DEPARTMENTS, paramValue);
        
        List<String> actual = dataSet.getDepartmentsParam();
        
        ListAssert.assertEquals(Arrays.asList("dep1", "dep2"), actual);
    }

    @Test
    public void testGetEmployeeUserNamesParam_runtime() throws Exception {
        Object paramValue = new String[] {"user1", "user2"};
        parameters.put(REPORT_PARAM_NAME_EMPLOYEE_USERNAMES, paramValue);
        
        List<String> actual = dataSet.getEmployeeUserNamesParam();
        
        ListAssert.assertEquals(Arrays.asList("user1", "user2"), actual);
    }
    
    @Test
    public void testGetEmployeeUserNamesParam_designtime() throws Exception {
        Object paramValue = new Object[] {"user1", "user2"};
        parameters.put(REPORT_PARAM_NAME_EMPLOYEE_USERNAMES, paramValue);
        
        List<String> actual = dataSet.getEmployeeUserNamesParam();
        
        ListAssert.assertEquals(Arrays.asList("user1", "user2"), actual);
    }

    @Test
    public void testGetStartDateParam_runtime() throws Exception {
        Object expected = new Date();
        parameters.put(REPORT_PARAM_NAME_START_DATE, expected);
        
        Date actual = dataSet.getStartDateParam();
        
        assertEquals(expected, actual);
    }

    @Test
    public void testGetEndDateParam_runtime() throws Exception {
        Object expected = new Date();
        parameters.put(REPORT_PARAM_NAME_END_DATE, expected);
        
        Date actual = dataSet.getEndDateParam();
        
        assertEquals(expected, actual);
    }

}

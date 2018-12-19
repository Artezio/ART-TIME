package com.artezio.arttime.report.datasource.pojo;

import static com.artezio.arttime.report.datasource.pojo.DataSet.*;

import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Before;
import org.junit.Test;

import com.artezio.arttime.datamodel.Hours;
import com.artezio.arttime.report.datasource.pojo.HoursDataSet;
import com.artezio.arttime.report.repositories.SampleDataRepository;

import junitx.framework.ListAssert;

public class HoursDataSetTest {
    
    private HoursDataSet dataSet;

    @Before
    public void setUp() throws Exception {
        dataSet = new HoursDataSet();
    }

    @Test
    public void testOpen() throws IllegalAccessException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(REPORT_PARAM_NAME_START_DATE, new GregorianCalendar(2018, 2, 4).getTime());
        parameters.put(REPORT_PARAM_NAME_END_DATE, new GregorianCalendar(2018, 2, 5).getTime());
        parameters.put(REPORT_PARAM_NAME_PROJECT_IDS, new Long[] {2L, 3L});
        parameters.put(REPORT_PARAM_NAME_DEPARTMENTS, new String[] {"Minsk", "Moscow"});
        parameters.put(REPORT_PARAM_NAME_EMPLOYEE_USERNAMES, new String[] {"user2", "user3"});
        parameters.put(REPORT_PARAM_NAME_HOUR_TYPE_IDS, new Long[] {1L, 2L, 3L});
        
        dataSet.open(null, parameters);
        
        Iterator<Hours> actual = (Iterator) FieldUtils.readField(dataSet, "dataIterator", true);
        List<Hours> hours = new SampleDataRepository().getHours();
        List<Hours> expected = hours.subList(1, 3);
        ListAssert.assertEquals(expected, IteratorUtils.toList(actual));
    }

}

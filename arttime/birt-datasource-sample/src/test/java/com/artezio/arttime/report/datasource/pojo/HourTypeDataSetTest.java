package com.artezio.arttime.report.datasource.pojo;

import static com.artezio.arttime.report.datasource.pojo.DataSet.REPORT_PARAM_NAME_HOUR_TYPE_IDS;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Before;
import org.junit.Test;

import com.artezio.arttime.datamodel.HourType;
import com.artezio.arttime.report.datasource.pojo.HourTypeDataSet;
import com.artezio.arttime.report.repositories.SampleDataRepository;

import junitx.framework.ListAssert;

public class HourTypeDataSetTest {
    
    private HourTypeDataSet dataSet;

    @Before
    public void setUp() throws Exception {
        dataSet = new HourTypeDataSet();
    }

    @Test
    public void testOpen() throws IllegalAccessException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(REPORT_PARAM_NAME_HOUR_TYPE_IDS, new Long[] {1L, 3L});
        
        dataSet.open(null, parameters);
        
        Iterator<HourType> actual = (Iterator) FieldUtils.readField(dataSet, "dataIterator", true);
        List<HourType> hourTypes = new SampleDataRepository().getHourTypes();
        List<HourType> expected = Arrays.asList(hourTypes.get(0), hourTypes.get(2));
        ListAssert.assertEquals(expected, IteratorUtils.toList(actual));
    }

}

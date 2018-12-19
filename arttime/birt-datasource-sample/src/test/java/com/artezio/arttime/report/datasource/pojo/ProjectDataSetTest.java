package com.artezio.arttime.report.datasource.pojo;

import static com.artezio.arttime.report.datasource.pojo.DataSet.REPORT_PARAM_NAME_PROJECT_IDS;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Before;
import org.junit.Test;

import com.artezio.arttime.datamodel.Project;
import com.artezio.arttime.report.datasource.pojo.ProjectDataSet;
import com.artezio.arttime.report.repositories.SampleDataRepository;

import junitx.framework.ListAssert;

public class ProjectDataSetTest {
    
    private ProjectDataSet dataSet;

    @Before
    public void setUp() throws Exception {
        dataSet = new ProjectDataSet();
    }

    @Test
    public void testOpen() throws IllegalAccessException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(REPORT_PARAM_NAME_PROJECT_IDS, new Long[] {2L, 3L});
        
        dataSet.open(null, parameters);
        
        Iterator<Project> actual = (Iterator) FieldUtils.readField(dataSet, "dataIterator", true);
        List<Project> projecs = new SampleDataRepository().getProjects();
        List<Project> expected = projecs.subList(1, 3);
        ListAssert.assertEquals(expected, IteratorUtils.toList(actual));
    }

}

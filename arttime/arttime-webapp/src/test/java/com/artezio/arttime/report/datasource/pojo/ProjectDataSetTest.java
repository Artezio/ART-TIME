package com.artezio.arttime.report.datasource.pojo;

import static com.artezio.arttime.report.datasource.pojo.DataSet.REPORT_PARAM_NAME_PROJECT_IDS;
import static org.easymock.EasyMock.expect;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.naming.NamingException;

import org.easymock.Mock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.artezio.arttime.report.datasource.pojo.ProjectDataSet;
import com.artezio.arttime.services.ProjectService;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ProjectDataSet.class)
public class ProjectDataSetTest {

    private ProjectDataSet projectDataSet;
    @Mock
    private ProjectService projectService;

    @Before
    public void setUp() throws Exception {
        projectDataSet = PowerMock.createPartialMock(ProjectDataSet.class, "getProjectService");
    }

    @Test
    public void testOpen() throws NamingException, NoSuchFieldException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(REPORT_PARAM_NAME_PROJECT_IDS, new Long[] {1L, 3L});

        expect(projectDataSet.getProjectService()).andReturn(projectService);
        expect(projectService.getProjects(Arrays.asList(1L, 3L))).andReturn(Collections.emptyList());
        expect(projectService.fetchComplete(Collections.emptyList())).andReturn(Collections.emptyList());
        replayAll();

        projectDataSet.open(null, parameters);

        verifyAll();
    }
    
    
    @Test(expected = RuntimeException.class)
    public void testOpen_noService() throws NamingException{
        expect(projectDataSet.getProjectService()).andThrow(new RuntimeException());
        replayAll();
        
        projectDataSet.open(null, new HashMap<>());
        
        verifyAll();
    }

}

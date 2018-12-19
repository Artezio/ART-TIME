package com.artezio.arttime.report;

import com.artezio.arttime.repositories.ReportTemplateRepository;
import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.eclipse.birt.report.engine.api.EngineException;
import org.eclipse.birt.report.engine.api.IReportEngine;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertTrue;

@RunWith(EasyMockRunner.class)
public class ReportEngineTest {
    
    private static final String TEMPLATE_RESOURCE_NAME = "/reports/testReport.rptdesign";

    @TestSubject
    private ReportEngine reportEngine = new ReportEngine();
    @Mock
    private ReportTemplateRepository reportTemplateRepository;

    @Test
    @Ignore
    public void testGenerate() {
        String templateName = "testReport";
        InputStream template = this.getClass().getResourceAsStream(TEMPLATE_RESOURCE_NAME);
        expect(reportTemplateRepository.getTemplate(templateName)).andReturn(template);
        replay(reportTemplateRepository);

        byte[] actual = reportEngine.generate(templateName, OutputFormat.EXCEL, new HashMap<>());

        verify(reportTemplateRepository);
        assertTrue(actual.length > 0);
    }

    @Test(expected = RuntimeException.class)
    public void testGenerate_IOException() throws EngineException, IOException {
        reportEngine = createMockBuilder(ReportEngine.class).addMockedMethod("getReportDesign").createMock();

        String templateName = "testReport";
        expect(reportEngine.getReportDesign(anyObject(IReportEngine.class), eq(templateName))).andThrow(new IOException());
        replay(reportEngine);

        reportEngine.generate(templateName, OutputFormat.EXCEL, new HashMap<>());

        verify(reportEngine);
    }

}

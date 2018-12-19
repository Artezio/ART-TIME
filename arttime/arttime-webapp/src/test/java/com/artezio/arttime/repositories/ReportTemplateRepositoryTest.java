package com.artezio.arttime.repositories;

import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

@RunWith(EasyMockRunner.class)
@Ignore
public class ReportTemplateRepositoryTest {

    @TestSubject
    private ReportTemplateRepository repository = new ReportTemplateRepository();
    @Mock
    private ServletContext context;

    @Test
    public void testGetNames() {
        List<String> expected = Arrays.asList("testReport1", "testReport2");
        Set<String> reportFileNames = new HashSet<>(Arrays.asList(
                "/WEB-INF/classes/report_templates/testReport1",
                "/WEB-INF/classes/report_templates/testReport2"));

        expect(context.getResourcePaths("/WEB-INF/classes/report_templates/")).andReturn(reportFileNames);
        replay(context);

        List<String> actual = repository.getNames();

        verify(context);
        assertEquals(expected, actual);
    }

    @Test
    public void testGetNames_noTemplates() {
        expect(context.getResourcePaths("/WEB-INF/classes/report_templates/")).andReturn(new HashSet<>());
        replay(context);

        List<String> actual = repository.getNames();

        verify(context);
        assertTrue(actual.isEmpty());
    }

    @Test
    public void testGetTemplate() throws IOException {
        InputStream expected = createMockBuilder(InputStream.class).createMock();

        expect(context.getResourceAsStream("/WEB-INF/classes/report_templates/testReport1.rptdesign"))
                .andReturn(expected);
        replay(context);

        InputStream actual = repository.getTemplate("testReport1");

        verify(context);
        assertSame(expected, actual);
    }

}

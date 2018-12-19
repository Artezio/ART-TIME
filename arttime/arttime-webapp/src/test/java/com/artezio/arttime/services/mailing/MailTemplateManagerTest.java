package com.artezio.arttime.services.mailing;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

@RunWith(EasyMockRunner.class)
public class MailTemplateManagerTest {

    @Mock
    private Template template;
    @Mock
    private Configuration configuration;
    private MailTemplateManager mailTemplateManager;

    @Before
    public void setUp() {
        mailTemplateManager = new MailTemplateManager();
    }

    @Test
    public void testGetTemplateText_byFileNameAndParams() throws IOException, TemplateException {
        mailTemplateManager = createMockBuilder(MailTemplateManager.class)
                .addMockedMethod("getConfiguration")
                .createMock();
        Map<String, Object> params = new HashMap<>();
        expect(mailTemplateManager.getConfiguration()).andReturn(configuration);
        expect(configuration.getTemplate("template.name")).andReturn(template);
        template.process(eq(params), anyObject(Writer.class));
        replay(mailTemplateManager, configuration, template);

        mailTemplateManager.getTemplateText("template.name", params);

        verify(mailTemplateManager, configuration, template);
    }

    @Test(expected = RuntimeException.class)
    public void testGetTemplateText_byFileNameAndParams_ifTemplateException() throws IOException, TemplateException {
        mailTemplateManager = createMockBuilder(MailTemplateManager.class)
                .addMockedMethod("getConfiguration")
                .createMock();
        Map<String, Object> params = new HashMap<>();
        expect(mailTemplateManager.getConfiguration()).andReturn(configuration);
        expect(configuration.getTemplate("template.name")).andReturn(template);
        template.process(eq(params), anyObject(Writer.class));
        expectLastCall().andThrow(new TemplateException(null));
        replay(mailTemplateManager, configuration, template);

        mailTemplateManager.getTemplateText("template.name", params);

        verify(mailTemplateManager, configuration, template);
    }

    @Test(expected = RuntimeException.class)
    public void testGetTemplateText_byFileNameAndParams_ifIOException() throws IOException, TemplateException {
        mailTemplateManager = createMockBuilder(MailTemplateManager.class)
                .addMockedMethod("getConfiguration")
                .createMock();
        Map<String, Object> params = new HashMap<>();
        expect(mailTemplateManager.getConfiguration()).andReturn(configuration);
        expect(configuration.getTemplate("template.name")).andReturn(template);
        template.process(eq(params), anyObject(Writer.class));
        expectLastCall().andThrow(new IOException());
        replay(mailTemplateManager, configuration, template);

        mailTemplateManager.getTemplateText("template.name", params);

        verify(mailTemplateManager, configuration, template);
    }

    @Test
    public void testGetConfiguration() {
        Configuration actual = mailTemplateManager.getConfiguration();

        assertNotNull(actual);
    }

}

package com.artezio.arttime.report;

import com.artezio.arttime.repositories.ReportTemplateRepository;
import org.eclipse.birt.core.exception.BirtException;
import org.eclipse.birt.core.framework.Platform;
import org.eclipse.birt.report.engine.api.*;
import org.mozilla.javascript.Context;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.logging.Level;

@Stateless
public class ReportEngine {

    private static IReportEngineFactory factory;
    private static EngineConfig config;
    static {
        config = new EngineConfig();
        config.setLogConfig(null, Level.OFF);
    }

    private static synchronized IReportEngine createReportEngine() throws BirtException {
        if (factory == null) {
            Platform.startup(config);
            factory = (IReportEngineFactory) Platform.createFactoryObject(IReportEngineFactory.EXTENSION_REPORT_ENGINE_FACTORY);
        }
        Context.enter().setOptimizationLevel(-1);
        return factory.createReportEngine(config);
    }

    @Inject
    private ReportTemplateRepository reportTemplateRepository;

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public byte[] generate(String templateName, OutputFormat outputFormat, Map<String, Object> params) {
        IReportEngine engine = null;
        IRunAndRenderTask task = null;
        try (ByteArrayOutputStream reportOutput = new ByteArrayOutputStream()) {
            engine = createReportEngine();
            task = createEngineTask(engine, templateName);
            setTaskParams(task, params);
            setTaskRenderOptions(task, outputFormat, reportOutput);
            task.run();
            return reportOutput.toByteArray();
        } catch (IOException | BirtException e) {
            throw new RuntimeException("Can't generate a report: ", e);
        } finally {
            if (task != null) task.close();
            if (engine != null) engine.destroy();
        }
    }

    private IRunAndRenderTask createEngineTask(IReportEngine engine, String templateName)
            throws EngineException, IOException {
        IReportRunnable design = getReportDesign(engine, templateName);
        IRunAndRenderTask result = engine.createRunAndRenderTask(design);
        result.getAppContext().put(EngineConstants.APPCONTEXT_CLASSLOADER_KEY, this.getClass().getClassLoader());
        return result;
    }

    protected IReportRunnable getReportDesign(IReportEngine engine, String templateName)
            throws EngineException, IOException {
        try (InputStream is = reportTemplateRepository.getTemplate(templateName)) {
            return engine.openReportDesign(is);
        }
    }

    protected void setTaskParams(IRunAndRenderTask task, Map<String, Object> params) {
        task.setParameterValues(params);
        task.validateParameters();
    }

    private void setTaskRenderOptions(IRunAndRenderTask task, OutputFormat outputFormat,
        ByteArrayOutputStream reportOutput) {
        RenderOption options = RenderOptionsBuilder.build(outputFormat, reportOutput);
        task.setRenderOption(options);
    }

}

package com.artezio.arttime.report;

import org.eclipse.birt.report.engine.api.RenderOption;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class RenderOptionsBuilder {

    private static Map<OutputFormat, RenderOptionsFactory> factories;

    static {
        factories = new HashMap<>();
        factories.put(OutputFormat.PDF, new PdfRenderOptionsFactory());
        factories.put(OutputFormat.EXCEL, new ExcelRenderOptionsFactory());
    }

    public static RenderOption build(OutputFormat outputFormat, ByteArrayOutputStream baos) {
        return factories.get(outputFormat).create(baos);
    }

}
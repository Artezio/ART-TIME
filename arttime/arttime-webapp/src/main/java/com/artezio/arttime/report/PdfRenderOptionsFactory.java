package com.artezio.arttime.report;

import org.eclipse.birt.report.engine.api.PDFRenderOption;
import org.eclipse.birt.report.engine.api.RenderOption;

import java.io.ByteArrayOutputStream;

public class PdfRenderOptionsFactory implements RenderOptionsFactory {

    @Override
    public RenderOption create(ByteArrayOutputStream baos) {
        RenderOption options = new PDFRenderOption();
        options.setOutputFormat(PDFRenderOption.OUTPUT_FORMAT_PDF);
        options.setOutputStream(baos);
        return options;
    }

}

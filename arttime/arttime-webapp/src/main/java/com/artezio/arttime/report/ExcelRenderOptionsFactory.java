package com.artezio.arttime.report;

import org.eclipse.birt.report.engine.api.EXCELRenderOption;
import org.eclipse.birt.report.engine.api.RenderOption;
import uk.co.spudsoft.birt.emitters.excel.ExcelEmitter;

import java.io.ByteArrayOutputStream;

public class ExcelRenderOptionsFactory implements RenderOptionsFactory {

    private static final String EXCEL_OUTPUT_FORMAT = "xlsx_comments";
    private static final String EXCEL_EMITTER_ID = "com.artezio.birt.emitters.XlsxEmitter";

    @Override
    public RenderOption create(ByteArrayOutputStream baos) {
        RenderOption options = new EXCELRenderOption();
        options.setEmitterID(EXCEL_EMITTER_ID);
        options.setOutputFormat(EXCEL_OUTPUT_FORMAT);
        options.setOption(ExcelEmitter.REMOVE_BLANK_ROWS, false);
        options.setOption(ExcelEmitter.DISABLE_GROUPING, true);
        options.setOutputStream(baos);
        return options;
    }

}

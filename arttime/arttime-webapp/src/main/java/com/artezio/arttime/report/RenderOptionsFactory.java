package com.artezio.arttime.report;

import org.eclipse.birt.report.engine.api.RenderOption;

import java.io.ByteArrayOutputStream;

public interface RenderOptionsFactory {

    RenderOption create(ByteArrayOutputStream baos);

}

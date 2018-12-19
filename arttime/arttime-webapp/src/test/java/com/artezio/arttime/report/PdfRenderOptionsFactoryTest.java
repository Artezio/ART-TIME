package com.artezio.arttime.report;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;

import org.eclipse.birt.report.engine.api.PDFRenderOption;
import org.eclipse.birt.report.engine.api.RenderOption;
import org.junit.Before;
import org.junit.Test;

public class PdfRenderOptionsFactoryTest {
	private PdfRenderOptionsFactory factory;
	
	@Before
	public void setUp() {
		factory = new PdfRenderOptionsFactory();
	}
	
	@Test
	public void testCreate() {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		RenderOption actual = factory.create(baos);
		
		assertTrue(actual instanceof PDFRenderOption);
		assertEquals("pdf", actual.getOutputFormat());				
		assertSame(baos, actual.getOutputStream());
	}
}

package com.artezio.arttime.report;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;

import org.eclipse.birt.report.engine.api.EXCELRenderOption;
import org.eclipse.birt.report.engine.api.RenderOption;
import org.junit.Before;
import org.junit.Test;

import uk.co.spudsoft.birt.emitters.excel.ExcelEmitter;

public class ExcelRenderOptionsFactoryTest {
	private ExcelRenderOptionsFactory factory;
	
	@Before
	public void setUp() {
		factory = new ExcelRenderOptionsFactory();
	}
	
	@Test
	public void testCreate() {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		RenderOption actual = factory.create(baos);
		
		assertTrue(actual instanceof EXCELRenderOption);
		assertEquals("com.artezio.birt.emitters.XlsxEmitter", actual.getEmitterID());
		assertEquals("xlsx_comments", actual.getOutputFormat());		
		assertFalse((Boolean) actual.getOption(ExcelEmitter.REMOVE_BLANK_ROWS));
		assertTrue((Boolean) actual.getOption(ExcelEmitter.DISABLE_GROUPING));
		assertSame(baos, actual.getOutputStream());
	}
}

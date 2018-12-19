package com.artezio.arttime.test.utils;

import static org.easymock.EasyMock.*;

import javax.el.ELContext;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.Flash;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.artezio.arttime.utils.ContextUtil;

@RunWith(PowerMockRunner.class)
@PrepareForTest(FacesContext.class)
public class ContextUtilTest {
	@Test
	public void testGetFacesContext() {
		PowerMock.mockStatic(FacesContext.class);
		FacesContext facesContext = createMock(FacesContext.class);
		expect(FacesContext.getCurrentInstance()).andReturn(facesContext);
		PowerMock.replayAll(FacesContext.class);
		
		FacesContext actual = ContextUtil.getFacesContext();
		
		PowerMock.verifyAll();
	}
	
	@Test
	public void testGetExternalContext() {
		PowerMock.mockStatic(FacesContext.class);
		FacesContext facesContext = createMock(FacesContext.class);
		ExternalContext externalContext = createMock(ExternalContext.class);
		expect(FacesContext.getCurrentInstance()).andReturn(facesContext);
		expect(facesContext.getExternalContext()).andReturn(externalContext);
		PowerMock.replayAll(FacesContext.class);
		
		ExternalContext actual = ContextUtil.getExternalContext();
		
		PowerMock.verifyAll();
	}
	
	@Test
	public void testGetELContext() {
		PowerMock.mockStatic(FacesContext.class);
		FacesContext facesContext = createMock(FacesContext.class);
		ELContext elContext = createMock(ELContext.class);
		expect(FacesContext.getCurrentInstance()).andReturn(facesContext);
		expect(facesContext.getELContext()).andReturn(elContext);
		PowerMock.replayAll(FacesContext.class);
		
		ELContext actual = ContextUtil.getELContext();
		
		PowerMock.verifyAll();
	}
	
	@Test
	public void testGetFlash() {
		PowerMock.mockStatic(FacesContext.class);
		Flash flash = createMock(Flash.class);
		FacesContext facesContext = createMock(FacesContext.class);
		ExternalContext externalContext = createMock(ExternalContext.class);
		expect(FacesContext.getCurrentInstance()).andReturn(facesContext);
		expect(facesContext.getExternalContext()).andReturn(externalContext);
		expect(externalContext.getFlash()).andReturn(flash);
		PowerMock.replayAll(FacesContext.class, facesContext, externalContext);
		
		Flash actual = ContextUtil.getFlash();
		
		PowerMock.verifyAll();
	}
	
	@Test
	public void testPutIntoFlash() {	
		PowerMock.mockStatic(FacesContext.class);
		Flash flash = createMock(Flash.class);
		FacesContext facesContext = createMock(FacesContext.class);
		ExternalContext externalContext = createMock(ExternalContext.class);
		expect(FacesContext.getCurrentInstance()).andReturn(facesContext);
		expect(facesContext.getExternalContext()).andReturn(externalContext);
		expect(externalContext.getFlash()).andReturn(flash);
		expect(flash.put("key", "value")).andReturn("value");
		PowerMock.replayAll(FacesContext.class, facesContext, externalContext, flash);
		
		ContextUtil.putIntoFlash("key", "value");
		
		PowerMock.verifyAll();
	}
	
	@Test
	public void testGetFromFlash() {	
		PowerMock.mockStatic(FacesContext.class);
		Flash flash = createMock(Flash.class);
		FacesContext facesContext = createMock(FacesContext.class);
		ExternalContext externalContext = createMock(ExternalContext.class);
		expect(FacesContext.getCurrentInstance()).andReturn(facesContext);
		expect(facesContext.getExternalContext()).andReturn(externalContext);
		expect(externalContext.getFlash()).andReturn(flash);
		expect(flash.get("key")).andReturn("value");
		PowerMock.replayAll(FacesContext.class, facesContext, externalContext, flash);
		
		Object actual = ContextUtil.getFromFlash("key");
		
		PowerMock.verifyAll();
	}
}

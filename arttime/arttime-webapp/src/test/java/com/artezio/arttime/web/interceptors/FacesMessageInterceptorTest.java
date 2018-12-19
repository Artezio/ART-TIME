package com.artezio.arttime.web.interceptors;

import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.faces.application.Application;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.Flash;
import javax.interceptor.InvocationContext;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({FacesContext.class, FacesMessageInterceptor.class, ResourceBundle.class})
public class FacesMessageInterceptorTest {

    private Flash flash;
    private Application application;
    private FacesContext facesContext;
    private ExternalContext externalContext;
    private InvocationContext invocationContext;
    private FacesMessageInterceptor interceptor;

    @Before
    public void setUp() {
        interceptor = new FacesMessageInterceptor();

        flash = createMock(Flash.class);
        application = createMock(Application.class);
        facesContext = createMock(FacesContext.class);
        externalContext = createMock(ExternalContext.class);
        invocationContext = createMock(InvocationContext.class);
    }

    @Test
    public void testAroundInvoke() throws Exception {
        ResourceBundle messageBundle = new ResourceBundle() {
            @Override
            protected Object handleGetObject(String key) {
                return "The action completed!";
            }

            @Override
            public Enumeration<String> getKeys() {
                return null;
            }
        };

        Method method = TestClass.class.getDeclaredMethod("method");
        PowerMock.mockStatic(FacesContext.class);
        PowerMock.mockStatic(ResourceBundle.class);
        expect(FacesContext.getCurrentInstance()).andReturn(facesContext);
        expect(invocationContext.proceed()).andReturn("result");
        expect(facesContext.getExternalContext()).andReturn(externalContext);
        expect(externalContext.getFlash()).andReturn(flash);
        flash.setKeepMessages(true);
        expect(invocationContext.getMethod()).andReturn(method);
        expect(externalContext.getRequestLocale()).andReturn(Locale.ENGLISH);
        expect(facesContext.getApplication()).andReturn(application);
        expect(application.getMessageBundle()).andReturn("message_bundle");
        expect(ResourceBundle.getBundle("message_bundle", Locale.ENGLISH)).andReturn(messageBundle);
        facesContext.addMessage(eq(null), anyObject(javax.faces.application.FacesMessage.class));
        PowerMock.replayAll(FacesContext.class, ResourceBundle.class, invocationContext, facesContext, externalContext, flash, application);

        Object actual = interceptor.aroundInvoke(invocationContext);

        PowerMock.verifyAll();
        assertEquals("result", actual);
    }

    @Test
    public void testAroundInvoke_ifFacesContextIsNull() throws Exception {
        PowerMock.mockStatic(FacesContext.class);
        expect(FacesContext.getCurrentInstance()).andReturn(null);
        expect(invocationContext.proceed()).andReturn("result");
        PowerMock.replayAll(FacesContext.class, invocationContext);

        Object actual = interceptor.aroundInvoke(invocationContext);

        PowerMock.verifyAll();
        assertEquals("result", actual);
    }

    class TestClass {

        @FacesMessage(onCompleteMessageKey = "complete")
        public void method() {
        }
    }
}

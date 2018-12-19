package com.artezio.arttime.web.exception_handler;

import static junitx.util.PrivateAccessor.getField;
import static org.easymock.EasyMock.createMockBuilder;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ExceptionQueuedEvent;
import javax.faces.event.ExceptionQueuedEventContext;
import javax.servlet.ServletContext;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.artezio.arttime.utils.MessagesUtil;
import com.artezio.arttime.utils.OrderedProperties;

@RunWith(PowerMockRunner.class)
@PrepareForTest({MessagesUtil.class, ExceptionHandler.class})
@PowerMockIgnore({"javax.xml.*", "org.xml.sax.*", "org.w3c.dom.*"})
public class ExceptionHandlerTest {

    private ExceptionHandler exceptionHandler;
    private javax.faces.context.ExceptionHandler wrappedExceptionHandler;
    private ServletContext servletContext;
    private String ERROR_MESSAGES_PROPERTIES_PATH = "errorMessages.xml";

    public static abstract class FacesContextMocker extends FacesContext {
        private FacesContextMocker() {
        }

        private static final Release RELEASE = new Release();

        private static class Release implements Answer<Void> {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                setCurrentInstance(null);
                return null;
            }
        }

        public static FacesContext mockFacesContext() {
            FacesContext context = Mockito.mock(FacesContext.class);
            setCurrentInstance(context);
            Mockito.doAnswer(RELEASE).when(context).release();
            return context;
        }
    }

    @Before
    public void setUp() throws Exception {

        FacesContextMocker.mockFacesContext();
        ExternalContext externalContext = mock(ExternalContext.class);
        when(FacesContext.getCurrentInstance().getExternalContext()).thenReturn(externalContext);
        servletContext = mock(ServletContext.class);
        when(FacesContext.getCurrentInstance().getExternalContext().getContext()).thenReturn(servletContext);

        InputStream input = new FileInputStream(new File(this.getClass().getClassLoader().getResource(ERROR_MESSAGES_PROPERTIES_PATH).toURI()));
        when(servletContext.getResourceAsStream(anyString())).thenReturn(input);

        UIViewRoot viewRoot = mock(UIViewRoot.class);
        when(FacesContext.getCurrentInstance().getViewRoot()).thenReturn(viewRoot);
        when(viewRoot.getChildren()).thenReturn(new ArrayList<>());

        wrappedExceptionHandler = mock(javax.faces.context.ExceptionHandler.class);
    }

    @Test
    public final void testHandle() {

        PowerMock.mockStatic(MessagesUtil.class);
        PowerMock.mockStatic(ExceptionHandler.class);

        exceptionHandler = createMockBuilder(ExceptionHandler.class)
        		.addMockedMethod("findErrorMessages")
                .addMockedMethod("getUnhandledExceptionQueuedEvents")
                .addMockedMethod("getWrapped")
                .createMock();
        wrappedExceptionHandler = EasyMock.createMock(javax.faces.context.ExceptionHandler.class);

        PowerMock.mockStatic(MessagesUtil.class);

        Throwable exceptionForSearch = new ArithmeticException();
        ExceptionQueuedEventContext exceptionEventContext = new ExceptionQueuedEventContext(
                FacesContext.getCurrentInstance(), exceptionForSearch);
        ExceptionQueuedEvent exceptionEvent = new ExceptionQueuedEvent(exceptionEventContext);
        Iterable<ExceptionQueuedEvent> exceptionEvents = new LinkedList<ExceptionQueuedEvent>(
                Arrays.asList(exceptionEvent));


        List<String> messages = Arrays.asList("Exception message");
        expect(exceptionHandler.getUnhandledExceptionQueuedEvents()).andReturn(exceptionEvents);
        expect(exceptionHandler.findErrorMessages(exceptionForSearch)).andReturn(messages);
        MessagesUtil.removeMessagesLessThan(FacesMessage.SEVERITY_ERROR);
        MessagesUtil.addError(null, messages.get(0));        
        expect(exceptionHandler.getWrapped()).andReturn(wrappedExceptionHandler);
        wrappedExceptionHandler.handle();

        PowerMock.replay(exceptionHandler, wrappedExceptionHandler, MessagesUtil.class);

        exceptionHandler.handle();

        PowerMock.verify(exceptionHandler, wrappedExceptionHandler, MessagesUtil.class);

    }

    @Test
    public final void testHandle_withoutExceptions() {

        PowerMock.mockStatic(MessagesUtil.class);
        PowerMock.mockStatic(ExceptionHandler.class);

        exceptionHandler = createMockBuilder(ExceptionHandler.class)
                .addMockedMethod("getUnhandledExceptionQueuedEvents").addMockedMethod("getWrapped").createMock();
        wrappedExceptionHandler = EasyMock.createMock(javax.faces.context.ExceptionHandler.class);

        PowerMock.mockStatic(MessagesUtil.class);

        Iterable<ExceptionQueuedEvent> exceptionEvents = new LinkedList<ExceptionQueuedEvent>();

        expect(exceptionHandler.getUnhandledExceptionQueuedEvents()).andReturn(exceptionEvents);
        expect(exceptionHandler.getWrapped()).andReturn(wrappedExceptionHandler);
        wrappedExceptionHandler.handle();
        PowerMock.expectLastCall();

        PowerMock.replay(exceptionHandler, wrappedExceptionHandler, MessagesUtil.class);

        exceptionHandler.handle();

        PowerMock.verify(exceptionHandler, wrappedExceptionHandler, MessagesUtil.class);

    }

    @Test
    public final void testHandle_ifErrorMessageIsNull() {

        PowerMock.mockStatic(MessagesUtil.class);
        PowerMock.mockStatic(ExceptionHandler.class);

        exceptionHandler = createMockBuilder(ExceptionHandler.class).addMockedMethod("findErrorMessages")
                .addMockedMethod("getUnhandledExceptionQueuedEvents").addMockedMethod("getWrapped").createMock();
        wrappedExceptionHandler = EasyMock.createMock(javax.faces.context.ExceptionHandler.class);

        PowerMock.mockStatic(MessagesUtil.class);

        Throwable exceptionForSearch = new ArithmeticException();
        ExceptionQueuedEventContext exceptionEventContext = new ExceptionQueuedEventContext(
                FacesContext.getCurrentInstance(), exceptionForSearch);
        ExceptionQueuedEvent exceptionEvent = new ExceptionQueuedEvent(exceptionEventContext);
        Iterable<ExceptionQueuedEvent> exceptionEvents = new LinkedList<ExceptionQueuedEvent>(
                Arrays.asList(exceptionEvent));
        
        expect(exceptionHandler.getUnhandledExceptionQueuedEvents()).andReturn(exceptionEvents);
        expect(exceptionHandler.findErrorMessages(exceptionForSearch)).andReturn(new ArrayList<String>());
        expect(exceptionHandler.getWrapped()).andReturn(wrappedExceptionHandler);
        wrappedExceptionHandler.handle();
        PowerMock.expectLastCall();

        PowerMock.replay(exceptionHandler, wrappedExceptionHandler, MessagesUtil.class);

        exceptionHandler.handle();

        PowerMock.verify(exceptionHandler, wrappedExceptionHandler, MessagesUtil.class);

    }

    @Test
    public final void testGetWrapped() {
        exceptionHandler = new ExceptionHandler(wrappedExceptionHandler);

        javax.faces.context.ExceptionHandler expected = wrappedExceptionHandler;
        javax.faces.context.ExceptionHandler actual = exceptionHandler.getWrapped();

        assertSame(expected, actual);
    }

    @Test
    public final void testFindErrorMessage() {
        exceptionHandler = createMockBuilder(ExceptionHandler.class).addMockedMethod("getCauses")
                .createMock();
        Throwable exceptionForSearch = new ArithmeticException();
        List<String> expected = Arrays.asList("message.ArithmeticException");

        List<Throwable> exceptions = Arrays.asList(exceptionForSearch);
        expect(exceptionHandler.getCauses(exceptionForSearch)).andReturn(exceptions);

        replay(exceptionHandler);

        List<String> actual = exceptionHandler.findErrorMessages(exceptionForSearch);

        verify(exceptionHandler);

        assertEquals(expected, actual);
    }

    @Test
    public final void testFindErrorMessage_ifExceptionForSearchIsNull() {
        exceptionHandler = createMockBuilder(ExceptionHandler.class)
        		.addMockedMethod("getCauses")
                .createMock();
        Throwable exceptionForSearch = null;

        expect(exceptionHandler.getCauses(exceptionForSearch)).andReturn(new ArrayList<Throwable>());

        replay(exceptionHandler);

        List<String> actual = exceptionHandler.findErrorMessages(exceptionForSearch);

        verify(exceptionHandler);

        assertTrue(actual.isEmpty());
    }

    @Test
    public final void testFindErrorMessage_findedMessageIsNull() {
        exceptionHandler = createMockBuilder(ExceptionHandler.class).addMockedMethod("getCauses")
                .createMock();
        Throwable exceptionForSearch = new Throwable();

        List<Throwable> exceptions = Arrays.asList(exceptionForSearch);
        expect(exceptionHandler.getCauses(exceptionForSearch)).andReturn(exceptions);

        replay(exceptionHandler);

        List<String> actual = exceptionHandler.findErrorMessages(exceptionForSearch);

        verify(exceptionHandler);

        assertTrue(actual.isEmpty());
    }

    @Test
    public final void testFindErrorMessage_ClassNotFoundException() throws FileNotFoundException,
            NoSuchFieldException {

        exceptionHandler = createMockBuilder(ExceptionHandler.class).addMockedMethod("getCauses")
                .createMock();

        OrderedProperties exceptionsToHandle = (OrderedProperties) getField(ExceptionHandler.class,
                "errorMessages");
        exceptionsToHandle.put("fakeClassName", "");

        Throwable exceptionForSearch = new Throwable();

        List<Throwable> exceptions = Arrays.asList(exceptionForSearch);
        expect(exceptionHandler.getCauses(exceptionForSearch)).andReturn(exceptions);

        replay(exceptionHandler);

        List<String> actual = exceptionHandler.findErrorMessages(exceptionForSearch);

        verify(exceptionHandler);

        assertTrue(actual.isEmpty());

    }

    @Test
    public final void testGetCauses() {
        exceptionHandler = new ExceptionHandler(wrappedExceptionHandler);

        Throwable throwable1 = new Throwable();
        Throwable throwable2 = new Throwable(throwable1);
        Throwable throwable3 = new Throwable(throwable2);

        List<Throwable> expected = new ArrayList<Throwable>(Arrays.asList(throwable3, throwable2, throwable1));
        List<Throwable> actual = exceptionHandler.getCauses(throwable3);

        assertThat(actual, is(expected));
    }

}

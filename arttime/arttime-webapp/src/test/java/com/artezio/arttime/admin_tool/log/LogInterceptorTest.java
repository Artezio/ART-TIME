package com.artezio.arttime.admin_tool.log;

import static org.easymock.EasyMock.anyString;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.createMockBuilder;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.interceptor.InvocationContext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.apache.commons.beanutils.PropertyUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

@RunWith(PowerMockRunner.class)
@PrepareForTest({PropertyUtils.class})
public class LogInterceptorTest {
    private Logger logger;

    private LogInterceptor logInterceptor;
    private InvocationContext invocationContext;
    private Principal principal; 
    
    
    @Before
    public void setUp() {
	logger = createMock(Logger.class);
        invocationContext = createMock(InvocationContext.class);
        principal = createMock(Principal.class);

        logInterceptor = createMockBuilder(LogInterceptor.class)
        	.addMockedMethod("getLogger", InvocationContext.class)
        	.createMock();
        Whitebox.setInternalState(logInterceptor, principal);
    }

    @Test
    public void testProcess() throws Exception {
        Method method = this.getClass().getMethod("loggedMethod", String.class);
        expect(invocationContext.getMethod()).andReturn(method);
        expect(invocationContext.getTarget()).andReturn(this).anyTimes();
        expect(invocationContext.getParameters()).andReturn(new Object[] { "param" });
        expect(invocationContext.proceed()).andReturn(loggedMethod("param"));
        expect(logInterceptor.getLogger(invocationContext)).andReturn(logger);
        expect(principal.getName()).andReturn("iivanov");
        expect(logger.isLoggable(Level.INFO)).andReturn(true);
        logger.log(Level.INFO, "(iivanov) loggedMethod - Before start");
        logger.log(Level.INFO, "(iivanov) loggedMethod(param)");
        logger.log(Level.INFO, "(iivanov) loggedMethod -  Result is  success");
        logger.log(Level.INFO, "(iivanov) loggedMethod - After complete");
        PowerMock.replayAll(logInterceptor, invocationContext, logger, principal);

        logInterceptor.process(invocationContext);

        PowerMock.verifyAll();
    }

    @Test
    public void testProcess_ifLogLevelTooSmall() throws Exception {
        Method method = this.getClass().getMethod("loggedMethod", String.class);
        expect(invocationContext.getMethod()).andReturn(method);
        expect(invocationContext.proceed()).andReturn(loggedMethod("param"));
        expect(logInterceptor.getLogger(invocationContext)).andReturn(logger);
        expect(logger.isLoggable(Level.INFO)).andReturn(false);
        replay(logInterceptor, invocationContext);

        logInterceptor.process(invocationContext);

        verify(logInterceptor, invocationContext);
    }

    @Test
    public void testProcess_ifExternalContextIsNull() throws Exception {
        Method method = this.getClass().getMethod("loggedMethod", String.class);
        expect(invocationContext.getMethod()).andReturn(method);
        expect(invocationContext.getTarget()).andReturn(this).anyTimes();
        expect(invocationContext.getParameters()).andReturn(new Object[] { "param" });
        expect(invocationContext.proceed()).andReturn(loggedMethod("param"));
        expect(logInterceptor.getLogger(invocationContext)).andReturn(logger);
        expect(logger.isLoggable(Level.INFO)).andReturn(true);
        logger.log(Level.INFO, "(null) loggedMethod - Before start");
        logger.log(Level.INFO, "(null) loggedMethod(param)");
        logger.log(Level.INFO, "(null) loggedMethod -  Result is  success");
        logger.log(Level.INFO, "(null) loggedMethod - After complete");
        replay(logInterceptor, invocationContext, logger);

        logInterceptor.process(invocationContext);

        verify(logInterceptor, invocationContext, logger);
    }

    @Test
    public void testProcess_ifUserPrincipalIsAnonymous() throws Exception {
        Method method = this.getClass().getMethod("loggedMethod", String.class);
        expect(invocationContext.getMethod()).andReturn(method);
        expect(invocationContext.getTarget()).andReturn(this).anyTimes();
        expect(invocationContext.getParameters()).andReturn(new Object[] { "param" });
        expect(invocationContext.proceed()).andReturn(loggedMethod("param"));
        expect(logInterceptor.getLogger(invocationContext)).andReturn(logger);
        expect(principal.getName()).andReturn(null);
        expect(logger.isLoggable(Level.INFO)).andReturn(true);
        logger.log(Level.INFO, "(null) loggedMethod - Before start");
        logger.log(Level.INFO, "(null) loggedMethod(param)");
        logger.log(Level.INFO, "(null) loggedMethod -  Result is  success");
        logger.log(Level.INFO, "(null) loggedMethod - After complete");
        PowerMock.replayAll(logInterceptor, invocationContext, logger, principal);

        logInterceptor.process(invocationContext);

        PowerMock.verifyAll();
    }

    @Test
    public void testProcess_ifLogResultFalse_AndLogParamsFalse() throws Exception {
        Method method = this.getClass().getMethod("method", String.class);
        expect(invocationContext.getMethod()).andReturn(method);
        expect(invocationContext.getTarget()).andReturn(this).anyTimes();
        expect(invocationContext.proceed()).andReturn(loggedMethod("param"));
        expect(logInterceptor.getLogger(invocationContext)).andReturn(logger);
        expect(logger.isLoggable(Level.INFO)).andReturn(true);
        replay(logInterceptor, invocationContext, logger);

        logInterceptor.process(invocationContext);

        verify(logInterceptor, invocationContext, logger);
    }

    @Test
    public void testProcess_ifLogDetailedParams() throws Exception {
        LoggedClassForTest loggedClassForTest = new LoggedClassForTest("ppetrov", "Petr", "Petrow", "ppetrov@mail.com");
        Method method = this.getClass().getMethod("logDetailedParams", LoggedClassForTest.class);
        expect(invocationContext.getMethod()).andReturn(method);
        expect(invocationContext.getTarget()).andReturn(this).anyTimes();
        expect(invocationContext.getParameters()).andReturn(new Object[] { loggedClassForTest });
        expect(invocationContext.proceed()).andReturn(null);
        expect(logInterceptor.getLogger(invocationContext)).andReturn(logger);
        expect(principal.getName()).andReturn("iivanov");
        expect(logger.isLoggable(Level.INFO)).andReturn(true);
        logger.log(Level.INFO,
                "(iivanov) logDetailedParams(LoggedClassForTest {firstName=Petr, lastName=Petrow, workLoad=100, department=null, userName=ppetrov, email=ppetrov@mail.com, former=false})");
        PowerMock.replayAll(logInterceptor, invocationContext, logger, principal);

        logInterceptor.process(invocationContext);

        PowerMock.verifyAll();
    }

    @Test
    public void testProcess_ifLogParams() throws Exception {
        LoggedClassForTest loggedClassForTest = new LoggedClassForTest("ppetrov", "Petr", "Petrow", "ppetrov@mail.com");
        Method method = this.getClass().getMethod("logParams", LoggedClassForTest.class);
        expect(invocationContext.getMethod()).andReturn(method);
        expect(invocationContext.getTarget()).andReturn(this).anyTimes();
        expect(invocationContext.getParameters()).andReturn(new Object[] { loggedClassForTest });
        expect(invocationContext.proceed()).andReturn(null);
        expect(logInterceptor.getLogger(invocationContext)).andReturn(logger);
        expect(principal.getName()).andReturn("iivanov");
        expect(logger.isLoggable(Level.INFO)).andReturn(true);
        logger.log(Level.INFO, "(iivanov) logParams - Before start");
        logger.log(Level.INFO, "(iivanov) logParams(LoggedClassForTest {userName=ppetrov})");
        logger.log(Level.INFO, "(iivanov) logParams - After complete");
        PowerMock.replayAll(logInterceptor, invocationContext, logger, principal);

        logInterceptor.process(invocationContext);

        PowerMock.verifyAll();
    }

    @Test(expected = RuntimeException.class)
    public void testProcess_ifProceedThrowsException() throws Exception {
        LoggedClassForTest loggedClassForTest = new LoggedClassForTest("ppetrov", "Petr", "Petrow", "ppetrov@mail.com");
        RuntimeException runtimeException = new RuntimeException();
        Method method = this.getClass().getMethod("logParams", LoggedClassForTest.class);
        expect(invocationContext.getMethod()).andReturn(method);
        expect(invocationContext.getTarget()).andReturn(this).anyTimes();
        expect(invocationContext.getParameters()).andReturn(new Object[] { loggedClassForTest });
        expect(invocationContext.proceed()).andThrow(runtimeException);
        expect(logInterceptor.getLogger(invocationContext)).andReturn(logger);
        expect(principal.getName()).andReturn("iivanov");
        expect(logger.isLoggable(Level.INFO)).andReturn(true);
        logger.log(Level.INFO, "(iivanov) logParams - Before start");
        logger.log(Level.INFO, "(iivanov) logParams(LoggedClassForTest {userName=ppetrov})");
        logger.log(eq(Level.SEVERE), anyString());
        logger.log(eq(Level.SEVERE), anyString());
        PowerMock.replayAll(logInterceptor, invocationContext, logger, principal);

        logInterceptor.process(invocationContext);

        PowerMock.verifyAll();
    }

    @Log(beforeExecuteMessage = "Before start", afterExecuteMessage = "After complete", logParams = true, logResult = true)
    public String loggedMethod(String param) {
        return "success";
    }

    @Log(logResult = false, logParams = false)
    public void method(String param) {
    }

    @Log(logParams = true)
    public void logDetailedParams(@DetailedLogged LoggedClassForTest loggedClassForTest) {
    }

    @Log(logParams = true, beforeExecuteMessage = "Before start", afterExecuteMessage = "After complete")
    public void logParams(LoggedClassForTest loggedClassForTest) {
    }

    @Test
    public void testGetDetailedString_ifObject() {
        LoggedClassForTest loggedClassForTest = new LoggedClassForTest("ppetrov", "Petr", "Petrow", "ppetrov@mail.com");
        String expected = "LoggedClassForTest {firstName=Petr, lastName=Petrow, workLoad=100, department=null, userName=ppetrov, email=ppetrov@mail.com, former=false}";

        String actual = logInterceptor.getDetailedString(loggedClassForTest);

        assertEquals(expected, actual);
    }

    @Test
    public void testGetDetailedString_ifCollection() {
        LoggedClassForTest employee1 = new LoggedClassForTest("ppetrov", "Petr", "Petrow", "ppetrov@mail.com");
        LoggedClassForTest employee2 = new LoggedClassForTest("iivanov", "Ivan", "Ivanov", "iivanov@mail.com");
        List<LoggedClassForTest> loggedClassForTests = Arrays.asList(employee1, employee2);
        String expected = "LoggedClassForTest {firstName=Petr, lastName=Petrow, workLoad=100, department=null, userName=ppetrov, email=ppetrov@mail.com, former=false}, "
                + "LoggedClassForTest {firstName=Ivan, lastName=Ivanov, workLoad=100, department=null, userName=iivanov, email=iivanov@mail.com, former=false}";

        String actual = logInterceptor.getDetailedString(loggedClassForTests);

        assertEquals(expected, actual);
    }

    @Test
    public void testGetLogger() {
        logInterceptor = new LogInterceptor();
        String target = "";
        expect(invocationContext.getTarget()).andReturn(target);
        replay(invocationContext);

        Logger actual = logInterceptor.getLogger(invocationContext);

        verify(invocationContext);
        assertNotNull(actual);
    }

    @Test
    public void testGetDescription() {
        LoggedClassForTest loggedClassForTest = new LoggedClassForTest("ppetrov", "Petr", "Petrow", "ppetrov@mail.com");
        String expected = "LoggedClassForTest {firstName=Petr, lastName=Petrow, workLoad=100, department=null, userName=ppetrov, email=ppetrov@mail.com, former=false}";

        String actual = logInterceptor.getDescription(loggedClassForTest);

        assertEquals(expected, actual);
    }

    @Test
    public void testGetDescription_ifThrownException()
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        LoggedClassForTest loggedClassForTest = new LoggedClassForTest("ppetrov", "Petr", "Petrow", "ppetrov@mail.com");
        PowerMock.mockStatic(PropertyUtils.class);
        expect(PropertyUtils.describe(loggedClassForTest)).andThrow(new IllegalAccessException());
        PowerMock.replayAll(PropertyUtils.class);
        String expected = "LoggedClassForTest {userName=ppetrov}";

        String actual = logInterceptor.getDescription(loggedClassForTest);

        PowerMock.verifyAll();
        assertEquals(expected, actual);
    }
}

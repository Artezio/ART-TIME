package com.artezio.arttime.admin_tool.cache;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.interceptor.InvocationContext;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.artezio.arttime.admin_tool.cache.WebCached.Scope;
import com.sun.faces.config.InitFacesContext;

public class WebCachedInterceptorTest {

	private WebCachedInterceptor interceptor;
	private FacesContext facesContextMock;
	private FacesContext facesContext;

	public static abstract class FacesContextMocker extends FacesContext {
		private FacesContextMocker() {

		}

		public static void setFacesContext(FacesContext context) {
			setCurrentInstance(context);
		}
	}

	@Before
	public void setUp() {
		interceptor = new WebCachedInterceptor();
		facesContext = FacesContextMocker.getCurrentInstance();
		facesContextMock = createMock(FacesContext.class);
		FacesContextMocker.setFacesContext(facesContextMock);

	}

	@After
	public void tearDown() {
		FacesContextMocker.setFacesContext(facesContext);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testProcess_addToCache() throws Exception {
		InvocationContext context = createMock(InvocationContext.class);
		Method method = this.getClass().getMethod("getTestValue");

		Map<String, Object> contextProperties = new HashMap<String, Object>();
		Object[] parameters = new Object[2];

		String targetInstance = "targetInstance";

		ExternalContext externalContext = createMock(ExternalContext.class);
		expect(context.getMethod()).andReturn(method);
		expect(context.getTarget()).andReturn(targetInstance);
		expect(facesContextMock.getExternalContext())
				.andReturn(externalContext);
		expect(externalContext.getRequestMap()).andReturn(contextProperties);
		expect(context.getParameters()).andReturn(parameters);
		expect(context.proceed()).andReturn(getTestValue());

		replay(context, facesContextMock, externalContext);

		Object actual = interceptor.process(context);

		verify(context, facesContextMock, externalContext);

		String cacheId = "_webCache:" + targetInstance.getClass().getName();
		Map<String, Object> cache = (Map<String, Object>) contextProperties
				.get(cacheId);
		WebCachedInterceptor.CacheKey cacheKey = interceptor.new CacheKey(
				targetInstance.getClass(), method, parameters);
		assertEquals(cache.get(cacheKey.toString()), actual);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testProcess_NotAddToCacheIfContextNull() throws Exception {
		FacesContextMocker.setFacesContext(null);
		InvocationContext context = createMock(InvocationContext.class);
		Method method = this.getClass().getMethod("getTestValue");

		String targetInstance = "targetInstance";
		expect(context.getMethod()).andReturn(method);
		expect(context.getTarget()).andReturn(targetInstance);
		expect(context.proceed()).andReturn(getTestValue());
		replay(context);

		Object actual = interceptor.process(context);

		verify(context);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testProcess_addToCache_ifClassAnnotated() throws Exception {
		InvocationContext context = createMock(InvocationContext.class);
		Method method = AnnotatedClass.class.getMethod("getTestValue");

		Map<String, Object> contextProperties = new HashMap<String, Object>();
		Object[] parameters = new Object[2];

		AnnotatedClass targetInstance = new AnnotatedClass();

		ExternalContext externalContext = createMock(ExternalContext.class);
		expect(context.getMethod()).andReturn(method);
		expect(context.getTarget()).andReturn(targetInstance);
		expect(facesContextMock.getExternalContext())
				.andReturn(externalContext);
		expect(externalContext.getRequestMap()).andReturn(contextProperties);
		expect(context.getParameters()).andReturn(parameters);
		expect(context.proceed()).andReturn(getTestValue());

		replay(context, facesContextMock, externalContext);

		Object actual = interceptor.process(context);

		verify(context, facesContextMock, externalContext);

		String cacheId = "_webCache:" + targetInstance.getClass().getName();
		Map<String, Object> cache = (Map<String, Object>) contextProperties
				.get(cacheId);
		WebCachedInterceptor.CacheKey cacheKey = interceptor.new CacheKey(
				targetInstance.getClass(), method, parameters);
		assertEquals(cache.get(cacheKey.toString()), actual);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testProcess_getFromCache() throws Exception {
		InvocationContext context = createMock(InvocationContext.class);
		Method method = this.getClass().getMethod("getTestValue");

		Map<String, Object> contextProperties = new HashMap<String, Object>();
		Object[] parameters = new Object[2];

		String targetInstance = "targetInstance";

		ExternalContext externalContext = createMock(ExternalContext.class);
		expect(context.getMethod()).andReturn(method).times(2);
		expect(context.getTarget()).andReturn(targetInstance).times(2);
		expect(facesContextMock.getExternalContext())
				.andReturn(externalContext).times(2);
		expect(externalContext.getRequestMap()).andReturn(contextProperties)
				.times(2);
		expect(context.getParameters()).andReturn(parameters).times(2);
		expect(context.proceed()).andReturn(getTestValue()).once();

		replay(context, facesContextMock, externalContext);

		Object actual = interceptor.process(context);
		actual = interceptor.process(context);

		verify(context, facesContextMock, externalContext);

		String cacheId = "_webCache:" + targetInstance.getClass().getName();
		Map<String, Object> cache = (Map<String, Object>) contextProperties
				.get(cacheId);
		WebCachedInterceptor.CacheKey cacheKey = interceptor.new CacheKey(
				targetInstance.getClass(), method, parameters);
		assertEquals(cache.get(cacheKey.toString()), actual);

	}

	@Test
	public void testProcess_resetCache() throws Exception {
		InvocationContext context = createMock(InvocationContext.class);
		Method method = this.getClass().getMethod("getTestValue1");

		Map<String, Object> contextProperties = new HashMap<String, Object>();
		Object[] parameters = new Object[2];

		String targetInstance = "targetInstance";

		ExternalContext externalContext = createMock(ExternalContext.class);
		expect(context.getMethod()).andReturn(method);
		expect(context.getTarget()).andReturn(targetInstance);
		expect(facesContextMock.getExternalContext())
				.andReturn(externalContext);
		expect(externalContext.getRequestMap()).andReturn(contextProperties);
		expect(context.getParameters()).andReturn(parameters);
		expect(context.proceed()).andReturn(getTestValue());

		replay(context, facesContextMock, externalContext);

		interceptor.process(context);

		verify(context, facesContextMock, externalContext);

		String cacheId = "_webCache:" + targetInstance.getClass().getName();
		@SuppressWarnings("unchecked")
		Map<String, Object> cache = (Map<String, Object>) contextProperties
				.get(cacheId);
		assertNull(cache);

	}

	@WebCached
	public Object getTestValue() {
		return new Object();
	}

	@WebCached(resetCache = true)
	public Object getTestValue1() {
		return new Object();
	}

	@Test
	public void testGetCache_keyIsNotFound() {
		Map<String, Object> contextProperties = new HashMap<String, Object>();
		String key = "key";
		Map<String, Object> actual = interceptor.getCache(contextProperties,
				key);

		assertTrue(actual.isEmpty());
	}

	@Test
	public void testGetContextProperties_ifFacesContextIsNull() throws NoSuchFieldException {
		FacesContextMocker.setFacesContext(null);

		Map<String, Object> actual = interceptor.getContextProperties(Scope.APPLICATION_SCOPED);

		assertNull(actual);
	}
	
	@Test
        public void testGetContextProperties_ifFacesContextIsInitFacesContext() throws NoSuchFieldException {
	        facesContextMock = createMock(InitFacesContext.class);
                FacesContextMocker.setFacesContext(facesContextMock);

                Map<String, Object> actual = interceptor.getContextProperties(Scope.APPLICATION_SCOPED);

                assertNull(actual);
        }

	@Test
	public void testGetContextProperties_forApplicationScope() {

		Map<String, Object> contextProperties = new HashMap<String, Object>();
		ExternalContext externalContext = createMock(ExternalContext.class);
		expect(facesContextMock.getExternalContext())
				.andReturn(externalContext);
		expect(externalContext.getApplicationMap())
				.andReturn(contextProperties);
		replay(facesContextMock, externalContext);

		Map<String, Object> actual = interceptor
				.getContextProperties(Scope.APPLICATION_SCOPED);

		verify(facesContextMock, externalContext);

		assertEquals(contextProperties, actual);
	}

	@Test
	public void testGetContextProperties_forSessionScope() {

		Map<String, Object> contextProperties = new HashMap<String, Object>();
		ExternalContext externalContext = createMock(ExternalContext.class);
		expect(facesContextMock.getExternalContext())
				.andReturn(externalContext);
		expect(externalContext.getSessionMap()).andReturn(contextProperties);
		replay(facesContextMock, externalContext);

		Map<String, Object> actual = interceptor
				.getContextProperties(Scope.SESSION_SCOPED);

		verify(facesContextMock, externalContext);

		assertEquals(contextProperties, actual);
	}

	@Test
	public void testGetContextProperties_forViewScope() {

		Map<String, Object> contextProperties = new HashMap<String, Object>();
		UIViewRoot viewRoot = createMock(UIViewRoot.class);
		expect(facesContextMock.getViewRoot()).andReturn(viewRoot);
		expect(viewRoot.getViewMap()).andReturn(contextProperties);
		replay(facesContextMock, viewRoot);

		Map<String, Object> actual = interceptor
				.getContextProperties(Scope.VIEW_SCOPED);

		verify(facesContextMock, viewRoot);

		assertEquals(contextProperties, actual);
	}

	@Test
	public void testGetContextProperties_forRequesScope() {

		Map<String, Object> contextProperties = new HashMap<String, Object>();
		ExternalContext externalContext = createMock(ExternalContext.class);
		expect(facesContextMock.getExternalContext())
				.andReturn(externalContext);
		expect(externalContext.getRequestMap()).andReturn(contextProperties);
		replay(facesContextMock, externalContext);

		Map<String, Object> actual = interceptor
				.getContextProperties(Scope.REQUEST_SCOPED);

		verify(facesContextMock, externalContext);

		assertEquals(contextProperties, actual);
	}

	@WebCached
	class AnnotatedClass {
		public Object getTestValue() {
			return new Object();
		}
	}
}

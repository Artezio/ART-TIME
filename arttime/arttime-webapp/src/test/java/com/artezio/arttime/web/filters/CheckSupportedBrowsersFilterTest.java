package com.artezio.arttime.web.filters;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import javax.servlet.FilterChain;
import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import net.sf.uadetector.ReadableUserAgent;
import net.sf.uadetector.UserAgent;
import net.sf.uadetector.UserAgentFamily;
import net.sf.uadetector.UserAgentStringParser;
import net.sf.uadetector.UserAgentType;
import net.sf.uadetector.VersionNumber;
import net.sf.uadetector.service.UADetectorServiceFactory;

@RunWith(PowerMockRunner.class)
@PrepareForTest({UADetectorServiceFactory.class})
public class CheckSupportedBrowsersFilterTest {
	private FilterChain filterChain;
	private HttpServletRequest request;
	private HttpServletResponse response;
	private ReadableUserAgent userAgent;
	private CheckSupportedBrowsersFilter filter;	
	
	@Before
	public void setUp() {
		filter = new CheckSupportedBrowsersFilter();
		
		filterChain = createMock(FilterChain.class);
		request = createMock(HttpServletRequest.class);
		response = createMock(HttpServletResponse.class);
		userAgent = createMock(ReadableUserAgent.class);
	}
	
	@Test
	public void testDoFilter() throws Exception {
		filter = createMockBuilder(CheckSupportedBrowsersFilter.class)
				.addMockedMethod("parseUserAgent", HttpServletRequest.class)
				.addMockedMethod("checkBrowser", ReadableUserAgent.class, HttpServletRequest.class)
				.addMockedMethod("parseMajorVersion", ReadableUserAgent.class, HttpServletRequest.class)
				.createMock();
		expect(filter.parseUserAgent(request)).andReturn(userAgent);
		expect(filter.checkBrowser(userAgent, request)).andReturn(true);
		expect(userAgent.getFamily()).andReturn(UserAgentFamily.CHROME);
		expect(filter.parseMajorVersion(userAgent, request)).andReturn(39);
		filterChain.doFilter(request, response);
		replay(filter, userAgent, filterChain);
		
		filter.doFilter(request, response, filterChain);
		
		verify(filter, userAgent, filterChain);
	}
	
	@Test
	public void testDoFilter_ifCheckBrowserReturnFalse() throws Exception {
		filter = createMockBuilder(CheckSupportedBrowsersFilter.class)
				.addMockedMethod("parseUserAgent", HttpServletRequest.class)
				.addMockedMethod("checkBrowser", ReadableUserAgent.class, HttpServletRequest.class)				
				.createMock();
		expect(filter.parseUserAgent(request)).andReturn(userAgent);
		expect(filter.checkBrowser(userAgent, request)).andReturn(false);		
		filterChain.doFilter(request, response);
		replay(filter, filterChain);
		
		filter.doFilter(request, response, filterChain);
		
		verify(filter, filterChain);
	}
	
	@Test
	public void testDoFilter_ifBrowserVersionLessThanRequired() throws Exception {			
		RequestDispatcher requestDispatcher = createMock(RequestDispatcher.class);
		filter = createMockBuilder(CheckSupportedBrowsersFilter.class)
				.addMockedMethod("parseUserAgent", HttpServletRequest.class)
				.addMockedMethod("checkBrowser", ReadableUserAgent.class, HttpServletRequest.class)
				.addMockedMethod("parseMajorVersion", ReadableUserAgent.class, HttpServletRequest.class)
				.createMock();
		expect(filter.parseUserAgent(request)).andReturn(userAgent);
		expect(filter.checkBrowser(userAgent, request)).andReturn(true);
		expect(userAgent.getFamily()).andReturn(UserAgentFamily.CHROME);
		expect(filter.parseMajorVersion(userAgent, request)).andReturn(35);
		expect(request.getRequestDispatcher("/unsupportedBrowser.xhtml")).andReturn(requestDispatcher);		
		requestDispatcher.forward(request, response);
		replay(filter, userAgent, requestDispatcher, request);
		
		filter.doFilter(request, response, filterChain);
		
		verify(filter, userAgent, requestDispatcher, request);
	}
	
	@Test
	public void testCheckBrowser_ifExcludeFromFilter() {
		filter = createMockBuilder(CheckSupportedBrowsersFilter.class)
				.addMockedMethod("excludeFromFilter")
				.createMock();
		expect(filter.excludeFromFilter(request)).andReturn(true);
		replay(filter);
		
		boolean actual = filter.checkBrowser(userAgent, request);
		
		verify(filter);
		assertFalse(actual);
	}
	
	@Test
	public void testCheckBrowser_ifUserAgentTypeIsNotBrowser() {
		filter = createMockBuilder(CheckSupportedBrowsersFilter.class)
				.addMockedMethod("excludeFromFilter")
				.createMock();
		expect(filter.excludeFromFilter(request)).andReturn(false);
		expect(userAgent.getType()).andReturn(UserAgentType.OTHER);
		replay(filter, userAgent);
		
		boolean actual = filter.checkBrowser(userAgent, request);
		
		verify(filter, userAgent);
		assertFalse(actual);
	}
	
	@Test
	public void testCheckBrowser_ifBrowserNotSupported() {
		filter = createMockBuilder(CheckSupportedBrowsersFilter.class)
				.addMockedMethod("excludeFromFilter")
				.createMock();
		expect(filter.excludeFromFilter(request)).andReturn(false);
		expect(userAgent.getType()).andReturn(UserAgentType.BROWSER);
		expect(userAgent.getFamily()).andReturn(UserAgentFamily.CHROME_MOBILE);
		replay(filter, userAgent);
		
		boolean actual = filter.checkBrowser(userAgent, request);
		
		verify(filter, userAgent);
		assertFalse(actual);
	}
	
	@Test
	public void testCheckBrowser_ifTrue() {
		filter = createMockBuilder(CheckSupportedBrowsersFilter.class)
				.addMockedMethod("excludeFromFilter")
				.createMock();
		expect(filter.excludeFromFilter(request)).andReturn(false);
		expect(userAgent.getType()).andReturn(UserAgentType.BROWSER);
		expect(userAgent.getFamily()).andReturn(UserAgentFamily.CHROME);
		replay(filter, userAgent);
		
		boolean actual = filter.checkBrowser(userAgent, request);
		
		verify(filter, userAgent);
		assertTrue(actual);
	}
	
	@Test
	public void testParseUserAgent() {
		UserAgentStringParser parser = createMock(UserAgentStringParser.class);
		PowerMock.mockStatic(UADetectorServiceFactory.class);
		expect(request.getHeader("User-Agent")).andReturn("Mozilla/5.0");		
		expect(UADetectorServiceFactory.getResourceModuleParser()).andReturn(parser);
		expect(parser.parse("Mozilla/5.0")).andReturn(userAgent);
		PowerMock.replayAll(UADetectorServiceFactory.class, parser, request);		
		
		ReadableUserAgent actual = filter.parseUserAgent(request);
		
		PowerMock.verifyAll();
		assertSame(userAgent, actual);
	}
	
	@Test
	public void testParseUserAgent_ifHeaderParamUserAgentIsEmpty() {
		expect(request.getHeader("User-Agent")).andReturn("");		
		replay(request);			
		
		ReadableUserAgent actual = filter.parseUserAgent(request);
		
		verify(request);
		assertSame(UserAgent.EMPTY, actual);
	}
	
	@Test
	public void testExcludeFromFilter_ifFacesResources() {
		expect(request.getServletPath()).andReturn("/javax.faces.resource");
		replay(request);
		
		boolean actual = filter.excludeFromFilter(request);
		
		verify(request);
		assertTrue(actual);
	}
	
	@Test
	public void testExcludeFromFilter_ifErrorPage() {
		expect(request.getServletPath()).andReturn("");
		expect(request.getRequestURI()).andReturn("/unsupportedBrowser.xhtml");
		replay(request);
		
		boolean actual = filter.excludeFromFilter(request);
		
		verify(request);
		assertTrue(actual);
	}
	
	@Test
	public void testExcludeFromFilter_ifFalse() {
		expect(request.getServletPath()).andReturn("");
		expect(request.getRequestURI()).andReturn("/directory.xhtml");
		replay(request);
		
		boolean actual = filter.excludeFromFilter(request);
		
		verify(request);
		assertFalse(actual);
	}
	
	@Test
	public void testParseMajorVersion_ifNotIE() {
		VersionNumber versionNumber = new VersionNumber("39");
		expect(userAgent.getVersionNumber()).andReturn(versionNumber);		
		expect(userAgent.getFamily()).andReturn(UserAgentFamily.CHROME);
		replay(userAgent);
		
		Integer actual = filter.parseMajorVersion(userAgent, request);
		
		verify(userAgent);
		assertEquals(39, actual.intValue());
	}
	
	@Test
	public void testParseMajorVersion_ifIE() {
		filter = createMockBuilder(CheckSupportedBrowsersFilter.class)
				.addMockedMethod("parseIEVersionInCompatibilityMode", Integer.class, HttpServletRequest.class)
				.createMock();
		VersionNumber versionNumber = new VersionNumber("39");
		expect(userAgent.getVersionNumber()).andReturn(versionNumber);		
		expect(userAgent.getFamily()).andReturn(UserAgentFamily.IE);
		expect(filter.parseIEVersionInCompatibilityMode(39, request)).andReturn(40);
		replay(userAgent, filter);
		
		Integer actual = filter.parseMajorVersion(userAgent, request);
		
		verify(userAgent, filter);
		assertEquals(40, actual.intValue());
	}
	
	@Test
	public void testParseIEVersionInCompatibilityMode_ifDeclaredVersionNotSupported_butEngineVersionOk() {
		Integer declaredVersion = new Integer(9);
		filter = createMockBuilder(CheckSupportedBrowsersFilter.class)
				.addMockedMethod("parseIEEngineVersion", HttpServletRequest.class)
				.createMock();
		expect(filter.parseIEEngineVersion(request)).andReturn(6);
		replay(filter);
		
		Integer actual = filter.parseIEVersionInCompatibilityMode(declaredVersion, request);
		
		verify(filter);
		assertEquals(10, actual.intValue());
	}
	
	@Test
	public void testParseIEEngineVersion() {
		String userAgentString = "Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.1; WOW64; Trident/6.0)";		
		expect(request.getHeader("User-Agent")).andReturn(userAgentString);		
		replay(request);
		
		Integer actual = filter.parseIEEngineVersion(request);
		
		verify(request);
		assertEquals(6, actual.intValue());
	}
	
	@Test
	public void testParseIEEngineVersion_ifNotContainsTridentVersion() {
		String userAgentString = "Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.1; WOW64)";		
		expect(request.getHeader("User-Agent")).andReturn(userAgentString);		
		replay(request);
		
		Integer actual = filter.parseIEEngineVersion(request);
		
		verify(request);
		assertEquals(0, actual.intValue());
	}
	
	@Test
	public void testParseIEEngineVersion_ifEmptyUserAgent() {
		String userAgentString = "";		
		expect(request.getHeader("User-Agent")).andReturn(userAgentString);		
		replay(request);
		
		Integer actual = filter.parseIEEngineVersion(request);
		
		verify(request);
		assertEquals(0, actual.intValue());
	}
}

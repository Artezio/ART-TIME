package com.artezio.arttime.web.filters;

import net.sf.uadetector.*;
import net.sf.uadetector.service.UADetectorServiceFactory;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@WebFilter(filterName = "checkSupportedBrowsersFilter", urlPatterns = {"*.xhtml", "*.jsf", "*.faces"})
public class CheckSupportedBrowsersFilter implements Filter {

    private final static String ERROR_PAGE_ID = "/unsupportedBrowser.xhtml";
    private final static Integer IE_ENGINE_MINIMUM_VERSION = 6;
    private final static Map<UserAgentFamily, Integer> MINIMUM_BROWSER_VERSIONS = new HashMap<>();
    static {
        MINIMUM_BROWSER_VERSIONS.put(UserAgentFamily.IE, 10);
        MINIMUM_BROWSER_VERSIONS.put(UserAgentFamily.FIREFOX, 30);
        MINIMUM_BROWSER_VERSIONS.put(UserAgentFamily.OPERA, 20);
        MINIMUM_BROWSER_VERSIONS.put(UserAgentFamily.CHROME, 37);
        MINIMUM_BROWSER_VERSIONS.put(UserAgentFamily.SAFARI, 7);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        ReadableUserAgent userAgent = parseUserAgent(request);

        if (checkBrowser(userAgent, request)) {
            Integer minimumVersion = MINIMUM_BROWSER_VERSIONS.get(userAgent.getFamily());
            Integer version = parseMajorVersion(userAgent, request);
            if (version < minimumVersion) {
                request.getRequestDispatcher(ERROR_PAGE_ID).forward(request, response);
                return;
            }
        }
        chain.doFilter(request, response);
    }

    protected boolean checkBrowser(ReadableUserAgent userAgent, HttpServletRequest request) {
        return !excludeFromFilter(request)
                && userAgent.getType() == UserAgentType.BROWSER
                && MINIMUM_BROWSER_VERSIONS.containsKey(userAgent.getFamily());
    }

    protected ReadableUserAgent parseUserAgent(HttpServletRequest request) {
        String userAgentString = request.getHeader("User-Agent");
        if (StringUtils.isNotBlank(userAgentString)) {
            UserAgentStringParser parser = UADetectorServiceFactory.getResourceModuleParser();
            return parser.parse(userAgentString);
        }
        return UserAgent.EMPTY;
    }

    protected boolean excludeFromFilter(HttpServletRequest request) {
        return request.getServletPath().startsWith("/javax.faces.resource")
                || request.getRequestURI().contains(ERROR_PAGE_ID);
    }

    protected Integer parseMajorVersion(ReadableUserAgent userAgent, HttpServletRequest request) {
        Integer version = Integer.valueOf(userAgent.getVersionNumber().getMajor());
        if (userAgent.getFamily() == UserAgentFamily.IE) {
            version = parseIEVersionInCompatibilityMode(version, request);
        }
        return version;
    }

    protected Integer parseIEVersionInCompatibilityMode(Integer declaredBrowserVersion, HttpServletRequest request) {
        if (declaredBrowserVersion < MINIMUM_BROWSER_VERSIONS.get(UserAgentFamily.IE)) {
            Integer tridentVersion = parseIEEngineVersion(request);
            if (tridentVersion >= IE_ENGINE_MINIMUM_VERSION) {
                return MINIMUM_BROWSER_VERSIONS.get(UserAgentFamily.IE);
            }
        }
        return declaredBrowserVersion;
    }

    protected Integer parseIEEngineVersion(HttpServletRequest request) {
        String userAgentString = request.getHeader("User-Agent");
        if (StringUtils.isNotBlank(userAgentString)) {
            Pattern pattern = Pattern.compile("Trident/(\\d+).\\d+");
            Matcher matcher = pattern.matcher(userAgentString);
            if (matcher.find() && matcher.groupCount() > 0) {
                return Integer.valueOf(matcher.group(1));
            }
        }
        return 0;
    }

    public static Map<UserAgentFamily, Integer> getMinimumBrowserVersions() {
        return new TreeMap<>(MINIMUM_BROWSER_VERSIONS);
    }

    @Override
    public void destroy() {
    }

}

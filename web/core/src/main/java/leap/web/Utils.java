/*
 *  Copyright 2018 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package leap.web;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import leap.lang.path.Paths;
import leap.lang.resource.Resource;
import leap.lang.resource.Resources;
import leap.lang.servlet.Servlets;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

public class Utils {
    public static final String RES_CLASSPATH_PREFIX = "classpath:META-INF/resources";

    private static final String ENCODED_PERCENT = "%25";

    private static final String PERCENT = "%";

    private static final List<String> FORBIDDEN_ENCODED_PERIOD = Collections
            .unmodifiableList(Arrays.asList("%2e", "%2E"));

    private static final List<String> FORBIDDEN_SEMICOLON = Collections
            .unmodifiableList(Arrays.asList(";", "%3b", "%3B"));

    private static final List<String> FORBIDDEN_FORWARDSLASH = Collections
            .unmodifiableList(Arrays.asList("%2f", "%2F"));

    private static final List<String> FORBIDDEN_DOUBLE_FORWARDSLASH = Collections
            .unmodifiableList(Arrays.asList("//", "%2f%2f", "%2f%2F", "%2F%2f", "%2F%2F"));

    private static final List<String> FORBIDDEN_BACKSLASH = Collections
            .unmodifiableList(Arrays.asList("\\", "%5c", "%5C"));

    private static final List<String> FORBIDDEN_NULL = Collections.unmodifiableList(Arrays.asList("\0", "%00"));

    private static final List<String> FORBIDDEN_LF = Collections.unmodifiableList(Arrays.asList("\n", "%0a", "%0A"));

    private static final List<String> FORBIDDEN_CR = Collections.unmodifiableList(Arrays.asList("\r", "%0d", "%0D"));

    private static final List<String> FORBIDDEN_LINE_SEPARATOR = Collections.unmodifiableList(Arrays.asList("\u2028"));

    private static final List<String> FORBIDDEN_PARAGRAPH_SEPARATOR = Collections
            .unmodifiableList(Arrays.asList("\u2029"));

    private static final Set<String> encodedUrlBlocklist = new HashSet<>();
    private static final Set<String> decodedUrlBlocklist = new HashSet<>();

    static {
        urlBlocklistsAddAll(FORBIDDEN_SEMICOLON);
        urlBlocklistsAddAll(FORBIDDEN_FORWARDSLASH);
//        urlBlocklistsAddAll(FORBIDDEN_DOUBLE_FORWARDSLASH);
        urlBlocklistsAddAll(FORBIDDEN_BACKSLASH);
        urlBlocklistsAddAll(FORBIDDEN_NULL);
        urlBlocklistsAddAll(FORBIDDEN_LF);
        urlBlocklistsAddAll(FORBIDDEN_CR);

        encodedUrlBlocklist.add(ENCODED_PERCENT);
        encodedUrlBlocklist.addAll(FORBIDDEN_ENCODED_PERIOD);
        decodedUrlBlocklist.add(PERCENT);
        decodedUrlBlocklist.addAll(FORBIDDEN_LINE_SEPARATOR);
        decodedUrlBlocklist.addAll(FORBIDDEN_PARAGRAPH_SEPARATOR);
    }

    private static void urlBlocklistsAddAll(Collection<String> collection) {
        encodedUrlBlocklist.addAll(collection);
        decodedUrlBlocklist.addAll(collection);
    }

    /**
     * @see org.springframework.security.web.firewall.StrictHttpFirewall
     */
    public static void rejectedBlocklistedUrls(HttpServletRequest request) {
        for (String forbidden : encodedUrlBlocklist) {
            if (encodedUrlContains(request, forbidden)) {
                throw new IllegalStateException(
                        "The request was rejected because the URL contained a potentially malicious String \""
                                + forbidden + "\"");
            }
        }
        for (String forbidden : decodedUrlBlocklist) {
            if (decodedUrlContains(request, forbidden)) {
                throw new IllegalStateException(
                        "The request was rejected because the URL contained a potentially malicious String \""
                                + forbidden + "\"");
            }
        }
    }

    private static boolean encodedUrlContains(HttpServletRequest request, String value) {
        if (valueContains(request.getContextPath(), value)) {
            return true;
        }
        return valueContains(request.getRequestURI(), value);
    }

    private static boolean decodedUrlContains(HttpServletRequest request, String value) {
        if (valueContains(request.getServletPath(), value)) {
            return true;
        }
        if (valueContains(request.getPathInfo(), value)) {
            return true;
        }
        return false;
    }

    private static boolean valueContains(String value, String contains) {
        return value != null && value.contains(contains);
    }

    public static Resource getResource(ServletContext sc, String path) {
        Resource resource = Servlets.getResource(sc, path);
        if(null == resource || !resource.exists()) {
            resource = Resources.getResource(RES_CLASSPATH_PREFIX + Paths.prefixWithSlash(path));
        }
        return resource;
    }

    public static String buildRequestUrl(HttpServletRequest r) {
        return buildRequestUrl(r.getServletPath(), r.getRequestURI(), r.getContextPath(), r.getPathInfo(),
                r.getQueryString());
    }

    private static String buildRequestUrl(String servletPath, String requestURI, String contextPath, String pathInfo,
                                          String queryString) {
        StringBuilder url = new StringBuilder();
        if (servletPath != null) {
            url.append(servletPath);
            if (pathInfo != null) {
                url.append(pathInfo);
            }
        }
        else {
            url.append(requestURI.substring(contextPath.length()));
        }
        if (queryString != null) {
            url.append("?").append(queryString);
        }
        return url.toString();
    }

    protected Utils() {

    }
}

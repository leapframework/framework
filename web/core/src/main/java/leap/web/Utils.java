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

import leap.lang.path.Paths;
import leap.lang.resource.Resource;
import leap.lang.resource.Resources;
import leap.lang.servlet.Servlets;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

public class Utils {
    public static final String RES_CLASSPATH_PREFIX = "classpath:META-INF/resources";

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

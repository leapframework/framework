/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package leap.lang.servlet;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.FilterConfig;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import leap.lang.Args;
import leap.lang.Locales;
import leap.lang.Strings;
import leap.lang.http.MimeTypes;
import leap.lang.resource.ResourceSet;
import leap.lang.resource.Resources;

/**
 * Servlet utils.
 */
public class Servlets {
	
	/**
	 * Standard Servlet 2.3+ spec request attributes for include URI and paths.
	 * <p>If included via a RequestDispatcher, the current resource will see the
	 * originating request. Its own URI and paths are exposed as request attributes.
	 */
	public static final String INCLUDE_REQUEST_URI_ATTRIBUTE  = "javax.servlet.include.request_uri";
	public static final String INCLUDE_CONTEXT_PATH_ATTRIBUTE = "javax.servlet.include.context_path";
	public static final String INCLUDE_SERVLET_PATH_ATTRIBUTE = "javax.servlet.include.servlet_path";
	public static final String INCLUDE_PATH_INFO_ATTRIBUTE    = "javax.servlet.include.path_info";
	public static final String INCLUDE_QUERY_STRING_ATTRIBUTE = "javax.servlet.include.query_string";

	/**
	 * Standard Servlet 2.4+ spec request attributes for forward URI and paths.
	 * <p>If forwarded to via a RequestDispatcher, the current resource will see its
	 * own URI and paths. The originating URI and paths are exposed as request attributes.
	 */
	public static final String FORWARD_REQUEST_URI_ATTRIBUTE  = "javax.servlet.forward.request_uri";
	public static final String FORWARD_CONTEXT_PATH_ATTRIBUTE = "javax.servlet.forward.context_path";
	public static final String FORWARD_SERVLET_PATH_ATTRIBUTE = "javax.servlet.forward.servlet_path";
	public static final String FORWARD_PATH_INFO_ATTRIBUTE    = "javax.servlet.forward.path_info";
	public static final String FORWARD_QUERY_STRING_ATTRIBUTE = "javax.servlet.forward.query_string";

	/**
	 * Standard Servlet 2.3+ spec request attributes for error pages.
	 * <p>To be exposed to JSPs that are marked as error pages, when forwarding
	 * to them directly rather than through the servlet container's error page
	 * resolution mechanism.
	 */
	public static final String ERROR_STATUS_CODE_ATTRIBUTE    = "javax.servlet.error.status_code";
	public static final String ERROR_EXCEPTION_TYPE_ATTRIBUTE = "javax.servlet.error.exception_type";
	public static final String ERROR_MESSAGE_ATTRIBUTE        = "javax.servlet.error.message";
	public static final String ERROR_EXCEPTION_ATTRIBUTE      = "javax.servlet.error.exception";
	public static final String ERROR_REQUEST_URI_ATTRIBUTE    = "javax.servlet.error.request_uri";
	public static final String ERROR_SERVLET_NAME_ATTRIBUTE   = "javax.servlet.error.servlet_name";	
	
	/**
	 * Standard Servlet spec context attribute that specifies a temporary
	 * directory for the current web application, of type {@code java.io.File}.
	 */
	public static final String TEMP_DIR_CONTEXT_ATTRIBUTE = "javax.servlet.context.tempdir";

	/**
	 * Gets a resource in the given servlet context.
	 */
	public static ServletResource getResource(ServletContext sc,String path){
		return new SimpleServletResource(sc,path);
	}
	
	public static ServletResource getResource(ServletContext sc,String path, Locale locale) {
		String[] paths = Locales.getLocaleFilePaths(locale, path);
		
		for (String p : paths) {
			try {
				if (null != sc.getResource(p)) {
					return new SimpleServletResource(sc, p);
				}
			} catch (MalformedURLException ex) {

			}
		}
		
		return null;
	}

	@SuppressWarnings("unchecked")
    public static Map<String,String> getInitParamsMap(FilterConfig config) {
		Map<String,String> map = new LinkedHashMap<String, String>();
		
		Enumeration<String> names = config.getInitParameterNames();
		
		while(names.hasMoreElements()){
			String name  = names.nextElement();
			String value = config.getInitParameter(name);
			
			map.put(name, value);
		}
		
		return map;
	}
	
	@SuppressWarnings("unchecked")
	public static Map<String, String> getInitParamsMap(ServletConfig config){
		Map<String,String> map = new LinkedHashMap<String, String>();
		
		Enumeration<String> names = config.getInitParameterNames();
		
		while(names.hasMoreElements()){
			String name  = names.nextElement();
			String value = config.getInitParameter(name);
			
			map.put(name, value);
		}
		
		return map;
	}
	
	@SuppressWarnings("unchecked")
	public static Map<String, String> getInitParamsMap(ServletContext sc){
		Map<String,String> map = new LinkedHashMap<String, String>();
		
		Enumeration<String> names = sc.getInitParameterNames();
		
		while(names.hasMoreElements()){
			String name  = names.nextElement();
			String value = sc.getInitParameter(name);
			
			map.put(name, value);
		}
		
		return map;
	}
	
	/**
	 * Return the real path of the given path within the web application,
	 * as provided by the servlet container.
	 * <p>Prepends a slash if the path does not already start with a slash,
	 * and throws a FileNotFoundException if the path cannot be resolved to
	 * a resource (in contrast to ServletContext's {@code getRealPath},
	 * which returns null).
	 * @param servletContext the servlet context of the web application
	 * @param path the path within the web application
	 * @return the corresponding real path
	 * @throws FileNotFoundException if the path cannot be resolved to a resource
	 * @see javax.servlet.ServletContext#getRealPath
	 */
	public static String getRealPath(ServletContext servletContext, String path) throws FileNotFoundException {
		Args.notNull(servletContext, "servletContext");
		// Interpret location as relative to the web application root directory.
		if (!path.startsWith("/")) {
			path = "/" + path;
		}
		String realPath = servletContext.getRealPath(path);
		if (realPath == null) {
			throw new FileNotFoundException(
					"ServletContext resource [" + path + "] cannot be resolved to absolute file path - " +
					"web application archive not expanded?");
		}
		return realPath;
	}
	
	public static String getRequestUriWithQueryString(HttpServletRequest request) {
		StringBuilder uri = new StringBuilder(request.getRequestURI());
		
		if(!Strings.isEmpty(request.getQueryString())){
			uri.append('?').append(request.getQueryString());
		}
		
		return uri.toString();
	}
	
    public static String getRequestPathFromUri(String url) {
        return getRequestPathFromUri(url, "");
    }
	
	public static String getRequestPathFromUri(String url, String contextPath) {
	    if(null == url) {
	        return null;
	    }
	    
	    try {
            URI uri = new URI(url);

            String path = uri.getPath();
            
            if(!Strings.isEmpty(contextPath)) {
                return path.substring(contextPath.length());
            }else{
                return path;
            }
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid url '" + url + ", " + e.getMessage(), e);
        }
	}
	
	/**
	 * Determine whether the given request is an include request,
	 * that is, not a top-level HTTP request coming in from the outside.
	 * <p>Checks the presence of the "javax.servlet.include.request_uri"
	 * request attribute. Could check any request attribute that is only
	 * present in an include request.
	 * @param request current servlet request
	 * @return whether the given request is an include request
	 */
	public static boolean isIncludeRequest(ServletRequest request) {
		return (request.getAttribute(INCLUDE_REQUEST_URI_ATTRIBUTE) != null);
	}	
	
	/**
	 * Return the temporary directory for the current web application,
	 * as provided by the servlet container.
	 * @param servletContext the servlet context of the web application
	 * @return the File representing the temporary directory
	 */
	public static File getTempDir(ServletContext servletContext) {
		Args.notNull(servletContext, "ServletContext");
		return (File) servletContext.getAttribute(TEMP_DIR_CONTEXT_ATTRIBUTE);
	}	
	
	public static String getMimeType(ServletContext sc,String filename){
		String mimeType = sc.getMimeType(filename);
		
		if(Strings.isEmpty(mimeType)){
			mimeType = MimeTypes.getMimeType(filename);
		}
		
		return mimeType;
	}
	
	protected Servlets(){
		
	}
	
}

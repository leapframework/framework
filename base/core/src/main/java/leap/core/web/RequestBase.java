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
package leap.core.web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;

import leap.core.RequestContext;
import leap.core.validation.Validation;
import leap.lang.exception.NestedIOException;
import leap.lang.exception.NestedUnsupportedEncodingException;
import leap.lang.http.HTTP;
import leap.lang.http.HTTP.Method;
import leap.lang.http.MimeType;
import leap.lang.io.InputStreamSource;

/**
 * An {@link HttpServletRequest} wrapper class.
 */
public abstract class RequestBase extends RequestContext implements InputStreamSource {
	
	/**
	 * Returns {@link ServletContext}.
	 */
	public abstract ServletContext getServletContext();
	
	/**
	 * Returns the wrapped {@link HttpServletRequest}.
	 */
	public abstract HttpServletRequest getServletRequest();
	
	/**
	 * Returns the server's url without slash suffix , example : http://server_host[:server_port]
	 */
	public abstract String getServerUrl();

	/**
	 * Returns the reverse proxy server's url without slash suffix , example : http://server_host[:server_port]
	 */
	public abstract String getReverseProxyServerUrl();
	
	/**
	 * @see HttpServletRequest#getContextPath()
	 */
	public abstract String getContextPath();
	
	/**
	 * Returns the url of server url and context path, example : http://server_host[:server_port][context_path]
	 */
	public abstract String getContextUrl();

	/**
	 * Returns the reverse proxy url of server url and context path, example : http://server_host[:server_port][context_path]
	 */
	public abstract String getReverseProxyContextUrl();

	/**
	 * Returns the real user agent hostname or ip address
	 */
	public abstract String getRealRemoteHost();
	
	/**
	 * Returns the request uri(the path with context path and without query string).
	 * 
	 * @see HttpServletRequest#getRequestURI();
	 */
	public abstract String getUri();
	
	/**
	 * Returns the request uri(the with context path and query string).
	 * 
	 * @see HttpServletRequest#getRequestURL()
	 */
	public abstract String getUriWithQueryString();
	
	/**
	 * Returns a path in the request uri without context path. 
	 * 
	 * <p>
	 * The request uri defined in {@link HttpServletRequest#getRequestURI()}.
	 */
	public abstract String getPath();
	
	/**
	 * Returns the request path in lowercase or original case.
	 * 
	 * @see #getPath().
	 */
	public abstract String getPath(boolean lowercase);
	
	/**
	 * @see HttpServletRequest#isSecure()
	 */
	public abstract boolean isSecure();
	
	/**
	 * Reutrns a not empty string indicates the charset's name of current request encoding.
	 */
	public abstract String getCharacterEncoding();
	
	/**
	 * @see HttpServletRequest#setCharacterEncoding(String)
	 */
	public abstract void setCharacterEncoding(String charset) throws NestedUnsupportedEncodingException;	
	
	/**
	 * Returns <code>true</code> if current is GET request.
	 */
	public boolean isGet() {
	    return "GET".equalsIgnoreCase(getMethod());
	}
	
	/**
	 * Returns <code>true</code> if current is POST request.
	 */
	public boolean isPost() {
	    return "POST".equalsIgnoreCase(getMethod());
	}

	/**
	 * Returns a upper case http method .
	 * 
	 * <p>
	 * Returns the override http method if exists.
	 */
	public abstract String getMethod();
	
	/**
	 * Returns the http method defined in the header <code>X-HTTP-Method-Override</code>.
	 */
	public abstract String getOverrideMethod();
	
	/**
	 * Returns the raw http method in {@link HttpServletRequest}
	 */
	public abstract String getRawMethod();
	
	/**
	 * @see HttpServletRequest#getContentLength()
	 */
	public abstract int getContentLength();
	
	/**
	 * Returns the content type in the request header or <code>null</code> if no value.
	 * 
	 * @see HttpServletRequest#getContentType()
	 */
	public abstract MimeType getContentType();
	
	/**
	 * @see HttpServletRequest#getContentType()
	 */
	public abstract String getContentTypeValue();
	
	/**
	 * @see HttpServletRequest#getQueryString()
	 */
	public abstract String getQueryString();
	
	/**
	 * @see HttpServletRequest#getParameter(String)
	 */
	public abstract String getParameter(String name);
	
	/**
	 * @see HttpServletRequest#getParameterValues(String)
	 */
	public abstract String[] getParameterValues(String name);
	
	/**
	 * Returns <code>true</code> if this request contains the given parameter name.
	 */
	public abstract boolean hasParameter(String name);
	
	/**
	 * Same as {@link #getParameter(String)} but only returns the parameter in query string.
	 */
	public abstract String getQueryParameter(String name);

	/**
	 * Same as {@link #getParameterValues(String)} but only returns the parameter in query string.
	 */
	public abstract String[] getQueryParameterValues(String name);
	
	/**
	 * Returns <code>true</code> if the query string contains the given parameter.
	 */
	public abstract boolean hasQueryParameter(String name);

	/**
	 * @see HttpServletRequest#getPart(String). 
	 */
	public abstract Part getPart(String name);
	
	/**
	 * @see HttpServletRequest#getParts().
	 */
	public abstract Collection<Part> getParts();
	
	/**
	 * Returns <code>true</code> if the header exists.
	 */
	public abstract boolean hasHeader(String name);
	
	/**
	 * @see HttpServletRequest#getHeader(String)
	 */
	public abstract String getHeader(String name);
	
	/**
	 * @see HttpServletRequest#getDateHeader(String)
	 */
	public abstract long getDateHeader(String name);
	
	/**
	 * Returns the cookie matched the given name or <code>null</code> if not exists.
	 */
	public abstract Cookie getCookie(String name);
	
	/**
	 * @see HttpServletRequest#getCookies()
	 */
	public abstract Cookie[] getCookies();
	
	/**
	 * @see HttpServletRequest#getInputStream()
	 */
	public abstract InputStream getInputStream() throws NestedIOException;	
	
	/**
	 * @see HttpServletRequest#getReader()
	 */
	public abstract BufferedReader getReader() throws NestedIOException;
	
	/**
	 * Returns <code>true</code> if current request is an ajax request.
	 */
	public abstract boolean isAjax();
	
	/**
	 * Sets <code>true</code> if currently is an ajax request or <code>false</code> if not.
	 */
	public abstract void setAjax(boolean ajax);
	
	/**
	 * Returns <code>true</code> if current request is a pjax request.
	 * 
	 * @see https://github.com/defunkt/jquery-pjax
	 */
	public abstract boolean isPjax();
	
	/**
	 * Sets pjax to <code>true</code> or <code>false</code>
	 */
	public abstract void setPjax(boolean pjax);
	
	/**
	 * Returns <code>true</code> if current request is an multipart form post request.
	 */
	public abstract boolean isMultipart(); 
	
	/**
	 * Returns <code>true</code> if the browser supports gzip encoding. 
	 */
	public abstract boolean isGzipSupport();
	
	/**
	 * Returns <code>true</code> if current request's http method match to the given {@link Method}.
	 */
	public abstract boolean isMethod(HTTP.Method httpMethod);
	
	/**
	 * Returns the {@link Validation} object in this request.
	 */
	public abstract Validation getValidation();
	
	/**
	 * Sets the {@link Validation} object in this request.
	 * 
	 * @throws IllegalStateException if the validation object is <code>null</code>
	 */
	public abstract void setValidation(Validation validation) throws IllegalStateException;
	
	/**
	 * Forwards current request to new path.
	 * 
	 * @see HttpServletRequest#getRequestDispatcher(String)
	 * @see RequestDispatcher#forward(javax.servlet.ServletRequest, javax.servlet.ServletResponse)
	 */
	public abstract void forward(String path) throws ServletException,IOException;
}

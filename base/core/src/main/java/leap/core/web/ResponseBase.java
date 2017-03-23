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

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Collection;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import leap.lang.exception.NestedIOException;

/**
 * A wrapper wrapps {@link HttpServletResponse}
 */
public interface ResponseBase {
	
	/**
	 * Returns the wrapped {@link HttpServletResponse}.
	 */
	HttpServletResponse getServletResponse();
	
	/**
	 * Sets the wrapped {@link HttpServletResponse}.
	 */
	void setServletResponse(HttpServletResponse response) throws IOException;
	
	/**
	 * @see HttpServletResponse#getStatus()
	 */
	int getStatus();
	
	/**
	 * @see HttpServletResponse#getOutputStream()
	 */
	OutputStream getOutputStream() throws NestedIOException;
	
	/**
	 * @see HttpServletResponse#getWriter()
	 */
	PrintWriter getWriter() throws NestedIOException;
	
	/**
	 * Sets the http resposne status.
	 * 
	 * @see HttpServletResponse#setStatus(int)
	 */
	void setStatus(int status);
	
	/**
	 * Sets the http response header.
	 * 
	 * @see HttpServletResponse#setHeader(String, String)
	 */
	void setHeader(String name, String value);
	
	/**
	 * @see HttpServletResponse#setDateHeader(String, long)
	 */
	void setDateHeader(String name, long date);
	
	/**
	 * Adds a header to response.
	 * 
	 * @see HttpServletResponse#addHeader(String, String).
	 */
	void addHeader(String name, String value);
	
	/**
	 * @see HttpServletResponse#addDateHeader(String, long)
	 */
	void addDateHeader(String name, long date);

    /**
     * @see HttpServletResponse#getHeader(String)
     */
    String getHeader(String name);

    /**
     * @see HttpServletResponse#getHeaders(String)
     */
    Collection<String> getHeaders(String name);

    /**
     * @see HttpServletResponse#getHeaderNames()
     */
    Collection<String> getHeaderNames();
	
	/**
	 * @see HttpServletResponse#addCookie(Cookie)
	 */
	void addCookie(Cookie cookie);
	
	/**
	 * Remove the cookie from the response. Will generate a cookie with empty value and max age 0.
	 */
	void removeCookie(Cookie cookie); 
	
	/**
	 * Sets the http resposne content type.
	 * 
	 * @see HttpServletResponse#setContentType(String)
	 */
	void setContentType(String contentType);
	
	/**
	 * @see HttpServletResponse#setContentLength(int).
	 */
	void setContentLength(int length);
	
	/**
	 * @see HttpServletResponse#setCharacterEncoding(String)
	 */
	void setCharacterEncoding(String charset);
	
	/**
	 * @see HttpServletResponse#sendRedirect(String)
	 */
	void sendRedirect(String location) throws NestedIOException;
	
	/**
	 * @see HttpServletResponse#sendError(int)
	 */
	void sendError(int status);
	
	/**
	 * @see HttpServletResponse#sendError(int, String)
	 */
	void sendError(int status,String message);
	
	/**
	 * @see HttpServletResponse#isCommitted()
	 */
	boolean isCommitted();
}
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
package leap.web;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Collection;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import leap.lang.Args;
import leap.lang.Exceptions;
import leap.lang.exception.NestedIOException;
import leap.lang.json.JSON;
import leap.lang.json.JsonWriter;

public class DefaultResponse extends Response {
	
	private Request			request;
	private WrappedResponse resp;
	private boolean			handled;
	private JsonWriter		jsonWriter;
	
	public DefaultResponse(HttpServletResponse servletResponse) throws IOException {
		this.resp = new WrappedResponse(servletResponse);
	}
	
	public void setRequest(Request request){
		this.request = request;
	}
	
	@Override
    public int getStatus() {
	    return resp.getStatus();
    }

	@Override
    public void setStatus(int status) {
		resp.setStatus(status);
    }
	
	@Override
    public void setHeader(String name, String value) {
		resp.setHeader(name, value);
    }
	
	@Override
    public void setDateHeader(String name, long date) {
		resp.setDateHeader(name, date);
    }

	@Override
    public void addHeader(String name, String value) {
		resp.addHeader(name, value);
    }
	
	@Override
    public void addDateHeader(String name, long date) {
		resp.addDateHeader(name, date);
    }

    @Override
    public String getHeader(String name) {
        return resp.getHeader(name);
    }

    @Override
    public Collection<String> getHeaders(String name) {
        return resp.getHeaders(name);
    }

    @Override
    public Collection<String> getHeaderNames() {
        return resp.getHeaderNames();
    }

    @Override
    public void addCookie(Cookie cookie) {
		resp.addCookie(cookie);
    }

	@Override
    public void removeCookie(Cookie cookie) {
		Cookie rm = new Cookie(cookie.getName(), "");
		
		if(null != cookie.getDomain()){
			rm.setDomain(cookie.getDomain());	
		}
		
		rm.setPath(cookie.getPath());
		rm.setMaxAge(0);

		resp.addCookie(rm);
    }

	@Override
    public void setContentType(String contentType) {
		resp.setContentType(contentType);
    }
	
	@Override
    public void setContentLength(int length) {
		resp.setContentLength(length);
    }

	@Override
    public void setCharacterEncoding(String charset) {
		resp.setCharacterEncoding(charset);
    }
	
	@Override
    public void sendRedirect(String location) throws NestedIOException {
		try {
			if(null != location){
				if(location.startsWith("/")) {
				    String cp = request.getContextPath();
				    if(!cp.isEmpty()) {
				        if(!(location.equals(cp) || location.startsWith(cp + "/"))) {
				            location = cp + location;    
				        }
				    }
				}else if(location.startsWith("~/")){
					location = request.getContextPath() + location.substring(1);
				}else if(location.startsWith("^/")) {
					location = location.substring(1);
				}
			}
	        resp.sendRedirect(location);
        } catch (IOException e) {
        	throw Exceptions.wrap("Error calling 'sendRedirect' from underlying response, " + e.getMessage(),e);
        }
    }
	
	@Override
    public void sendError(int status) {
		try {
	        resp.sendError(status);
        } catch (IOException e) {
        	throw Exceptions.wrap("Error calling 'sendError' from underlying response, " + e.getMessage(), e);
        }
    }

	@Override
    public void sendError(int status, String message) {
		try {
	        resp.sendError(status,message);
        } catch (IOException e) {
        	throw Exceptions.wrap("Error calling 'sendError' from underlying response, " + e.getMessage(), e);
        }
    }

	@Override
	public HttpServletResponse getServletResponse() {
		return resp;
	}

	@Override
    public void setServletResponse(HttpServletResponse response) throws IOException {
		Args.notNull(response,"response");
		this.resp = new WrappedResponse(response);
    }
	
	@Override
    public OutputStream getOutputStream() throws NestedIOException {
	    try {
	        return resp.getOutputStream();
        } catch (IOException e) {
        	throw Exceptions.wrap("Error calling 'getOutputStream' from underlying response, " + e.getMessage(), e);
        }
    }

	@Override
    public PrintWriter getWriter() throws NestedIOException {
	    try {
	        return resp.getWriter();
        } catch (IOException e) {
        	throw Exceptions.wrap("Error calling 'getWriter' from underlying response, " + e.getMessage(), e);
        }
    }

	@Override
    public boolean isCommitted() {
	    return resp.isCommitted();
    }

	@Override
    public boolean isHandled() {
	    return handled;
    }
	
	@Override
    public void markHandled() {
		this.handled = true;
    }
	
	@Override
    public JsonWriter getJsonWriter() {
		if(null == jsonWriter) {
			jsonWriter = JSON.createWriter(getWriter());
		}
	    return jsonWriter;
    }

	protected class WrappedResponse extends HttpServletResponseWrapper {
		
		public WrappedResponse(HttpServletResponse response) throws IOException {
			super(response);
		}
		
		@Override
        public ServletOutputStream getOutputStream() throws IOException {
	        return super.getOutputStream();
        }

		@Override
        public PrintWriter getWriter() throws IOException {
			return super.getWriter();
		}

		@Override
        public void sendError(int sc, String msg) throws IOException {
			handled = true;
	        super.sendError(sc, msg);
        }

		@Override
        public void sendError(int sc) throws IOException {
			handled = true;
	        super.sendError(sc);
        }

		@Override
        public void sendRedirect(String location) throws IOException {
			handled = true;
			super.sendRedirect(location);
        }

		@Override
        public void setStatus(int sc) {
			handled = true;
	        super.setStatus(sc);
        }

        @Override
        @SuppressWarnings("deprecation")
        public void setStatus(int sc, String sm) {
			handled = true;
	        super.setStatus(sc, sm);
        }
	}
}
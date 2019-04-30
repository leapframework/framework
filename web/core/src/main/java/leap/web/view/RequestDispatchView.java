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
package leap.web.view;

import leap.lang.Strings;
import leap.lang.http.ContentTypes;
import leap.lang.http.MimeTypes;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.path.Paths;
import leap.lang.resource.Resource;
import leap.lang.servlet.Servlets;
import leap.web.App;
import leap.web.Request;
import leap.web.Response;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

//some codes from spring framework

public class RequestDispatchView extends AbstractServletResourceView {
	
	protected static final Log log = LogFactory.get(RequestDispatchView.class);
	
	protected boolean preventDispatchLoop = false;
	protected boolean alwaysInclude       = false;

	
	public RequestDispatchView(App app, String path, Resource resource) {
	    super(app, path, resource);
	    this.defaultContentType = ContentTypes.create(MimeTypes.TEXT_HTML, app.getDefaultCharset().name());
    }
	
	public boolean isPreventDispatchLoop() {
		return preventDispatchLoop;
	}

	public void setPreventDispatchLoop(boolean preventDispatchLoop) {
		this.preventDispatchLoop = preventDispatchLoop;
	}
	
	/**
	 * Specify whether to always include the view rather than forward to it.
	 * <p>Default is "false". Switch this flag on to enforce the use of a
	 * Servlet include, even if a forward would be possible.
	 * @see javax.servlet.RequestDispatcher#forward
	 * @see javax.servlet.RequestDispatcher#include
	 * @see #useInclude(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	public void setAlwaysInclude(boolean alwaysInclude) {
		this.alwaysInclude = alwaysInclude;
	}

	@Override
    protected void renderAndSetContentType(Request request, Response response, ViewData data) throws Exception {
		if(null != characterEncoding) {
			response.setCharacterEncoding(characterEncoding);
		}
		String contentType = getContentType(request);
		if(!Strings.isEmpty(contentType)) {
			SetContentTypeResponse lctr = new SetContentTypeResponse(response.getServletResponse(), contentType);
			response.setServletResponse(lctr);
			
			doRender(request, response, data);
			
			response.setServletResponse(lctr.getWrapped());
			response.setContentType(contentType);
		}else{
			response.setContentType(defaultContentType);
			doRender(request, response, data);
		}
    }

	@Override
    protected void doRender(Request request, Response response, ViewData data) throws Exception {
		HttpServletRequest  req  = request.getServletRequest();
		HttpServletResponse resp = response.getServletResponse();

		// Determine the path for the request dispatcher.
		String dispatcherPath = prepareForRendering(req, resp);
		
		// Obtain a RequestDispatcher for the target resource (typically a JSP).
		RequestDispatcher rd = getRequestDispatcher(req, dispatcherPath);
		if (rd == null) {
			throw new ServletException("Could not get RequestDispatcher for [" + path +
					"]: Check that the corresponding file exists within your web application archive!");
		}
		
		// If already included or response already committed, perform include, else forward.
		if (useInclude(req, resp)) {
			response.setContentType(getContentType(request));
			if (log.isDebugEnabled()) {
				log.debug("Including resource [" + path + "]");
			}
			rd.include(req, resp);
		} else {
			// Note: The forwarded resource is supposed to determine the content type itself.
			if (log.isDebugEnabled()) {
				log.debug("Forwarding to resource [" + path + "]");
			}
			rd.forward(req, resp);
		}		
    }

	protected String prepareForRendering(HttpServletRequest request, HttpServletResponse response) throws Exception {
		if (this.preventDispatchLoop) {
			String uri = request.getRequestURI();
			if (path.startsWith("/") ? uri.equals(path) : uri.equals(Paths.applyRelative(uri, path))) {
				throw new ServletException("Circular view path [" + path + "]: would dispatch back " +
						"to the current handler URL [" + uri + "] again. Check your ViewResolver setup! " +
						"(Hint: This may be the result of an unspecified view, due to default view name generation.)");
			}
		}
		return resourcePath;
	}
	
	/**
	 * Obtain the RequestDispatcher to use for the forward/include.
	 * <p>The default implementation simply calls
	 * {@link HttpServletRequest#getRequestDispatcher(String)}.
	 * Can be overridden in subclasses.
	 * @param request current HTTP request
	 * @param path the target URL (as returned from {@link #prepareForRendering})
	 * @return a corresponding RequestDispatcher
	 */
	protected RequestDispatcher getRequestDispatcher(HttpServletRequest request, String path) {
		return request.getRequestDispatcher(path);
	}
	
	/**
	 * Determine whether to use RequestDispatcher's {@code include} or
	 * {@code forward} method.
	 * <p>Performs a check whether an include URI attribute is found in the request,
	 * indicating an include request, and whether the response has already been committed.
	 * In both cases, an include will be performed, as a forward is not possible anymore.
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @return {@code true} for include, {@code false} for forward
	 * @see javax.servlet.RequestDispatcher#forward
	 * @see javax.servlet.RequestDispatcher#include
	 * @see javax.servlet.ServletResponse#isCommitted
	 */
	protected boolean useInclude(HttpServletRequest request, HttpServletResponse response) {
		return (this.alwaysInclude || Servlets.isIncludeRequest(request) || response.isCommitted());
	}	
	
	protected static final class SetContentTypeResponse extends HttpServletResponseWrapper {
		
		private final HttpServletResponse wrapped;
		private final String 			  contentType;

		public SetContentTypeResponse(HttpServletResponse response,String contentType) {
	        super(response);
	        this.contentType = contentType;
	        this.wrapped = response;
        }

		@Override
        public void setContentType(String type) {
			wrapped.setContentType(contentType);
		}

		public HttpServletResponse getWrapped() {
			return wrapped;
		}
	}
}

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

import java.util.Map;

import leap.core.validation.Errors;
import leap.lang.Args;
import leap.lang.Strings;
import leap.lang.http.ContentTypes;
import leap.lang.http.MimeTypes;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.path.Paths;
import leap.lang.servlet.Servlets;
import leap.web.App;
import leap.web.Request;
import leap.web.Response;
import leap.web.Result;

public abstract class AbstractView implements View {
	
	private static final Log log = LogFactory.get(AbstractView.class);
	
	public static final String DEFAULT_RETURN_VALUE_ATTRIBUTE = "returnValue";
	public static final String DEFAULT_ERRORS_ATTRIBUTE       = "errors";
	
	protected final App    app;
	protected final String path;
	
	protected String returnValueAttribute = DEFAULT_RETURN_VALUE_ATTRIBUTE;
	protected String errorsAttribute      = DEFAULT_ERRORS_ATTRIBUTE;
	protected String contentType;
	protected String characterEncoding;
	
	protected AbstractView(App app, String path) {
		Args.notNull(app,"app");
		Args.notNull(path,"path");
		this.app  = app;
		this.path = path;
		this.resolveContentTypeByPath();
	}
	
	public void setReturnValueAttribute(String name) {
		this.returnValueAttribute = name;
	}
	
	public void setErrorsAttribute(String name) {
		this.errorsAttribute = name;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	
	public String getContentType() {
		return contentType;
	}

	public String getCharacterEncoding() {
		return characterEncoding;
	}

	public void setCharacterEncoding(String characterEncoding) {
		this.characterEncoding = characterEncoding;
	}

	@Override
	public String getContentType(Request request) {
		return contentType;
	}
	
	@Override
    public boolean reloadable() {
	    return false;
    }

	@Override
    public boolean reload() {
	    return false;
    }

	@Override
    public final void render(Request request, Response response) throws Exception {
		Result result = request.getResult();
		render(request,response,null == result ? null : result.getViewData());
    }

	@Override
    public final void render(Request request, Response response, ViewData data) throws Exception {
		if(null == data){
			data = WrappedViewData.EMPTY;
		}
		
		//Expose built-in attributes
		exposeBuiltInAttributes(request);
		
		// Expose the view data as request attributes.
		exposeViewDataAsRequestAttributes(data, request);
		
		// Expose helpers as request attributes, if any.
		exposeHelpers(request);
		
		renderAndSetContentType(request, response, data);
    }
	
	protected void renderAndSetContentType(Request request, Response response, ViewData data) throws Exception {
		// Set content-type and charset encoding.
		if(null != contentType){
			response.setContentType(contentType);
		}
		
		if(null != characterEncoding){
			response.setCharacterEncoding(characterEncoding);
		}
		
		//render content
		doRender(request, response, data);
	}
	
	protected abstract void doRender(Request request,Response response,ViewData model) throws Exception ;
	
	protected void exposeBuiltInAttributes(Request request) {
		request.setAttribute("app", 	 request.app());
		request.setAttribute("request",  request);
		request.setAttribute("response", request.response());
	}

	/**
	 * Expose the given view data as request attributes. Names will be taken from the model Map. 
	 * 
	 * <p>
	 * This method is suitable for all resources reachable by {@link javax.servlet.RequestDispatcher}.
	 */
	protected void exposeViewDataAsRequestAttributes(ViewData model, Request request) throws Exception {
		Object value = model.getReturnValue();
		if(null != value){
			request.setAttribute(returnValueAttribute, value);
		}else{
			request.removeAttribute(returnValueAttribute);
		}
		
		Map<String, Object> attributes = model;
		if(null != attributes){
			for (Map.Entry<String, Object> entry : attributes.entrySet()) {
				String modelName = entry.getKey();
				Object modelValue = entry.getValue();
				if (modelValue != null) {
					request.setAttribute(modelName, modelValue);
					if (log.isDebugEnabled()) {
						log.debug("Added model object '" + modelName + "' of type [" + modelValue.getClass().getName() +
								"] to request in view '" + toString() + "'");
					}
				} else {
					request.removeAttribute(modelName);
					if (log.isDebugEnabled()) {
						log.debug("Removed model object '" + modelName +
								"' from request in view '" + toString() + "'");
					}
				}
			}
		}
		
		Errors errors = request.getValidation().errors();
		if(null != errors){
			request.setAttribute(errorsAttribute, errors);
		}
	}

	protected void exposeHelpers(Request request) throws Exception {
		
	}
	
	protected void resolveContentTypeByPath() {
		String filename = Paths.getFileName(path);
		if(filename.lastIndexOf('.') > 0) {
			String mimeType = Servlets.getMimeType(app.getServletContext(), filename);
			if(!Strings.isEmpty(mimeType)) {
				if(MimeTypes.isText(mimeType)) {
					this.contentType = ContentTypes.create(mimeType, app.getDefaultCharset().name());
				}else{
					this.contentType = mimeType;
				}
			}
		}
	}
}
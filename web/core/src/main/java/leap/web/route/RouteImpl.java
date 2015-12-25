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
package leap.web.route;

import java.util.Collections;
import java.util.Map;

import leap.core.web.path.PathTemplate;
import leap.lang.Args;
import leap.lang.Sourced;
import leap.web.action.Action;
import leap.web.action.FailureHandler;
import leap.web.format.RequestFormat;
import leap.web.format.ResponseFormat;
import leap.web.view.View;

class RouteImpl implements Sourced, Route {
	
	protected final Object		 		source;
	protected final String 		 		method;
	protected final PathTemplate 		pathTemplate;
	protected final Action		 		action;
	protected final RequestFormat		requestFormat;
	protected final ResponseFormat		responseFormat;
	protected final View		 		defaultView;
	protected final String		 		defaultViewName;
	protected final String		 		controllerPath;
	protected final Object		 		executionAttributes;
	protected final FailureHandler[]	failureHandlers;
	protected final Map<String, String> requiredParameters;
	
	protected Boolean corsEnabled;
	protected Boolean csrfEnabled;
	protected Boolean supportsMultipart;
	protected boolean acceptValidationError;
	protected boolean httpsOnly;
	protected boolean allowAnonymou;
	protected boolean allowClientOnly;
	
	public RouteImpl(Object 	    source, 
					 String 	    method, 
					 PathTemplate   pathTemplate, 
					 Action 	    action,
					 Boolean		corsEnabled,
					 Boolean		csrfEnabled,
					 Boolean		supportsMultipart,
					 Boolean        acceptValidationError,
					 RequestFormat  requestFormat,
					 ResponseFormat responseFormat,
					 View 		    defaultView, 
					 String		    defaultViewName,
					 String 	    controllerPath, 
					 Object 	    executionAttributes,
					 FailureHandler[]	failureHandlers,
					 Map<String, String> requiredParameters) {
		
		Args.notEmpty(method,"http method");
		Args.notNull(pathTemplate,"path template");
		Args.notNull(action,"action");
		
		this.source              = source;
		this.method              = method;
	    this.pathTemplate        = pathTemplate;
	    this.action              = action;
	    this.corsEnabled		 = corsEnabled;
	    this.csrfEnabled		 = csrfEnabled;
	    this.supportsMultipart   = supportsMultipart;
	    this.acceptValidationError = null == acceptValidationError ? false : acceptValidationError;
	    this.requestFormat       = requestFormat;
	    this.responseFormat	     = responseFormat;
	    this.defaultView	     = defaultView;
	    this.defaultViewName     = defaultViewName;
		this.controllerPath      = controllerPath;
	    this.executionAttributes = executionAttributes;
	    this.failureHandlers     = failureHandlers;
	    this.requiredParameters  = null == requiredParameters ? Collections.emptyMap() : Collections.unmodifiableMap(requiredParameters);
    }
	
	/**
	 * Returns a object indicates the source location defined this route.
	 */
    @Override
	public Object getSource() {
		return source;
	}

	/**
	 * Returns the http method defined in this routing rule.
	 * 
	 * <p>
	 * The returned http method muse be a valid http method name in upper case.
	 * 
	 * <p>
	 * Returns <code>*</code> means to match all http methods.
	 */
	@Override
    public String getMethod(){
		return method;
	}
		
	/**
	 * Returns the path template defined in this routing rule use to match a request path.
	 */
	@Override
    public PathTemplate getPathTemplate() {
		return pathTemplate;
	}
	
	/**
	 * Returns a {@link Action} object to handle http request or <code>null</code> if no action.
	 */
	@Override
    public Action getAction() {
		return action;
	}
	
	@Override
    public FailureHandler[] getFailureHandlers() {
	    return failureHandlers;
    }

	/**
	 * Returns <code>true</code> if this action supports multipart request.
	 */
	@Override
    public boolean supportsMultipart() {
		return supportsMultipart == Boolean.TRUE;
	}
	
	/**
	 * Returns <code>true</code> if this enables cors support explicitly.
	 */
	@Override
    public boolean isCorsEnabled() {
		return corsEnabled == Boolean.TRUE;
	}
	
	/**
	 * Returns <code>true</code> if this disables cors support explicitly.
	 */
	@Override
    public boolean isCorsDisabled() {
		return corsEnabled == Boolean.FALSE;
	}
	
	@Override
    public RequestFormat getRequestFormat() {
		return requestFormat;
	}

	@Override
    public ResponseFormat getResponseFormat() {
		return responseFormat;
	}

	/**
	 * Returns a {@link View} object for rendering the result of action.
	 * 
	 * <p>
	 * Returns <code>null</code> if no view exists for the action.
	 */
    public View getDefaultView() {
		return defaultView;
	}
	
    public String getDefaultViewName(){
		return defaultViewName;
	}
	
	@Override
    public String getControllerPath() {
		return controllerPath;
	}
	
	@Override
    public Object getExecutionAttributes() {
		return executionAttributes;
	}
	
	@Override
    public Map<String, String> getRequiredParameters() {
		return requiredParameters;
	}

	@Override
    public void setCorsEnabled(Boolean enabled) {
		if(null != enabled) {
			corsEnabled = enabled;
		}
    }
	
	@Override
    public void setSupportsMultipart(boolean supports) {
		this.supportsMultipart = supports;
    }
	
	@Override
    public boolean isCsrfEnabled() {
	    return csrfEnabled == Boolean.TRUE;
    }

	@Override
    public boolean isCsrfDisabled() {
	    return csrfEnabled == Boolean.FALSE;
    }

	@Override
    public void setCsrfEnabled(Boolean enabled) {
		csrfEnabled = enabled;
    }
	
	@Override
    public boolean isAcceptValidationError() {
        return acceptValidationError;
    }

    @Override
    public void setAcceptValidationError(boolean allow) {
        this.acceptValidationError = allow;
    }
    
    public boolean isHttpsOnly() {
        return httpsOnly;
    }

    public void setHttpsOnly(boolean httpsOnly) {
        this.httpsOnly = httpsOnly;
    }
    
    @Override
    public boolean isAllowAnonymous() {
        return allowAnonymou;
    }

    @Override
    public void setAllowAnonymous(boolean allow) {
        this.allowAnonymou = allow;
    }

    @Override
    public boolean isAllowClientOnly() {
        return allowClientOnly;
    }

    @Override
    public void setAllowClientOnly(boolean allow) {
        this.allowClientOnly = allow;
    }

    @Override
    public String toString() {
		return method + "  " + pathTemplate.getTemplate() + "  " + action;
    }
}
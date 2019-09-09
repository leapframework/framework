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

import leap.core.security.SimpleSecurity;
import leap.core.web.path.PathTemplate;
import leap.lang.Args;
import leap.lang.ExtensibleBase;
import leap.lang.Sourced;
import leap.web.action.Action;
import leap.web.action.FailureHandler;
import leap.web.format.RequestFormat;
import leap.web.format.ResponseFormat;
import leap.web.view.View;

import java.util.Collections;
import java.util.Map;

class DefaultRoute extends ExtensibleBase implements Sourced, Route {

	protected final Object		 		source;
	protected final String 		 		method;
	protected final PathTemplate 		pathTemplate;
	protected final RequestFormat		requestFormat;
	protected final ResponseFormat		responseFormat;
	protected final View		 		defaultView;
	protected final String		 		defaultViewName;
	protected final String		 		controllerPath;
	protected final Object		 		executionAttributes;
	protected final FailureHandler[]	failureHandlers;
	protected final Map<String, String> requiredParameters;

    protected boolean          enabled;
    protected boolean          executable;
    protected Action           action;
    protected Integer          successStatus;
    protected Boolean          corsEnabled;
    protected String[]         corsExposeHeaders;
    protected Boolean          securityDisabled;
    protected Boolean          csrfEnabled;
    protected Boolean          supportsMultipart;
    protected boolean          acceptValidationError;
    protected boolean          httpsOnly;
    protected Boolean          allowAnonymous;
    protected Boolean          allowRememberMe;
    protected Boolean          allowClientOnly;
    protected String[]         permissions;
    protected String[]         roles;
    protected SimpleSecurity[] securities;

	public DefaultRoute(Object 	    source,
						String 	    method,
						PathTemplate   pathTemplate,
						Action 	    action,
						Boolean		corsEnabled,
						Boolean		csrfEnabled,
						Boolean		supportsMultipart,
                        Boolean     allowAnonymous,
                        Boolean     allowClientOnly,
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

        this.enabled             = true;
        this.executable          = true;
		this.source              = source;
		this.method              = method;
	    this.pathTemplate        = pathTemplate;
	    this.action              = action;
	    this.corsEnabled		 = corsEnabled;
	    this.csrfEnabled		 = csrfEnabled;
	    this.supportsMultipart   = supportsMultipart;
        this.allowAnonymous      = allowAnonymous;
        this.allowClientOnly     = allowClientOnly;
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

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean isExecutable() {
        return executable;
    }

    public void setExecutable(boolean executable) {
        this.executable = executable;
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
    public void setAction(Action action) {
        Args.notNull(action);
        this.action = action;
    }

    @Override
    public FailureHandler[] getFailureHandlers() {
	    return failureHandlers;
    }

    @Override
    public Integer getSuccessStatus() {
        return successStatus;
    }

    @Override
    public void setSuccessStatus(Integer status) throws IllegalStateException {
        if(null != status) {
            if(status < 200 || status > 299) {
                throw new IllegalStateException("The status must be null or 2xx");
            }
        }
        this.successStatus = status;
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
    public String[] getCorsExposeHeaders() {
        return corsExposeHeaders;
    }

    @Override
    public void setCorsExposeHeaders(String... headers) {
        corsExposeHeaders = headers;
    }
	
	@Override
    public void setSupportsMultipart(boolean supports) {
		this.supportsMultipart = supports;
    }

    @Override
    public Boolean getAllowAnonymous() {
        return allowAnonymous;
    }

    public void setAllowAnonymous(Boolean allowAnonymous) {
        this.allowAnonymous = allowAnonymous;
    }

    @Override
    public Boolean getAllowRememberMe() {
        return allowRememberMe;
    }

    @Override
    public void setAllowRememberMe(Boolean allowRememberMe) {
        this.allowRememberMe = allowRememberMe;
    }

    @Override
    public Boolean getAllowClientOnly() {
        return allowClientOnly;
    }

    public void setAllowClientOnly(Boolean allowClientOnly) {
        this.allowClientOnly = allowClientOnly;
    }

    @Override
    public boolean isSecurityDisabled() {
        return null != securityDisabled && securityDisabled;
    }

    @Override
    public void setSecurityDisabled(Boolean disabled) {
        this.securityDisabled = disabled;
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
    public String[] getPermissions() {
        return permissions;
    }

    @Override
    public void setPermissions(String[] permissions) {
        this.permissions = permissions;
    }

    @Override
    public String[] getRoles() {
        return roles;
    }

    @Override
    public void setRoles(String[] roles) {
        this.roles = roles;
    }

    @Override
    public SimpleSecurity[] getSecurities() {
        return securities;
    }

    @Override
    public void setSecurities(SimpleSecurity[] securities) {
        this.securities = securities;
    }

    @Override
    public String toString() {
		return method + "  " + pathTemplate.getTemplate() + "  " + action;
    }
}
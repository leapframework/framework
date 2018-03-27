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

import leap.core.web.path.PathTemplate;
import leap.lang.Assert;
import leap.lang.Buildable;
import leap.lang.ExtensibleBase;
import leap.web.action.Action;
import leap.web.action.FailureHandler;
import leap.web.annotation.Cors;
import leap.web.annotation.Csrf;
import leap.web.format.RequestFormat;
import leap.web.format.ResponseFormat;
import leap.web.view.View;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class RouteBuilder extends ExtensibleBase implements RouteBase, Buildable<Route> {
	
	protected Object	   		   source;
	protected String       		   method;
	protected PathTemplate 		   pathTemplate;
	protected Action	   		   action;
	protected Integer			   successStatus;
	protected Boolean			   corsEnabled;
	protected Boolean			   csrfEnabled;
	protected Boolean			   supportsMultipart;
	protected Boolean              acceptValidationError;
	protected RequestFormat		   requestFormat;
	protected ResponseFormat       responseFormat;
	protected View		   		   defaultView;
	protected String	   		   defaultViewName;
	protected String	  		   controllerPath;
	protected Object	   		   executionAttributes;
	protected Map<String, String>  requiredParameters;
	protected List<FailureHandler> failureHandlers = new ArrayList<>();

    protected Boolean              enabled;
    protected Boolean              httpsOnly;
    protected Boolean              allowAnonymous;
    protected Boolean              allowClientOnly;
    protected Boolean              allowRememberMe;
    protected String[]             permissions;
    protected String[]             clientOnlyPermissions;
    protected String[]             roles;

    public RouteBuilder() {

    }

    public RouteBuilder(String method, PathTemplate pathTemplate) {
        this.method       = method;
        this.pathTemplate = pathTemplate;
    }

    public RouteBuilder(String method, PathTemplate pathTemplate, Action action) {
        this.method       = method;
        this.pathTemplate = pathTemplate;
        this.action       = action;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public RouteBuilder setEnabled(Boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public Object getSource() {
		return source;
	}

	public RouteBuilder setSource(Object source) {
		this.source = source;
		return this;
	}

	public String getMethod() {
		return method;
	}

	public RouteBuilder setMethod(String method) {
		this.method = method;
		return this;
	}
	
	public PathTemplate getPathTemplate() {
		return pathTemplate;
	}

	public RouteBuilder setPathTemplate(PathTemplate pathTemplate) {
		this.pathTemplate = pathTemplate;
		return this;
	}

	public Action getAction() {
		return action;
	}

	public void setAction(Action action) {
		this.action = action;
	}

	public Integer getSuccessStatus() {
		return successStatus;
	}

	public RouteBuilder setSuccessStatus(Integer successStatus) {
		this.successStatus = successStatus;
        return this;
	}

	public Boolean getCorsEnabled() {
		return corsEnabled;
	}

	public RouteBuilder setCorsEnabled(Boolean corsEnabled) {
		this.corsEnabled = corsEnabled;
		return this;
	}
	
	public Boolean getCsrfEnabled() {
		return csrfEnabled;
	}

	public RouteBuilder setCsrfEnabled(Boolean csrfEnabled) {
		this.csrfEnabled = csrfEnabled;
		return this;
	}

	public Boolean getSupportsMultipart() {
		return supportsMultipart;
	}

	public RouteBuilder setSupportsMultipart(Boolean supportsMultipart) {
		this.supportsMultipart = supportsMultipart;
		return this;
	}
	
	public Boolean getAcceptValidationError() {
        return acceptValidationError;
    }

    public RouteBuilder setAcceptValidationError(Boolean acceptValidationError) {
        this.acceptValidationError = acceptValidationError;
        return this;
    }

    public RequestFormat getRequestFormat() {
		return requestFormat;
	}

	public RouteBuilder setRequestFormat(RequestFormat requestFormat) {
		this.requestFormat = requestFormat;
		return this;
	}

	public ResponseFormat getResponseFormat() {
		return responseFormat;
	}

	public RouteBuilder setResponseFormat(ResponseFormat responseFormat) {
		this.responseFormat = responseFormat;
		return this;
	}

	public View getDefaultView() {
		return defaultView;
	}

	public RouteBuilder setDefaultView(View view) {
		this.defaultView = view;
		return this;
	}
	
	public String getDefaultViewName() {
		return defaultViewName;
	}

	public RouteBuilder setDefaultViewName(String defaultViewName) {
		this.defaultViewName = defaultViewName;
		return this;
	}

	public String getControllerPath() {
		return controllerPath;
	}

	public RouteBuilder setControllerPath(String controllerPath) {
		this.controllerPath = controllerPath;
		return this;
	}
	
	public Object getExecutionAttributes() {
		return executionAttributes;
	}

	public RouteBuilder setExecutionAttributes(Object executionAttributes) {
		this.executionAttributes = executionAttributes;
		return this;
	}

	public Map<String, String> getRequiredParameters() {
		return requiredParameters;
	}

	public RouteBuilder setRequiredParameters(Map<String, String> parameters) {
		this.requiredParameters = parameters;
		return this;
	}
	
	public List<FailureHandler> getFailureHandlers() {
		return failureHandlers;
	}
	
 	public RouteBuilder addFailureHandler(FailureHandler h) {
 		if(null != h) {
 			failureHandlers.add(h);
 		}
 		return this;
 	}
 	
 	public RouteBuilder addFailureHandlers(Collection<FailureHandler> c) {
 		if(null != c) {
 			failureHandlers.addAll(c);
 		}
 		return this;
 	}
 	
	public Boolean getHttpsOnly() {
        return httpsOnly;
    }

    public RouteBuilder setHttpsOnly(Boolean httpsOnly) {
        this.httpsOnly = httpsOnly;
        return this;
    }

    public Boolean getAllowAnonymous() {
        return allowAnonymous;
    }

    public RouteBuilder setAllowAnonymous(Boolean allowAnonymous) {
        this.allowAnonymous = allowAnonymous;
        return this;
    }

    public Boolean getAllowClientOnly() {
        return allowClientOnly;
    }

    public RouteBuilder setAllowClientOnly(Boolean allowClientOnly) {
        this.allowClientOnly = allowClientOnly;
        return this;
    }

    public Boolean getAllowRememberMe() {
        return allowRememberMe;
    }

    public RouteBuilder setAllowRememberMe(Boolean allowRememberMe) {
        this.allowRememberMe = allowRememberMe;
        return this;
    }

    public String[] getPermissions() {
        return permissions;
    }

    public RouteBuilder setPermissions(String[] permissions) {
        this.permissions = permissions;
        return this;
    }

    public String[] getClientOnlyPermissions() {
        return clientOnlyPermissions;
    }

    public void setClientOnlyPermissions(String[] clientOnlyPermissions) {
        this.clientOnlyPermissions = clientOnlyPermissions;
    }

    public String[] getRoles() {
        return roles;
    }

    public RouteBuilder setRoles(String[] roles) {
        this.roles = roles;
        return this;
    }

    @Override
	public Route build() {
		Assert.notNull(action, "action cannot be null");
		
		if(null == corsEnabled) {
			Cors cors = action.searchAnnotation(Cors.class);
			this.corsEnabled = null != cors ? cors.value() : null;
		}
		
		if(null == csrfEnabled) {
			Csrf csrf = action.searchAnnotation(Csrf.class);
			this.csrfEnabled = null != csrf ? csrf.value() : null;
		}

		DefaultRoute route = new DefaultRoute(source, method, pathTemplate, action,
            							 corsEnabled, csrfEnabled,
                                         supportsMultipart,
                                         allowAnonymous,
                                         allowClientOnly,
                                         acceptValidationError,
            							 requestFormat,responseFormat,
            							 defaultView, defaultViewName, 
            							 controllerPath, 
            							 executionAttributes,
            							 failureHandlers.toArray(new FailureHandler[failureHandlers.size()]),
            							 requiredParameters);

        if(null != enabled) {
            route.setEnabled(enabled);
        }

        //success status.
        route.setSuccessStatus(successStatus);

        //https only
		if(null != httpsOnly) {
		    route.setHttpsOnly(httpsOnly);
		}

        //remember-me
        route.setAllowRememberMe(allowRememberMe);

        //permissions
        route.setPermissions(permissions);

        //client only permissions
        route.setClientOnlyPermissions(clientOnlyPermissions);

        //roles
        route.setRoles(this.roles);

        //extensions.
        extensions.forEach((t,ex) -> route.setExtension(t, ex));

		return route;
	}
}

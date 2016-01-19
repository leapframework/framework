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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import leap.core.security.UserPrincipal;
import leap.core.web.RequestBase;
import leap.web.assets.AssetSource;
import leap.lang.http.Headers;
import leap.lang.http.MimeType;
import leap.web.action.ActionContext;
import leap.web.format.FormatManager;
import leap.web.route.Route;
import leap.web.view.ViewSource;

/**
 * Wrapps a {@link HttpServletRequest} to provide some more convenient methods.
 */
public abstract class Request extends RequestBase {
	
	/**
	 * Returns current http request.
	 * 
	 * @throws IllegalStateException if http request not exists in current execution context.
	 */
	public static Request current() throws IllegalStateException{
		Request request = (Request)threadlocal.get();
		if(null == request){
			throw new IllegalStateException("Current request not exists");
		}
		return request;
	}
	
	public static Request tryGetCurrent() {
		return (Request)threadlocal.get();
	}
	
	/**
	 * Returns current application.
	 */
	public abstract App app();
	
	/**
	 * Returns current response.
	 */
	public abstract Response response();
	
	/**
	 * Returns a {@link Params} object wrapps current request's parameters.
	 */
	public abstract Params params();
	
	/**
	 * Returns the base path in current request.
	 * 
	 * <p>
	 * A base path is the prefix of request path, default is {@link App#getBasePath()}.
	 * 
	 * <p>
	 * Returns <code>""</code> if no base path.
	 */
	public abstract String getBasePath();
	
	/**
	 * Returns the service path in current request.
	 * 
	 * <p>
	 * A base path concats service path equals to the request path.
	 * 
	 * <p>
	 * Returns <code>""</code> for the root resource.
	 */
	public abstract String getServicePath();
	
	/**
	 * Returns the prefix part of service path without the path extension.
	 * 
	 * <p>
	 * Example : 
	 * 
	 * <pre>
	 * request path : /a/b.html   
	 * service path : /a/b.html  
	 * service path(without extension) : /a/b
	 * </pre>
	 * 
	 * @see #getPath()
	 * @see #getPathExtension()
	 */
	public abstract String getServicePathWithoutExtension();
	
	/**
	 * A path extension is the suffix part of current request path, start from last index of dot character.
	 * 
	 * <p>
	 * Returns the path extension withou the dot character.
	 * 
	 * <p>
	 * Returns empty string "" if no path extension.
	 * 
	 * <p>
	 * Example : 
	 * 
	 * <pre>
	 * /a/b.html -> "html"
	 * /a/b      -> ""
	 * /a/b.     -> ""
	 * </pre>
	 */
	public abstract String getPathExtension();
	
	/**
	 * Returns <code>true</code> if the path extension is not empty.
	 * 
	 * @see #getPathExtension()
	 */
	public abstract boolean hasPathExtension();

    /**
     * Retrieves the body of the request as binary data.
     *
     * @throws IllegalStateException if getInputStream or getReader method has been called.
     */
	public abstract BufferedInputStream peekInputStream() throws IOException;
	
	/**
	 * Returns an array contains all the media types parsed from request header {@link Headers#ACCEPT}.
	 * 
	 * <p>
	 * Returns an empty array if the accept header is empty.
	 */
	public abstract MimeType[] getAcceptableMediaTypes();
	
	/**
	 * Returns the theme name in current request or <code>null</code> if not defined.
	 */
	public abstract String getThemeName();
	
	/**
	 * Sets the theme name in current request.
	 */
	public abstract void setThemeName(String themeName);
	
	/**
	 * Returns an immutable map contains all the parameters in current request.
	 * 
	 * <p>
	 * The key is the parameter name.
	 * 
	 * <p>
	 * The value is a string value if the parameter has only one value.
	 * 
	 * If the parameter is multivalued the value in map is a string array.
	 */
	public abstract Map<String, Object> getParameters();
	
	/**
	 * Same as {@link #getParameters()} but only contains query parametres.
	 */
	public abstract Map<String, Object> getQueryParameters();
	
	/**
	 * Forwards current request to a view
	 */
    public abstract void forwardToView(String viewName) throws ServletException, IOException;
    
	/**
	 * Forwards current request to a action.
	 */
    public abstract void forwardToAction(String actionPath) throws ServletException, IOException;
	
	/**
	 * Returns the {@link AssetSource} in current request or <code>null</code> if not defined.
	 */
	public abstract AssetSource getAssetSource();
	
	/**
	 * Sets {@link AssetSource} in current request.
	 */
	public abstract void setAssetSource(AssetSource assetSource);
	
	/**
	 * Returns the {@link ViewSource} in current request or <code>null</code> if not defined.
	 */
	public abstract ViewSource getViewSource();

	/**
	 * Sets {@link ViewSource} in current request.
	 */
	public abstract void setViewSource(ViewSource viewSource);
	
	/**
	 * Returns the {@link FormatManager} in current request.
	 */
	public abstract FormatManager getFormatManager();
	
	/**
	 * Sets {@link FormatManager} in current request.
	 */
	public abstract void setFormatManager(FormatManager formatManager);

	/**
	 * Returns current {@link ActionContext} or <code>null</code>
	 */
	public abstract ActionContext getActionContext();
	
	/**
	 * Sets current {@link ActionContext}.
	 */
	public abstract void setActionContext(ActionContext actionContext);
	
	/**
	 * @see Route#isAcceptValidationError()
	 */
	public abstract Boolean getAcceptValidationError();
	
	/**
	 * @see Route#setAcceptValidationError(boolean)
	 */
	public abstract void setAcceptValidationError(Boolean accept);
	
	/**
	 * Returns current {@link Result} or <code>null</code>.
	 */
	public abstract Result getResult();
	
	/**
	 * Sets current {@link Result}.
	 */
	public abstract void setResult(Result result);
	
	/**
	 * Returns current {@link UserPrincipal} or <code>null</code>.
	 */
	public abstract UserPrincipal getUser();
	
	/**
	 * Sets current {@link UserPrincipal}.
	 */
	public abstract void setUser(UserPrincipal user);
}
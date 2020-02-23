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
package leap.web.action;

import leap.lang.accessor.AttributeAccessor;
import leap.web.Request;
import leap.web.Response;
import leap.web.Result;
import leap.web.ajax.AjaxDetector;
import leap.web.format.RequestFormat;
import leap.web.format.ResponseFormat;
import leap.web.route.Route;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public interface ActionContext extends AttributeAccessor {

	/**
	 * Returns {@link Request} object wrapps current {@link HttpServletRequest}.
	 */
	Request getRequest();

	/**
	 * Returns {@link Response} object wrapps current {@link HttpServletResponse}.
	 */
	Response getResponse();
	
	/**
	 * Returns the {@link Result} object for current request.
	 */
	Result getResult();
	
	/**
	 * Returns the http method.
	 */
	String getMethod();
	
	/**
	 * Returns the requested action path.
	 */
	String getPath();
	
	/**
	 * Returns the route matched current action.
	 * 
	 * <p>
	 * Returns <code>null</code> if the no route matched. 
	 */
	Route getRoute();
	
	/**
	 * Returns current executing action.
	 */
	Action getAction();

	/**
	 * Returns the {@link ActionParams} contains all the argument values, valid after all the arguments has been resolved.
	 */
	ActionParams getResolvedParams();

	/**
	 * Sets the {@link ActionParams}.
	 */
	void setResolvedParams(ActionParams params);

	/**
	 * Returns execution of current executing action
	 */
	ActionExecution getActionExecution();

	/**
	 * Sets execution of current executing action
	 */
	void setActionExecution(ActionExecution execution);
	
	/**
	 * Returns an immutable map contains all the variables resolved in current routing path.
	 */
	Map<String, String> getPathParameters();
	
	/**
	 * Returns an immutable map contains all the parameters in request and path variables.
	 * 
	 * <p>
	 * The path variable will override the request's parameter if the name is same.
	 */
	Map<String, Object> getMergedParameters();

	/**
	 * Returns the parameters from {@link #getMergedParameters()} and the resolved arguments of action.
	 */
	Map<String, Object> getMergedParametersWithArgs();

	/**
	 * Returns the requested format or <code>null</code>.
	 * 
	 * <p>
	 * Returns <code>null</code> if the {@link RequestFormat} did not resolved.
	 */
	RequestFormat getRequestFormat();
	
	/**
	 * Sets the {@link RequestFormat} of current requst.
	 */
	void setRequestFormat(RequestFormat format);
	
	/**
	 * Returns the requested response format.
	 * 
	 * <p>
	 * Returns <code>null</code> if the {@link ResponseFormat} did not resolved.
	 */
	ResponseFormat getResponseFormat();
	
	/**
	 * Sets the {@link ResponseFormat} of current request.
	 */
	void setResponseFormat(ResponseFormat format);
	
	/**
	 * @see Route#isAcceptValidationError()
	 */
	boolean isAcceptValidationError();
	
	/**
	 * Returns <code>true</code> if current request is an ajax request.
	 * 
	 * @see Request#isAjax()
	 * @see AjaxDetector#detectAjaxRequest(Request)
	 */
	default boolean isAjax() {
		return getRequest().isAjax();
	}
}
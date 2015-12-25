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

import javax.servlet.ServletException;

import leap.core.AppContext;
import leap.web.action.Action;
import leap.web.action.ActionContext;
import leap.web.exception.ResponseException;

/**
 * An object to manage and run a mvc application.
 */
public interface AppHandler extends AppAware {
	
	/**
	 * Init the managed application
	 */
	void initApp() throws Throwable;
	
	/**
	 * Starts the managed application.
	 * 
	 * <p>
	 * Returns a object as a token use to call the {@link #destroy(AppContext, Object)} method.
	 * 
	 * @throws IllegalStateException if this handler was started.
	 */
	Object startApp() throws ServletException, IllegalStateException;
	
	/**
     * Stops the managed application.
     * 
     * @throws IllegalStateException if the token is invalid or this handler was stopped.
     */
    void stopApp(Object token) throws IllegalStateException;
	
	/**
	 * Initializes the given request.
	 * 
	 * <p>
	 * This method called before {@link #handleRequest(Request, Response)}.
	 */
	void prepareRequest(Request request,Response response) throws Throwable;
	
	/**
	 * Handles current http request.
	 * 
	 * <p>
	 * Returns <code>true</code> if current http request was handled by this handler.
	 * 
	 * <p>
	 * Returns <code>false</code> if current http request not handled by this handler.
	 */
	boolean handleRequest(Request request,Response response) throws Throwable;
	
	/**
	 * Executes current http request by the {@link Action} in the given {@link ActionContext}.
	 */
	void executeAction(Request request,Response response,ActionContext actionContext) throws Throwable;
	
	/**
	 * Handles current http request by the {@link Action} in the given action path.
	 * 
	 * <p>
	 * Returns <code>true</code> if the given action path was handled by this handler.
	 * 
	 * <p>
	 * Returns <code>false</code> if the given action path not handled by this handler.
	 */
	boolean handleAction(Request request,Response response,String actionPath) throws Throwable;
	
	/**
	 * Handles http response error.
	 * 
	 * <p>
	 * Returns <code>true</code> if the response error was handled by this handler.
	 */
	boolean handleError(Request request,Response response, int status) throws Throwable;

	/**
	 * Handles http response error.
	 * 
	 * <p>
	 * Returns <code>true</code> if the response error was handled by this handler.
	 */
	boolean handleError(Request request,Response response, int status, String message) throws Throwable;
	
	/**
	 * Handles uncaught exception.
	 * 
	 * <p>
	 * Returns <code>true</code> if the error was handled by this handler.
	 */
	boolean handleError(Request request,Response response,Throwable e) throws Throwable;

	/**
	 * Handles the {@link ResponseException}.
	 */
	void renderResponseException(Request request,Response response,ResponseException e) throws Throwable;
}
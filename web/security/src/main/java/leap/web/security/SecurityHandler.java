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
package leap.web.security;

import leap.web.Request;
import leap.web.Response;
import leap.core.security.Authentication;
import leap.core.security.Authorization;

public interface SecurityHandler {

	/**
	 * Resolves the {@link Authentication} from current request.
	 * 
	 * <p>
	 * Returns an {@link Authentication} object represents the authentication result.
	 * 
	 * <p>
	 * Returns <code>null</code> if no authentication resolved.
	 */
	Authentication resolveAuthentication(Request request,Response response,SecurityContextHolder context) throws Throwable;
	
	/**
	 * Resolves the {@link Authorization} from current request and {@link Authentication}.
	 * 
	 * <p>
	 * Returns an {@link Authorization} object represents the authorization result.
	 * 
	 * <p>
	 * Returns <code>null</code> if no authorization resolved.
	 */
	Authorization resolveAuthorization(Request request,Response response, SecurityContextHolder context) throws Throwable;

    /**
     * Checks the authentication is allowed.
     *
     * <p/>
     * Returns true if allowed.
     */
	boolean checkAuthentication(Request request, Response response, SecurityContextHolder context) throws Throwable;

	/**
	 * Handles if current authentication has no permission to access the resource. 
	 */
	void handleAuthenticationDenied(Request request,Response response, SecurityContextHolder context) throws Throwable;

    /**
     * Checks the authorization is allowed.
     *
     * <p/>
     * Returns true if allowed.
     */
    boolean checkAuthorization(Request request, Response response, SecurityContextHolder context) throws Throwable;
	
	/**
	 * Handles if current authorization has no permission to access the resource.
	 */
	void handleAuthorizationDenied(Request request,Response response, SecurityContextHolder context) throws Throwable;

	/**
	 * Handles login request.
	 * 
	 * <p>
	 * Returns <code>true</code> if handled.
	 */
	boolean handleLoginRequest(Request request,Response response,SecurityContextHolder context) throws Throwable;
	
	/**
	 * Handles logout request
	 * 
	 * <p>
	 * Returns <code>true</code> if handled.
	 */
	boolean handleLogoutRequest(Request request,Response response,SecurityContextHolder context) throws Throwable;

}
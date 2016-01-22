/*
 * Copyright 2014 the original author or authors.
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
package leap.web.security.authc;

import java.io.IOException;

import javax.servlet.ServletException;

import leap.lang.Result;
import leap.lang.intercepting.State;
import leap.web.Request;
import leap.web.Response;

public interface AuthenticationResolver {

	/**
	 * Resolves {@link Authentication} in the request.
	 *
	 * <p/>
	 * Returns a failure result if failed to resolve authentication and the request was handled by the resolver.
     */
	Result<Authentication> resolveAuthentication(Request request, Response response, AuthenticationContext context) throws Throwable;

	/**
	 * Invoked when login success.
     */
	default void onLoginSuccess(Request request,Response response,Authentication authentication) {
		
	}

	/**
	 * Invoked when logout success.
     */
	default void onLogoutSuccess(Request request,Response response) {
		
	}

}
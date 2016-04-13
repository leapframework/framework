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

import leap.core.security.Authentication;
import leap.core.security.Credentials;
import leap.core.security.UserPrincipal;
import leap.web.Request;
import leap.web.Response;
import leap.web.security.authc.credentials.AuthenticateCredentialsContext;

public interface AuthenticationManager {

    /**
     * Resolves the {@link Authentication} from the request.
     */
    Authentication resolveAuthentication(Request request,Response response, AuthenticationContext context) throws Throwable;
    
    /**
     * Authenticates the given {@link Credentials} and return the {@link Authentication} as result.
     * 
     * <p>
     * Returns <code>null</code> if the given {@link Credentials} invalid.
     */
    Authentication authenticate(AuthenticateCredentialsContext authenticationContext, Credentials credentials);
    
    /**
     * Creates a new anonymous user principal.
     */
	UserPrincipal createAnonymous(Request request, Response response, AuthenticationContext context);
	
	/**
	 * Save the authentication immediately.
	 */
	void loginImmediately(Request request, Response response, Authentication authentication);
	
	/**
	 * Destroy current authentication immediately.
	 */
	void logoutImmediately(Request request, Response response);
	
}
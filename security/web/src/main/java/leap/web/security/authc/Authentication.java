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
package leap.web.security.authc;

import leap.core.security.ClientPrincipal;
import leap.core.security.Credentials;
import leap.core.security.UserPrincipal;

public interface Authentication {

	/**
	 * Returns <code>true</code> if the authentication is authenticated.
     */
	boolean isAuthenticated();
	
	/**
	 * Returns <code>true</code> if the authentication is authenticated from remember-me store.
	 */
	boolean isRememberMe();
	
	/**
	 * Required. Returns the authentication credentials.
	 */
	Credentials getCredentials();
	
	/**
	 * Optional. Returns the authentication user.
	 */
	UserPrincipal getUserPrincipal();
	
	/**
	 * Optional. Returns the authentication client.
	 */
	ClientPrincipal getClientPrincipal();

	/**
	 * Optional. Returns the authentication token if exists.
     */
	String getToken();

	/**
	 * Sets the authentication token.
	 *
	 * @throws IllegalStateException if the authentication token already exists.
     */
	void setToken(String token) throws IllegalStateException;

	/**
	 * Returns <code>true</code> if the authentication only contains client, no user.
     */
	default boolean isClientOnly() {
		return getUserPrincipal() == null || getUserPrincipal().isAnonymous();
	}

}
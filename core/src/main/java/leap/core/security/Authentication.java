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
package leap.core.security;

import leap.lang.Arrays2;

public interface Authentication {

	/**
	 * Returns <code>true</code> if the authentication is authenticated.
     */
	boolean isAuthenticated();
	
	/**
	 * Returns <code>true</code> if the authentication is authenticated from remember-me store.
	 */
	default boolean isRememberMe() {
        return false;
    }

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
	 * Required. Returns the authentication credentials.
	 */
	Object getCredentials();
	
	/**
	 * Optional. Returns the user principal.
	 */
	UserPrincipal getUser();
	
    /**
     * Optional. Returns the client principal.
     */
    default ClientPrincipal getClient() {
        return null;
    }

    /**
     * Optional. Returns the granted permissions.
     */
	default String[] getPermissions() {
        return Arrays2.EMPTY_STRING_ARRAY;
    }

    /**
     * Optional. Returns the granted roles.
     */
    default String[] getRoles() {
        return Arrays2.EMPTY_STRING_ARRAY;
    }

    /**
     * Returns true if the client is not null.
     */
    default boolean hasClient() {
        return null != getClient();
    }

	/**
	 * Returns <code>true</code> if the authentication only contains client, no user.
     */
	default boolean isClientOnly() {
		return hasClient() && (getUser() == null || getUser().isAnonymous());
	}

    /**
     * Returns <code>true</code> if the user is authenticated and not remember-me.
     */
    default boolean isFullyAuthenticated() {
        return isAuthenticated() && !isRememberMe();
    }
}
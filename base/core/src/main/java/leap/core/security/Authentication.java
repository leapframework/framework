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
     * Returns the granted permissions.
     */
	String[] getPermissions();

    /**
     * Sets the granted permissions.
     */
    void setPermissions(String... permissions);

    /**
     * Returns the granted roles.
     */
    String[] getRoles();

    /**
     * Sets the granted roles.
     */
    void setRoles(String... roles);

    /**
     * Returns true if the client is not null.
     */
    default boolean hasClient() {
        return null != getClient();
    }

    /**
     * Returns true if the user is not null and not anonymous.
     */
    default boolean isUserAuthenticated() {
        return null != getUser() && !getUser().isAnonymous();
    }

    /**
     * Returns true if client is not null and not anonymous.
     */
    default boolean isClientAuthenticated() {
        return null != getClient() && !getClient().isAnonymous();
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
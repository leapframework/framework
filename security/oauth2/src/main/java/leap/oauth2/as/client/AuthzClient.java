/*
 * Copyright 2015 the original author or authors.
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
package leap.oauth2.as.client;

/**
 * A registered oauth2.0 client.
 */
public interface AuthzClient {
	
	/**
	 * Required.
	 */
	String getId();
	
	/**
	 * Required.
	 */
	String getSecret();
	
	/**
	 * Returns the expires of access token defined in the client.
	 * 
	 * <p>
	 * Return <code>null</code> if use default expires.
	 */
	Integer getAccessTokenExpires();
	
    /**
     * Returns the expires of refresh token defined in the client.
     * 
     * <p>
     * Return <code>null</code> if use default expires.
     */
    Integer getRefreshTokenExpires();

	/**
	 * Optional. Returns the default redirect uri of the client.
     */
	String getRedirectUri();

	/**
	 * Optional. Returns the default logout uri of the client.
     */
	String getLogoutUri();
	
	/**
	 * Returns <code>true</code> if the client is enabled.
	 */
	boolean isEnabled();

	/**
	 * Returns <code>true</code> if the client allows to obtain authorization code.
	 */
	boolean isAllowAuthorizationCode();

	/**
	 * Returns <code>true</code> if the client allows to use refresh token.
	 */
	boolean isAllowRefreshToken();

	/**
	 * Returns <code>true</code> if the client allows to use login token.
     */
	boolean isAllowLoginToken();

	/**
     * Returns <code>true</code> if the client accepts the given uri as redirect uri.
     */
    boolean acceptsRedirectUri(String uri);

	/**
	 * Returns <code>true</code> if the client accepts the given uri as logout uri.
     */
	boolean acceptsLogoutUri(String uri);
}
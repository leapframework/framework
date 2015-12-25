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
package leap.oauth2.as;

import java.security.PrivateKey;

import leap.core.AppConfig;
import leap.oauth2.as.client.AuthzClientStore;
import leap.oauth2.as.code.AuthzCodeStore;
import leap.oauth2.as.token.AuthzTokenStore;

/**
 * The configuration of oauth2 authorization server.
 */
public interface OAuth2ServerConfig {

	/**
	 * Returns <code>true</code> if oauth2 authorization server is enabled.
	 *
	 * <p/>
	 * Default is <code>false</code>.
     */
	boolean isEnabled();
	
	/**
	 * Returns <code>true</code> if enables to cleanup expired tokens, authorization codes, etc.
	 * 
	 * <p>
	 * Default is <code>true</code>.
	 */
	boolean isCleanupEnabled();
	
	/**
	 * Returns the interval in seconds for cleanup expired tokens, authorization codes, etc. 
	 */
	int getCleanupInterval();
	
	/**
	 * Returns <code>true</code> if the auth server accepts https request only. 
	 * 
	 * <p>
	 * Default is <code>true</code>.
	 */
	boolean isHttpsOnly();
	
	/**
	 * Returns <code>true</code> if the Open ID Connect is enabled.
	 * 
	 * <p>
	 * Default is <code>true</code>.
	 */
	boolean isOpenIDConnectEnabled();

	/**
	 * Returns <code>true</code> if single logout (single signout) enabled.
	 *
	 * <p/>
	 * Default is <code>true</code>.
     */
	boolean isSingleLogoutEnabled();

	/**
	 * Returns <code>true</code> if oauth2 token endpoint is enabled.
	 *
	 * <p/>
	 * Default is enabled, but only valid if {@link #isEnabled()} returns <code>true</code>.
     */
	boolean isTokenEndpointEnabled();

	/**
	 * Returns <code>true</code> if oauth2 authorization endpoint is enabled.
	 *
	 * <p/>
	 * Default is enabled, but only valid if {@link #isEnabled()} returns <code>true</code>.
	 */
	boolean isAuthzEndpointEnabled();

	/**
	 * Returns <code>true</code> if oauth2 token info endpoint is enabled.
	 *
	 * <p/>
	 * The token info endpoint is not a standard endpoint in oauth2,
	 *
	 * see <a href="https://developers.google.com/identity/protocols/OAuth2UserAgent#validatetoken">Google OAuth2.0 protocols</a> for details.
	 *
	 * <p/>
	 * Default is enabled, but only valid if {@link #isEnabled()} returns <code>true</code>.
	 */
	boolean isTokenInfoEndpointEnabled();

	/**
	 * Returns <code>true</code> if oauth2 token info endpoint is enabled.
	 *
	 * <p/>
	 * The logout endpoint is an endpoint defined in open id connect.
	 *
	 * <p/>
	 * Default is enabled, but only valid if both {@link #isEnabled()} and {@link #isOpenIDConnectEnabled()} returns <code>true</code>.
	 */
	boolean isLogoutEndpointEnabled();

	boolean isPasswordCredentialsEnabled();
	
	boolean isRefreshTokenEnabled();
	
	boolean isClientCredentialsEnabled();
	
	boolean isAuthorizationCodeEnabled();
	
	boolean isImplicitGrantEnabled();
	
	String getTokenEndpointPath();
	
	String getAuthzEndpointPath();
	
	String getTokenInfoEndpointPath();
	
	String getLogoutEndpointPath();
	
	String getErrorView();
	
	/**
	 * Returns the login view or <code>null</code> if use default login flow.
	 */
	String getLoginView();
	
	/**
	 * Returns the logout view or <code>null</code> use default.
	 */
	String getLogoutView();
	
	/**
	 * The default expires in of access token.
	 */
	int getDefaultAccessTokenExpires();
	
	/**
	 * The default expires in of refresh token.
	 */
	int getDefaultRefreshTokenExpires();
	
	/**
	 * The default expres in of authorization code.
	 */
	int getDefaultAuthorizationCodeExpires();
	
	/**
	 * The default expires in of id token for Open ID Connect.
	 */
	int getDefaultIdTokenExpires();
	
	/**
	 * Returns the datasource name if use jdbc store.
	 */
	String getJdbcDataSourceName();
	
	/**
	 * Returns the global private key of authz server.
	 * 
	 *  <p>
	 *  Returns <code>null</code> if the private key not configured.
	 */
	PrivateKey getPrivateKey();
	
	/**
	 * Returns the global private key of authz server.
	 * 
	 * <p>
	 * Returns the private key from {@link AppConfig#ensureGetPrivateKey()} if not configured.
	 */
	PrivateKey ensureGetPrivateKey();
	
	/**
	 * Returns the {@link AuthzClientStore}.
	 */
	AuthzClientStore getClientStore();
	
	/**
	 * Returns the {@link AuthzCodeStore}.
	 */
	AuthzCodeStore getCodeStore();
	
	/**
	 * Returns the {@link AuthzTokenStore}.
	 */
	AuthzTokenStore getTokenStore();
}

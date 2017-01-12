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
import java.security.PublicKey;

import leap.core.AppConfig;
import leap.core.security.token.jwt.JwtVerifier;
import leap.oauth2.as.client.AuthzClientStore;
import leap.oauth2.as.code.AuthzCodeStore;
import leap.oauth2.as.sso.AuthzSSOStore;
import leap.oauth2.as.token.AuthzTokenStore;

/**
 * The configuration of oauth2 authorization server.
 */
public interface OAuth2AuthzServerConfig {

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
     *
     * <p/>
     * Default is {@link OAuth2AuthzServerConfigurator#DEFAULT_CLEANUP_INTERVAL}.
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
	 * Returns <code></code> if single login enabled.
	 *
	 * <p/>
	 * Default is <code>true</code>.
     */
	boolean isSingleLoginEnabled();

	/**
	 * Returns <code>true</code> if single logout enabled.
	 *
	 * <p/>
	 * Default is <code>true</code>.
     */
	boolean isSingleLogoutEnabled();

    /**
     * Returns <code>true</code> if login token (endpoint) enabled.
     *
     * <p/>
     * Deault is <code>true</code>.
     */
	boolean isLoginTokenEnabled();

    /**
     * Returns <code>true</code> if user info (endpoint) enabled.
     *
     * <p/>
     * Default is <code>true</code>.
     */
	boolean isUserInfoEnabled();

    /**
     * Returns <code>true</code> if client credentials grant type is allowed.
     *
     * <p/>
     * Default is <code>true</code>.
     */
	boolean isClientCredentialsEnabled();

	/**
	 * Returns <code>true</code> if token client grant type is allowed.
	 *
	 * <p/>
	 * Default is <code>true</code>.
	 */
	boolean isTokenClientEnabled();

    /**
     * Returns <code>true</code> if password credentials grant type is allowed.
     *
     * <p/>
     * Default is <code>true</code>.
     */
    boolean isPasswordCredentialsEnabled();

	boolean isRequestLevelScopeEnabled();

    /**
     * Returns <code>true</code> if authorization code flow enabled.
     *
     * <p/>
     * Default is <code>true</code>.
     */
	boolean isAuthorizationCodeEnabled();

    /**
     * Returns <code>true</code> if implicit flow enabled.
     *
     * <p/>
     * Default is <code>true</code>
     */
	boolean isImplicitGrantEnabled();

    /**
     * Returns the path of authorization endpoint.
     *
     * <p/>
     * Default is {@link OAuth2AuthzServerConfigurator#DEFAULT_AUTHZ_ENDPOINT_PATH}.
     */
    String getAuthzEndpointPath();

    /**
     * Returns the path of token endpoint.
     *
     * <p/>
     * Default is {@link OAuth2AuthzServerConfigurator#DEFAULT_TOKEN_ENDPOINT_PATH}.
     */
	String getTokenEndpointPath();

    /**
     * Returns the path of tokeninfo endpoint.
     *
     * <p/>
     * Default is {@link OAuth2AuthzServerConfigurator#DEFAULT_TOKENINFO_ENDPOINT_PATH}.
     */
	String getTokenInfoEndpointPath();

    /**
     * Returns the path of logintoken endpoint.
     *
     * <p/>
     * Default is {@link OAuth2AuthzServerConfigurator#DEFAULT_LOGINTOKEN_ENDPOINT_PATH}.
     */
	String getLoginTokenEndpointPath();

    /**
     * Returns the path of logout endopint.
     *
     * <p/>
     * Default is {@link OAuth2AuthzServerConfigurator#DEFAULT_LOGOUT_ENDPOINT_PATH}.
     */
    String getLogoutEndpointPath();

    /**
     * Returns the path of userinfo endpoint.
     *
     * <p/>
     *
     * Default is {@link OAuth2AuthzServerConfigurator#DEFAULT_USERINFO_ENDPOINT_PATH}.
     */
    String getUserInfoEndpointPath();

    /**
     * Retruns the error view for rendering oauth2 request error.
     *
     * <p/>
     * Default is {@link OAuth2AuthzServerConfigurator#DEFAULT_ERROR_VIEW}.
     */
	String getErrorView();
	
	/**
	 * Returns the login view or <code>null</code> if use default login flow.
     *
     * <p/>
     * Default is {@link OAuth2AuthzServerConfigurator#DEFAULT_LOGIN_VIEW}.
	 */
	String getLoginView();
	
	/**
	 * Returns the logout view or <code>null</code> use default.
     *
     * <p/>
     * Default is {@link OAuth2AuthzServerConfigurator#DEFAULT_LOGOUT_VIEW}.
	 */
	String getLogoutView();
	
	/**
	 * Returns the default expires in (seconds) of access token.
	 */
	int getDefaultAccessTokenExpires();
	
	/**
	 * Returns the default expires in (seconds) of refresh token.
	 */
	int getDefaultRefreshTokenExpires();
	
	/**
	 * Returns the default expires in (seconds) of authorization code.
	 */
	int getDefaultAuthorizationCodeExpires();

	/**
	 * Returns the default expires in (seconds) of login token.
     */
	int getDefaultLoginTokenExpires();
	
	/**
	 * Returns the default expires in (seconds) of id token for Open ID Connect.
	 */
	int getDefaultIdTokenExpires();

	/**
	 * Returns the default expires in (seconds) of a sso login session.
     */
	int getDefaultLoginSessionExpires();
	
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
	 * Returns the global public key of authz server.
	 *
	 *  <p>
	 *  Returns <code>null</code> if the public key not configured.
	 */
	PublicKey getPublicKey();

	/**
	 * Returns the jwt verifier
	 */
	JwtVerifier getJwtVerifier();
	
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

	/**
	 * Returns the {@link AuthzSSOStore}
     */
	AuthzSSOStore getSSOStore();

}

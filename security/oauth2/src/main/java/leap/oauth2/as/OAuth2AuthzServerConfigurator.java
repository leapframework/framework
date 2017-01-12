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

import leap.core.security.token.jwt.JwtVerifier;
import leap.oauth2.as.store.AuthzInMemoryStore;

import java.security.PublicKey;

/**
 * The configurator of {@link OAuth2AuthzServerConfig}.
 */
public interface OAuth2AuthzServerConfigurator {

    String DEFAULT_AUTHZ_ENDPOINT_PATH      = "/oauth2/authorize";
    String DEFAULT_TOKEN_ENDPOINT_PATH      = "/oauth2/token";
    String DEFAULT_TOKENINFO_ENDPOINT_PATH  = "/oauth2/tokeninfo";
    String DEFAULT_LOGINTOKEN_ENDPOINT_PATH = "/oauth2/logintoken";
    String DEFAULT_USERINFO_ENDPOINT_PATH   = "/oauth2/userinfo";
    String DEFAULT_LOGOUT_ENDPOINT_PATH     = "/oauth2/logout";

    String DEFAULT_ERROR_VIEW  = "/oauth2/error";
    String DEFAULT_LOGIN_VIEW  = "/oauth2/login";
    String DEFAULT_LOGOUT_VIEW = "/oauth2/logout";

    int DEFAULT_CLEANUP_INTERVAL           = 60 * 5;         //5 minutes.
    int DEFAULT_ACCESS_TOKEN_EXPIRES       = 3600;           //1 hour.
    int DEFAULT_REFRESH_TOKEN_EXPIRES      = 3600 * 24 * 30; //30 days
    int DEFAULT_LOGIN_TOKEN_EXPIRES        = 60 * 5;         //5 minutes
    int DEFAULT_AUTHORIZATION_CODE_EXPIRES = 60 * 5;         //5 minutes
    int DEFAULT_ID_TOKEN_EXPIRES           = 60 * 5;         //5 minutes
    int DEFAULT_LOGIN_SESSION_EXPIRES      = 3600 * 24;      //24 hours

	/**
	 * Returtns the {@link OAuth2AuthzServerConfig}.
	 */
	OAuth2AuthzServerConfig config();
	
	/**
	 * Returns the {@link AuthzInMemoryStore} for configuration.
	 */
	AuthzInMemoryStore inMemoryStore();
	
	/**
	 * Enables oauth2 authorization server.
	 */
	default OAuth2AuthzServerConfigurator enable() {
	    return setEnabled(true);
	}

	/**
	 * Use {@link AuthzInMemoryStore} as client, code, token and sso store.
	 */
	OAuth2AuthzServerConfigurator useInMemoryStore();
	
	/**
	 * Use jdbc (database) as client, code, token and sso store.
	 */
	OAuth2AuthzServerConfigurator useJdbcStore();

	/**
	 * Enables or Disables oauth2 authorization server.
     *
     * <p/>
     * Default is disabled.
	 */
	OAuth2AuthzServerConfigurator setEnabled(boolean enabled);

    /**
     * Enables or Diables cleanup expired data.
     *
     * <p/>
     * Default is enabled.
     */
	OAuth2AuthzServerConfigurator setCleanupEnabled(boolean cleanup);

    /**
     * Sets the cleanup interval in seconds.
     *
     * <p/>
     * Default is {@link #DEFAULT_CLEANUP_INTERVAL}.
     */
	OAuth2AuthzServerConfigurator setCleanupInterval(int seconds);

    /**
     * Sets allow https request only in authorization server.
     *
     * <p/>
     * Default is <code>true</code>.
     */
	OAuth2AuthzServerConfigurator setHttpsOnly(boolean httpsOnly);

    /**
     * Enables or Disables single login.
     *
     * <p/>
     * Default is enabled.
     */
	OAuth2AuthzServerConfigurator setSingleLoginEnabled(boolean enabled);

    /**
     * Enables or Disables single logout. Valid only single login is enabled.
     *
     * <p/>
     * Default is enabled.
     */
	OAuth2AuthzServerConfigurator setSingleLogoutEnabled(boolean enabled);

    /**
     * Enables or Diables login token.
     *
     * <p/>
     * Default is enabled.
     */
    OAuth2AuthzServerConfigurator setLoginTokenEnabled(boolean enabled);

    /**
     * Enables or Disables client credentials grant type.
     *
     * <p/>
     * Default is enabled.
     */
	OAuth2AuthzServerConfigurator setClientCredentialsEnabled(boolean enabled);

    /**
     * Enables or Disables request level scope.
     *
     * <p/>
     * Default is disable.
     */
    OAuth2AuthzServerConfigurator setRequestLevelScopeEnabled(boolean enabled);

    /**
     * Enables or Disables password credentials grant type.
     *
     * <p/>
     * Default is enabled.
     */
    OAuth2AuthzServerConfigurator setPasswordCredentialsEnabled(boolean eanbled);

    /**
     * Enables or Disables authorization code flow.
     *
     * <p/>
     * Default is enabled.
     */
    OAuth2AuthzServerConfigurator setAuthorizationCodeEnabled(boolean enabled);

    /**
     * Enables or Disables implicit grant flow.
     *
     * <p/>
     * Default is enabled.
     */
	OAuth2AuthzServerConfigurator setImplicitGrantEnabled(boolean enabled);

    /**
     * Sets the path of authorization endpoint.
     *
     * <p/>
     * Default {@link #DEFAULT_AUTHZ_ENDPOINT_PATH}.
     */
    OAuth2AuthzServerConfigurator setAuthzEndpointPath(String path);

    /**
     * Sets the path of token endpoint.
     *
     * <p/>
     * Default is {@link #DEFAULT_TOKEN_ENDPOINT_PATH}.
     */
	OAuth2AuthzServerConfigurator setTokenEndpointPath(String path);

    /**
     * Sets the path of tokeninfo endpoint.
     *
     * <p/>
     * Default is {@link #DEFAULT_TOKENINFO_ENDPOINT_PATH}.
     */
	OAuth2AuthzServerConfigurator setTokenInfoEndpointPath(String path);

    /**
     * Sets the paht of logintoken endpoint.
     *
     * <p/>
     * Default is {@link #DEFAULT_LOGINTOKEN_ENDPOINT_PATH}.
     */
    OAuth2AuthzServerConfigurator setLoginTokenEndpointPath(String path);

    /**
     * Sets the path logout endpoint.
     *
     * <p/>
     * Default is {@link #DEFAULT_LOGOUT_ENDPOINT_PATH}.
     */
	OAuth2AuthzServerConfigurator setLogoutEndpointPath(String path);

    /**
     * Sets the path of error view.
     *
     * <p/>
     * Default is {@link #DEFAULT_ERROR_VIEW}.
     */
	OAuth2AuthzServerConfigurator setErrorView(String view);

    /**
     * Sets the path of login view.
     *
     * <p/>
     * Default is {@link #DEFAULT_LOGIN_VIEW}.
     */
	OAuth2AuthzServerConfigurator setLoginView(String view);

    /**
     * Sets the path of logout view.
     *
     * <p/>
     * Default is {@link #DEFAULT_LOGOUT_VIEW}.
     */
	OAuth2AuthzServerConfigurator setLogoutView(String view);

    /**
     * Sets the default expires in (seconds) of access token.
     *
     * <p/>
     * Default is {@link #DEFAULT_ACCESS_TOKEN_EXPIRES}.
     */
	OAuth2AuthzServerConfigurator setDefaultAccessTokenExpires(int seconds);

    /**
     * Sets the default expires in (seconds) of refresh token.
     *
     * <p/>
     * Default is {@link #DEFAULT_REFRESH_TOKEN_EXPIRES}.
     */
	OAuth2AuthzServerConfigurator setDefaultRefreshTokenExpires(int seconds);

    /**
     * Sets the default expires in (seconds) of authorization code.
     *
     * <p/>
     * Default is {@link #DEFAULT_AUTHORIZATION_CODE_EXPIRES}.
     */
	OAuth2AuthzServerConfigurator setDefaultAuthorizationCodeExpires(int seconds);

    /**
     * Sets the default expires in (seconds) of id token.
     *
     * <p/>
     * Default is {@link #DEFAULT_ID_TOKEN_EXPIRES}.
     */
	OAuth2AuthzServerConfigurator setDefaultIdTokenExpires(int seconds);

    /**
     * Sets the default expires in (seconds) of login session.
     *
     * <p/>
     * Default is {@link #DEFAULT_LOGIN_SESSION_EXPIRES}.
     */
	OAuth2AuthzServerConfigurator setDefaultLoginSessionExpires(int seconds);

    /**
     * use rsa verifier as the jwt verifier.
     */
    OAuth2AuthzServerConfigurator useRsaJwtVerifier();

    /**
     * use the specify erifier as the jwt verifier.
     */
    OAuth2AuthzServerConfigurator useJwtVerifier(JwtVerifier verifier);
    
    /**
     * set the public key for jwt verifier.
     */
    OAuth2AuthzServerConfigurator setPublicKey(PublicKey publicKey);
}
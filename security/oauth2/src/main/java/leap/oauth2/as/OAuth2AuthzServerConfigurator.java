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

import leap.oauth2.as.client.AuthzClientStore;
import leap.oauth2.as.code.AuthzCodeStore;
import leap.oauth2.as.store.AuthzInMemoryStore;

public interface OAuth2AuthzServerConfigurator {
	
    String DEFAULT_AUTHZ_ENDPOINT_PATH     = "/oauth2/authorize";
    String DEFAULT_TOKEN_ENDPOINT_PATH     = "/oauth2/token";
    String DEFAULT_TOKENINFO_ENDPOINT_PATH = "/oauth2/tokeninfo";
    String DEFAULT_LOGOUT_ENDPOINT_PATH    = "/oauth2/logout";
    
    String DEFAULT_ERROR_VIEW              = "/oauth2/error";
    String DEFAULT_LOGIN_VIEW              = "/oauth2/login";
    String DEFAULT_LOGOUT_VIEW             = "/oauth2/logout";
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
	 * Enables Open ID Connect (login & logout).
	 */
	default OAuth2AuthzServerConfigurator enableOpenIDConnect() {
	    return setOpenIDConnectEnabled(true);
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
	 * Sets enable or disable oauth2 authorization server.
	 */
	OAuth2AuthzServerConfigurator setEnabled(boolean enabled);
	
	OAuth2AuthzServerConfigurator setCleanupEnabled(boolean cleanup);
	
	OAuth2AuthzServerConfigurator setCleanupInterval(int seconds);
	
	OAuth2AuthzServerConfigurator setHttpsOnly(boolean httpsOnly);
	
	OAuth2AuthzServerConfigurator setOpenIDConnectEnabled(boolean enabled);

	OAuth2AuthzServerConfigurator setSingleLoginEnabled(boolean enabled);

	OAuth2AuthzServerConfigurator setSingleLogoutEnabled(boolean enabled);
	
	OAuth2AuthzServerConfigurator setAuthzEndpointEnabled(boolean enabled);
	
	OAuth2AuthzServerConfigurator setTokenEndpointEnabled(boolean enabled);
	
	OAuth2AuthzServerConfigurator setTokenInfoEndpointEnabled(boolean enabled);
	
	OAuth2AuthzServerConfigurator setLogoutEndpointEnabled(boolean enabled);
	
	OAuth2AuthzServerConfigurator setPasswordCredentialsEnabled(boolean eanbled);
	
	OAuth2AuthzServerConfigurator setRefreshTokenEnabled(boolean enabled);
	
	OAuth2AuthzServerConfigurator setClientCredentialsEnabled(boolean enabled);
	
	OAuth2AuthzServerConfigurator setAuthorizationCodeEnabled(boolean enabled);
	
	OAuth2AuthzServerConfigurator setImplicitGrantEnabled(boolean enabled);
	
	OAuth2AuthzServerConfigurator setTokenEndpointPath(String path);
	
	OAuth2AuthzServerConfigurator setAuthzEndpointPath(String path);
	
	OAuth2AuthzServerConfigurator setTokenInfoEndpointPath(String path);
	
	OAuth2AuthzServerConfigurator setLogoutEndpointPath(String path);
	
	OAuth2AuthzServerConfigurator setErrorView(String view);
	
	OAuth2AuthzServerConfigurator setLoginView(String view);
	
	OAuth2AuthzServerConfigurator setLogoutView(String view);

	OAuth2AuthzServerConfigurator setDefaultAccessTokenExpires(int seconds);
	
	OAuth2AuthzServerConfigurator setDefaultRefreshTokenExpires(int seconds);
	
	OAuth2AuthzServerConfigurator setDefaultAuthorizationCodeExpires(int seconds);
	
	OAuth2AuthzServerConfigurator setDefaultIdTokenExpires(int seconds);

	OAuth2AuthzServerConfigurator setDefaultLoginSessionExpires(int seconds);
	
	OAuth2AuthzServerConfigurator setClientStore(AuthzClientStore clientStore);
	
	OAuth2AuthzServerConfigurator setCodeStore(AuthzCodeStore codeStore);
	
	OAuth2AuthzServerConfigurator setPrivateKey(PrivateKey privateKey);
	
	OAuth2AuthzServerConfigurator setPrivateKey(String privateKey);
	
}
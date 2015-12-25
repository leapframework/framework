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

public interface OAuth2ServerConfigurator {
	
    String DEFAULT_AUTHZ_ENDPOINT_PATH     = "/oauth2/authorize";
    String DEFAULT_TOKEN_ENDPOINT_PATH     = "/oauth2/token";
    String DEFAULT_TOKENINFO_ENDPOINT_PATH = "/oauth2/tokeninfo";
    String DEFAULT_LOGOUT_ENDPOINT_PATH    = "/oauth2/logout";
    
    String DEFAULT_ERROR_VIEW              = "/oauth2/error";
    String DEFAULT_LOGIN_VIEW              = "/oauth2/login";
    String DEFAULT_LOGOUT_VIEW             = "/oauth2/logout";
	/**
	 * Returtns the {@link OAuth2ServerConfig}.
	 */
	OAuth2ServerConfig config();
	
	/**
	 * Returns the {@link AuthzInMemoryStore} for configuration.
	 */
	AuthzInMemoryStore inMemoryStore();
	
	/**
	 * Enables oauth2 authorization server.
	 */
	default OAuth2ServerConfigurator enable() {
	    return setEnabled(true);
	}
	
	/**
	 * Enables Open ID Connect (login & logout).
	 */
	default OAuth2ServerConfigurator enableOpenIDConnect() {
	    return setOpenIDConnectEnabled(true);
	}
	
	/**
	 * Use {@link AuthzInMemoryStore} as client, code and token store.
	 */
	OAuth2ServerConfigurator useInMemoryStore();
	
	/**
	 * Use {@link AuthzJdbcStore} as client, code and token store.
	 */
	OAuth2ServerConfigurator useJdbcStore();

	/**
	 * Sets enable or disable oauth2 authorization server.
	 */
	OAuth2ServerConfigurator setEnabled(boolean enabled);
	
	OAuth2ServerConfigurator setCleanupEnabled(boolean cleanup);
	
	OAuth2ServerConfigurator setCleanupInterval(int seconds);
	
	OAuth2ServerConfigurator setHttpsOnly(boolean httpsOnly);
	
	OAuth2ServerConfigurator setOpenIDConnectEnabled(boolean enabled);

	OAuth2ServerConfigurator setSingleLogoutEnabled(boolean enabled);
	
	OAuth2ServerConfigurator setAuthzEndpointEnabled(boolean enabled);
	
	OAuth2ServerConfigurator setTokenEndpointEnabled(boolean enabled);
	
	OAuth2ServerConfigurator setTokenInfoEndpointEnabled(boolean enabled);
	
	OAuth2ServerConfigurator setLogoutEndpointEnabled(boolean enabled);
	
	OAuth2ServerConfigurator setPasswordCredentialsEnabled(boolean eanbled);
	
	OAuth2ServerConfigurator setRefreshTokenEnabled(boolean enabled);
	
	OAuth2ServerConfigurator setClientCredentialsEnabled(boolean enabled);
	
	OAuth2ServerConfigurator setAuthorizationCodeEnabled(boolean enabled);
	
	OAuth2ServerConfigurator setImplicitGrantEnabled(boolean enabled);
	
	OAuth2ServerConfigurator setTokenEndpointPath(String path);
	
	OAuth2ServerConfigurator setAuthzEndpointPath(String path);
	
	OAuth2ServerConfigurator setTokenInfoEndpointPath(String path);
	
	OAuth2ServerConfigurator setLogoutEndpointPath(String path);
	
	OAuth2ServerConfigurator setErrorView(String view);
	
	OAuth2ServerConfigurator setLoginView(String view);
	
	OAuth2ServerConfigurator setLogoutView(String view);

	OAuth2ServerConfigurator setDefaultAccessTokenExpires(int seconds);
	
	OAuth2ServerConfigurator setDefaultRefreshTokenExpires(int seconds);
	
	OAuth2ServerConfigurator setDefaultAuthorizationCodeExpires(int seconds);
	
	OAuth2ServerConfigurator setDefaultIdTokenExpires(int seconds);
	
	OAuth2ServerConfigurator setClientStore(AuthzClientStore clientStore);
	
	OAuth2ServerConfigurator setCodeStore(AuthzCodeStore codeStore);
	
	OAuth2ServerConfigurator setPrivateKey(PrivateKey privateKey);
	
	OAuth2ServerConfigurator setPrivateKey(String privateKey);
	
}
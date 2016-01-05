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
package leap.oauth2.rs;

/**
 * The configurator of oauth2 resource server.
 */
public interface OAuth2ResServerConfigurator {

	/**
	 * The mode of authorization server that the resource server will obtain token details from it.
	 */
	enum AuthzServerMode {
		NONE,
		LOCAL,
		REMOTE
	}

	/**
	 * Returns the configuration.
     */
	OAuth2ResServerConfig config();

	/**
	 * Enables oauth2 resource server in current webapp.
     */
	default OAuth2ResServerConfigurator enable() {
		return setEnabled(true);
	}

	/**
	 * Sets enable or disable oauth2 resource server in current webapp.
     */
	OAuth2ResServerConfigurator setEnabled(boolean enabled);

	/**
	 * Use local authz server.
	 *
	 * @see {@link OAuth2ResServerConfig#isUseLocalAuthzServer()}.
     */
	OAuth2ResServerConfigurator useLocalAuthzServer();

	/**
	 * Use remote authz server.
	 *
	 * <p/>
	 * The {@link #setTokenInfoEndpointUrl(String)} must be invoked later.
     */
	OAuth2ResServerConfigurator useRemoteAuthzServer();

	/**
	 * Use remote authz server and sets the token info endpoint url.
	 *
	 * @see {@link OAuth2ResServerConfig#isUseRemoteAuthzServer()}.
	 *
	 * @param tokenInfoEndpointUrl the token info endpoint url or remote authorization server.
     */
	OAuth2ResServerConfigurator useRemoteAuthzServer(String tokenInfoEndpointUrl);

	/**
	 * Sets the mode of authz server.
     */
	OAuth2ResServerConfigurator setAuthzServerMode(AuthzServerMode mode);

	/**
	 * Sets the url of token info endpoint in oauth2 authorization server.
	 *
	 * <p/>
	 * Valid when use remote authz server.
     */
	OAuth2ResServerConfigurator setTokenInfoEndpointUrl(String url);
	
}
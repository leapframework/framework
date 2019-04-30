/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package leap.oauth2.webapp;

/**
 * The configurator of oauth2 web app.
 */
public interface OAuth2Configurator {

	/**
	 * Returns the configuration.
     */
	OAuth2Config config();

	/**
	 * Enables oauth2 in current web app.
     */
	default OAuth2Configurator enable() {
		return setEnabled(true);
	}

	/**
	 * Sets enable or disable oauth2 in current web app.
     */
	OAuth2Configurator setEnabled(boolean enabled);

    /**
     * Automatic sets all the urls by given the root url of oauth2 server.
     */
    OAuth2Configurator setServerUrl(String url);

    /**
     * todo : doc
     */
    OAuth2Configurator setAuthorizeUrl(String url);

	/**
	 * Sets the url of token info endpoint in oauth2 authorization server.
	 *
	 * <p/>
	 * Valid when use remote authz server.
     */
	OAuth2Configurator setTokenInfoUrl(String url);

    /**
     * Sets the url of user info endpoint in oauth2 authorization server.
     */
    OAuth2Configurator setUserInfoUrl(String url);

	/**
	 * Add path to ignore access token resolve.
	 */
	OAuth2Configurator ignorePath(String path);
    
    /**
     * todo : doc
     */
    OAuth2Configurator setTokenUrl(String url);

	/**
	 * todo : doc
	 */
	OAuth2Configurator setIndirectTokenUrl(String url);

    /**
     * todo: doc
     */
    OAuth2Configurator setPublicKeyUrl(String url);

	/**
     * todo: doc
	 */
	OAuth2Configurator setClientId(String clientId);

	/**
     * todo: doc
	 */
	OAuth2Configurator setClientSecret(String clientSecret);

	
}
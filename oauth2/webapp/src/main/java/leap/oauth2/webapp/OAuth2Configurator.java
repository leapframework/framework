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

import leap.core.security.token.jwt.JwtVerifier;

/**
 * The configurator of oauth2 resource server.
 */
public interface OAuth2Configurator {

	/**
	 * Returns the configuration.
     */
	OAuth2Config config();

	/**
	 * Enables oauth2 resource server in current webapp.
     */
	default OAuth2Configurator enable() {
		return setEnabled(true);
	}

	/**
	 * Sets enable or disable oauth2 resource server in current webapp.
     */
	OAuth2Configurator setEnabled(boolean enabled);

	/**
	 * 
	 * Sets the ras public key string for rsa jwt verifier
	 * 
	 * @param publicKey ths rsa public key string
	 */
	OAuth2Configurator setRsaPublicKeyStr(String publicKey);
	/**
	 * Use rsa jwt verifier to verify jwt token.
	 */
	OAuth2Configurator useRsaJwtVerifier();
	
	/**
	 * Use the specify jwt verifier to verify jwt token
	 */
	OAuth2Configurator useJwtVerifier(JwtVerifier verifier);
	
	/**
	 * Sets the url of token info endpoint in oauth2 authorization server.
	 *
	 * <p/>
	 * Valid when use remote authz server.
     */
	OAuth2Configurator setRemoteTokenInfoEndpointUrl(String url);

    /**
     * Sets the url of user info endpoint in oauth2 authorization server.
     *
     * <p/>
     * Valid when use remote authz server.
     */
    OAuth2Configurator setRemoteUserInfoEndpointUrl(String url);

    /**
     * todo : doc
     */
    OAuth2Configurator setTokenEndpointUrl(String url);

    /**
     * todo : doc
     */
    OAuth2Configurator setAuthorizationEndpointUrl(String url);

    /**
     * todo : doc
     */
    OAuth2Configurator setUseRemoteUserInfo(Boolean used);

	/**
	 * Sets the resource server Id of this server in oauth2 authorization server.
	 *
	 * <p/>
	 * use when use remote authz server to validate access token.
	 */
	OAuth2Configurator setResourceServerId(String resourceServerId);

	/**
	 * Sets the resource server secret of this server in oauth2 authorization server.
	 *
	 * <p/>
	 * use when use remote authz server to validate access token.
	 */
	OAuth2Configurator setResourceServerSecret(String resourceServerSecret);

	
}
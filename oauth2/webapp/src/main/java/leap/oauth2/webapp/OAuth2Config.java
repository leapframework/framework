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
 * The configuration of oauth2 web app.
 *
 * @see OAuth2Configurator
 */
public interface OAuth2Config {

	/**
	 * Returns <code>true</code> if oauth2.0 is enabled in current web app.
	 */
	boolean isEnabled();

    /**
     * todo : doc
     */
    String getAuthorizationUrl();

    /**
     * todo : doc
     */
    String getTokenUrl();

	/**
	 * Returns the url of token info endpoint in oauth2 authorization server.
	 */
	String getTokenInfoUrl();

    /**
     * Returns the url of user info endpoint in oauth2 authorization server.
     */
    String getUserInfoUrl();

    /**
     * todo: doc
     */
    String getPublicKeyUrl();

	/**
	 * todo: doc
     */
	String getClientId();

	/**
	 * todo: doc
     */
	String getClientSecret();

	/**
	 * Returns the jwt verifier of this resource server
	 */
	JwtVerifier getJwtVerifier();
}
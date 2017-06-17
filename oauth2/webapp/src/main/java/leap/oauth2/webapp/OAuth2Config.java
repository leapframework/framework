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
 * The configuration of oauth2 resource server in web app.
 *
 * @see OAuth2Configurator
 */
public interface OAuth2Config {

	/**
	 * Returns <code>true</code> if oauth2.0 resource server is enabled in current web app.
	 */
	boolean isEnabled();

    /**
     * todo : doc
     */
    boolean isUseRemoteUserInfo();

	/**
	 * Returns the url of token info endpoint in oauth2 authorization server.
     *
     * <p/>
     * Required if use remote authorization server.
	 */
	String getRemoteTokenInfoEndpointUrl();

    /**
     * Returns the url of user info endpoint in oauth2 authorization server.
     *
     * <p/>
     * Required if use remote authorization server.
     */
    String getRemoteUserInfoEndpointUrl();

    /**
     * todo : doc
     */
    String getTokenEndpointUrl();

    /**
     * todo : doc
     */
    String getAuthorizationEndpointUrl();

	/**
	 * Returns the resource id of this resource server in authz server
	 * @return resourceId
     */
	String getResourceServerId();

	/**
	 * Returns the resource secret of this resource server in authz server
	 * @return resourceSecret
     */
	String getResourceServerSecret();

	/**
	 * Returns the jwt verifier of this resource server
	 * @return
	 */
	JwtVerifier getJwtVerifier();
}
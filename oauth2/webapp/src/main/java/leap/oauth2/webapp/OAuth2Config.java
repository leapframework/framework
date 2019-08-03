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

import leap.core.web.RequestIgnore;

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
     * Returns true if login from oauth2 server.
     *
     * <p/>
     * Default is false.
     */
    boolean isLogin();

    /**
     * Returns true if logout from oauth2 server.
     *
     * <p/>
     * Default is false.
     */
    boolean isLogout();

    /**
     * Returns true will obtains the access token details (includes refresh token) after login success.
     *
     * <p/>
     * Default is false.
     */
    boolean isLoginWithAccessToken();

    /**
     * Returns true if force to lookup user info from remote server.
     *
     * <p/>
     * Default is false.
     */
    boolean isForceLookupUserInfo();

    /**
     * todo : doc
     */
    String getAuthorizeUrl();

    /**
     * todo : doc
     */
    String getTokenUrl();

    /**
     * Optional.
     */
    String getIndirectTokenUrl();

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
    String getJwksUrl();

    /**
     * Returns the url of oauth2 server' logout endpoint.
     */
    String getLogoutUrl();

	/**
	 * todo: doc
     */
	String getClientId();

	/**
	 * todo: doc
     */
	String getClientSecret();

    /**
     * todo: doc
     */
    String getRedirectUri();

    /**
     * Optional. Returns the view for display error from oauth2 server.
     */
    String getErrorView();

    /**
     * Optional. Returns the view for logout success from oauth2 server.
     */
    String getLogoutView();

    /**
     * Returns the request of ignore access token resolved. 
     */
    RequestIgnore[] getIgnores();

    /**
     * return kid of jwks when server return mulit jwks
     */
    default String getJwksKeyId(){
        return "oauth2_authorize_server_public_key";
    };
}
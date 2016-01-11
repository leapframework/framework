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
package leap.oauth2.wac;

import leap.oauth2.wac.token.WacTokenStore;

/**
 * The configuration of oauth2 web app client.
 */
public interface OAuth2WebAppConfig {
    
    /**
     * Returns <code>true</code> if current web app was enabled as oauth2 web app client.
     */
    boolean isEnabled();

    /**
     * Returns <code>true</code> if login from remote oauth2 authorization server is enabled.
     *
     * <p/>
     * If enabled, the oauth2 interceptor will redirect the user to the login page in oauth2 authorization server.
     */
    boolean isOAuth2LoginEnabled();
    
    /**
     * Returns <code>true</code> if logout from remote oauth2 authorization server is enabled.
     * 
     * <p>
     * If enabled, the oauth2 interceptor will logout from remote oauth server first,
     * 
     * and then logout from local web app.
     */
    boolean isOAuth2LogoutEnabled();
    
    /**
     * Returns <code>true</code> if the web app use user's access token to request another oauth2 protected resource.
     *
     * <p/>
     * Default is <code>false</code>.
     */
    boolean isAccessTokenEnabled();
    
    /**
     * Required. Returns the client id.
     */
    String getClientId();
    
    /**
     * Required. Returns the client secret.
     */
    String getClientSecret();
    
    /**
     * Returns the uri of redirect endpoint.
     */
    String getClientRedirectUri();

    /**
     * Optional.
     */
    String getClientLogoutUri();
    
    /**
     * Required. Returns the token endpoint url of remote oauth server.
     */
    String getServerTokenEndpointUrl();
    
    /**
     * Required. Returns the authorization endpoint url of remote oauth server.
     */
    String getServerAuthorizationEndpointUrl();
    
    /**
     * Optional. Returns the logout endpoint url or remote oauth server.
     * 
     * <p>
     * Required if remote logout enabled.
     */
    String getServerLogoutEndpointUrl();
    
    /**
     * Returns the error view for rendering error info returned by oauth server.
     */
    String getErrorView();
    
    /**
     * Returns the cookie name for saving the info of access token in browser.
     */
    String getAccessTokenCookieName();
    
    /**
     * Returns the {@link WacTokenStore} or <code>null</code> if not enabled.
     */
    WacTokenStore getTokenStore();
}
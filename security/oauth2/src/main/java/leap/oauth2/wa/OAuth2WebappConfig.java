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
package leap.oauth2.wa;

import leap.oauth2.wa.token.WebAccessTokenStore;

public interface OAuth2WebAppConfig {
    
    /**
     * Returns <code>true</code> if login from remote oauth server is enabled.
     */
    boolean isEnabled();
    
    /**
     * Returns <code>true</code> if logout from remote oauth server is enabled.
     * 
     * <p>
     * If eanbled, the oauth2 interceptor will logout from remote oauth server first, 
     * 
     * and then logout from local web app.
     */
    boolean isRemoteLogoutEnabled();
    
    /**
     * Returns <code>true</code> if user access token enabled.
     */
    boolean isUserAccessTokenEnabled();
    
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
     * Required. Returns the token endpoint url of remote oauth server.
     */
    String getRemoteTokenEndpointUrl();
    
    /**
     * Required. Returns the authorization endpoint url of remote oauth server.
     */
    String getRemoteAuthzEndpointUrl();
    
    /**
     * Optional. Returns the logout endpoint url or remote oauth server.
     * 
     * <p>
     * Required if remoute logout enabled.
     */
    String getRemoteLogoutEndpointUrl();
    
    /**
     * Returns the error view for rendering error info returned by oauth server.
     */
    String getErrorView();
    
    /**
     * Returns the cookie name for saving the info of access token in browser.
     */
    String getAccessTokenCookieName();
    
    /**
     * Returns the {@link WebAccessTokenStore} or <code>null</code> if not enabled.
     */
    WebAccessTokenStore getAccessTokenStore();
}
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
package leap.oauth2.web;

public interface OAuth2WebConfigurator {
    
    String DEFAULT_REDIRECT_PATH            = "/oauth2_redirect";
    String DEFAULT_ERROR_VIEW               = "/oauth2_error";
    String DEFAULT_ACCESS_TOKEN_COOKIE_NAME = "web_at";
    
    /**
     * Enables oauth2 client web app.
     */
    default OAuth2WebConfigurator enable() {
        return enable(true);
    }
    
    /**
     * Enables oauth2 client web app.
     */
    default OAuth2WebConfigurator enable(boolean remoteLogout) {
        return setEnabled(true).setRemoteLogoutEnabled(remoteLogout);
    }
    
    /**
     * Enables remote logout (remote login must be enabeld).
     */
    default OAuth2WebConfigurator enabledRemoteLogout() {
        return setRemoteLogoutEnabled(true);
    }
    
    /**
     * Enables user's access token. 
     */
    default OAuth2WebConfigurator enableUserAccessToken() {
        return setUserAccessTokenEnabled(true);
    }
    
    /**
     * Use jdbc as access token store to persist the user's access token.
     */
    OAuth2WebConfigurator useJdbcAccessTokenStore();
    
    /**
     * Sets enable or disable for remote login.
     */
    OAuth2WebConfigurator setEnabled(boolean enabled);
    
    /**
     * Sets enable or disable for remote logout;
     */
    OAuth2WebConfigurator setRemoteLogoutEnabled(boolean enabled);
    
    /**
     * Sets enable or disable access token management for login user.
     * 
     * <p>
     * See oauth2.0 sepcification.
     */
    OAuth2WebConfigurator setUserAccessTokenEnabled(boolean enabled);
    
    /**
     * Sets the required client id of this webapp.
     */
    OAuth2WebConfigurator setClientId(String clientId);
    
    /**
     * Sets the required client secret of this webapp.
     */
    OAuth2WebConfigurator setClientSecret(String clientSecret);
    
    /**
     * Sets the redirect endpoint path ( the redirect_uri sends to authorization server) .
     */
    OAuth2WebConfigurator setClientRedirectUri(String uri);
    
    /**
     * Sets the authorization server's url, such as <code>https://example.com</code>
     * 
     * <p>
     * Optional if remote urls configured by {@link #setRemoteAuthzEndpointUrl(String)} and {@link #setRemoteTokenEndpointUrl(String)}. 
     */
    OAuth2WebConfigurator setRemoteServerUrl(String url);

    /**
     * Sets the token endpoint url of authorization server.
     * 
     * <p>
     * Optional if the authorization server url was configured by {@link #setRemoteServerUrl(String)}. 
     */
    OAuth2WebConfigurator setRemoteTokenEndpointUrl(String url);
    
    /**
     * Sets the authorization endpoint url of authorization server.
     * 
     * <p>
     * Optional if the authorization server url was configured by {@link #setRemoteServerUrl(String)}. 
     */
    OAuth2WebConfigurator setRemoteAuthzEndpointUrl(String url);
    
    /**
     * Sets the error view name.
     */
    OAuth2WebConfigurator setErrorView(String viewName);
}
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

import leap.core.AppConfigException;
import leap.core.annotation.ConfigProperty;
import leap.core.annotation.Configurable;
import leap.core.annotation.Inject;
import leap.core.ds.DataSourceManager;
import leap.core.store.JdbcStore;
import leap.lang.Strings;
import leap.lang.path.Paths;
import leap.oauth2.wac.token.WacTokenStore;
import leap.web.App;
import leap.web.AppInitializable;
import leap.web.security.SecurityConfigurator;

@Configurable(prefix="oauth2.wac")
public class DefaultOAuth2WebAppConfig implements OAuth2WebAppConfig, OAuth2WebAppConfigurator, AppInitializable {

    protected @Inject         SecurityConfigurator sc;
    protected @Inject("jdbc") WacTokenStore        jdbcTokenStore;

    protected WacTokenStore tokenStore;
    protected boolean       enabled;
    protected boolean       oauth2LoginEnabled;
    protected boolean       oauth2LogoutEnabled;
    protected boolean       accessTokenEnabled;
    protected String        clientId;
    protected String        clientSecret;

    protected String clientRedirectUri = DEFAULT_REDIRECT_PATH;
    protected String clientLogoutUri   = DEFAULT_LOGOUT_PATH;
    protected String serverUrl;
    protected String serverTokenEndpointUrl;
    protected String serverAuthorizationEndpointUrl;
    protected String serverLogoutEndpointUrl;
    protected String errorView             = DEFAULT_ERROR_VIEW;
    protected String accessTokenCookieName = DEFAULT_ACCESS_TOKEN_COOKIE_NAME;
    
    @Override
    public OAuth2WebAppConfigurator useJdbcTokenStore() {
        this.tokenStore = jdbcTokenStore;
        return this;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
    
    @ConfigProperty
    public OAuth2WebAppConfigurator setEnabled(boolean enabled) {
        this.enabled = enabled;
        this.oauth2LoginEnabled = enabled;
        this.oauth2LogoutEnabled = enabled;
        return this;
    }

    @Override
    public boolean isOAuth2LoginEnabled() {
        return oauth2LoginEnabled;
    }

    @Override
    public boolean isOAuth2LogoutEnabled() {
        return oauth2LogoutEnabled;
    }

    public boolean isAccessTokenEnabled() {
        return accessTokenEnabled;
    }

    public OAuth2WebAppConfigurator setAccessTokenEnabled(boolean userAccessTokenEnabled) {
        this.accessTokenEnabled = userAccessTokenEnabled;
        return this;
    }

    public String getClientId() {
        return clientId;
    }

    @ConfigProperty
    public OAuth2WebAppConfigurator setClientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    @ConfigProperty
    public OAuth2WebAppConfigurator setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
        return this;
    }

    public String getClientRedirectUri() {
        return clientRedirectUri;
    }

    @ConfigProperty
    public OAuth2WebAppConfigurator setClientRedirectUri(String uri) {
        this.clientRedirectUri = Paths.suffixWithoutSlash(uri);
        return this;
    }

    @Override
    public String getClientLogoutUri() {
        return clientLogoutUri;
    }

    @ConfigProperty
    public OAuth2WebAppConfigurator setClientLogoutUri(String uri) {
        this.clientLogoutUri = uri;
        return this;
    }

    @ConfigProperty
    public OAuth2WebAppConfigurator setServerUrl(String url) {
        this.serverUrl = Paths.suffixWithoutSlash(url);
        this.serverAuthorizationEndpointUrl = this.serverUrl + "/oauth2/authorize";
        this.serverTokenEndpointUrl = this.serverUrl + "/oauth2/token";
        this.serverLogoutEndpointUrl = this.serverUrl + "/oauth2/logout";
        return this;
    }

    public String getServerTokenEndpointUrl() {
        return serverTokenEndpointUrl;
    }

    @ConfigProperty
    public OAuth2WebAppConfigurator setServerTokenEndpointUrl(String url) {
        this.serverTokenEndpointUrl = url;
        return this;
    }
    
    @Override
    public String getServerAuthorizationEndpointUrl() {
        return serverAuthorizationEndpointUrl;
    }
    
    @ConfigProperty
    public OAuth2WebAppConfigurator setServerAuthorizationEndpointUrl(String url) {
        this.serverAuthorizationEndpointUrl = url;
        return this;
    }
    
    @Override
    public String getServerLogoutEndpointUrl() {
        return serverLogoutEndpointUrl;
    }

    @ConfigProperty
    public OAuth2WebAppConfigurator setServerLogoutEndpointUrl(String url) {
        this.serverLogoutEndpointUrl = url;
        return this;
    }

    public String getErrorView() {
        return errorView;
    }

    @ConfigProperty
    public OAuth2WebAppConfigurator setErrorView(String errorView) {
        this.errorView = errorView;
        return this;
    }

    public String getAccessTokenCookieName() {
        return accessTokenCookieName;
    }

    @ConfigProperty
    public OAuth2WebAppConfigurator setAccessTokenCookieName(String accessTokenCookieName) {
        this.accessTokenCookieName = accessTokenCookieName;
        return this;
    }

    public WacTokenStore getTokenStore() {
        return tokenStore;
    }

    @Override
    public OAuth2WebAppConfigurator setTokenStore(WacTokenStore tokenStore) {
        return setAccessTokenStore(tokenStore);
    }

    public OAuth2WebAppConfigurator setAccessTokenStore(WacTokenStore accessTokenStore) {
        this.tokenStore = accessTokenStore;
        return this;
    }

    public void checkConfiguration() throws AppConfigException {
        if(enabled) {
            if(Strings.isEmpty(clientId)) {
                throw new AppConfigException("clientId must not be empty.");
            }
            
            if(Strings.isEmpty(clientSecret)) {
                throw new AppConfigException("clientSecret must not be empty.");
            }
            
            if(Strings.isEmpty(clientRedirectUri)) {
                throw new AppConfigException("clientRedirectUri must not be empty.");
            }
            
            if(Strings.isEmpty(getServerAuthorizationEndpointUrl())) {
                throw new AppConfigException("serverAuthorizationEndpointUrl must not be empty.");
            }
            
            if(accessTokenEnabled) {
                if(Strings.isEmpty(serverTokenEndpointUrl)) {
                    throw new AppConfigException("serverTokenEndpointUrl must not be empty.");
                }
            }
            
            if(oauth2LogoutEnabled) {
                if(Strings.isEmpty(serverLogoutEndpointUrl)) {
                    throw new AppConfigException("serverLogoutEndpointUrl must not be empty.");
                }
            }
        }
    }

    @Override
    public void postAppInit(App app) throws Throwable {
        checkConfiguration();
        
        if(enabled) {
            if(!sc.config().isEnabled()) {
                sc.enable(true);
            }
        }

        if(accessTokenEnabled) {
            
            if(tokenStore instanceof JdbcStore) {
                ((JdbcStore) tokenStore).setDataSourceName(DataSourceManager.DEFAULT_DATASOURCE_NAME);
            }
            
        }
    }
    
}
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
import leap.core.annotation.Configurable;
import leap.core.annotation.Inject;
import leap.core.ds.DataSourceManager;
import leap.core.store.JdbcStore;
import leap.lang.Strings;
import leap.lang.path.Paths;
import leap.oauth2.wac.token.WebAccessTokenStore;
import leap.web.App;
import leap.web.AppInitializable;
import leap.web.security.SecurityConfigurator;

@Configurable(prefix="oauth2.webapp")
public class DefaultOAuth2WebAppConfig implements OAuth2WebAppConfig, OAuth2WebAppConfigurator, AppInitializable {

    protected @Inject              SecurityConfigurator sc;
    protected @Inject(false)       WebAccessTokenStore  accessTokenStore;
    protected @Inject(name="jdbc") WebAccessTokenStore  jdbcAccessTokenStore;
    
    protected boolean          enabled;
    protected boolean          remoteLogoutEnabled;
    protected boolean          userAccessTokenEnabled;
    protected String           clientId;
    protected String           clientSecret;
    protected String           clientRedirectUri = DEFAULT_REDIRECT_PATH;
    protected String           remoteServerUrl;
    protected String           remoteTokenEndpointUrl;
    protected String           remoteAuthzEndpointUrl;
    protected String           remoteLogoutEndpointUrl;
    protected String           errorView             = DEFAULT_ERROR_VIEW;
    protected String           accessTokenCookieName = DEFAULT_ACCESS_TOKEN_COOKIE_NAME;
    
    @Override
    public OAuth2WebAppConfigurator useJdbcAccessTokenStore() {
        this.accessTokenStore = jdbcAccessTokenStore;
        return this;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
    
    @Configurable.Property
    public OAuth2WebAppConfigurator setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }
    
    @Override
    public boolean isRemoteLogoutEnabled() {
        return remoteLogoutEnabled;
    }

    public OAuth2WebAppConfigurator setRemoteLogoutEnabled(boolean enabled) {
        this.remoteLogoutEnabled = enabled;
        return this;
    }
    
    public boolean isUserAccessTokenEnabled() {
        return userAccessTokenEnabled;
    }

    public OAuth2WebAppConfigurator setUserAccessTokenEnabled(boolean userAccessTokenEnabled) {
        this.userAccessTokenEnabled = userAccessTokenEnabled;
        return this;
    }

    public String getClientId() {
        return clientId;
    }

    @Configurable.Property
    public OAuth2WebAppConfigurator setClientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    @Configurable.Property
    public OAuth2WebAppConfigurator setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
        return this;
    }

    public String getClientRedirectUri() {
        return clientRedirectUri;
    }

    @Configurable.Property
    public OAuth2WebAppConfigurator setClientRedirectUri(String uri) {
        this.clientRedirectUri = Paths.suffixWithoutSlash(uri);
        return this;
    }

    @Configurable.Property
    public OAuth2WebAppConfigurator setRemoteServerUrl(String url) {
        this.remoteServerUrl = Paths.suffixWithoutSlash(url);
        this.remoteAuthzEndpointUrl = this.remoteServerUrl + "/oauth2/authorize";
        this.remoteTokenEndpointUrl = this.remoteServerUrl + "/oauth2/token";
        this.remoteLogoutEndpointUrl = this.remoteServerUrl + "/oauth2/logout";
        return this;
    }

    public String getRemoteTokenEndpointUrl() {
        return remoteTokenEndpointUrl;
    }

    @Configurable.Property
    public OAuth2WebAppConfigurator setRemoteTokenEndpointUrl(String url) {
        this.remoteTokenEndpointUrl = url;
        return this;
    }
    
    @Override
    public String getRemoteAuthzEndpointUrl() {
        return remoteAuthzEndpointUrl;
    }
    
    @Configurable.Property
    public OAuth2WebAppConfigurator setRemoteAuthzEndpointUrl(String url) {
        this.remoteAuthzEndpointUrl = url;
        return this;
    }
    
    @Override
    public String getRemoteLogoutEndpointUrl() {
        return remoteLogoutEndpointUrl;
    }

    @Configurable.Property
    public OAuth2WebAppConfigurator setRemoteLogoutEndpointUrl(String url) {
        this.remoteLogoutEndpointUrl = url;
        return this;
    }

    public String getErrorView() {
        return errorView;
    }

    @Configurable.Property
    public OAuth2WebAppConfigurator setErrorView(String errorView) {
        this.errorView = errorView;
        return this;
    }

    public String getAccessTokenCookieName() {
        return accessTokenCookieName;
    }

    @Configurable.Property
    public OAuth2WebAppConfigurator setAccessTokenCookieName(String accessTokenCookieName) {
        this.accessTokenCookieName = accessTokenCookieName;
        return this;
    }

    public WebAccessTokenStore getAccessTokenStore() {
        return accessTokenStore;
    }

    public OAuth2WebAppConfigurator setAccessTokenStore(WebAccessTokenStore accessTokenStore) {
        this.accessTokenStore = accessTokenStore;
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
            
            if(Strings.isEmpty(getRemoteAuthzEndpointUrl())) {
                throw new AppConfigException("remoteAuthzEndpointUrl must not be empty.");
            }
            
            if(userAccessTokenEnabled) {
                if(Strings.isEmpty(remoteTokenEndpointUrl)) {
                    throw new AppConfigException("remoteTokenEndpointUrl must not be empty.");
                }
            }
            
            if(remoteLogoutEnabled) {
                if(Strings.isEmpty(remoteLogoutEndpointUrl)) {
                    throw new AppConfigException("remoteLogoutEndpointUrl must not be empty.");
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

        if(userAccessTokenEnabled) {
            
            if(accessTokenStore instanceof JdbcStore) {
                ((JdbcStore) accessTokenStore).setDataSourceName(DataSourceManager.DEFAULT_DATASOURCE_NAME);
            }
            
        }
    }
    
}
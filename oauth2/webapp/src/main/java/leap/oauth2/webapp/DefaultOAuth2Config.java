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

import leap.core.BeanFactory;
import leap.core.annotation.ConfigProperty;
import leap.core.annotation.Configurable;
import leap.core.annotation.Inject;
import leap.core.cache.Cache;
import leap.core.cache.CacheManager;
import leap.core.ioc.PostCreateBean;
import leap.core.security.token.jwt.JwtVerifier;
import leap.lang.path.Paths;
import leap.web.App;
import leap.web.AppInitializable;
import leap.web.security.SecurityConfigurator;

@Configurable(prefix="oauth2")
public class DefaultOAuth2Config implements OAuth2Config, OAuth2Configurator, PostCreateBean, AppInitializable {

	protected @Inject SecurityConfigurator sc;
    protected @Inject CacheManager         cm;

	protected boolean               enabled;
    protected boolean               login;
    protected boolean               logout;
    protected String                authorizationUrl;
    protected String                tokenUrl;
	protected String                tokenInfoUrl;
    protected String                userInfoUrl;
    protected String                publicKeyUrl;
    protected String                logoutUrl;

	protected String                clientId;
	protected String                clientSecret;
    protected String                redirectUri;

	protected Cache<String, Object> cachedInterceptUrls;
	protected String				rsaPublicKeyStr;
	protected JwtVerifier           jwtVerifier;

	@Override
	public OAuth2Config config() {
		return this;
	}

    public boolean isEnabled() {
	    return enabled;
    }

	@ConfigProperty
    public OAuth2Configurator setEnabled(boolean enabled) {
		this.enabled = enabled;
	    return this;
    }

    @Override
    public boolean isLogin() {
        return login;
    }

    @ConfigProperty
    public void setLogin(boolean login) {
        this.login = login;
    }

    @Override
    public boolean isLogout() {
        return logout;
    }

    @ConfigProperty
    public void setLogout(boolean logout) {
        this.logout = logout;
    }

    @Override
    @ConfigProperty
    public OAuth2Configurator setServerUrl(String serverUrl) {//don't change the parameter name (used by config property)
        serverUrl = Paths.suffixWithoutSlash(serverUrl);

        this.authorizationUrl = serverUrl + "/oauth2/authorize";
        this.tokenUrl         = serverUrl + "/oauth2/token";
        this.tokenInfoUrl     = serverUrl + "/oauth2/tokeninfo";
        this.userInfoUrl      = serverUrl + "/oauth2/userinfo";
        this.publicKeyUrl     = serverUrl + "/oauth2/publickey";
        this.logoutUrl        = serverUrl + "/oauth2/logout";

        return this;
    }

    @Override
    public String getAuthorizationUrl() {
        return authorizationUrl;
    }

    @Override
    public OAuth2Configurator setAuthorizationUrl(String url) {
        this.authorizationUrl = url;
        return this;
    }

    @Override
    public String getTokenUrl() {
        return tokenUrl;
    }

    @Override
    public OAuth2Configurator setTokenUrl(String url) {
        this.tokenUrl = url;
        return this;
    }

	@Override
    public String getTokenInfoUrl() {
        return tokenInfoUrl;
    }

    @ConfigProperty
	public OAuth2Configurator setTokenInfoUrl(String url) {
	    this.tokenInfoUrl = url;
	    return this;
	}

    @Override
    public String getUserInfoUrl() {
        return userInfoUrl;
    }

    @Override
    public OAuth2Configurator setUserInfoUrl(String url) {
        this.userInfoUrl = url;
        return this;
    }

    @Override
    public String getPublicKeyUrl() {
        return publicKeyUrl;
    }

    @Override
    public OAuth2Configurator setPublicKeyUrl(String url) {
        this.publicKeyUrl = url;
        return this;
    }

    @Override
    public String getLogoutUrl() {
        return logoutUrl;
    }

    @ConfigProperty
    public void setLogoutUrl(String logoutUrl) {
        this.logoutUrl = logoutUrl;
    }

    @Override
	public String getClientId() {
		return clientId;
	}

	@Override
	public String getClientSecret() {
		return clientSecret;
	}

	@Override
	public OAuth2Configurator setClientId(String clientId) {
		this.clientId = clientId;
		return this;
	}

	@ConfigProperty
	public OAuth2Configurator setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
		return this;
	}

    @Override
    public String getRedirectUri() {
        return redirectUri;
    }

    @ConfigProperty
    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }

    @Override
    public JwtVerifier getJwtVerifier() {
        return jwtVerifier;
    }

    @ConfigProperty
    public OAuth2Configurator setRsaPublicKeyStr(String publicKey) {
        this.rsaPublicKeyStr = publicKey;
        return this;
    }

    @Override
    public void postCreate(BeanFactory factory) throws Throwable {
        this.cachedInterceptUrls = cm.createSimpleLRUCache(1024);
    }

    @Override
    public void postAppInit(App app) throws Throwable {
        if(enabled) {

            if(!sc.config().isEnabled()) {
                sc.enable(true);
            }


        }
    }
}
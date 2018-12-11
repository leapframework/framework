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

import leap.core.annotation.ConfigProperty;
import leap.core.annotation.Configurable;
import leap.core.annotation.Inject;
import leap.core.web.RequestIgnore;
import leap.lang.Strings;
import leap.lang.net.Urls;
import leap.lang.path.AntPathPattern;
import leap.lang.path.Paths;
import leap.oauth2.webapp.token.at.AccessToken;
import leap.oauth2.webapp.user.UserDetailsLookup;
import leap.web.App;
import leap.web.AppInitializable;
import leap.web.AppListener;
import leap.web.ServerInfo;
import leap.web.security.SecurityConfigurator;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Configurable(prefix="oauth2")
public class DefaultOAuth2Config implements OAuth2Config, OAuth2Configurator, AppInitializable, AppListener {

	protected @Inject SecurityConfigurator sc;

    protected boolean enabled;
    protected boolean login;
    protected boolean logout;
    protected boolean loginWithAccessToken;
    protected boolean forceLookupUserInfo;
    protected String  authorizeUrl;
    protected String  tokenUrl;
    protected String  indirectTokenUrl;
    protected String  tokenInfoUrl;
    protected String  userInfoUrl;
    protected String  publicKeyUrl;
    protected String  logoutUrl;
    protected String  clientId;
    protected String  clientSecret;
    protected String  redirectUri;
    protected String  errorView;
    protected String  logoutView;

    private List<RequestIgnore> ignoresList = new ArrayList<>();
    private RequestIgnore[] ignoresArray = new RequestIgnore[] {};

    protected @Inject UserDetailsLookup userDetailsLookup;

	@Override
	public OAuth2Config config() {
		return this;
	}

    @Override
    public boolean isEnabled() {
	    return enabled;
    }

    @Override
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
    public boolean isLoginWithAccessToken() {
        return loginWithAccessToken;
    }

    @ConfigProperty
    public void setLoginWithAccessToken(boolean loginWithAccessToken) {
        this.loginWithAccessToken = loginWithAccessToken;
    }

    @Override
    public boolean isForceLookupUserInfo() {
        return forceLookupUserInfo;
    }

    @ConfigProperty
    public void setForceLookupUserInfo(boolean forceLookupUserInfo) {
        this.forceLookupUserInfo = forceLookupUserInfo;
    }

    @Override
    @ConfigProperty
    public OAuth2Configurator setServerUrl(String serverUrl) {//don't change the parameter name (used by config property)
        serverUrl = Paths.suffixWithoutSlash(serverUrl);

        if(null == this.authorizeUrl)    this.authorizeUrl = serverUrl + "/oauth2/authorize";
        if(null == this.tokenUrl)        this.tokenUrl     = serverUrl + "/oauth2/token";
        if(null == this.tokenInfoUrl)    this.tokenInfoUrl = serverUrl + "/oauth2/tokeninfo";
        if(null == this.userInfoUrl)     this.userInfoUrl  = serverUrl + "/oauth2/userinfo";
        if(null == this.publicKeyUrl)    this.publicKeyUrl = serverUrl + "/oauth2/publickey";
        if(null == this.logoutUrl)       this.logoutUrl    = serverUrl + "/oauth2/logout";

        return this;
    }

    @ConfigProperty
    public void setIndirectServerUrl(String indirectServerUrl) {//don't change the parameter name (used by config property)
        indirectServerUrl = Paths.suffixWithoutSlash(indirectServerUrl);
        this.authorizeUrl     = indirectServerUrl + "/oauth2/authorize";
        this.logoutUrl        = indirectServerUrl + "/oauth2/logout";
        this.indirectTokenUrl = indirectServerUrl + "/oauth2/token";
    }

    @ConfigProperty
    public void setDirectServerUrl(String directServerUrl) {//don't change the parameter name (used by config property)
        directServerUrl = Paths.suffixWithoutSlash(directServerUrl);

        this.tokenUrl     = directServerUrl + "/oauth2/token";
        this.tokenInfoUrl = directServerUrl + "/oauth2/tokeninfo";
        this.userInfoUrl  = directServerUrl + "/oauth2/userinfo";
        this.publicKeyUrl = directServerUrl + "/oauth2/publickey";
    }

    @Override
    public void onServerInfoResolved(App app, ServerInfo serverInfo) {
        this.authorizeUrl = parseUrl(this.authorizeUrl,serverInfo);
        this.tokenUrl = parseUrl(this.tokenUrl,serverInfo);
        this.tokenInfoUrl = parseUrl(this.tokenInfoUrl,serverInfo);
        this.userInfoUrl = parseUrl(this.userInfoUrl,serverInfo);
        this.publicKeyUrl = parseUrl(this.publicKeyUrl,serverInfo);
        this.logoutUrl = parseUrl(this.logoutUrl,serverInfo);
    }
    
    protected boolean needParse(String url){
	    if(Strings.isEmpty(url)){
	        // if user never set the url, don't process.
	        return false;
        }
        boolean startWithScheme = Strings.startsWith(url,"http://")||Strings.startsWith(url,"https://");
	    return !startWithScheme;
    }
    
    protected String parseUrl(String url,ServerInfo serverInfo){
        if(!needParse(url)){
            return url;
        }
        URI uri = URI.create(Strings.replace(serverInfo.getServerUrl(), " ", "%20"));
        String resolvedUrl = Urls.resolveUrlExpr(url, uri);
        if(needParse(resolvedUrl)){
            return serverInfo.getServerUrl()+Paths.prefixWithSlash(resolvedUrl);
        }
        return resolvedUrl;
        
    }
    
    @Override
    public String getAuthorizeUrl() {
        return authorizeUrl;
    }

    @Override
    @ConfigProperty
    public OAuth2Configurator setAuthorizeUrl(String url) {
        this.authorizeUrl = url;
        return this;
    }

    @Override
    public String getTokenUrl() {
        return tokenUrl;
    }

    @Override
    public String getIndirectTokenUrl() {
        return indirectTokenUrl;
    }

    @Override
    @ConfigProperty
    public OAuth2Configurator setIndirectTokenUrl(String indirectTokenUrl) {
        this.indirectTokenUrl = indirectTokenUrl;
        return this;
    }

    @Override
    @ConfigProperty
    public OAuth2Configurator setTokenUrl(String url) {
        this.tokenUrl = url;
        return this;
    }

	@Override
    public String getTokenInfoUrl() {
        return tokenInfoUrl;
    }

    @Override
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
    @ConfigProperty
    public OAuth2Configurator setUserInfoUrl(String url) {
        this.userInfoUrl = url;
        return this;
    }

    @Override
    public String getPublicKeyUrl() {
        return publicKeyUrl;
    }

    @Override
    @ConfigProperty
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
    public OAuth2Configurator ignorePath(String path) {
        AntPathPattern pattern = new AntPathPattern(path);
        ignoresList.add((req) -> pattern.matches(req.getPath()));
        ignoresArray = ignoresList.toArray(new RequestIgnore[ignoresList.size()]);
        return this;
    }

    @Override
    public RequestIgnore[] getIgnores() {
        return ignoresArray;
    }

    @Override
    @ConfigProperty
	public OAuth2Configurator setClientId(String clientId) {
		this.clientId = clientId;
		return this;
	}

    @Override
    public String getClientSecret() {
        return clientSecret;
    }

    @Override
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
    public String getErrorView() {
        return errorView;
    }

    @ConfigProperty
    public void setErrorView(String errorView) {
        this.errorView = errorView;
    }

    @Override
    public String getLogoutView() {
        return logoutView;
    }

    @ConfigProperty
    public void setLogoutView(String logoutView) {
        this.logoutView = logoutView;
    }

    @Override
    public void postAppInit(App app) throws Throwable {
        if(enabled) {
            if(!sc.config().isEnabled()) {
                sc.enable(true);
            }

            //Auto enable login access token if user details lookup exists.
            if(null != userDetailsLookup) {
                loginWithAccessToken = true;
            }
        }
    }
}
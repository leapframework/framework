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
package leap.oauth2.as.client;

import leap.lang.Strings;
import leap.lang.net.Urls;
import leap.lang.path.PathPattern;

public class SimpleAuthzClient implements AuthzClient {
    
    protected String      id;
    protected String      secret;
    protected String      redirectUri;
    protected PathPattern redirectUriPattern;
    protected String      logoutUri;
    protected PathPattern logoutUriPattern;
    protected Integer     accessTokenExpires;
    protected Integer     refreshTokenExpires;
    protected Boolean     allowAuthorizationCode;
    protected Boolean     allowRefreshToken;
    protected Boolean     allowLoginToken;
    protected String      grantedScope;
    protected Boolean     enabled;
    protected Boolean     authenticated = false;
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }
    
    public PathPattern getRedirectUriPattern() {
        return redirectUriPattern;
    }

    public void setRedirectUriPattern(PathPattern redirectUriPattern) {
        this.redirectUriPattern = redirectUriPattern;
    }

    public String getLogoutUri() {
        return logoutUri;
    }

    public void setLogoutUri(String logoutUri) {
        this.logoutUri = logoutUri;
    }

    public PathPattern getLogoutUriPattern() {
        return logoutUriPattern;
    }

    public void setLogoutUriPattern(PathPattern logoutUriPattern) {
        this.logoutUriPattern = logoutUriPattern;
    }

    public Integer getAccessTokenExpires() {
        return accessTokenExpires;
    }

    public void setAccessTokenExpires(Integer acessTokenExpires) {
        this.accessTokenExpires = acessTokenExpires;
    }

    public Integer getRefreshTokenExpires() {
        return refreshTokenExpires;
    }

    public void setRefreshTokenExpires(Integer refreshTokenExpires) {
        this.refreshTokenExpires = refreshTokenExpires;
    }
    
    public Boolean getAllowAuthorizationCode() {
        return allowAuthorizationCode;
    }

    public void setAllowAuthorizationCode(Boolean allowAuthorizationCode) {
        this.allowAuthorizationCode = allowAuthorizationCode;
    }

    public Boolean getAllowRefreshToken() {
        return allowRefreshToken;
    }

    public void setAllowRefreshToken(Boolean allowRefreshToken) {
        this.allowRefreshToken = allowRefreshToken;
    }

    public Boolean getAllowLoginToken() {
        return allowLoginToken;
    }

    public void setAllowLoginToken(Boolean allowLoginToken) {
        this.allowLoginToken = allowLoginToken;
    }

    @Override
    public String getGrantedScope() {
        return grantedScope;
    }

    public void setGrantedScope(String grantedScope) {
        this.grantedScope = grantedScope;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isAllowAuthorizationCode() {
        return null == allowAuthorizationCode || allowAuthorizationCode;
    }

    public boolean isAllowRefreshToken() {
        return null == allowRefreshToken || allowRefreshToken;
    }

    public boolean isAllowLoginToken() {
        return null == allowLoginToken || allowLoginToken;
    }

    public boolean isEnabled() {
        return null == enabled || enabled;
    }
    
    @Override
    public boolean acceptsRedirectUri(String uri) {
        if(null == uri) {
            return false;
        }
        
        if(null != redirectUri && uri.startsWith(redirectUri)) {
            return true;
        }
        
        if(null != redirectUriPattern) {

            //todo: hack ant path matcher
            String pattern = redirectUriPattern.pattern();
            if(pattern.equals("*") || pattern.equals("**")) {
                return true;
            }

            return redirectUriPattern.matches(Urls.removeQueryString(uri));
        }
        
        return false;
    }

    @Override
    public boolean acceptsLogoutUri(String uri) {
        if(null == uri) {
            return false;
        }

        if(null != logoutUri && uri.startsWith(logoutUri)) {
            return true;
        }

        if(null != logoutUriPattern) {
            return logoutUriPattern.matches(Urls.removeQueryString(uri));
        }

        return false;
    }

    @Override
    public boolean acceptsSecret(String secret) {
        return Strings.equals(secret,this.secret);
    }

    @Override
    public boolean isAuthenticated() {
        return authenticated;
    }

    public Boolean getAuthenticated() {
        return authenticated;
    }

    @Override
    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }
}
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
package server.models;

import leap.orm.annotation.AutoGenerateColumns;
import leap.orm.annotation.Column;
import leap.orm.annotation.Id;
import leap.orm.annotation.Table;
import leap.orm.model.Model;

@Table("oauth2_client")
@AutoGenerateColumns(false)
public class Client extends Model {
    
    @Id
    @Column(length=50)
    protected String id;

    @Column(length=500)
    protected String secret;

    @Column(name="redirect_uri", length=1000)
    protected String redirectUri;
    
    @Column(name="redirect_uri_pattern", length=300)
    protected String redirectUriPattern;

    @Column(name="logout_uri", length = 1000)
    protected String logoutUri;

    @Column(name="logout_uri_pattern", length=300)
    protected String logoutUriPattern;
    
    @Column(name="at_expires")
    protected Integer accessTokenExpires;
    
    @Column(name="rt_expires")
    protected Integer refreshTokenExpires;
    
    @Column(name="allow_authz_code")
    protected Boolean allowAuthorizationCode;
    
    @Column(name="allow_refresh_token")
    protected Boolean allowRefreshToken;
    
    @Column
    protected boolean enabled = true;

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

    public String getRedirectUriPattern() {
        return redirectUriPattern;
    }

    public void setRedirectUriPattern(String redirectUriPattern) {
        this.redirectUriPattern = redirectUriPattern;
    }

    public String getLogoutUri() {
        return logoutUri;
    }

    public void setLogoutUri(String logoutUri) {
        this.logoutUri = logoutUri;
    }

    public String getLogoutUriPattern() {
        return logoutUriPattern;
    }

    public void setLogoutUriPattern(String logoutUriPattern) {
        this.logoutUriPattern = logoutUriPattern;
    }

    public Integer getAccessTokenExpires() {
        return accessTokenExpires;
    }

    public void setAccessTokenExpires(Integer accessTokenExpires) {
        this.accessTokenExpires = accessTokenExpires;
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

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

}

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
package leap.oauth2.server.client;

import leap.lang.Buildable;
import leap.lang.path.AntPathPattern;
import leap.lang.path.PathPattern;

public class OAuth2ClientBuilder implements Buildable<OAuth2Client> {
    
    protected String      id;
    protected String      secret;
    protected String      redirectUri;
    protected PathPattern redirectUriPattern;
    protected String      logoutUri;
    protected PathPattern logoutUriPattern;
    
    public OAuth2ClientBuilder(String id, String secret) {
        this.id = id;
        this.secret = secret;
    }
    
    public String getId() {
        return id;
    }

    public OAuth2ClientBuilder setId(String id) {
        this.id = id;
        return this;
    }

    public String getSecret() {
        return secret;
    }

    public OAuth2ClientBuilder setSecret(String secret) {
        this.secret = secret;
        return this;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public OAuth2ClientBuilder setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
        return this;
    }

    public PathPattern getRedirectUriPattern() {
        return redirectUriPattern;
    }

    public OAuth2ClientBuilder setRedirectUriPattern(PathPattern redirectUriPattern) {
        this.redirectUriPattern = redirectUriPattern;
        return this;
    }

    public OAuth2ClientBuilder setRedirectUriPattern(String antPathPattern) {
        this.redirectUriPattern = new AntPathPattern(antPathPattern);
        return this;
    }

    public String getLogoutUri() {
        return logoutUri;
    }

    public OAuth2ClientBuilder setLogoutUri(String logoutUri) {
        this.logoutUri = logoutUri;
        return this;
    }

    public PathPattern getLogoutUriPattern() {
        return logoutUriPattern;
    }

    public OAuth2ClientBuilder setLogoutUriPattern(PathPattern logoutUriPattern) {
        this.logoutUriPattern = logoutUriPattern;
        return this;
    }

    @Override
    public OAuth2Client build() {
        SimpleOAuth2Client client = new SimpleOAuth2Client();
        
        client.setId(id);
        client.setSecret(secret);
        client.setRedirectUri(redirectUri);
        client.setRedirectUriPattern(redirectUriPattern);
        client.setLogoutUri(logoutUri);
        client.setLogoutUriPattern(logoutUriPattern);
        
        return client;
    }
    
}

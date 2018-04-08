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

package leap.oauth2.webapp.login;

import leap.core.security.Authentication;
import leap.core.security.Credentials;
import leap.core.security.UserPrincipal;
import leap.oauth2.webapp.token.at.AccessToken;
import leap.web.security.authc.SimpleAuthentication;

public class OAuth2LoginAuthentication extends SimpleAuthentication implements Authentication {

    protected AccessToken accessToken;

    public OAuth2LoginAuthentication(UserPrincipal user, Credentials credentials) {
        super(user, credentials);
    }

    public AccessToken getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(AccessToken accessToken) {
        this.accessToken = accessToken;
    }
}
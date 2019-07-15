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
package leap.oauth2.webapp.authc;

import leap.core.security.Authentication;
import leap.core.security.ClientPrincipal;
import leap.core.security.UserPrincipal;
import leap.oauth2.webapp.token.Token;
import leap.web.security.authc.AbstractAuthentication;

public class SimpleOAuth2Authentication extends AbstractAuthentication implements Authentication, OAuth2Authentication {

    protected final Token           credentials;
    protected final UserPrincipal   user;
    protected final ClientPrincipal client;

    public SimpleOAuth2Authentication(Token credentials, UserPrincipal user, ClientPrincipal client) {
        this.credentials = credentials;
        this.user = user;
        this.client = client;
    }

    @Override
    public Token getCredentials() {
        return credentials;
    }

    @Override
    public UserPrincipal getUser() {
        return user;
    }

    @Override
    public ClientPrincipal getClient() {
        return client;
    }

    @Override
    public OAuth2Authentication newAuthentication() {
        return new SimpleOAuth2Authentication(credentials, user, client);
    }
}
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
package leap.oauth2.rs.auth;

import leap.core.security.ClientPrincipal;
import leap.core.security.UserPrincipal;
import leap.oauth2.rs.token.AccessToken;
import leap.web.security.authc.AbstractAuthentication;
import leap.web.security.authc.Authentication;

public class SimpleOAuth2Authentication extends AbstractAuthentication implements Authentication, OAuth2Authentication {
	
    protected final AccessToken     credentials;
    protected final UserPrincipal   user;
    protected final ClientPrincipal client;

	public SimpleOAuth2Authentication(AccessToken credentials, UserPrincipal user, ClientPrincipal client) {
        this.credentials = credentials;
        this.user   = user;
        this.client = client;
	}

	@Override
	public boolean isAuthenticated() {
		return true;
	}

	@Override
	public boolean isRememberMe() {
		return false;
	}

    @Override
	public AccessToken getCredentials() {
		return credentials;
	}

	@Override
	public UserPrincipal getUserPrincipal() {
		return user;
	}

	@Override
    public ClientPrincipal getClientPrincipal() {
	    return client;
    }

}
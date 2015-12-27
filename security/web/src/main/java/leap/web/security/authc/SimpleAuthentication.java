/*
 * Copyright 2013 the original author or authors.
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
package leap.web.security.authc;

import leap.core.security.ClientPrincipal;
import leap.core.security.Credentials;
import leap.core.security.UserPrincipal;

public class SimpleAuthentication extends AbstractAuthentication implements Authentication {
	
	protected Credentials     credentials;
	protected UserPrincipal   userPrincipal;
	protected ClientPrincipal clientPrincipal;
	
	public SimpleAuthentication(UserPrincipal user) {
		this.userPrincipal = user;
	}
	
	public SimpleAuthentication(UserPrincipal user, Credentials credentials) {
		this.userPrincipal = user;
		this.credentials   = credentials;
	}
	
	@Override
    public boolean isAuthenticated() {
	    return null != userPrincipal && userPrincipal.isAuthenticated();
    }
	
	@Override
    public boolean isRememberMe() {
	    return null != userPrincipal && userPrincipal.isRememberMe();
    }

	@Override
	public Credentials getCredentials() {
		return credentials;
	}

	public void setCredentials(Credentials credentials) {
		this.credentials = credentials;
	}

	public void setUserPrincipal(UserPrincipal principal) {
		this.userPrincipal = principal;
	}

	@Override
	public UserPrincipal getUserPrincipal() {
		return userPrincipal;
	}

	public ClientPrincipal getClientPrincipal() {
		return clientPrincipal;
	}

	public void setClientPrincipal(ClientPrincipal clientPrincipal) {
		this.clientPrincipal = clientPrincipal;
	}
	
}
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

import leap.core.security.UserPrincipal;
import leap.oauth2.rs.ResourceOwner;

public class OAuth2UserPrincipal implements UserPrincipal {

	private static final long serialVersionUID = -3280681234961513540L;
	
	protected final ResourceOwner user;
	
	public OAuth2UserPrincipal(ResourceOwner user) {
		this.user = user; 
	}

	@Override
	public Object getId() {
		return user.getId();
	}

	@Override
	public boolean isAnonymous() {
		return false;
	}

	@Override
	public boolean isRememberMe() {
		return false;
	}

	@Override
	public boolean isAuthenticated() {
		return true;
	}

	@Override
	public String getName() {
		return user.getName();
	}

	@Override
	public String getLoginName() {
		return user.getLoginName();
	}

    @Override
    @SuppressWarnings("unchecked")
	public <T> T getDetails() {
		return (T)user.getDetails();
	}

}
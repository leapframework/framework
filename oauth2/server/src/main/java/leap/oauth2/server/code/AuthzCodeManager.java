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
package leap.oauth2.server.code;

import leap.oauth2.server.authc.AuthzAuthentication;
import leap.oauth2.server.sso.AuthzSSOSession;

public interface AuthzCodeManager {
    
    /**
     * Creates a new authorization code.
     */
    AuthzCode createAuthorizationCode(AuthzAuthentication authc, AuthzSSOSession session);
	
	/**
	 * Returns the consumed code or <code>null</code>.
	 * 
	 * <p>
	 * The code will be removed, cannot conume it again.
	 */
	AuthzCode consumeAuthorizationCode(String code);

	/**
	 * Removes the authorizaiton code if exists.
	 */
	void removeAuthorizationCode(AuthzCode code);
}

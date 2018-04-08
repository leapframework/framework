/*
 * Copyright 2014 the original author or authors.
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
package leap.web.security.authz;

import leap.core.security.Authentication;
import leap.core.security.Authorization;
import leap.core.security.SecurityContext;
import leap.web.security.SecurityConfig;
import leap.web.security.permission.PermissionManager;

public interface AuthorizationContext {

	/**
	 * Returns {@link SecurityConfig}.
	 */
	SecurityConfig getSecurityConfig();

	/**
	 * Returns {@link SecurityContext}
	 */
	SecurityContext getSecurityContext();

	/**
	 * Returns {@link PermissionManager}
     */
	PermissionManager getPermissionManager();

	/**
	 * Required. Returns current {@link Authentication}
	 */
	Authentication getAuthentication();

    /**
     * Returns the {@link Authorization}.
     */
	Authorization getAuthorization();

    /**
     * Sets the {@link Authorization}.
     */
    void setAuthorization(Authorization authz);

}
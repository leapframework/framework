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
package leap.oauth2.as.authc;

import leap.oauth2.OAuth2Params;
import leap.oauth2.as.client.AuthzClient;
import leap.web.security.authc.Authentication;
import leap.web.security.user.UserDetails;

/**
 * An authentication of oauth2 authorization server.
 */
public interface AuthzAuthentication {

    /**
     * Optional. Returns the authenticated client's details object.
     */
    AuthzClient getClientDetails();

    /**
     * Optional. Returns the authenticated user's details object.
     */
    UserDetails getUserDetails();

    /**
     * Required. Returns the params created this authentication.
     */
    OAuth2Params getParams();

    /**
     * Optional. Returns the user's authentication if the user was authenticated by normal login flow.
     */
    Authentication getAuthentication();

    /**
     * Optional. Returns the oauth2 scope.
     */
    default String getScope() {
        return getParams().getScope();
    }

    /**
     * Returns the redirect uri.
     */
    default String getRedirectUri() {
        return getParams().getRedirectUri();
    }

    /**
     * Returns <code>true</code> if no authenticated user.
     */
    default boolean isClientOnly() {
        return null != getClientDetails() && null == getUserDetails();
    }

    /**
     * Returns <code>true</code> if no authenticated client.
     */
    default boolean isUserOnly() {
        return null != getUserDetails() && null == getClientDetails();
    }
    
}
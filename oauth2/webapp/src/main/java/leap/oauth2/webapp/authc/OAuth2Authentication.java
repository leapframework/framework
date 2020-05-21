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
import leap.oauth2.webapp.token.Token;
import leap.oauth2.webapp.token.TokenInfo;

public interface OAuth2Authentication extends Authentication {

    /**
     * The credentials must be the type of {@link Token}.
     */
    Token getCredentials();

    /**
     * Returns the {@link TokenInfo} of the access token.
     */
    TokenInfo getTokenInfo();

    /**
     * Returns the granted scopes.
     */
    default String[] getGrantedScope() {
        return null;
    }

    /**
     * Returns a new {@link OAuth2Authentication}.
     */
    OAuth2Authentication newAuthentication();
}
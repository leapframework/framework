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

package leap.oauth2.webapp.token.id;

import leap.core.security.ClientPrincipal;
import leap.core.security.UserPrincipal;
import leap.core.security.token.TokenCredentials;

/**
 * The id token redirect back from oauth2 server.
 *
 * <p/>
 * Used at web app login.
 */
public interface IdToken extends TokenCredentials {

    String getClientId();

    String getUserId();

    /**
     * Optional.
     */
    default UserPrincipal getUserInfo() {
        return null;
    }

    /**
     * Optional.
     */
    default ClientPrincipal getClientInfo() {
        return null;
    }
}
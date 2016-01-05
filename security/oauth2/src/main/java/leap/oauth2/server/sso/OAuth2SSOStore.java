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
package leap.oauth2.server.sso;

import java.util.List;

public interface OAuth2SSOStore {

    /**
     * Optional. Returns the exists sso session by searching the user's id and token.
     *
     * @param username the login name of user.
     * @param token the authentication token.
     */
    OAuth2SSOSession loadSessionByToken(String username, String token);

    /**
     * Loads all the login(s) in the sso session.
     *
     * <p/>
     * Returns an empty list if no login(s) has been found.
     */
    List<OAuth2SSOLogin> loadLoginsInSession(OAuth2SSOSession session);

    /**
     * Saves the sso session and the initial sso login.
     */
    void saveSession(OAuth2SSOSession session, OAuth2SSOLogin initialLogin);

    /**
     * Saves the new sso login in the given sso session.
     */
    void saveLogin(OAuth2SSOSession session, OAuth2SSOLogin newlogin);

    /**
     * Cleanup expired data.
     */
    void cleanupSSO();

}
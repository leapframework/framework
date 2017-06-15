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
package leap.oauth2.as.sso;

/**
 * Indicates an user login in a {@link AuthzSSOSession}.
 */
public interface AuthzSSOLogin {

    /**
     * Required. Returns the time user logged in.
     */
    long getLoginTime();

    /**
     * Optional. Returns the post logout uri of relying party application.
     *
     * <p/>
     * When user logged out from sso server, the server will notify the user-agent request all the logout uri(s) in current session.
     */
    String getLogoutUri();

    /**
     * Optional. Returns the <code>client_id</code> of relying party application.
     */
    String getClientId();

    /**
     * Returns <code>true</code> if the login is the initial login of a session.
     */
    boolean isInitial();

}
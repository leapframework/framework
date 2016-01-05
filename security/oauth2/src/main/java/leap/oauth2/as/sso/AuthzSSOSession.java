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

import leap.lang.expirable.TimeExpirable;
import leap.web.security.authc.Authentication;

/**
 * Indicates a single-sign-on session of an user.
 */
public interface AuthzSSOSession extends TimeExpirable {

    /**
     * Required. Returns the id of sso session.
     */
    String getId();

    /**
     * Required. The id of authenticated user who creates the session.
     */
    String getUserId();

    /**
     * Required. The login name of authenticated user who creates the session.
     */
    String getUsername();

    /**
     * Required. The token associates with the session.
     *
     * @see {@link Authentication#getToken()}
     */
    String getToken();

}
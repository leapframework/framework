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

package leap.oauth2.webapp.login;

import leap.lang.intercepting.State;
import leap.oauth2.webapp.OAuth2Config;
import leap.web.Request;
import leap.web.Response;
import leap.web.security.authc.AuthenticationContext;

public interface OAuth2LoginHandler {

    /**
     * Handles the redirect back request from oauth2 server.
     */
    State handleServerRedirectRequest(Request request, Response response, AuthenticationContext context) throws Throwable;

    State handleAuthenticationResolved(Request request, Response response, AuthenticationContext context) throws Throwable;
}

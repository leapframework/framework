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
package leap.oauth2.server;

import leap.web.Response;

public interface OAuth2ServerErrorHandler {

    void response(Response response, int status, String error, String desc);

    void invalidRequest(Response response, String desc);

    void unsupportedGrantType(Response response, String desc);

    /**
     * Client authentication failed
     */
    void invalidClient(Response response, String desc);

    /**
     * The provided authorization grant (e.g., authorization
     * code, resource owner credentials) or refresh token is
     * invalid, expired, revoked, does not match the redirection
     * URI used in the authorizat
     */
    void invalidGrant(Response response, String desc);

    /**
     * The authenticated client is not authorized to use this authorization grant type.
     */
    void unauthorizedClient(Response response, String desc);

    /**
     * Handles internal server error.
     */
    void serverError(Response response, String desc);
    
    /**
     * Handles internal server erro4. 
     */
    void serverError(Response response, String desc, Throwable e);

}
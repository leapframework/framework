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
package leap.oauth2.as;

import static leap.oauth2.OAuth2Errors.ERROR_INVALID_CLIENT;
import static leap.oauth2.OAuth2Errors.ERROR_INVALID_GRANT;
import static leap.oauth2.OAuth2Errors.ERROR_INVALID_REQUEST;
import static leap.oauth2.OAuth2Errors.ERROR_SERVER_ERROR;
import static leap.oauth2.OAuth2Errors.ERROR_UNAUTHORIZED_CLIENT;
import static leap.oauth2.OAuth2Errors.ERROR_UNSUPPORTED_GRANT_TYPE;
import leap.lang.http.HTTP;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.oauth2.OAuth2Errors;
import leap.web.Response;

public class DefaultOAuth2AuthzServerErrorHandler implements OAuth2AuthzServerErrorHandler {
    
    private static final Log log = LogFactory.get(DefaultOAuth2AuthzServerErrorHandler.class);

    @Override
    public void response(Response response, int status, String error, String desc){
        OAuth2Errors.response(response, status, error, desc);
    }
    
    @Override
    public void invalidRequest(Response response, String desc) {
        response(response, HTTP.SC_BAD_REQUEST, ERROR_INVALID_REQUEST, desc);
    }
    
    @Override
    public void unsupportedGrantType(Response response, String desc) {
        response(response, HTTP.SC_BAD_REQUEST, ERROR_UNSUPPORTED_GRANT_TYPE, desc);
    }
    
    /**
     * Client authentication failed
     */
    @Override
    public void invalidClient(Response response, String desc) {
        response(response, HTTP.SC_UNAUTHORIZED, ERROR_INVALID_CLIENT, desc);
    }
    
    /**
     * The provided authorization grant (e.g., authorization
     * code, resource owner credentials) or refresh token is
     * invalid, expired, revoked, does not match the redirection
     * URI used in the authorizat
     */
    @Override
    public void invalidGrant(Response response, String desc) {
        response(response, HTTP.SC_BAD_REQUEST, ERROR_INVALID_GRANT, desc);
    }
    
    /**
     * The authenticated client is not authorized to use this authorization grant type.
     */
    @Override
    public void unauthorizedClient(Response response, String desc) {
        response(response, HTTP.SC_BAD_REQUEST, ERROR_UNAUTHORIZED_CLIENT, desc);
    }   
    
    @Override
    public void serverError(Response response, String desc) {
        log.error("Auth Server Internal Error : {}", desc);
        response(response, HTTP.SC_INTERNAL_SERVER_ERROR, ERROR_SERVER_ERROR, desc);
    }

    @Override
    public void serverError(Response response, String desc, Throwable e) {
        log.error("Auth Server Internal Error : {}", desc, e);
        response(response, HTTP.SC_INTERNAL_SERVER_ERROR, ERROR_SERVER_ERROR, desc);
    }
    
    
}

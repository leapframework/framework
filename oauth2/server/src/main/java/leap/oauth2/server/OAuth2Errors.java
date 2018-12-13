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

import leap.core.i18n.MessageKey;
import leap.lang.Strings;
import leap.lang.http.ContentTypes;
import leap.lang.http.HTTP;
import leap.lang.json.JSON;
import leap.lang.json.JsonWriter;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.net.Urls;
import leap.web.Request;
import leap.web.Response;

import java.util.Locale;

public class OAuth2Errors {

    private static final Log log = LogFactory.get(OAuth2Errors.class);

    //Standards
    public static final String ERROR_INVALID_REQUEST           = "invalid_request";
    public static final String ERROR_INVALID_CLIENT            = "invalid_client";
    public static final String ERROR_UNSUPPORTED_GRANT_TYPE    = "unsupported_grant_type";
    public static final String ERROR_UNSUPPORTED_RESPONSE_TYPE = "unsupported_response_type";
    public static final String ERROR_INVALID_GRANT             = "invalid_grant";
    public static final String ERROR_UNAUTHORIZED_CLIENT       = "unauthorized_client";
    public static final String ERROR_SERVER_ERROR              = "server_error";
    public static final String ERROR_INVALID_SCOPE             = "invalid_scope";
    public static final String ERROR_ACCESS_DENIED             = "access_denied";
    public static final String ERROR_INSUFFICIENT_SCOPE        = "insufficient_scope";

    //Non Standards.
    public static final String ERROR_INVALID_TOKEN    = "invalid_token";
    public static final String ERROR_TOKEN_EXPIRED    = "token_expired";
    public static final String ERROR_INCORRECT_SECRET = "incorrect_secret";

    //i18n message key
    public static final String ERROR_INVALID_REQUEST_KEY           = "oauth2.as.invalid_request";
    public static final String ERROR_UNSUPPORTED_GRANT_TYPE_KEY    = "oauth2.as.unsupported_grant_type";
    public static final String ERROR_INVALID_CLIENT_KEY            = "oauth2.as.invalid_client";
    public static final String ERROR_INVALID_GRANT_KEY             = "oauth2.as.invalid_grant";
    public static final String ERROR_UNAUTHORIZED_CLIENT_KEY       = "oauth2.as.unauthorized_client";
    public static final String ERROR_SERVER_ERROR_KEY              = "oauth2.as.server_error";
    public static final String ERROR_INVALID_TOKEN_KEY             = "oauth2.as.invalid_token";
    public static final String ERROR_UNSUPPORTED_RESPONSE_TYPE_KEY = "oauth2.as.unsupported_response_type";
    public static final String ERROR_ACCESS_DENIED_KEY             = "oauth2.as.access_denied";
    public static final String ERROR_INVALID_SCOPE_KEY             = "oauth2.as.invalid_scope";

    public static void response(Response response, OAuth2Error error) {
        response.setStatus(error.getStatus());
        response.setContentType(ContentTypes.APPLICATION_JSON_UTF8);

        JsonWriter w = JSON.createWriter(response.getWriter());
        w.startObject()
                .property("error", error.getError())
                .propertyOptional("error_description", error.getErrorDescription())
                .endObject();
    }

    public static MessageKey messageKey(Locale locale, String key, Object... args) {
        MessageKey messageKey = new MessageKey(locale, key, args);
        return messageKey;
    }

    public static OAuth2Error oauth2Error(Request request, int status, String error, MessageKey key, String defaultDesc) {
        String desc = defaultDesc;
        if (key != null) {
            String localeDesc = request.getMessageSource().tryGetMessage(key);
            if (localeDesc != null) {
                desc = localeDesc;
            }
        }
        OAuth2Error err = new SimpleOAuth2Error(status, error, desc, key);
        return err;
    }

    public static void redirect(Response response, String uri, OAuth2Error error) {
        log.debug("redirect error '{}', desc : {}", error.getError(), error.getErrorDescription());

        StringBuilder qs = new StringBuilder();
        qs.append("error=").append(error.getError());

        if (!Strings.isEmpty(error.getErrorDescription())) {
            qs.append("&error_description=").append(Urls.encode(error.getErrorDescription()));
        }

        response.sendRedirect(Urls.appendQueryString(uri, qs.toString()));
    }

    /**
     * unsupported response type start
     **/
    public static void redirectUnsupportedResponseType(Request request, Response response, String uri, MessageKey key) {
        redirectUnsupportedResponseType(request, response, uri, key, "unsupported response type");
    }

    public static void redirectUnsupportedResponseType(Request request, Response response, String uri, MessageKey key, String defaultDesc) {
        OAuth2Error error = redirectUnsupportedResponseTypeError(request, key, defaultDesc);
        redirect(response, uri, error);
    }

    public static OAuth2Error redirectUnsupportedResponseTypeError(Request request, MessageKey key, String defaultDesc) {
        if (key == null) {
            key = messageKey(request.getLocale(), ERROR_UNSUPPORTED_RESPONSE_TYPE_KEY, null);
        }
        return oauth2Error(request, HTTP.SC_FOUND, ERROR_UNSUPPORTED_RESPONSE_TYPE, key, defaultDesc);
    }
    /** unsupported response type end **/

    /**
     * access denied start
     **/
    public static void redirectAccessDenied(Request request, Response response, String uri, MessageKey key) {
        redirectAccessDenied(request, response, uri, key, "access denied");
    }

    public static void redirectAccessDenied(Request request, Response response, String uri, MessageKey key, String defaultDesc) {
        OAuth2Error error = redirectAccessDeniedError(request, key, defaultDesc);
        redirect(response, uri, error);
    }

    public static OAuth2Error redirectAccessDeniedError(Request request, MessageKey key, String defaultDesc) {
        if (key == null) {
            key = messageKey(request.getLocale(), ERROR_ACCESS_DENIED_KEY, defaultDesc);
        }
        return oauth2Error(request, HTTP.SC_FOUND, ERROR_ACCESS_DENIED, key, defaultDesc);
    }
    /** access denied end **/

    /**
     * unauthorized client start
     **/
    public static void redirectUnauthorizedClient(Request request, Response response, String uri, MessageKey key) {
        redirectUnauthorizedClient(request, response, uri, key, "unauthorized client");
    }

    public static void redirectUnauthorizedClient(Request request, Response response, String uri, MessageKey key, String defaultDesc) {
        OAuth2Error error = redirectUnauthorizedClientError(request, key, defaultDesc);
        redirect(response, uri, error);
    }

    public static OAuth2Error redirectUnauthorizedClientError(Request request, MessageKey key, String defaultDesc) {
        if (key == null) {
            key = messageKey(request.getLocale(), ERROR_UNAUTHORIZED_CLIENT_KEY, defaultDesc);
        }
        return oauth2Error(request, HTTP.SC_FOUND, ERROR_UNAUTHORIZED_CLIENT, key, defaultDesc);
    }
    /** unauthorized client end **/

    /**
     * redirect invalid request start
     **/
    public static void redirectInvalidRequest(Request request, Response response, String uri, MessageKey key) {
        redirectInvalidRequest(request, response, uri, key, "invalid request");
    }

    public static void redirectInvalidRequest(Request request, Response response, String uri, MessageKey key, String defaultDesc) {
        OAuth2Error error = redirectInvalidRequestError(request, key, defaultDesc);
        redirect(response, uri, error);
    }

    public static OAuth2Error redirectInvalidRequestError(Request request, MessageKey key, String defaultDesc) {
        if (key == null) {
            key = messageKey(request.getLocale(), ERROR_INVALID_REQUEST_KEY, defaultDesc);
        }
        return oauth2Error(request, HTTP.SC_FOUND, ERROR_INVALID_REQUEST, key, defaultDesc);
    }
    /** redirect invalid request end **/

    /**
     * invalid scope start
     **/
    public static void redirectInvalidScope(Request request, Response response, String uri, MessageKey key) {
        redirectInvalidScope(request, response, uri, key, "invalid scope");
    }

    public static void redirectInvalidScope(Request request, Response response, String uri, MessageKey key, String defaultDesc) {
        OAuth2Error error = redirectInvalidScopeError(request, key, defaultDesc);
        redirect(response, uri, error);
    }

    public static OAuth2Error redirectInvalidScopeError(Request request, MessageKey key, String defaultDesc) {
        if (key == null) {
            key = messageKey(request.getLocale(), ERROR_INVALID_SCOPE_KEY, defaultDesc);
        }
        return oauth2Error(request, HTTP.SC_FOUND, ERROR_INVALID_SCOPE, key, defaultDesc);
    }
    /** invalid scope end **/

    /**
     * server error start
     **/
    public static void redirectServerError(Request request, Response response, String uri, MessageKey key) {
        redirectServerError(request, response, uri, key, "server error");
    }

    public static void redirectServerError(Request request, Response response, String uri, MessageKey key, String defaultDesc) {
        OAuth2Error error = redirectServerErrorError(request, key, defaultDesc);
        redirect(response, uri, error);
    }

    public static OAuth2Error redirectServerErrorError(Request request, MessageKey key, String defaultDesc) {
        if (key == null) {
            key = messageKey(request.getLocale(), ERROR_SERVER_ERROR_KEY, defaultDesc);
        }
        return oauth2Error(request, HTTP.SC_FOUND, ERROR_SERVER_ERROR, key, defaultDesc);
    }
    /** server error end **/

    /**
     * invalid request start
     **/
    public static void invalidRequest(Request request, Response response, MessageKey key) {
        invalidRequest(request, response, key, "invalid request");
    }

    public static void invalidRequest(Request request, Response response, MessageKey key, String defaultDesc) {
        OAuth2Error error = invalidRequestError(request, key, defaultDesc);
        response(response, error);
    }

    public static OAuth2Error invalidRequestError(Request request, MessageKey key, String defaultValue) {
        if (key == null) {
            key = messageKey(request.getLocale(), ERROR_INVALID_REQUEST_KEY, null);
        }
        return oauth2Error(request, HTTP.SC_BAD_REQUEST, ERROR_INVALID_REQUEST, key, defaultValue);
    }
    /** invalid request end **/

    /**
     * unsupported grant type start
     **/
    public static void unsupportedGrantType(Request request, Response response, MessageKey key) {
        unsupportedGrantType(request, response, key, "unsupported grant type");
    }

    public static void unsupportedGrantType(Request request, Response response, MessageKey key, String defaultDesc) {
        OAuth2Error error = unsupportedGrantTypeError(request, key, defaultDesc);
        response(response, error);
    }

    public static OAuth2Error unsupportedGrantTypeError(Request request, MessageKey key, String defaultDesc) {
        if (key == null) {
            key = messageKey(request.getLocale(), ERROR_UNSUPPORTED_GRANT_TYPE_KEY, null);
        }
        return oauth2Error(request, HTTP.SC_BAD_REQUEST, ERROR_UNSUPPORTED_GRANT_TYPE, key, defaultDesc);
    }
    /** unsupported grant type end **/

    /**
     * invalid client start
     */
    public static void invalidClient(Request request, Response response, MessageKey key) {
        invalidClient(request, response, key, "invalid client");
    }

    public static void invalidClient(Request request, Response response, MessageKey key, String defaultDesc) {
        OAuth2Error error = invalidClientError(request, key, defaultDesc);
        response(response, error);
    }

    public static OAuth2Error invalidClientError(Request request, MessageKey key, String defaultDesc) {
        if (key == null) {
            key = messageKey(request.getLocale(), ERROR_INVALID_CLIENT_KEY, null);
        }
        return oauth2Error(request, HTTP.SC_UNAUTHORIZED, ERROR_INVALID_CLIENT, key, defaultDesc);
    }
    /** invalid client end */

    /** invalid grant start **/
    /**
     * The provided authorization grant (e.g., authorization
     * code, resource owner credentials) or refresh token is
     * invalid, expired, revoked, does not match the redirection
     * URI used in the authorizat
     */
    public static void invalidGrant(Request request, Response response, MessageKey key) {
        invalidGrant(request, response, key, "invalid grant");
    }

    public static void invalidGrant(Request request, Response response, MessageKey key, String defaultDesc) {
        OAuth2Error error = invalidGrantError(request, key, defaultDesc);
        response(response, error);
    }

    public static OAuth2Error invalidGrantError(Request request, MessageKey key, String defaultDesc) {
        if (key == null) {
            key = messageKey(request.getLocale(), ERROR_INVALID_GRANT_KEY, null);
        }
        return oauth2Error(request, HTTP.SC_UNAUTHORIZED, ERROR_INVALID_GRANT, key, defaultDesc);
    }
    /** invalid grant end **/

    /** unauthorized client start **/
    /**
     * The authenticated client is not authorized to use this authorization grant type.
     */
    public static void unauthorizedClient(Request request, Response response, MessageKey key) {
        unauthorizedClient(request, response, key, "unauthorized client");
    }

    public static void unauthorizedClient(Request request, Response response, MessageKey key, String defaultDesc) {
        OAuth2Error error = unauthorizedClientError(request, key, defaultDesc);
        response(response, error);
    }

    public static OAuth2Error unauthorizedClientError(Request request, MessageKey key, String defaultDesc) {
        if (key == null) {
            key = messageKey(request.getLocale(), ERROR_UNAUTHORIZED_CLIENT_KEY, null);
        }
        return oauth2Error(request, HTTP.SC_BAD_REQUEST, ERROR_UNAUTHORIZED_CLIENT, key, defaultDesc);
    }
    /** unauthorized client end **/

    /**
     * server error start
     **/
    public static void serverError(Request request, Response response, MessageKey key) {
        serverError(request, response, key, "server error");
    }

    public static void serverError(Request request, Response response, MessageKey key, String defaultDesc) {
        OAuth2Error error = serverErrorError(request, key, defaultDesc);
        response(response, error);
    }

    public static OAuth2Error serverErrorError(Request request, MessageKey key, String defaultDesc) {
        if (key == null) {
            key = messageKey(request.getLocale(), ERROR_SERVER_ERROR_KEY, null);
        }
        return oauth2Error(request, HTTP.SC_INTERNAL_SERVER_ERROR, ERROR_SERVER_ERROR, key, defaultDesc);
    }

    /**
     * server error start
     **/


    public static OAuth2ResponseException invalidClientException(String desc) {
        return new OAuth2ResponseException(HTTP.SC_BAD_REQUEST, ERROR_INVALID_CLIENT, desc);
    }

    public static OAuth2ResponseException serverErrorException(String desc) {
        return new OAuth2ResponseException(HTTP.SC_INTERNAL_SERVER_ERROR, ERROR_SERVER_ERROR, desc);
    }


    /**
     * invalid token start
     **/
    public static void invalidToken(Request request, Response response, MessageKey key) {
        invalidToken(request, response, key, "invalid token");
    }

    public static void invalidToken(Request request, Response response, MessageKey key, String defaultDesc) {
        OAuth2Error error = invalidTokenError(request, key, defaultDesc);
        response(response, error);
    }

    public static OAuth2Error invalidTokenError(Request request, MessageKey key, String defaultDesc) {
        if (key == null) {
            key = messageKey(request.getLocale(), ERROR_INVALID_TOKEN_KEY, null);
        }
        return oauth2Error(request, HTTP.SC_UNAUTHORIZED, ERROR_INVALID_TOKEN, key, defaultDesc);
    }

    /**
     * invalid token end
     **/

    protected OAuth2Errors() {

    }

}

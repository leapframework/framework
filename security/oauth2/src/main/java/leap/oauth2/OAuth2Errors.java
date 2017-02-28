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
package leap.oauth2;

import leap.lang.Strings;
import leap.lang.http.ContentTypes;
import leap.lang.http.HTTP;
import leap.lang.json.JSON;
import leap.lang.json.JsonWriter;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.net.Urls;
import leap.web.Response;

public class OAuth2Errors {
    
    private static final Log log = LogFactory.get(OAuth2Errors.class);
    
    //Standards
	public static final String ERROR_INVALID_REQUEST 		   = "invalid_request";
	public static final String ERROR_INVALID_CLIENT  		   = "invalid_client";
	public static final String ERROR_UNSUPPORTED_GRANT_TYPE    = "unsupported_grant_type";
	public static final String ERROR_UNSUPPORTED_RESPONSE_TYPE = "unsupported_response_type";
	public static final String ERROR_INVALID_GRANT		       = "invalid_grant";
	public static final String ERROR_UNAUTHORIZED_CLIENT       = "unauthorized_client";
	public static final String ERROR_SERVER_ERROR    	   	   = "server_error";
	public static final String ERROR_INVALID_SCOPE    		   = "invalid_scope";
	public static final String ERROR_ACCESS_DENIED             = "access_denied";
	public static final String ERROR_INSUFFICIENT_SCOPE        = "insufficient_scope";
	
	//Non Standards.
	public static final String ERROR_INVALID_TOKEN             = "invalid_token";
	
	public static void response(Response response, OAuth2Error error) {
		response.setStatus(error.getStatus());
		response.setContentType(ContentTypes.APPLICATION_JSON_UTF8);
		
		JsonWriter w = JSON.createWriter(response.getWriter());
		w.startObject()
		 .property("error", error.getError())
		 .propertyOptional("error_description", error.getErrorDescription())
		 .endObject();
	}
	
	public static void redirect(Response response, String uri, String error, String desc) {
	    log.debug("redirect error '{}', desc : {}", error, desc);
	    
		StringBuilder qs = new StringBuilder();
		qs.append("error=").append(error);
		
		if(!Strings.isEmpty(desc)) {
			qs.append("&error_description=").append(Urls.encode(desc));
		}
		
		response.sendRedirect(Urls.appendQueryString(uri, qs.toString()));
	}
	
	public static void redirectUnsupportedResponseType(Response response, String uri, String desc){
		redirect(response, uri, ERROR_UNSUPPORTED_RESPONSE_TYPE, desc);
	}
	
	public static void redirectAccessDenied(Response response, String uri, String desc){
		redirect(response, uri, ERROR_ACCESS_DENIED, desc);
	}
	
	public static void redirectUnauthorizedClient(Response response, String uri, String desc){
		redirect(response, uri, ERROR_UNAUTHORIZED_CLIENT, desc);
	}
	
	public static void redirectInvalidRequest(Response response, String uri, String desc) {
		redirect(response, uri, ERROR_INVALID_REQUEST, desc);
	}
	
	public static void redirectInvalidScope(Response response, String uri, String desc){
		redirect(response, uri, ERROR_INVALID_SCOPE, desc);
	}
	
	public static void redirectServerError(Response response, String uri, String desc){
		redirect(response, uri, ERROR_SERVER_ERROR, desc);
	}
	
	public static void invalidRequest(Response response, String desc) {
		OAuth2Error error = invalidRequestError(desc);
		response(response, error);
	}
	public static OAuth2Error invalidRequestError(String desc) {
		OAuth2Error error = new SimpleOAuth2Error(HTTP.SC_BAD_REQUEST,ERROR_INVALID_REQUEST,desc);
		return error;
	}
	
	public static void unsupportedGrantType(Response response, String desc) {
		OAuth2Error error = unsupportedGrantTypeError(desc);
		response(response, error);
	}
	public static OAuth2Error unsupportedGrantTypeError(String desc) {
		OAuth2Error error = new SimpleOAuth2Error(HTTP.SC_BAD_REQUEST, ERROR_UNSUPPORTED_GRANT_TYPE, desc);
		return error;
	}
	/**
	 * Client authentication failed
	 */
	public static void invalidClient(Response response, String desc) {
		OAuth2Error error = invalidClientError(desc);
		response(response, error);
	}
	public static OAuth2Error invalidClientError(String desc) {
		OAuth2Error error = new SimpleOAuth2Error(HTTP.SC_UNAUTHORIZED, ERROR_INVALID_CLIENT, desc);
		return error;
	}
	/**
	 * The provided authorization grant (e.g., authorization
     * code, resource owner credentials) or refresh token is
     * invalid, expired, revoked, does not match the redirection
     * URI used in the authorizat
	 */
	public static void invalidGrant(Response response, String desc) {
		OAuth2Error error = invalidGrantError(desc);
		response(response, error);
	}
	public static OAuth2Error invalidGrantError(String desc) {
		OAuth2Error error = new SimpleOAuth2Error(HTTP.SC_UNAUTHORIZED, ERROR_INVALID_GRANT, desc);
		return error;
	}
	/**
 	 * The authenticated client is not authorized to use this authorization grant type.
	 */
	public static void unauthorizedClient(Response response, String desc) {
		OAuth2Error error = unauthorizedClientError(desc);
		response(response, error);
	}
	public static OAuth2Error unauthorizedClientError(String desc) {
		OAuth2Error error = new SimpleOAuth2Error(HTTP.SC_BAD_REQUEST, ERROR_UNAUTHORIZED_CLIENT, desc);
		return error;
	}
	public static void serverError(Response response, String desc) {
		OAuth2Error error = serverErrorError(desc);
		response(response, error);
	}
	public static OAuth2Error serverErrorError(String desc) {
		OAuth2Error error = new SimpleOAuth2Error(HTTP.SC_INTERNAL_SERVER_ERROR, ERROR_SERVER_ERROR, desc);
		return error;
	}
	
	public static OAuth2ResponseException invalidClientException(String desc) {
		return new OAuth2ResponseException(HTTP.SC_BAD_REQUEST, ERROR_INVALID_CLIENT, desc);
	}
	
	public static OAuth2ResponseException serverErrorException(String desc) {
		return new OAuth2ResponseException(HTTP.SC_INTERNAL_SERVER_ERROR, ERROR_SERVER_ERROR, desc);
	}
	
    public static void invalidToken(Response response, String desc) {
		OAuth2Error error = invalidTokenError(desc);
        response(response, error);
    }
	public static OAuth2Error invalidTokenError(String desc) {
		OAuth2Error error = new SimpleOAuth2Error(HTTP.SC_UNAUTHORIZED, ERROR_INVALID_TOKEN, desc);
		return error;
	}
	protected OAuth2Errors() {
		
	}
	
}

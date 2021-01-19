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

import jdk.nashorn.internal.ir.SplitReturn;
import leap.lang.Strings;
import leap.web.exception.NotImplementedException;

/**
 * OAuth2.0 parameters.
 */
public interface OAuth2Params {
	
	String CLIENT_ID     = "client_id";
	String CLIENT_SECRET = "client_secret";
	String USERNAME      = "username";
	String PASSWORD 	 = "password";
	String GRANT_TYPE	 = "grant_type";
	String STATE	     = "state";
	String SCOPE		 = "scope";
	String REFRESH_TOKEN = "refresh_token";
	String CODE			 = "code";
	String REDIRECT_URI  = "redirect_uri";
	String RESPONSE_TYPE = "response_type";
	String TOKEN_TYPE    = "token_type";
	String ACCESS_TOKEN  = "access_token";
	
	//Error
    String ERROR             = "error";
    String ERROR_DESCRIPTION = "error_description";
	
	//Open ID Connect params
	String NONCE                    = "nonce";
	String ID_TOKEN                 = "id_token";
	String LOGOUT_URI				= "logout_uri"; 			  //http://openid.net/specs/openid-connect-logout-1_0.html
	String POST_LOGOUT_REDIRECT_URI = "post_logout_redirect_uri"; //http://openid.net/specs/openid-connect-logout-1_0.html

	default String getTokenType() {
	    return getParameter(TOKEN_TYPE);
	}
	
	default String getGrantType() {
		return getParameter(GRANT_TYPE);
	}
	
	default String getResponseType() {
		return getParameter(RESPONSE_TYPE);
	}
	
	default String getClientId() {
		return getParameter(CLIENT_ID);
	}
	
	default String getClientSecret() {
		return getParameter(CLIENT_SECRET);
	}
	
	default String getUsername() {
		return getParameter(USERNAME);
	}
	
	default String getPassword() {
		return getParameter(PASSWORD,false);
	}
	
	default String getState() {
		return getParameter(STATE);
	}

	default String getAccessToken() {
		return getParameter(ACCESS_TOKEN);
	}
	
	default String getRefreshToken() {
		return getParameter(REFRESH_TOKEN);
	}
	
	default String getScope() {
		return getParameter(SCOPE);
	}
	
	default String getCode() {
		return getParameter(CODE);
	}
	
	default String getRedirectUri() {
		return getParameter(REDIRECT_URI);
	}

	default String getLogoutUri() {
		return getParameter(LOGOUT_URI);
	}
	
	default String getError() {
	    return getParameter(ERROR);
	}
	
	default String getErrorDescription() {
	    return getParameter(ERROR_DESCRIPTION);
	}
	
	default String getIdToken() {
	    return getParameter(ID_TOKEN);
	}
	
	default String getNonce() {
	    return getParameter(NONCE);
	}
	
	default String getPostLogoutRedirectUri() {
	    return getParameter(POST_LOGOUT_REDIRECT_URI);
	}
	
	default boolean isError() {
	    return !Strings.isEmpty(getError());
	}
	
	default boolean isPasswordGrantType(){
		return "password".equals(getGrantType());
	}
	
	String getParameter(String name);

	default String getParameter(String name, boolean autoTrim){
		throw new NotImplementedException();
	};
}
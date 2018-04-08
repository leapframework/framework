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
package leap.oauth2.webapp;

import leap.lang.Strings;

/**
 * OAuth2.0 parameters.
 */
public interface OAuth2Params {
	
	String CLIENT_ID     = "client_id";
	String CODE			 = "code";
	String REDIRECT_URI  = "redirect_uri";
	String RESPONSE_TYPE = "response_type";

	//Error
    String ERROR             = "error";
    String ERROR_DESCRIPTION = "error_description";
	
	//Open ID Connect params
	String ID_TOKEN                 = "id_token";
	String LOGOUT_URI				= "logout_uri"; 			  //http://openid.net/specs/openid-connect-logout-1_0.html
	String POST_LOGOUT_REDIRECT_URI = "post_logout_redirect_uri"; //http://openid.net/specs/openid-connect-logout-1_0.html

	default String getClientId() {
		return getParameter(CLIENT_ID);
	}
	
	default String getCode() {
		return getParameter(CODE);
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
	
	default boolean isError() {
	    return !Strings.isEmpty(getError());
	}
	
	String getParameter(String name);
	
}
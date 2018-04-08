/*
 * Copyright 2014 the original author or authors.
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
package leap.web.security;

import leap.lang.http.Headers;
import leap.web.Renderable;

public class SecurityConstants {

    public static final int    DEFAULT_AUTHENTICATION_EXPIRES       = 3600 * 8;                        //8 hours

    public static final String DEFAULT_LOGIN_ACTION                 = "/login";
    public static final String DEFAULT_LOGOUT_ACTION                = "/logout";
    public static final String DEFAULT_LOGOUT_SUCCESS_URL           = Renderable.REDIRECT_PREFIX + "/";

    public static final String DEFAULT_RETURN_URL_PARAMETER         = "return_url";
    public static final String DEFAULT_REMEMBERME_PARAMETER         = "remember_me";
    public static final String DEFAULT_REMEMBERME_EXPIRES_PARAMETER = "remember_me_expires";

    public static final int    DEFAULT_REMEMBERME_EXPIRES           = 3600 * 24 * 7;                   //one weaks (7 days)
    public static final String DEFAULT_REMEMBERME_COOKIE            = "remember_me";

    public static final String DEFAULT_TOKEN_AUTHENTICATION_COOKIE  = "auth_token";
    public static final String DEFAULT_TOKEN_AUTHENTICATION_HEADER  = "X-Auth-Token";
    public static final String DEFAULT_TOKEN_TYPE                   = "jwt";

    public static final String DEFAULT_CSRF_HEADER                  = Headers.X_CSRF_TOKEN;
    public static final String DEFAULT_CSRF_PARAMETER               = "csrf_token";
	
	protected SecurityConstants(){
		
	}

}
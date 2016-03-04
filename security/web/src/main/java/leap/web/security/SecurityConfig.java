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
package leap.web.security;

import leap.core.AppConfig;
import leap.core.security.crypto.PasswordEncoder;
import leap.core.web.RequestIgnore;
import leap.web.security.csrf.CsrfStore;
import leap.web.security.path.SecuredPaths;
import leap.web.security.user.UserStore;

/**
 * The configuration of security module.
 */
public interface SecurityConfig {
	
	/**
	 * Returns <code>true</code> if web security is enabled.
	 */
	boolean isEnabled();
	
	/**
	 * Returns <code>true</code> if user authentication is cross contexts in the same web server.
	 * 
	 * <p>
	 * That means single sign on corss contexts.
	 * 
	 * <p>
	 * Default is <code>false</code>.
	 */
	boolean isCrossContext();
	
	/**
	 * Returns <code>true</code> if csrf security enabled.
	 * 
	 * <p>
	 * Default is <code>true</code>, it should not be disabled for security reason. 
	 */
	boolean isCsrfEnabled();
	
	/**
	 * Returns <code>true</code> if all request paths will be intercepted by security module and checks the user authentication.
	 * 
	 * <p>
	 * Default is <code>false</code>.
	 */
	boolean isAuthenticateAnyRequests();
	
	/**
	 * Returns <code>true</code> if all request paths will be intercepted by security module and checks the user authorization.
	 * 
	 * <p>
	 * Default is <code>false</code>.
	 */
	boolean isAuthorizeAnyRequests();
	
	/**
	 * Returns the default expires time in seconds used in persisted authentication, such authentication cookie.
	 */
	int getDefaultAuthenticationExpires();
	
	/**
	 * Returns the request parameter name of return url used by sigin flow.
	 */
	String getReturnUrlParameterName();
	
	/**
	 * Returns <code>true</code> if remember-me authentication is enabled.
	 * 
	 * <p>
	 * Default is <code>true</code>.
	 */
	boolean isRememberMeEnabled();
	
	/**
	 * Returns the name of remember-me cookie.
	 */
	String getRememberMeCookieName();
	
	/**
	 * Returns the request parameter name for enable or disable remember-me in login flow.
	 */
	String getRememberMeParameterName();
	
	/**
	 * Returns the secret key use to sign the remember-me cookie.
	 * 
	 * <p>
	 * Default is {@link AppConfig#getSecret()}.
	 */
	String getRememberMeSecret();
	
	/**
	 * Returns the request parameter name for specify the expires time of remember-me cookie in login flow.
	 */
	String getRememberMeExpiresParameterName();
	
	/**
	 * Returns the default expires time in seconds of remember-me cookie.
	 */
	int getDefaultRememberMeExpires();

	/**
	 *
	 */
	String getSecret();

	/**
	 * 
	 */
	String getCsrfHeaderName();
	
	/**
	 * 
	 */
	String getCsrfParameterName();
	
	/**
	 * 
	 */
	boolean isAuthenticationTokenEnabled();
	
	/**
	 * 
	 */
	String getAuthenticationTokenCookieName();
	
	/**
	 * 
	 */
	String getAuthenticationTokenHeaderName();
	
	/**
	 * 
	 */
	String getAuthenticationTokenType();
	
	/**
	 * Optional.
	 */
	String getCookieDomain();
	
    /**
     * Returns the url for promote user login.
     */
    String getLoginUrl();
	
	/**
	 * Required. Returns the action path for handling user's login authentication.
	 */
	String getLoginAction();
	
	/** 
	 * Required. Returns the action path for handling user's logout request.
	 */
	String getLogoutAction();
	
	/**
	 * Optional.
	 */
	String getLogoutSuccessUrl();
	
	/**
	 * Required.
	 */
	UserStore getUserStore();
	
	/**
	 * Required.
	 */
	CsrfStore getCsrfStore();
	
	/**
	 * Required. Returns the {@link PasswordEncoder} for authenticating user's password in security module.
	 */
	PasswordEncoder getPasswordEncoder();
	
	/**
	 * Optional.
	 */
	RequestIgnore[] getIgnores();

	/**
	 * Returns the {@link SecuredPaths}.
     */
	SecuredPaths getSecuredPaths();
	
	/**
	 * Optional.
	 */
	SecurityInterceptor[] getInterceptors();
}
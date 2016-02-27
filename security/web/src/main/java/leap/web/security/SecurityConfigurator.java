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

import leap.core.ioc.BeanList;
import leap.web.security.csrf.CsrfStore;
import leap.web.security.user.UserStore;

/**
 * The configurator of {@link SecurityConfig}.
 */
public interface SecurityConfigurator {
	
	/**
	 * Returns the {@link SecurityConfig} configuration object for current web application.
	 */
	SecurityConfig config();
	
	/**
	 * Enables web security.
	 */
	default SecurityConfigurator enable() {
	    return enable(false);
	}
	
	/**
     * Enables web security.
     */
    default SecurityConfigurator enable(boolean authenticateAnyRequest) {
        return setEnabled(true).setAuthenticateAnyRequests(authenticateAnyRequest);
    }
	
	/**
	 * Authenticating any requests.
	 */
	default SecurityConfigurator authenticateAnyRequests() {
		return setAuthenticateAnyRequests(true);
	}
	
	/**
	 * Sets enable or disable.
	 */
	SecurityConfigurator setEnabled(boolean enabled);
	
	/**
	 * Enables or Disables cross context web security.
	 */
	SecurityConfigurator setCrossContext(boolean crossContext);
	
	/**
	 * Sets the value of {@link SecurityConfig#isAuthenticateAnyRequests()}
	 */
	SecurityConfigurator setAuthenticateAnyRequests(boolean authenticateAnyRequest);
	
	/**
	 * Authorizing any requests.
	 */
	default SecurityConfigurator authorizeAnyRequests() {
		return setAuthenticateAnyRequests(true);
	}
	
	/**
	 * Sets the value of {@link SecurityConfig#isAuthorizeAnyRequests()}
	 */
	SecurityConfigurator setAuthorizeAnyRequests(boolean authorizeAnyRequests);
	
	/**
	 * Sets the default implementation of {@link UserStore}.
	 */
	SecurityConfigurator setUserStore(UserStore userStore);
	
	/**
	 * Enables or Disables remember me.
	 */
	SecurityConfigurator setRememberMeEnabled(boolean rememberMeEnabled);

	/**
	 * Sets the remember-me secret key.
	 */
	SecurityConfigurator setRememberMeSecret(String rememberMeSecret);
	
	/**
	 * Sets the remember-me cookie's name.
	 */
	SecurityConfigurator setRememberMeCookieName(String rememberMeCookieName);
	
	/**
	 * Sets the value of {@link SecurityConfig#isCsrfEnabled()}
	 */
	SecurityConfigurator setCsrfEnabled(boolean csrfEnabled);

	/**
	 * Sets the csrf header name in {@link SecurityConfig#getCsrfHeaderName()}.
	 */
	SecurityConfigurator setCsrfHeaderName(String csrfHeaderName);
	
	/**
	 * Sets the csf parameter name in {@link SecurityConfig#getCsrfParameterName()}
	 */
	SecurityConfigurator setCsrfParameterName(String csrfParameterName);
	
	/**
	 * Sets the default implementation of {@link CsrfStore}.
	 */
	SecurityConfigurator setCsrfStore(CsrfStore csrfStore);
	
	/**
	 * Sets the value of {@link SecurityConfig#isAuthenticationTokenEnabled()}
	 */
	SecurityConfigurator setAuthenticationTokenEnabled(boolean tokenAuthenticationEnabled);
	
	/**
	 * Sets the login action.
	 */
	SecurityConfigurator setLoginAction(String path);
	
	/**
	 * Sets the logout action.
	 */
	SecurityConfigurator setLogoutAction(String path);
	
	/**
	 * Sets the login url.
	 */
	SecurityConfigurator setLoginUrl(String url);
	
	/**
	 * Do not intercept the given path.
	 */
	SecurityConfigurator ignore(String path);
	
	/**
	 * Adds an secured path.
	 */
	SecurityConfigurator secured(SecurityPath path);

	/**
	 * Adds an intercept url.
	 */
	SecurityConfigurator secured(String path,boolean allowAnonymous);

	/**
	 * Adds an secured path.
	 */
	default SecurityConfigurator secured(String path) {
		return secured(path,false);
	}

	/**
	 * Enables anonymous access to the given paths.
	 */
	default SecurityConfigurator allowAnonymousAccessTo(String... paths) {
		for(String path : paths){
			secured(path,true);
		}
		return this;
	}
	
	/**
	 * Returns a mutable list contains all {@link SecurityInterceptor}. 
	 */
	BeanList<SecurityInterceptor> interceptors();
}
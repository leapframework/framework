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
import leap.core.security.crypto.PasswordEncoder;
import leap.web.route.Route;
import leap.web.security.csrf.CsrfStore;
import leap.web.security.path.SecuredPaths;
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
     * Returns the paths configurator.
     */
    SecuredPaths paths();
	
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
     * Enables anonymous access to the given paths.
     */
    default SecurityConfigurator allowAnonymousAccessTo(String... paths) {
        for(String path : paths){
            paths().apply(path,true);
        }
        return this;
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
     * Sets the default implementation of {@link PasswordEncoder}.
     */
    SecurityConfigurator setPasswordEncoder(PasswordEncoder pe);
	
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
     * Sets the value of {@link SecurityConfig#isLoginEnabled()}
     */
    SecurityConfigurator setLoginEnabled(boolean enabled);

    /**
     * Sets the value of {@link SecurityConfig#isLogoutEnabled()}
     */
    SecurityConfigurator setLogoutEnabled(boolean enabled);
	
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
     * Sets the {@link SecurityFailureHandler} for the path prefix.
     */
    SecurityConfigurator setPathPrefixFailureHandler(String pathPrefix, SecurityFailureHandler failureHandler);
	
	/**
	 * Do not intercept the given path.
	 */
	SecurityConfigurator ignore(String path);

	/**
	 * Returns a mutable list contains all {@link SecurityInterceptor}. 
	 */
	BeanList<SecurityInterceptor> interceptors();
}
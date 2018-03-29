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
package leap.web.security;

import leap.core.security.Authentication;
import leap.core.security.Credentials;
import leap.core.security.UserPrincipal;
import leap.lang.Out;
import leap.lang.intercepting.Interceptor;
import leap.lang.intercepting.State;
import leap.web.Request;
import leap.web.Response;
import leap.web.security.authc.AuthenticationContext;
import leap.web.security.authc.credentials.CredentialsAuthenticationContext;
import leap.web.security.authz.AuthorizationContext;
import leap.web.security.login.LoginContext;
import leap.web.security.logout.LogoutContext;
import leap.web.security.path.SecuredPath;

public interface SecurityInterceptor extends Interceptor{

    default SecuredPath resolveSecuredPath(SecurityContextHolder context) {
        return null;
    }

	default State preResolveAuthentication(Request request,Response response, AuthenticationContext context) throws Throwable {
		return State.CONTINUE;
	}
	
	default State postResolveAuthentication(Request request, Response response, AuthenticationContext context) throws Throwable {
		return State.CONTINUE;
	}

	/**
	 * @since 0.6.4b
	 */
	default State preAuthenticateCredentials(CredentialsAuthenticationContext context, Credentials credentials, Out<UserPrincipal> out) throws Throwable{
		return State.CONTINUE;
	}
	/**
	 * @since 0.6.4b
	 */
	default State postAuthenticateCredentials(CredentialsAuthenticationContext context, Credentials credentials, Authentication authentication) throws Throwable{
		return State.CONTINUE;
	}
	
    default State onAuthenticationDenied(Request request, Response response, SecurityContextHolder context) throws Throwable {
        return State.CONTINUE;
    }
	
	default State preResolveAuthorization(Request request, Response response, AuthorizationContext context) throws Throwable {
		return State.CONTINUE;
	}

    default State postResolveAuthorization(Request request, Response response, AuthorizationContext context) throws Throwable {
        return State.CONTINUE;
    }

	default State onAuthorizationDenied(Request request, Response response, AuthorizationContext context) throws Throwable {
        return State.CONTINUE;
    }

	default State prePromoteLogin(Request request, Response response, LoginContext context) throws Throwable {
	    return State.CONTINUE;
	}
	
	default State preLoginAuthentication(Request request, Response response, LoginContext context) throws Throwable {
	    return State.CONTINUE;
	}

	default State onLoginAuthenticationSuccess(Request request, Response response, LoginContext context, Authentication authc) throws Throwable {
	    return State.CONTINUE;
	}

    default State onLoginAuthenticationFailure(Request request, Response response, LoginContext context) throws Throwable {
        return State.CONTINUE;
    }
	
	default State preLogout(Request request, Response response, LogoutContext context) throws Throwable {
	    return State.CONTINUE;
	}

	default State postLogout(Request request, Response response, LogoutContext context) throws Throwable {
		return State.CONTINUE;
	}
}
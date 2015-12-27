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
package leap.web.security.authc;

import leap.core.annotation.Inject;
import leap.core.security.Anonymous;
import leap.core.security.Credentials;
import leap.core.security.UserPrincipal;
import leap.core.validation.ValidationContext;
import leap.lang.Out;
import leap.lang.Strings;
import leap.web.Request;
import leap.web.Response;
import leap.web.security.SecurityConfig;
import leap.web.security.SecuritySessionManager;
import leap.web.security.authc.credentials.CredentialsAuthenticator;

public class DefaultAuthenticationManager implements AuthenticationManager {
	
    protected @Inject SecurityConfig             securityConfig;
    protected @Inject AuthenticationHandler[]    handlers;
    protected @Inject SecuritySessionManager     sessionManager;
    protected @Inject TokenAuthenticationManager tokenAuthenticationManager;
    protected @Inject RememberMeManager          rememberMeManager;
    protected @Inject CredentialsAuthenticator[] credentialsAuthenticators;
	
	@Override
    public Authentication authenticate(ValidationContext context, Credentials credentials) {
		Out<UserPrincipal> user = new Out<>();
		
		for(CredentialsAuthenticator a : credentialsAuthenticators) {
			if(a.authenticate(context, credentials, user)) {
				break;
			}
		}
		
		if(null != user.getValue()) {
			return new SimpleAuthentication(user.getValue(), credentials);
		}
		
	    return null;
    }

	@Override
    public Authentication resolveAuthentication(Request request, Response response, AuthenticationContext context) throws Throwable {
		boolean handled = false;
		for(AuthenticationHandler h : handlers){
			if(h.resolveAuthentication(request, response, context).isIntercepted()){
				handled = true;
				break;
			}
		}
		
		if(!handled && securityConfig.isAuthenticationTokenEnabled()){
			handled = tokenAuthenticationManager.resolveAuthentication(request, response, context).isIntercepted();
		}
		
		Authentication authc = sessionManager.getAuthentication(request);
		if(null != authc) {
		    return authc;
		}
		
		if(!handled && securityConfig.isRememberMeEnabled()) {
			rememberMeManager.resolveAuthentication(request, response, context);	
		}
		
		authc = context.getAuthentication();
		if(null != authc && authc.isAuthenticated()){
			loginImmediately(request, response, authc);
		}
		
		return null == authc ? createAnonymousAuthentication(request, response, context) : authc;
    }

	@Override
    public void loginImmediately(Request request, Response response, Authentication authc) {
		saveAuthentication(request, response, authc);
		
		if(securityConfig.isAuthenticationTokenEnabled()) {
			tokenAuthenticationManager.onLoginSuccess(request, response, authc);
		}
		
		if(securityConfig.isRememberMeEnabled() && !authc.isRememberMe()) {
			rememberMeManager.onLoginSuccess(request, response, authc);	
		}

		for(AuthenticationHandler h : handlers) {
			h.onLoginSuccess(request, response, authc);
		}
	}

	@Override
    public void logoutImmediately(Request request, Response response) {
	    //TODO : handle exception.
	    sessionManager.removeAuthentication(request);
	    
		if(securityConfig.isAuthenticationTokenEnabled()) {
			tokenAuthenticationManager.onLogoutSuccess(request, response);
		}
		
		if(securityConfig.isRememberMeEnabled()) {
			rememberMeManager.onLogoutSuccess(request, response);	
		}
		
        for (AuthenticationHandler h : handlers) {
            h.onLogoutSuccess(request, response);
        }
    }
	
	protected void saveAuthentication(Request request,Response response, Authentication authentication) {
		sessionManager.saveAuthentication(request, authentication);
	}
	
	protected Authentication createAnonymousAuthentication(Request request,Response response,AuthenticationContext context) {
		return new SimpleAuthentication(createAnonymous(request, response, context));
	}
	
	public UserPrincipal createAnonymous(Request request,Response response,AuthenticationContext context) {
		Anonymous anonymous = new Anonymous();
		
		String name = request.getMessageSource().getMessage("websecurity.anonymous.name");
		if(Strings.isEmpty(name)){
			name = "Anonymous";
		}
		anonymous.setNickName(name);
		
		return anonymous;
	}
}
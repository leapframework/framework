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
import leap.core.security.Authentication;
import leap.core.security.ClientPrincipal;
import leap.core.security.Credentials;
import leap.core.security.UserPrincipal;
import leap.lang.Out;
import leap.lang.Result;
import leap.lang.Strings;
import leap.lang.intercepting.State;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.web.Request;
import leap.web.RequestIntercepted;
import leap.web.Response;
import leap.web.security.SecurityConfig;
import leap.web.security.SecurityInterceptor;
import leap.web.security.SecuritySessionManager;
import leap.web.security.authc.credentials.CredentialsAuthenticationContext;
import leap.web.security.authc.credentials.CredentialsAuthenticator;

public class DefaultAuthenticationManager implements AuthenticationManager {

    private static final Log log = LogFactory.get(DefaultAuthenticationManager.class);

    protected @Inject SecurityConfig             securityConfig;
    protected @Inject AuthenticationResolver[]   resolvers;
    protected @Inject SecuritySessionManager     sessionManager;
    protected @Inject TokenAuthenticationManager tokenAuthenticationManager;
    protected @Inject RememberMeManager          rememberMeManager;
    protected @Inject CredentialsAuthenticator[] credentialsAuthenticators;
	
	@Override
    public Authentication authenticate(CredentialsAuthenticationContext context, Credentials credentials) {
		Out<UserPrincipal> user = new Out<>();
		
        State s = beforeAuthenticate(context,credentials,user);
        if(State.isIntercepted(s)){
            if(null != user.getValue()) {
                return new SimpleAuthentication(user.getValue(), credentials);
            }else {
                return null;
            }
        }

        Authentication authentication = authentication(context,credentials,user);
        
	    return afterAuthenticate(context,credentials,authentication);
    }
    
    protected State beforeAuthenticate(CredentialsAuthenticationContext context, Credentials credentials, Out<UserPrincipal> user){
        for(SecurityInterceptor interceptor : securityConfig.getInterceptors()){
            try {
                State state = interceptor.preAuthenticateCredentials(context,credentials,user);
                if(State.isIntercepted(state)){
                    return state;
                }
            } catch (Throwable throwable) {
                throw new AuthenticationException(throwable);
            }
        }
        return State.CONTINUE;
    }

    protected Authentication authentication(CredentialsAuthenticationContext context, Credentials credentials,Out<UserPrincipal> user){
        for(CredentialsAuthenticator a : credentialsAuthenticators) {
            if(a.authenticate(context, credentials, user)) {
                break;
            }
        }

        Authentication authentication = null;
        if(null != user.getValue()) {
            authentication = new SimpleAuthentication(user.getValue(), credentials);
        }
        return authentication;
    }
    
    protected Authentication afterAuthenticate(CredentialsAuthenticationContext context, Credentials credentials, Authentication authentication){
        for(SecurityInterceptor interceptor : securityConfig.getInterceptors()){
            try {
                State state = interceptor.postAuthenticateCredentials(context,credentials,authentication);
                if(State.isIntercepted(state)){
                    return authentication;
                }
            } catch (Throwable throwable) {
                throw new AuthenticationException(throwable);
            }
        }
        return authentication;
    }
    
	@Override
    public Authentication resolveAuthentication(Request request, Response response, AuthenticationContext context) throws Throwable {
        Authentication authc = null;

        if(State.isContinue(tokenAuthenticationManager.preResolveAuthentication(request, response, context))) {
            authc = context.getAuthentication();

            if(null == authc) {
                for(AuthenticationResolver h : resolvers){
                    Result<Authentication> r = h.resolveAuthentication(request, response, context);
                    if(null == r || r.isEmpty()) {
                        continue;
                    }

                    if(r.isIntercepted()) {
                        RequestIntercepted.throwIt();
                    }

                    if(r.isPresent()) {
                        authc = r.get();
                        break;
                    }
                }

                if(null == authc) {
                    authc = sessionManager.getAuthentication(request);
                    if(null != authc) {
                        return authc;
                    }

                    authc = Result.value(tokenAuthenticationManager.resolveAuthentication(request, response, context));
                    if(null == authc) {
                        authc = Result.value(rememberMeManager.resolveAuthentication(request, response, context));
                    }
                }
            }

            if(null != authc) {
                if(null == authc.getUser()) {
                    authc = new WrappedAuthentication(authc,createAnonymous(request, response, context));
                }

                if(authc.isAuthenticated() && !authc.isClientOnly()) {
                    loginImmediately(request, response, authc);
                }
            }
        }

		return null == authc ? createAnonymousAuthentication(request, response, context) : authc;
    }

	@Override
    public void loginImmediately(Request request, Response response, Authentication authc) {

        log.debug("User {} logged in", authc.getUser().getLoginName());

		saveAuthentication(request, response, authc);
		
		if(securityConfig.isAuthenticationTokenEnabled()) {
			tokenAuthenticationManager.onLoginSuccess(request, response, authc);
		}
		
		if(securityConfig.isRememberMeEnabled() && !authc.isRememberMe()) {
			rememberMeManager.onLoginSuccess(request, response, authc);	
		}

		for(AuthenticationResolver h : resolvers) {
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
		
        for (AuthenticationResolver h : resolvers) {
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
        String name = request.getMessageSource().getMessage("websecurity.anonymous.name");
        if(Strings.isEmpty(name)){
            name = "Anonymous";
        }
		return new Anonymous(name);
	}

    protected static final class WrappedAuthentication implements Authentication {

        private final Authentication wrapped;
        private final UserPrincipal  user;

        WrappedAuthentication(Authentication wrapped, UserPrincipal user) {
            this.wrapped = wrapped;
            this.user    = user;
        }

        @Override
        public boolean isAuthenticated() {
            return wrapped.isAuthenticated();
        }

        @Override
        public boolean isRememberMe() {
            return wrapped.isRememberMe();
        }

        @Override
        public Object getCredentials() {
            return wrapped.getCredentials();
        }

        @Override
        public UserPrincipal getUser() {
            return user;
        }

        @Override
        public ClientPrincipal getClient() {
            return wrapped.getClient();
        }

        @Override
        public String getToken() {
            return wrapped.getToken();
        }

        @Override
        public void setToken(String token) throws IllegalStateException {
            wrapped.setToken(token);
        }

        @Override
        public String[] getPermissions() {
            return wrapped.getPermissions();
        }

        @Override
        public void setPermissions(String... permissions) {
            wrapped.setPermissions(permissions);
        }

        @Override
        public String[] getRoles() {
            return wrapped.getRoles();
        }

        @Override
        public void setRoles(String... roles) {
            wrapped.setRoles(roles);
        }

        @Override
        public String toString() {
            return wrapped.toString();
        }
    }
}
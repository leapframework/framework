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

import leap.core.annotation.Inject;
import leap.core.security.Authentication;
import leap.core.security.Authorization;
import leap.lang.http.HTTP;
import leap.lang.intercepting.State;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.web.Request;
import leap.web.Response;
import leap.web.security.authc.AuthenticationManager;
import leap.web.security.authz.AuthorizationManager;
import leap.web.security.login.LoginManager;
import leap.web.security.logout.LogoutManager;
import leap.web.security.path.SecuredPath;

public class DefaultSecurityHandler implements SecurityHandler {

    private static final Log log = LogFactory.get(DefaultSecurityHandler.class);

    protected @Inject SecurityConfig        config;
    protected @Inject AuthenticationManager authcManager;
    protected @Inject AuthorizationManager  authzManager;
    protected @Inject LoginManager          loginManager;
    protected @Inject LogoutManager         logoutManager;
    
	@Override
    public Authentication resolveAuthentication(Request request, Response response,SecurityContextHolder context) throws Throwable {
		return authcManager.resolveAuthentication(request, response, context);
    }
	
	@Override
    public Authorization resolveAuthorization(Request request, Response response, SecurityContextHolder context) throws Throwable {
		return authzManager.resolveAuthorization(request,response,context);
    }

    @Override
    public boolean checkAuthentication(Request request, Response response, SecurityContextHolder context) throws Throwable {
        SecuredPath path = context.getSecuredPath();

        if(null != path) {
            return path.checkAuthentication(request, context);
        }

        return true;
    }

    @Override
    public boolean checkAuthorization(Request request, Response response, SecurityContextHolder context) throws Throwable {
        SecuredPath path = context.getSecuredPath();
        if(!authzManager.checkAuthorization(request,response,context)){
            return false;
        }
        if(null != path) {
            return path.checkAuthorization(request, context);
        }

        return true;
    }

    @Override
    public void handleAuthenticationDenied(Request request, Response response, SecurityContextHolder context) throws Throwable {
        SecuredPath path = context.getSecuredPath();
        if(null != path && null != path.getFailureHandler()) {
            if(path.getFailureHandler().handleAuthenticationDenied(request,response, context)) {
                return;
            }
        }

        for(SecurityInterceptor si : config.getInterceptors()) {
            if(State.isIntercepted(si.onAuthenticationDenied(request, response, context))) {
                return;
            }
        }
        
        loginManager.promoteLogin(request, response, context.getLoginContext());
        if(response.getStatus() < HTTP.SC_MULTIPLE_CHOICES && response.getStatus() >= HTTP.SC_OK){
            response.setStatus(HTTP.SC_UNAUTHORIZED);
        }
    }

    @Override
    public void handleAuthorizationDenied(Request request, Response response, SecurityContextHolder context) throws Throwable {
        SecuredPath path = context.getSecuredPath();
        if(null != path && null != path.getFailureHandler()) {
            if(path.getFailureHandler().handleAuthorizationDenied(request,response, context)) {
                return;
            }
        }

        for(SecurityInterceptor si : config.getInterceptors()) {
            if(State.isIntercepted(si.onAuthorizationDenied(request, response, context))) {
                return;
            }
        }

        if(request.isAjax()){
            response.setStatus(HTTP.SC_FORBIDDEN);
        }else{
            //TODO : error view ?
            response.sendError(HTTP.SC_FORBIDDEN);
        }
    }
    
    @Override
    public boolean handleLoginRequest(Request request, Response response, SecurityContextHolder context) throws Throwable {
		return loginManager.handleLoginRequest(request, response, context.getLoginContext());
	}

	@Override
    public boolean handleLogoutRequest(Request request, Response response, SecurityContextHolder context) throws Throwable {
		return logoutManager.handleLogoutRequest(request, response, context.getLogoutContext());
	}
}
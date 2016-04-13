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
package leap.web.security.logout;

import leap.core.annotation.Inject;
import leap.lang.intercepting.State;
import leap.web.Request;
import leap.web.Response;
import leap.web.security.DefaultSecurityContextHolder;
import leap.web.security.SecurityConfig;
import leap.web.security.SecurityInterceptor;
import leap.web.security.authc.AuthenticationManager;
import leap.web.security.permission.PermissionManager;

public class DefaultLogoutManager implements LogoutManager {

    protected @Inject SecurityConfig        config;
    protected @Inject LogoutHandler[]       handlers;
    protected @Inject AuthenticationManager authcManager;
    protected @Inject LogoutViewHandler     viewHandler;
    protected @Inject LogoutAjaxHandler     ajaxHandler;
    protected @Inject PermissionManager     permissionManager;

    @Override
    public boolean handleLogoutRequest(Request request, Response response, LogoutContext context) throws Throwable {
        if(!isLogoutRequest(request, response, context)) {
            return false;
            
        }
        
        logout(request, response, context);
        
        return true;
    }
    
    @Override
    public void logout(Request request, Response response) throws Throwable {
    	DefaultSecurityContextHolder context = new DefaultSecurityContextHolder(config, permissionManager, request);
        logout(request, response, context.getLogoutContext());
    }

    @Override
    public void logout(Request request, Response response, LogoutContext context) throws Throwable {
        
        for(SecurityInterceptor i : config.getInterceptors()) {
            if(State.isIntercepted(i.preLogout(request, response, context))) {
                return;
            }
        }

        for(LogoutHandler handler : handlers) {
            if(State.isIntercepted(handler.handleLogout(request, response, context))){
                return;
            }
        }
        
        //Do logout.
        authcManager.logoutImmediately(request, response);
        
        //Logout success.
        if(request.isAjax()) {
            ajaxHandler.handleLogoutSuccess(request, response, context);
        }else{
            viewHandler.handleLogoutSuccess(request, response, context);
        }

        for(SecurityInterceptor i : config.getInterceptors()) {
            if(State.isIntercepted(i.postLogout(request, response, context))) {
                return;
            }
        }
    }

    protected boolean isLogoutRequest(Request request, Response response, LogoutContext context) throws Throwable {
        return request.getPath().equals(config.getLogoutAction());
    }    
}
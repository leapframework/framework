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
package leap.web.security.login;

import leap.core.annotation.Inject;
import leap.core.security.Authentication;
import leap.lang.intercepting.State;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.web.Request;
import leap.web.Response;
import leap.web.security.DefaultSecurityContextHolder;
import leap.web.security.SecurityConfig;
import leap.web.security.SecurityInterceptor;
import leap.web.security.authc.AuthenticationManager;
import leap.web.security.authc.SimpleAuthentication;
import leap.web.security.permission.PermissionManager;

public class DefaultLoginManager implements LoginManager {

    private static final Log log = LogFactory.get(DefaultLoginManager.class);
    
    protected @Inject SecurityConfig        config;
    protected @Inject LoginHandler[]        handlers;
    protected @Inject LoginAjaxHandler      ajaxHandler;
    protected @Inject LoginViewHandler      viewHandler;
    protected @Inject AuthenticationManager authcManager;
    protected @Inject PermissionManager     permissionManager;
    
    @Override
    public boolean promoteLogin(Request request, Response response, LoginContext context) throws Throwable {
        for(SecurityInterceptor i : config.getInterceptors()) {
            if(State.isIntercepted(i.prePromoteLogin(request, response, context))) {
                return true;
            }
        }
        
        if(request.isAjax()) {
            log.debug("Promote login for ajax request");
            ajaxHandler.promoteLogin(request, response, context);
        }else{
            log.debug("Promote login");
            viewHandler.promoteLogin(request, response, context);    
        }

        return true;
    }

    @Override
    public boolean handleLoginRequest(Request request, Response response, LoginContext context) throws Throwable {
        if(!isLoginRequest(request, response, context)) {
            return false;
        }
        
        request.setAcceptValidationError(true);
        
        if(request.isGet()) {
            handleLoginView(request, response, context);
        }else {
            handleLoginAuthentication(request, response, context);
        }
        
        return true;
    }
    
    @Override
    public void handleLoginSuccess(Request request, Response response, Authentication authc) throws Throwable {
        DefaultSecurityContextHolder context = new DefaultSecurityContextHolder(config, permissionManager, request);
        context.setAuthentication(authc);
        handleLoginSuccessView(request, response, context.getLoginContext());
    }

    protected void handleLoginView(Request request, Response response, LoginContext context) throws Throwable {
        
        for(LoginHandler handler : handlers){
            if(State.isIntercepted(handler.handleLoginView(request, response, context))){
                return;
            }
        }
        
        viewHandler.goLoginUrl(request, response, context);    
    }
    
    protected void handleLoginSuccessView(Request request, Response response, LoginContext context) throws Throwable {
        if(request.isAjax()) {
            ajaxHandler.handleLoginSuccess(request, response, context);
        }else{
            viewHandler.handleLoginSuccess(request, response, context);            
        }
    }
    
    protected void handleLoginAuthentication(Request request, Response response, LoginContext context) throws Throwable {
        
        for(SecurityInterceptor i : config.getInterceptors()) {
            if(State.isIntercepted(i.preLoginAuthentication(request, response, context))){
                return;
            }
        }


        if(!context.isAuthenticated() && !context.isError()) {

            for(LoginHandler handler : handlers){
                if(State.isIntercepted(handler.handleLoginAuthentication(request, response, context))){
                    return;
                }
            }

        }

        //If authentication success.
        if(context.isAuthenticated() && !context.isError()){
            Authentication authc = new SimpleAuthentication(context.getUser(), context.getCredentials());
            
            //login the authentication.
            authcManager.loginImmediately(request, response, authc);
            
            //login success.
            for(SecurityInterceptor i : config.getInterceptors()) {
                if(State.isIntercepted(i.onLoginAuthenticationSuccess(request, response, context, authc))) {
                    return ;
                }
            }
            
            handleLoginSuccessView(request, response, context);
        }else{
            //login failed.
            for(SecurityInterceptor i : config.getInterceptors()) {
                if(State.isIntercepted(i.onLoginAuthenticationFailure(request, response, context))) {
                    return ;
                }
            }

            if(request.isAjax()) {
                ajaxHandler.handleLoginFailure(request, response, context);
            }else{
                viewHandler.handleLoginFailure(request, response, context);
            }
        }
    }

    protected boolean isLoginRequest(Request request, Response response, LoginContext context) throws Throwable {
        return request.getPath().equals(config.getLoginAction());
    }
}
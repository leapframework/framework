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
package leap.oauth2.server.endpoint;

import leap.core.annotation.Inject;
import leap.lang.Strings;
import leap.lang.intercepting.State;
import leap.oauth2.OAuth2Params;
import leap.oauth2.RequestOAuth2Params;
import leap.web.App;
import leap.web.Request;
import leap.web.Response;
import leap.web.route.Routes;
import leap.web.security.SecurityContextHolder;
import leap.web.security.SecurityInterceptor;
import leap.web.security.authc.AuthenticationManager;
import leap.web.security.logout.LogoutManager;
import leap.web.view.View;
import leap.web.view.ViewSource;

public class LogoutEndpoint extends AbstractAuthzEndpoint implements SecurityInterceptor {
    
    protected @Inject AuthenticationManager authcManager;
    protected @Inject ViewSource            viewSource;
    protected @Inject LogoutManager         logoutManager;
    
    protected View defaultLogoutView;
    
    @Override
    public void startEndpoint(App app, Routes routes) throws Throwable {
        if(config.isEnabled() && config.isLogoutEndpointEnabled()) {
            sc.interceptors().add(this);
            
            if(!Strings.isEmpty(config.getLogoutView())) {
                this.defaultLogoutView = viewSource.getView(config.getLogoutView());
            }
        }
    }

    @Override
    public State postResolveAuthentication(Request request, Response response, SecurityContextHolder context) throws Throwable {
        if(!request.getPath().equals(config.getLogoutEndpointPath())) {
            return State.CONTINUE;
        }
        
        //TODO : validate the request.
        
        //Do Logout
        OAuth2Params params = new RequestOAuth2Params(request);
        
        String redirectUri = params.getPostLogoutRedirectUri();
        if(Strings.isEmpty(redirectUri)) {
            //No redirect uri, render the view.
            if(null != defaultLogoutView) {
                authcManager.logoutImmediately(request, response);
                defaultLogoutView.render(request, response);
            }else{
                logoutManager.logout(request, response, context);
            }
        }else{
            //Logout 
            authcManager.logoutImmediately(request, response);
            
            //Redirect to the uri;
            response.sendRedirect(redirectUri);
        }

        return State.INTERCEPTED;
    }
}
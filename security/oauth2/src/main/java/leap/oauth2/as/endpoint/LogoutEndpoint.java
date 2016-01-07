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
package leap.oauth2.as.endpoint;

import leap.core.AppConfigException;
import leap.core.annotation.Inject;
import leap.lang.Strings;
import leap.lang.intercepting.State;
import leap.oauth2.OAuth2Params;
import leap.oauth2.RequestOAuth2Params;
import leap.oauth2.as.endpoint.logout.PostLogoutHandler;
import leap.web.App;
import leap.web.Request;
import leap.web.Response;
import leap.web.route.Routes;
import leap.web.security.SecurityContextHolder;
import leap.web.security.SecurityInterceptor;
import leap.web.security.authc.AuthenticationManager;
import leap.web.view.View;
import leap.web.view.ViewSource;

public class LogoutEndpoint extends AbstractAuthzEndpoint implements SecurityInterceptor {
    
    protected @Inject AuthenticationManager authcManager;
    protected @Inject ViewSource            viewSource;
    protected @Inject PostLogoutHandler     postLogoutHandler;

    protected View defaultLogoutView;
    
    @Override
    public void startEndpoint(App app, Routes routes) throws Throwable {
        if(config.isEnabled() && config.isLogoutEndpointEnabled()) {
            sc.interceptors().add(this);
            
            if(!Strings.isEmpty(config.getLogoutView())) {
                this.defaultLogoutView = viewSource.getView(config.getLogoutView());
            }

            if(null == defaultLogoutView) {
                throw new AppConfigException("The oauth2 logout view must be configured if logout endpoint enabled");
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
        authcManager.logoutImmediately(request, response);

        //Handle post logout.
        postLogoutHandler.handlePostLogout(request, response, context, defaultLogoutView);

        return State.INTERCEPTED;
    }
}
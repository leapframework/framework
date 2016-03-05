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

import leap.core.annotation.Inject;
import leap.oauth2.OAuth2Errors;
import leap.oauth2.OAuth2Params;
import leap.oauth2.RequestOAuth2Params;
import leap.oauth2.as.endpoint.logintoken.LoginTokenHandler;
import leap.oauth2.as.endpoint.tokeninfo.TokenInfoHandler;
import leap.web.*;
import leap.web.route.Routes;

/**
 * Not a standard endpoint, to generate a new login token for login.
 */
public class LoginTokenEndpoint extends AbstractAuthzEndpoint implements Endpoint,Handler {
    
    protected @Inject LoginTokenHandler[] handlers;
    
	@Override
    public void startEndpoint(App app, Routes routes) {
	    if(config.isEnabled() && config.isLoginTokenEnabled()) {
	        sc.ignore(config.getLoginTokenEndpointPath());
	        
	        routes.create()
	              .get(config.getLoginTokenEndpointPath(), this)
	              .apply();
	    }
    }

	@Override
    public void handle(Request request, Response response) throws Throwable {

		OAuth2Params params = new RequestOAuth2Params(request);

	    for(LoginTokenHandler h : handlers) {
	        
	        if(h.handleLoginTokenRequest(request, response, params)) {
	            return;
	        }
	        
	    }
	    
	    OAuth2Errors.invalidRequest(response, "invalid parameters");;
    }
	
}
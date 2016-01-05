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
package leap.oauth2.rs;

import java.io.IOException;

import javax.servlet.ServletException;

import leap.core.annotation.Inject;
import leap.lang.Result;
import leap.lang.intercepting.State;
import leap.oauth2.OAuth2InvalidTokenExcepiton;
import leap.oauth2.OAuth2ResponseException;
import leap.oauth2.rs.auth.ResAuthenticationManager;
import leap.oauth2.rs.auth.ResAuthentication;
import leap.oauth2.rs.token.ResAccessToken;
import leap.oauth2.rs.token.ResAccessTokenExtractor;
import leap.web.Request;
import leap.web.Response;
import leap.web.security.SecurityContextHolder;
import leap.web.security.SecurityInterceptor;
import leap.web.security.authc.AuthenticationContext;

public class OAuth2ResServerSecurityInterceptor implements SecurityInterceptor {
    
    //private static final Log log = LogFactory.get(OAuth2ResServerSecurityInterceptor.class);

    protected @Inject OAuth2ResServerConfig       config;
    protected @Inject ResAccessTokenExtractor     tokenExtractor;
    protected @Inject OAuth2ResServerErrorHandler errorHandler;
    protected @Inject ResAuthenticationManager    authcManager;
	
	public OAuth2ResServerSecurityInterceptor() {
        super();
    }

    @Override
	public State preResolveAuthentication(Request request, Response response, SecurityContextHolder context) throws ServletException, IOException {
		if (config.isEnabled()) {
			
			//ResPath url = config.getResourcePath(request.getPath());
			
			//if(null == url && !config.isInterceptAnyRequests()) {
			//	return State.CONTINUE;
			//}else{
			    //log.debug("Authenticating oauth2 request '{}'...", request.getPath());
				return preAuthentication(request, response, context, null);
			//}

		}
		
		return State.CONTINUE;
	}
	
	protected State preAuthentication(Request request, Response response, AuthenticationContext context, ResPath url) throws ServletException, IOException {
		ResAccessToken token = tokenExtractor.extractTokenFromRequest(request);
		
		if(null == token) {
			//errorHandler.handleInvalidRequest(request, response, "Access token requried");
			//return State.INTERCEPTED;
		    return State.CONTINUE;
		}
		
        try {
            Result<ResAuthentication> result = authcManager.authenticate(token);
            
            if(!result.isPresent()) {
                errorHandler.handleInvalidToken(request, response, "Invalid access token");
                return State.INTERCEPTED;
            }
            
            ResAuthentication authc = result.get();
            if(null != url) {
                if(!url.isAllow(request, context, authc)) {
                    errorHandler.handleInsufficientScope(request, response, "Access denied");
                    return State.INTERCEPTED;
                }
            }
            
            context.setAuthentication(authc);
            return State.CONTINUE;
            
        } catch (OAuth2InvalidTokenExcepiton e) {
            errorHandler.handleInvalidToken(request, response, e.getMessage());
            return State.INTERCEPTED;
        } catch (OAuth2ResponseException e) {
            errorHandler.responseError(request, response, e.getStatus(), e.getError(), e.getMessage());
            return State.INTERCEPTED;
        } catch (Throwable e) {
            errorHandler.handleServerError(request, response, e);
            return State.INTERCEPTED;
        }
	}
	
}
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

import leap.core.annotation.Inject;
import leap.lang.Result;
import leap.lang.intercepting.State;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.oauth2.OAuth2InvalidTokenException;
import leap.oauth2.OAuth2ResponseException;
import leap.oauth2.rs.auth.ResAuthentication;
import leap.oauth2.rs.auth.ResAuthenticationResolver;
import leap.oauth2.rs.auth.ResCredentialsAuthenticator;
import leap.oauth2.rs.token.ResAccessToken;
import leap.oauth2.rs.token.ResAccessTokenExtractor;
import leap.web.Request;
import leap.web.Response;
import leap.web.security.SecurityContextHolder;
import leap.web.security.SecurityInterceptor;
import leap.web.security.authc.Authentication;
import leap.web.security.authc.AuthenticationContext;
import leap.web.security.csrf.CSRF;

import javax.servlet.ServletException;
import java.io.IOException;

/**
 * The {@link SecurityInterceptor} for protecting resource request in oauth2 resource server.
 */
public class OAuth2ResServerSecurityInterceptor implements SecurityInterceptor {
    
    private static final Log log = LogFactory.get(OAuth2ResServerSecurityInterceptor.class);

    protected @Inject OAuth2ResServerConfig       config;
    protected @Inject ResAccessTokenExtractor     tokenExtractor;
    protected @Inject OAuth2ResServerErrorHandler errorHandler;
    protected @Inject ResCredentialsAuthenticator credentialsAuthenticator;
    protected @Inject ResAuthenticationResolver[] authenticationResolvers;
	
	public OAuth2ResServerSecurityInterceptor() {
        super();
    }

    @Override
	public State preResolveAuthentication(Request request, Response response, SecurityContextHolder context) throws Throwable {
		if (config.isEnabled()) {
			
			//ResPath url = config.getResourcePath(request.getPath());
			
			//if(null == url && !config.isInterceptAnyRequests()) {
			//	return State.CONTINUE;
			//}else{
			    //log.debug("Authenticating oauth2 request '{}'...", request.getPath());
				return preResolveAuthentication(request, response, context, null);
			//}

		}
		
		return State.CONTINUE;
	}
	
	protected State preResolveAuthentication(Request request, Response response, AuthenticationContext context, ResPath path) throws Throwable {
        Authentication authc = null;
        for(ResAuthenticationResolver resolver : authenticationResolvers) {
            Result<Authentication> result = resolver.resolveAuthentication(request, response, context);
            if(result.isPresent()) {
                authc = result.get();
                break;
            }
            if(result.isIntercepted()) {
                return State.INTERCEPTED;
            }
        }

        ResAccessToken token = null;
        if(null == authc) {
            //Extract access token from request.
            token = tokenExtractor.extractTokenFromRequest(request);
            if(null == token) {
                //If no access token just ignore resolving authentication from access token.
                return State.CONTINUE;
            }
        }

        //Resolving authentication from access token.
        try {
            if(null == authc) {
                Result<ResAuthentication> result = credentialsAuthenticator.authenticate(token);
                if(!result.isPresent()) {
                    errorHandler.handleInvalidToken(request, response, "Invalid access token");
                    return State.INTERCEPTED;
                }
                authc = result.get();
            }

            if(null != path) {
                if(!path.isAllow(request, context, authc)) {
                    errorHandler.handleInsufficientScope(request, response, "Access denied");
                    return State.INTERCEPTED;
                }
            }

            //Set the authentication.
            context.setAuthentication(authc);

            //Ignore csrf token checking.
            CSRF.ignore(request.getServletRequest());

            return State.CONTINUE;
        } catch (OAuth2InvalidTokenException e) {
            errorHandler.handleInvalidToken(request, response, e.getMessage());
            return State.INTERCEPTED;
        } catch (OAuth2ResponseException e) {
            errorHandler.responseError(request, response, e.getStatus(), e.getError(), e.getMessage());
            return State.INTERCEPTED;
        } catch (Throwable e) {
            log.error("Error resolving authentication from access token : {}", e.getMessage(), e);
            errorHandler.handleServerError(request, response, e);
            return State.INTERCEPTED;
        }
	}
	
}
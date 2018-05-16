/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package leap.oauth2.webapp.authc;

import leap.core.AppConfig;
import leap.core.annotation.Inject;
import leap.core.security.token.TokenVerifyException;
import leap.core.web.RequestIgnore;
import leap.lang.intercepting.State;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.oauth2.webapp.OAuth2Config;
import leap.oauth2.webapp.OAuth2ErrorHandler;
import leap.oauth2.webapp.OAuth2ResponseException;
import leap.oauth2.webapp.Oauth2InvalidTokenException;
import leap.oauth2.webapp.token.Token;
import leap.oauth2.webapp.token.TokenExtractor;
import leap.web.App;
import leap.web.Request;
import leap.web.Response;
import leap.web.security.SecurityInterceptor;
import leap.web.security.authc.AuthenticationContext;
import leap.web.security.csrf.CSRF;

/**
 * The {@link SecurityInterceptor} for protecting resource request in oauth2 resource server.
 */
public class OAuth2AuthenticationInterceptor implements SecurityInterceptor {
    
    private static final Log log = LogFactory.get(OAuth2AuthenticationInterceptor.class);
    
    protected @Inject App                 app;
    protected @Inject OAuth2Config        config;
    protected @Inject TokenExtractor      tokenExtractor;
    protected @Inject OAuth2ErrorHandler  errorHandler;
    protected @Inject OAuth2Authenticator authenticator;

    @Override
	public State preResolveAuthentication(Request request, Response response, AuthenticationContext context) throws Throwable {
		if (!config.isEnabled()) {
            return State.CONTINUE;
        }
        Object o = app.getAttribute("oauth2.skipTokenAuthenticateUrl");
		if(o instanceof String[]){
            String[] skips = (String[]) o;
            for (String skip : skips){
                if(request.getPath().equals(skip)){
                    return State.CONTINUE;
                }
            }
        }
        for(RequestIgnore ignore : config.getIgnores()){
		    if(ignore.matches(request)){
                return State.CONTINUE;
            }
        }
        
        //Extract access token from request.
        Token token = tokenExtractor.extractTokenFromRequest(request);
        if(null == token) {
            return State.CONTINUE;
        }

        //Resolving authentication from access token.
        try {
            OAuth2Authentication authc = authenticator.authenticate(token);
            if(null == authc) {
                log.warn("Invalid access token '{}'", token.getToken());
                return State.CONTINUE;
            }

            //Set the authentication.
            context.setAuthentication(authc);

            //Ignore csrf token checking.
            CSRF.ignore(request.getServletRequest());

            return State.CONTINUE;
        } catch (TokenVerifyException e) {
            errorHandler.handleInvalidToken(request, response, e.getMessage());
            return State.INTERCEPTED;
        } catch (OAuth2ResponseException e) {
            if (e instanceof Oauth2InvalidTokenException){
                if(null != context.getSecuredPath() && context.getSecuredPath().isAllowAnonymous()){
                    return State.CONTINUE;
                }
            }
            errorHandler.responseError(request, response, e.getStatus(), e.getError(), e.getMessage());
            return State.INTERCEPTED;
        } catch (Throwable e) {
            log.error("Error resolving authentication from access token : {}", e.getMessage(), e);
            errorHandler.handleServerError(request, response, e);
            return State.INTERCEPTED;
        }
	}

}
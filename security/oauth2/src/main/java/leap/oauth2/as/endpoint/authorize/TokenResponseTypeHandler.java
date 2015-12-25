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
package leap.oauth2.as.endpoint.authorize;

import leap.core.annotation.Inject;
import leap.lang.Strings;
import leap.lang.net.Urls;
import leap.oauth2.OAuth2Params;
import leap.oauth2.OAuth2Errors;
import leap.oauth2.as.AuthzAuthentication;
import leap.oauth2.as.OAuth2ServerConfig;
import leap.oauth2.as.code.AuthzCodeManager;
import leap.oauth2.as.token.AuthzAccessToken;
import leap.oauth2.as.token.AuthzTokenManager;
import leap.web.Request;
import leap.web.Response;

/**
 * Implicit authorization. 
 */
public class TokenResponseTypeHandler extends AbstractResponseTypeHandler implements ResponseTypeHandler {
    
    protected @Inject OAuth2ServerConfig config;
    protected @Inject AuthzCodeManager  codeManager;
    protected @Inject AuthzTokenManager tokenManager;

    @Override
    public void handleResponseType(Request request, Response response, AuthzAuthentication authc) throws Throwable {
        if(!config.isImplicitGrantEnabled()) {
            OAuth2Errors.redirectUnsupportedResponseType(response, authc.getRedirectUri(), null);
            return;
        }

        //Create a new access token.
        AuthzAccessToken at = tokenManager.createAccessToken(authc);
        
        //Response
        sendAccessTokenRedirect(request, response, authc, at);
    }

    protected void sendAccessTokenRedirect(Request request, Response response, AuthzAuthentication authc, AuthzAccessToken at) {
        OAuth2Params params = authc.getParams();
        
        StringBuilder query = new StringBuilder();
        
        query.append("access_token=").append(at.getToken())
             .append("&token_type=").append(at.getTokenType())
             .append("&expires_in=").append(at.getExpiresIn());
        
        String state = params.getState();
        if(!Strings.isEmpty(state)) {
            query.append("&state=").append(Urls.encode(state));
        }
        
        response.sendRedirect(Urls.appendQueryString(authc.getRedirectUri(), query.toString()));
    }
}
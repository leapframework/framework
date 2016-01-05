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
package leap.oauth2.server.endpoint.authorize;

import leap.core.annotation.Inject;
import leap.lang.Strings;
import leap.lang.net.Urls;
import leap.oauth2.OAuth2Params;
import leap.oauth2.OAuth2Errors;
import leap.oauth2.server.authc.OAuth2Authentication;
import leap.oauth2.server.OAuth2ServerConfig;
import leap.oauth2.server.code.OAuth2AuthzCodeManager;
import leap.oauth2.server.token.OAuth2AccessToken;
import leap.oauth2.server.token.OAuth2TokenManager;
import leap.web.Request;
import leap.web.Response;

/**
 * Implicit authorization. 
 */
public class TokenResponseTypeHandler extends AbstractResponseTypeHandler implements ResponseTypeHandler {
    
    protected @Inject OAuth2ServerConfig     config;
    protected @Inject OAuth2AuthzCodeManager codeManager;
    protected @Inject OAuth2TokenManager     tokenManager;

    @Override
    public void handleResponseType(Request request, Response response, OAuth2Authentication authc) throws Throwable {
        if(!config.isImplicitGrantEnabled()) {
            OAuth2Errors.redirectUnsupportedResponseType(response, authc.getRedirectUri(), null);
            return;
        }

        //Create a new access token.
        OAuth2AccessToken at = tokenManager.createAccessToken(authc);
        
        //Response
        sendAccessTokenRedirect(request, response, authc, at);
    }

    protected void sendAccessTokenRedirect(Request request, Response response, OAuth2Authentication authc, OAuth2AccessToken at) {
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
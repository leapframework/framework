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
import leap.oauth2.server.OAuth2Errors;
import leap.oauth2.server.authc.AuthzAuthentication;
import leap.oauth2.server.OAuth2AuthzServerConfig;
import leap.oauth2.server.code.AuthzCodeManager;
import leap.oauth2.server.token.AuthzAccessToken;
import leap.oauth2.server.token.AuthzTokenManager;
import leap.web.Request;
import leap.web.Response;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Implicit authorization.
 */
public class TokenResponseTypeHandler extends AbstractResponseTypeHandler implements ResponseTypeHandler {

    protected @Inject OAuth2AuthzServerConfig config;
    protected @Inject AuthzCodeManager        codeManager;
    protected @Inject AuthzTokenManager       tokenManager;

    @Override
    public void handleResponseType(Request request, Response response, AuthzAuthentication authc) throws Throwable {
        if(!config.isImplicitGrantEnabled()) {
            OAuth2Errors.redirectUnsupportedResponseType(request, response, authc.getRedirectUri(),null);
            return;
        }

        //Create a new access token.
        AuthzAccessToken at = tokenManager.createAccessToken(authc);

        //Response
        sendAccessTokenRedirect(request, response, authc, at);
    }

    protected void sendAccessTokenRedirect(Request request, Response response, AuthzAuthentication authc, AuthzAccessToken at) {
        Map<String,String> qs=new HashMap<>();

    	qs.put("access_token",at.getToken());
    	qs.put("token_type",at.getTokenType());
    	qs.put("expires_in", Objects.toString(at.getExpiresInFormNow()));

        sendSuccessRedirect(request,response,authc,qs);

    }
}
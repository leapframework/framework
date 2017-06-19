/*
 *
 *  * Copyright 2013 the original author or authors.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  
 */

package leap.oauth2.as.endpoint.authorize;

import leap.core.annotation.Inject;
import leap.oauth2.as.OAuth2AuthzServerErrorHandler;
import leap.oauth2.as.authc.AuthzAuthentication;
import leap.oauth2.as.openid.IdTokenGenerator;
import leap.oauth2.as.token.AuthzAccessToken;
import leap.oauth2.as.token.AuthzTokenManager;
import leap.web.Request;
import leap.web.Response;

import java.util.HashMap;
import java.util.Map;

/**
 * response_type=id_token token
 * @author kael.
 */
public class IdTokenTokenResponseTypeHandler extends AbstractResponseTypeHandler implements ResponseTypeHandler {
    
    protected @Inject OAuth2AuthzServerErrorHandler errorHandler;
    protected @Inject IdTokenGenerator              idTokenGenerator;
    protected @Inject AuthzTokenManager             tokenManager;
    
    @Override
    public void handleResponseType(Request request, Response response, AuthzAuthentication authc) throws Throwable {
        if(!config.isSingleLoginEnabled()) {
            errorHandler.invalidRequest(response, "Single login disabled");
            return;
        }

        String idToken = idTokenGenerator.generateIdToken(authc);
        AuthzAccessToken accessToken = tokenManager.createAccessToken(authc);

        sendCodeIdTokenRedirect(request,response,authc,accessToken,idToken);
    }
    protected void sendCodeIdTokenRedirect(Request request, Response response, AuthzAuthentication authc, AuthzAccessToken accessToken, String idToken) {
        Map<String,String> qs=new HashMap<>();
        qs.put("access_token", accessToken.getToken());
        qs.put("token_type",accessToken.getTokenType());
        qs.put("id_token", idToken);
        qs.put("expires_in",String.valueOf(accessToken.getExpiresInFormNow()));

        sendSuccessRedirect(request, response, authc, qs);
    }
}

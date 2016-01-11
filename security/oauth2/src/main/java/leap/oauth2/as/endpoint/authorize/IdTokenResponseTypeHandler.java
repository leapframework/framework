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
import leap.lang.http.QueryStringBuilder;
import leap.oauth2.as.authc.AuthzAuthentication;
import leap.oauth2.as.OAuth2AuthzServerConfig;
import leap.oauth2.as.OAuth2AuthzServerErrorHandler;
import leap.oauth2.as.openid.IdTokenGenerator;
import leap.oauth2.as.sso.AuthzSSOManager;
import leap.web.Request;
import leap.web.Response;

public class IdTokenResponseTypeHandler extends AbstractResponseTypeHandler implements ResponseTypeHandler {

    protected @Inject OAuth2AuthzServerConfig       config;
    protected @Inject OAuth2AuthzServerErrorHandler errorHandler;
    protected @Inject IdTokenGenerator              idTokenGenerator;
    protected @Inject AuthzSSOManager               ssoManager;
    
    @Override
    public void handleResponseType(Request request, Response response, AuthzAuthentication authc) throws Throwable {
        if(!config.isOpenIDConnectEnabled()) {
            errorHandler.invalidRequest(response, "Open ID Connect disabled");
            return;
        }

        //Notify sso manager.
        ssoManager.onOAuth2LoginSuccess(request, response, authc);

        //Response id token.
        String idToken = idTokenGenerator.generateIdToken(authc);
        sendIdTokenRedirect(request, response, authc, idToken);
    }
    
    protected void sendIdTokenRedirect(Request request, Response response, AuthzAuthentication authc, String idToken) {
        QueryStringBuilder qs = new QueryStringBuilder(request.getCharacterEncoding());
        
        qs.add("id_token", idToken);
        
        sendSuccessRedirect(response, authc, qs);
    }
}
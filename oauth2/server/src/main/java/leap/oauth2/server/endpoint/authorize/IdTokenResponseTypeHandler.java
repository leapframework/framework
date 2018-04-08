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

import java.util.HashMap;
import java.util.Map;

import leap.core.annotation.Inject;
import leap.oauth2.server.authc.AuthzAuthentication;
import leap.oauth2.server.OAuth2AuthzServerConfig;
import leap.oauth2.server.OAuth2AuthzServerErrorHandler;
import leap.oauth2.server.openid.IdTokenGenerator;
import leap.oauth2.server.sso.AuthzSSOManager;
import leap.web.Request;
import leap.web.Response;

public class IdTokenResponseTypeHandler extends AbstractResponseTypeHandler implements ResponseTypeHandler {

    protected @Inject OAuth2AuthzServerConfig       config;
    protected @Inject OAuth2AuthzServerErrorHandler errorHandler;
    protected @Inject IdTokenGenerator              idTokenGenerator;
    protected @Inject AuthzSSOManager               ssoManager;

    @Override
    public void handleResponseType(Request request, Response response, AuthzAuthentication authc) throws Throwable {
        if(!config.isSingleLoginEnabled()) {
            errorHandler.invalidRequest(response, "Single login disabled");
            return;
        }

        //Notify sso manager.
        ssoManager.onOAuth2LoginSuccess(request, response, authc);

        //Response id token.
        String idToken = idTokenGenerator.generateIdToken(authc);
        sendIdTokenRedirect(request, response, authc, idToken);
    }

    protected void sendIdTokenRedirect(Request request, Response response, AuthzAuthentication authc, String idToken) {
    	Map<String,String> qs=new HashMap<>();
    	qs.put("id_token", idToken);

        sendSuccessRedirect(request, response, authc, qs);
    }
}
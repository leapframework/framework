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
import leap.lang.http.QueryStringBuilder;
import leap.oauth2.server.authc.OAuth2Authentication;
import leap.oauth2.server.OAuth2ServerConfig;
import leap.oauth2.server.OAuth2ServerErrorHandler;
import leap.oauth2.server.code.OAuth2AuthzCode;
import leap.oauth2.server.code.OAuth2AuthzCodeManager;
import leap.oauth2.server.openid.IdTokenGenerator;
import leap.oauth2.server.sso.OAuth2SSOManager;
import leap.web.Request;
import leap.web.Response;

public class CodeIdTokenResponseTypeHandler extends AbstractResponseTypeHandler implements ResponseTypeHandler {

    protected @Inject OAuth2ServerConfig       config;
    protected @Inject OAuth2ServerErrorHandler errorHandler;
    protected @Inject OAuth2AuthzCodeManager   codeManager;
    protected @Inject IdTokenGenerator         idTokenGenerator;
    protected @Inject OAuth2SSOManager         ssoManager;
    
    @Override
    public void handleResponseType(Request request, Response response, OAuth2Authentication authc) throws Throwable {
        if(!config.isOpenIDConnectEnabled()) {
            errorHandler.invalidRequest(response, "Open ID Connect disabled");
            return;
        }

        //Notify sso manager.
        ssoManager.onAuthenticated(request, response, authc);

        //Create a new authorization code and id token.
        OAuth2AuthzCode code = codeManager.createAuthorizationCode(authc);
        String idToken = idTokenGenerator.generateIdToken(authc);

        //Response
        sendCodeIdTokenRedirect(request, response, authc, code, idToken);
    }
    
    protected void sendCodeIdTokenRedirect(Request request, Response response, OAuth2Authentication authc, OAuth2AuthzCode code, String idToken) {
        QueryStringBuilder qs = new QueryStringBuilder(request.getCharacterEncoding());
        
        qs.add("code", code.getCode());
        qs.add("id_token", idToken);
        
        sendSuccessRedirect(response, authc, qs);
    }

}
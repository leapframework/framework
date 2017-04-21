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

import java.util.HashMap;
import java.util.Map;

import leap.core.annotation.Inject;
import leap.oauth2.OAuth2Errors;
import leap.oauth2.as.OAuth2AuthzServerConfig;
import leap.oauth2.as.authc.AuthzAuthentication;
import leap.oauth2.as.code.AuthzCode;
import leap.oauth2.as.code.AuthzCodeManager;
import leap.web.Request;
import leap.web.Response;

public class CodeResponseTypeHandler extends AbstractResponseTypeHandler implements ResponseTypeHandler {

	protected @Inject OAuth2AuthzServerConfig config;
	protected @Inject AuthzCodeManager        codeManager;

	@Override
    public void handleResponseType(Request request, Response response, AuthzAuthentication authc) throws Throwable {
        if(!config.isAuthorizationCodeEnabled()) {
            OAuth2Errors.redirectUnsupportedResponseType(request, response, authc.getRedirectUri(),null);
            return;
        }

        //Create a new authorization code.
        AuthzCode code = codeManager.createAuthorizationCode(authc);

        //Response
        sendAuthorizationCodeRedirect(request, response, authc, code);
    }

    protected void sendAuthorizationCodeRedirect(Request request, Response response, AuthzAuthentication authc, AuthzCode code) {
    	Map<String,String> qs=new HashMap<>();
    	qs.put("code", code.getCode());


        sendSuccessRedirect(request, response, authc, qs);
    }
}
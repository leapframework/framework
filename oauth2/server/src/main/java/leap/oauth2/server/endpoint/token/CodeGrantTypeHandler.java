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
package leap.oauth2.server.endpoint.token;

import java.util.function.Consumer;

import leap.core.annotation.Inject;
import leap.lang.Strings;
import leap.oauth2.server.OAuth2Errors;
import leap.oauth2.server.OAuth2Params;
import leap.oauth2.server.authc.AuthzAuthentication;
import leap.oauth2.server.authc.AuthzAuthenticationManager;
import leap.oauth2.server.client.AuthzClient;
import leap.oauth2.server.client.AuthzClientCredentials;
import leap.oauth2.server.code.AuthzCode;
import leap.oauth2.server.code.AuthzCodeManager;
import leap.oauth2.server.token.AuthzAccessToken;
import leap.oauth2.server.token.AuthzTokenManager;
import leap.web.Request;
import leap.web.Response;
import leap.web.security.SecurityConfig;

import static leap.oauth2.server.Oauth2MessageKey.*;

/**
 * grant_type=authorization_code
 */
public class CodeGrantTypeHandler extends AbstractGrantTypeHandler implements GrantTypeHandler {

    protected @Inject SecurityConfig    sc;
    protected @Inject AuthzCodeManager  codeManager;
    protected @Inject AuthzTokenManager tokenManager;
    @Inject
	private  AuthzAuthenticationManager authzAuthenticationManager;

	@Override
	public void handleRequest(Request request, Response response, OAuth2Params params, Consumer<AuthzAccessToken> callback) throws Throwable {
		if(!config.isAuthorizationCodeEnabled()) {
			handleError(request,response,params,
					getOauth2Error(key -> OAuth2Errors.unsupportedGrantTypeError(request,key,null),ERROR_UNSUPPORTED_GRANT_TYPE_TYPE,"authorization_code"));
			return;
		}

		String code = params.getCode();
		if(Strings.isEmpty(code)) {
			handleError(request,response,params,
					getOauth2Error(key -> OAuth2Errors.invalidRequestError(request,key,"authorization code required"),INVALID_REQUEST_AUTHORIZATION_CODE_REQUIRED));
			return;
		}

		AuthzClientCredentials credentials=extractClientCredentials(request, response, params);
		
		if(credentials == null){
			return;
		}
		
		AuthzClient client = validateClientSecret(request, response, credentials);
		if(null == client) {
		    return;
		}

        if(!client.isAllowAuthorizationCode()) {
			handleError(request,response,params,
					getOauth2Error(key -> OAuth2Errors.invalidGrantError(request,key,"authorization code not allow"),
							ERROR_INVALID_GRANT_AUTHORIZATION_CODE_NOT_ALLOW,client.getId()));
            return;
        }

        AuthzCode authzCode = codeManager.consumeAuthorizationCode(code);
        if (null == authzCode) {
			handleError(request,response,params,
					getOauth2Error(key -> OAuth2Errors.invalidGrantError(request,key,"invalid authorization code"),
							ERROR_INVALID_GRANT_INVALID_AUTHORIZATION_CODE,authzCode));
            return;
        }

        if(authzCode.isExpired()) {
            codeManager.removeAuthorizationCode(authzCode);
			handleError(request,response,params,
					getOauth2Error(key -> OAuth2Errors.invalidGrantError(request,key,"authorization code expired"),ERROR_INVALID_GRANT_AUTHORIZATION_CODE_EXPIRED,authzCode));
            return;
        }

        AuthzAuthentication authc = authzAuthenticationManager.createAuthzAuthentication(params, client,authzCode);
		if(null == authc) {
			handleError(request,response,params,
					getOauth2Error(key -> OAuth2Errors.invalidGrantError(request,key,"user id '" + authzCode.getUserId() + "' not found"),ERROR_INVALID_GRANT_USER_NOT_FOUND,authzCode.getUserId()));
            return;
		}
		//Create access token.
		callback.accept(tokenManager.createAccessToken(authc));
	}

}
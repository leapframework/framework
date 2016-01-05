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
import leap.oauth2.OAuth2Errors;
import leap.oauth2.OAuth2Params;
import leap.oauth2.server.authc.OAuth2Authentication;
import leap.oauth2.server.authc.SimpleOAuth2Authentication;
import leap.oauth2.server.client.OAuth2Client;
import leap.oauth2.server.code.OAuth2AuthzCode;
import leap.oauth2.server.code.OAuth2AuthzCodeManager;
import leap.oauth2.server.token.OAuth2AccessToken;
import leap.oauth2.server.token.OAuth2TokenManager;
import leap.web.Request;
import leap.web.Response;
import leap.web.security.SecurityConfig;
import leap.web.security.user.UserDetails;
import leap.web.security.user.UserStore;

/**
 * grant_type=authorization_code
 */
public class CodeGrantTypeHandler extends AbstractGrantTypeHandler implements GrantTypeHandler {
	
    protected @Inject SecurityConfig         sc;
    protected @Inject OAuth2AuthzCodeManager codeManager;
    protected @Inject OAuth2TokenManager     tokenManager;

	@Override
	public void handleRequest(Request request, Response response, OAuth2Params params, Consumer<OAuth2AccessToken> callback) throws Throwable {
		if(!config.isAuthorizationCodeEnabled()) {
			OAuth2Errors.unsupportedGrantType(response,null);
			return;
		}
		
		String code = params.getCode();
		if(Strings.isEmpty(code)) {
			OAuth2Errors.invalidRequest(response, "authorization code required");
			return;
		}
		
		OAuth2Client client = validateClientSecret(request, response, params);
		if(null == client) {
		    return;
		}
		
        if(!client.isAllowAuthorizationCode()) {
            OAuth2Errors.invalidGrant(response, "authorization code not allow");
            return;
        }
        
        OAuth2AuthzCode authzCode = codeManager.consumeAuthorizationCode(code);
        if (null == authzCode) {
            OAuth2Errors.invalidGrant(response, "invalid authorization code");
            return;
        }
        
        if(authzCode.isExpired()) {
            codeManager.removeAuthorizationCode(authzCode);
            OAuth2Errors.invalidGrant(response, "authorization code expired");
            return;
        }
		
		//Load user details.
		UserStore us = sc.getUserStore();
		UserDetails userDetails = us.findUserDetailsByIdString(authzCode.getUserId());
		if(null == userDetails) {
            OAuth2Errors.invalidGrant(response, "user id '" + authzCode.getUserId() + "' not found");
            return;
		}
		
		//Create authentication.
		OAuth2Authentication authc = new SimpleOAuth2Authentication(params, client, userDetails);

		//Create access token.
		callback.accept(tokenManager.createAccessToken(authc));
	}

}
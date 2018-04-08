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

import leap.core.annotation.Inject;
import leap.oauth2.server.OAuth2Errors;
import leap.oauth2.server.OAuth2Params;
import leap.oauth2.server.OAuth2AuthzServerConfig;
import leap.oauth2.server.authc.SimpleAuthzAuthentication;
import leap.oauth2.server.client.AuthzClient;
import leap.oauth2.server.client.AuthzClientCredentials;
import leap.oauth2.server.token.AuthzAccessToken;
import leap.oauth2.server.token.AuthzTokenManager;
import leap.web.Request;
import leap.web.Response;

import java.util.function.Consumer;

import static leap.oauth2.server.Oauth2MessageKey.ERROR_UNSUPPORTED_GRANT_TYPE_TYPE;

/**
 * grant_type=client_credentials
 */
public class ClientCredentialsGrantTypeHandler extends AbstractGrantTypeHandler {
	
	protected @Inject OAuth2AuthzServerConfig config;
	protected @Inject AuthzTokenManager       tokenManager;
	
	@Override
	public void handleRequest(Request request, Response response, OAuth2Params params, Consumer<AuthzAccessToken> callback) throws Throwable {
		if(!config.isClientCredentialsEnabled()) {
			handleError(request,response,params,
					getOauth2Error(key -> OAuth2Errors.invalidRequestError(request,key,null),ERROR_UNSUPPORTED_GRANT_TYPE_TYPE,"client_credentials"));
			return;
		}
		
		AuthzClientCredentials credentials = extractClientCredentials(request,response,params);
		
		if(credentials == null){
			return;
		}
		
		//Authenticate client.
		AuthzClient client = validateClientSecret(request,response,credentials);
		if(client == null) {
			return;
		}
		
		//Generate token.
		callback.accept(tokenManager.createAccessToken(new SimpleAuthzAuthentication(params, client)));
	}

}
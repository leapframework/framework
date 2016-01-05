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
import leap.lang.Result;
import leap.oauth2.OAuth2Params;
import leap.oauth2.OAuth2Errors;
import leap.oauth2.server.OAuth2ServerConfig;
import leap.oauth2.server.authc.SimpleOAuth2Authentication;
import leap.oauth2.server.client.OAuth2Client;
import leap.oauth2.server.client.OAuth2ClientManager;
import leap.oauth2.server.token.OAuth2AccessToken;
import leap.oauth2.server.token.OAuth2TokenManager;
import leap.web.Request;
import leap.web.Response;

/**
 * grant_type=client_credentials
 */
public class ClientGrantTypeHandler implements GrantTypeHandler {
	
	protected @Inject OAuth2ServerConfig  config;
	protected @Inject OAuth2ClientManager clientManager;
	protected @Inject OAuth2TokenManager  tokenManager;
	
	@Override
	public void handleRequest(Request request, Response response, OAuth2Params params, Consumer<OAuth2AccessToken> callback) throws Throwable {
		if(!config.isClientCredentialsEnabled()) {
			OAuth2Errors.unsupportedGrantType(response,null);
			return;
		}
		
		//Authenticate client.
		Result<OAuth2Client> client = clientManager.authenticate(params);
		if(!client.isPresent()) {
			OAuth2Errors.invalidClient(response, client.isError() ? client.error().getMessage() : "invalid client credentials");
			return;
		}
		
		//Generate token.
		callback.accept(tokenManager.createAccessToken(new SimpleOAuth2Authentication(params, client.get())));
	}

}
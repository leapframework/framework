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
import leap.core.security.UserPrincipal;
import leap.lang.Strings;
import leap.oauth2.OAuth2Params;
import leap.oauth2.OAuth2Errors;
import leap.oauth2.server.authc.OAuth2Authentication;
import leap.oauth2.server.OAuth2ServerConfig;
import leap.oauth2.server.authc.SimpleOAuth2Authentication;
import leap.oauth2.server.client.OAuth2Client;
import leap.oauth2.server.client.OAuth2ClientManager;
import leap.oauth2.server.token.OAuth2AccessToken;
import leap.oauth2.server.token.OAuth2RefreshToken;
import leap.oauth2.server.token.OAuth2TokenManager;
import leap.web.Request;
import leap.web.Response;
import leap.web.security.SecurityConfig;
import leap.web.security.authc.AuthenticationManager;
import leap.web.security.user.SimpleUserDetailsPrincipal;
import leap.web.security.user.UserDetails;
import leap.web.security.user.UserManager;
import leap.web.security.user.UserStore;

/**
 * grant_type=refresh_token
 */
public class RefreshTokenGrantTypeHandler implements GrantTypeHandler {
	
	protected @Inject OAuth2ServerConfig    config;
	protected @Inject OAuth2TokenManager    tokenManager;
	protected @Inject AuthenticationManager authcManager;
	protected @Inject OAuth2ClientManager   clientManager;
	protected @Inject SecurityConfig        sc;
	protected @Inject UserManager           um;
	
	@Override
	public void handleRequest(Request request, Response response, OAuth2Params params, Consumer<OAuth2AccessToken> callback) {
		if(!config.isRefreshTokenEnabled()) {
			OAuth2Errors.unsupportedGrantType(response,null);
			return;
		}
		
		String refreshToken = params.getRefreshToken();
		if(Strings.isEmpty(refreshToken)) {
			OAuth2Errors.invalidRequest(response, "refresh_token required");
			return;
		}
		
		//Load token.
		OAuth2RefreshToken token = tokenManager.loadRefreshToken(refreshToken);
		if(null == token) {
			OAuth2Errors.invalidGrant(response, "invalid refresh token");
			return;
		}
		
		//Check expired?
		if(token.isExpired()) {
			tokenManager.removeRefreshToken(token);
			OAuth2Errors.invalidGrant(response, "refresh token expired");
			return;
		}
		
		//Authenticate user.
		UserPrincipal user = null;
		if(!token.isClientOnly()) {
			//Authenticate user.
			UserStore   us = sc.getUserStore();
		    UserDetails ud = us.findUserDetailsByIdString(token.getUserId());
			if(null == ud || !ud.isEnabled()) {
				tokenManager.removeRefreshToken(token);
				OAuth2Errors.invalidGrant(response, "invalid user");
				return;
			}
			user = new SimpleUserDetailsPrincipal(ud);
		}
		
		//Authenticate client.
		OAuth2Client client = null;
		if(!Strings.isEmpty(token.getClientId())) {
			client = clientManager.loadClientById(token.getClientId());
			if(null == client || !client.isEnabled()) {
				tokenManager.removeRefreshToken(token);
				OAuth2Errors.invalidGrant(response, "invalid client");
				return;
			}
		}
		
		OAuth2Authentication oauthAuthc = new SimpleOAuth2Authentication(params, client, um.getUserDetails(user));
		
		//Generates a new token.
		callback.accept(tokenManager.createAccessToken(oauthAuthc, token));
	}

}
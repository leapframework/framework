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
package leap.oauth2.as.endpoint.token;

import java.util.function.Consumer;

import leap.core.annotation.Inject;
import leap.core.security.UserPrincipal;
import leap.lang.Strings;
import leap.oauth2.OAuth2Params;
import leap.oauth2.OAuth2Errors;
import leap.oauth2.as.authc.AuthzAuthentication;
import leap.oauth2.as.OAuth2AuthzServerConfig;
import leap.oauth2.as.authc.SimpleAuthzAuthentication;
import leap.oauth2.as.client.AuthzClient;
import leap.oauth2.as.client.AuthzClientManager;
import leap.oauth2.as.token.AuthzAccessToken;
import leap.oauth2.as.token.AuthzRefreshToken;
import leap.oauth2.as.token.AuthzTokenManager;
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
	
	protected @Inject OAuth2AuthzServerConfig config;
	protected @Inject AuthzTokenManager       tokenManager;
	protected @Inject AuthenticationManager   authcManager;
	protected @Inject AuthzClientManager      clientManager;
	protected @Inject SecurityConfig          sc;
	protected @Inject UserManager             um;
	
	@Override
	public void handleRequest(Request request, Response response, OAuth2Params params, Consumer<AuthzAccessToken> callback) {
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
		AuthzRefreshToken token = tokenManager.loadRefreshToken(refreshToken);
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
		AuthzClient client = null;
		if(!Strings.isEmpty(token.getClientId())) {
			client = clientManager.loadClientById(token.getClientId());
			if(null == client || !client.isEnabled()) {
				tokenManager.removeRefreshToken(token);
				OAuth2Errors.invalidGrant(response, "invalid client");
				return;
			}
		}
		
		AuthzAuthentication oauthAuthc = new SimpleAuthzAuthentication(params, client, um.getUserDetails(user));
		
		//Generates a new token.
		callback.accept(tokenManager.createAccessToken(oauthAuthc, token));
	}

}
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
import leap.core.security.UserPrincipal;
import leap.lang.Strings;
import leap.lang.http.HTTP;
import leap.oauth2.server.OAuth2Errors;
import leap.oauth2.server.OAuth2Params;
import leap.oauth2.server.OAuth2AuthzServerConfig;
import leap.oauth2.server.authc.AuthzAuthentication;
import leap.oauth2.server.authc.SimpleAuthzAuthentication;
import leap.oauth2.server.client.AuthzClient;
import leap.oauth2.server.client.AuthzClientAuthenticationContext;
import leap.oauth2.server.client.AuthzClientCredentials;
import leap.oauth2.server.client.AuthzClientManager;
import leap.oauth2.server.client.DefaultAuthzClientAuthenticationContext;
import leap.oauth2.server.token.AuthzAccessToken;
import leap.oauth2.server.token.AuthzRefreshToken;
import leap.oauth2.server.token.AuthzTokenManager;
import leap.web.Request;
import leap.web.Response;
import leap.web.security.SecurityConfig;
import leap.web.security.authc.AuthenticationManager;
import leap.web.security.user.UserDetails;
import leap.web.security.user.UserManager;
import leap.web.security.user.UserStore;

import java.util.function.Consumer;

import static leap.oauth2.server.OAuth2Errors.ERROR_TOKEN_EXPIRES;
import static leap.oauth2.server.Oauth2MessageKey.*;

/**
 * grant_type=refresh_token
 * 
 * The authorization server MUST:
 * <ul>
 *     <li>
 *         require client authentication for confidential clients or for any client that was issued client 
 *         credentials (or with other authentication requirements)
 *     </li>
 *     <li>
 *         authenticate the client if client authentication is included and ensure that the refresh token was
 *         issued to the authenticated client.
 *     </li>
 *     <li>
 *         validate the refresh token
 *     </li>
 * </ul>
 * 
 * @see <a href="https://tools.ietf.org/html/rfc6749#page-47">OAuth2 page 47</a>
 */
public class RefreshTokenGrantTypeHandler extends AbstractGrantTypeHandler implements GrantTypeHandler {
	
	protected @Inject OAuth2AuthzServerConfig config;
	protected @Inject AuthzTokenManager       tokenManager;
	protected @Inject AuthenticationManager   authcManager;
	protected @Inject AuthzClientManager      clientManager;
	protected @Inject SecurityConfig          sc;
	protected @Inject UserManager             um;
	
	@Override
	public void handleRequest(Request request, Response response, OAuth2Params params, Consumer<AuthzAccessToken> callback) {
		String refreshToken = params.getRefreshToken();
		if(Strings.isEmpty(refreshToken)) {
			handleError(request,response,params,
					getOauth2Error(key -> OAuth2Errors.invalidRequestError(request,key,"refresh_token required"),INVALID_REQUEST_REFRESH_TOKEN_REQUIRED));
			return;
		}
		
		//Load token.
		AuthzRefreshToken token = tokenManager.loadRefreshToken(refreshToken);
		if(null == token) {
			handleError(request,response,params,
					getOauth2Error(key -> OAuth2Errors.invalidTokenError(request,key,"invalid refresh token"),ERROR_INVALID_GRANT_INVALID_REFRESH_TOKEN,refreshToken));
			return;
		}
		//Check expired?
		if(token.isExpired()) {
			tokenManager.removeRefreshToken(token);
			
			handleError(request,response,params,
					getOauth2Error(key -> OAuth2Errors.oauth2Error(request, HTTP.SC_UNAUTHORIZED, ERROR_TOKEN_EXPIRES, key,"refresh token expired"),
							ERROR_INVALID_GRANT_REFRESH_TOKEN_EXPIRED,refreshToken));
			return;
		}
		
		//Authenticate user.
		UserPrincipal user = null;
		if(!token.isClientOnly()) {
			//Authenticate user.
			UserStore   us = sc.getUserStore();
		    UserDetails ud = us.loadUserDetailsByIdString(token.getUserId());
			if(null == ud || !ud.isEnabled()) {
				tokenManager.removeRefreshToken(token);
				handleError(request,response,params,
						getOauth2Error(key -> OAuth2Errors.invalidTokenError(request,key,"invalid user"),INVALID_REQUEST_INVALID_USERNAME,token.getUserId()));
				return;
			}
			user = ud;
		}
		
		//Authenticate client.
		AuthzClient client = null;
		if(!Strings.isEmpty(token.getClientId())) {
			// basic authenticate client
			client = authcClient(request,response,params);
			if(null == client){
				return;
			}
			String clientId =client.getId();
			if(!Strings.equals(token.getClientId(),client.getId())){
				handleError(request,response,params,
						getOauth2Error(key -> OAuth2Errors.invalidTokenError(request,key,"this refresh token is not for client "+clientId),ERROR_INVALID_GRANT_INVALID_REFRESH_TOKEN,refreshToken));
				return;
			}
			
			if(!client.isEnabled()) {
				tokenManager.removeRefreshToken(token);
				handleError(request,response,params,
						getOauth2Error(key -> OAuth2Errors.invalidClientError(request,key,"invalid client"),INVALID_REQUEST_INVALID_CLIENT,token.getClientId()));
				return;
			}
		}
		UserDetails ud = null;
		if(null != user){
			ud = um.getUserDetails(user);
		}
		AuthzAuthentication oauthAuthc = new SimpleAuthzAuthentication(params, client, ud);
		
		//Generates a new token.
		callback.accept(tokenManager.createAccessToken(oauthAuthc, token));
	}

	protected AuthzClient authcClient(Request request, Response response, OAuth2Params params){
		AuthzClientCredentials credentials = extractClientCredentials(request,response,params);
		if(null == credentials){
			return null;
		}
		AuthzClient client;
		try {
			client = validateClientSecret(request,response,credentials);
			if(null == client){
				return null;
			}
		} catch (Throwable throwable) {
			OAuth2Errors.serverError(request,response,null, throwable.getMessage());
			return null;
		}
		return client;
	}
	
}
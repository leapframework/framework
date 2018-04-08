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
import leap.core.validation.Validation;
import leap.lang.NamedError;
import leap.lang.Strings;
import leap.oauth2.server.OAuth2Params;
import leap.oauth2.server.OAuth2Errors;
import leap.oauth2.server.authc.AuthzAuthentication;
import leap.oauth2.server.OAuth2AuthzServerConfig;
import leap.oauth2.server.authc.SimpleAuthzAuthentication;
import leap.oauth2.server.client.AuthzClient;
import leap.oauth2.server.client.AuthzClientCredentials;
import leap.oauth2.server.client.AuthzClientManager;
import leap.oauth2.server.client.AuthzClientValidator;
import leap.oauth2.server.token.AuthzAccessToken;
import leap.oauth2.server.token.AuthzTokenManager;
import leap.web.Request;
import leap.web.Response;
import leap.core.security.Authentication;
import leap.web.security.authc.AuthenticationManager;
import leap.web.security.authc.credentials.DefaultAuthenticateCredentialsContext;
import leap.web.security.user.SimpleUsernamePasswordCredentials;
import leap.web.security.user.UserManager;
import leap.web.security.user.UsernamePasswordCredentials;

import static leap.oauth2.server.Oauth2MessageKey.*;

/**
 * grant_type=password
 */
public class PasswordGrantTypeHandler extends AbstractGrantTypeHandler implements GrantTypeHandler {
	
    protected @Inject OAuth2AuthzServerConfig config;
    protected @Inject AuthzTokenManager       tokenManager;
    protected @Inject AuthzClientManager      clientManager;
    protected @Inject AuthenticationManager   authenticationManager;
    protected @Inject AuthzClientValidator    clientValidator;
    protected @Inject UserManager             userManager;
	

	@Override
    public void handleRequest(Request request, Response response, OAuth2Params params, Consumer<AuthzAccessToken> callback) throws Throwable{
		if(!config.isPasswordCredentialsEnabled()) {
			handleError(request,response,params,
					getOauth2Error(key -> OAuth2Errors.unsupportedGrantTypeError(request,key,null),ERROR_UNSUPPORTED_GRANT_TYPE_TYPE,"password"));
			return;
		}
		
		String username = params.getUsername();
		String password = params.getPassword();
		if(Strings.isEmpty(username)) {
			handleError(request,response,params,
					getOauth2Error(key -> OAuth2Errors.invalidRequestError(request,key,"username are requried."),INVALID_REQUEST_USERNAME_REQUIRED));
			return;
		}
		if(Strings.isEmpty(password)){
			handleError(request,response,params,
					getOauth2Error(key -> OAuth2Errors.invalidRequestError(request,key,"password are requried."),INVALID_REQUEST_PASSWORD_REQUIRED));
			return;
		}
		
		
		DefaultAuthenticateCredentialsContext context = new DefaultAuthenticateCredentialsContext(request.getValidation());

		SimpleUsernamePasswordCredentials credentials = new SimpleUsernamePasswordCredentials(username,password);
		
		//Authenticate user.
		Authentication authc = authenticationManager.authenticate(context, credentials);
		if(null == authc) {
			Validation validation = context.validation();
			String errorKey = INVALID_REQUEST_INVALID_USERNAME;
			if(context.validation().hasErrors()){
				NamedError error = validation.errors().first();
				if(Strings.equals(error.getName(),UsernamePasswordCredentials.PASSWORD)){
					errorKey = INVALID_REQUEST_INVALID_PASSWORD;
				}
			}
			handleError(request,response,params,
					getOauth2Error(key -> OAuth2Errors.invalidGrantError(request,key,"invalid username or password"),errorKey,credentials.getUsername()));
			return;
		}

		//Validates the client.
		AuthzClientCredentials clientCredentials = extractClientCredentials(request,response,params);
		if(clientCredentials == null){
			return;
		}
		AuthzClient client = validateClientSecret(request,response,clientCredentials);
		if(client == null){
			return;
		}
		
		AuthzAuthentication oauthAuthc = new SimpleAuthzAuthentication(params, client, userManager.getUserDetails(authc.getUser()));
		
		//Generate token.
		callback.accept(tokenManager.createAccessToken(oauthAuthc));
    }
}
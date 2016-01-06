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
import leap.lang.Strings;
import leap.oauth2.OAuth2Params;
import leap.oauth2.OAuth2Errors;
import leap.oauth2.as.authc.AuthzAuthentication;
import leap.oauth2.as.OAuth2AuthzServerConfig;
import leap.oauth2.as.authc.SimpleAuthzAuthentication;
import leap.oauth2.as.client.AuthzClient;
import leap.oauth2.as.client.AuthzClientManager;
import leap.oauth2.as.client.AuthzClientValidator;
import leap.oauth2.as.token.AuthzAccessToken;
import leap.oauth2.as.token.AuthzTokenManager;
import leap.web.Request;
import leap.web.Response;
import leap.web.security.authc.Authentication;
import leap.web.security.authc.AuthenticationManager;
import leap.web.security.user.UserManager;

/**
 * grant_type=password
 */
public class PasswordGrantTypeHandler implements GrantTypeHandler {
	
    protected @Inject OAuth2AuthzServerConfig config;
    protected @Inject AuthzTokenManager       tokenManager;
    protected @Inject AuthzClientManager      clientManager;
    protected @Inject AuthenticationManager   authenticationManager;
    protected @Inject AuthzClientValidator    clientValidator;
    protected @Inject UserManager             userManager;
	
	protected boolean validateClient = true;
	
	public boolean isValidateClient() {
		return validateClient;
	}

	public void setValidateClient(boolean validate) {
		this.validateClient = validate;
	}

	@Override
    public void handleRequest(Request request, Response response, OAuth2Params params, Consumer<AuthzAccessToken> callback) throws Throwable{
		if(!config.isPasswordCredentialsEnabled()) {
			OAuth2Errors.unsupportedGrantType(response,null);
			return;
		}
		
		String username = params.getUsername();
		String password = params.getPassword();
		if(Strings.isEmpty(username) || Strings.isEmpty(password)) {
			OAuth2Errors.invalidRequest(response, "username and password are requried.");
			return;
		}
		
		//Authenticate user.
		Authentication authc = authenticationManager.authenticate(request.getValidation(), params);
		if(null == authc) {
			OAuth2Errors.invalidGrant(response, "invalid username or password");
			return;
		}

		//Validates the client.
		AuthzClient client = null;
		if(validateClient) {
            client = clientValidator.validatePasswordGrantRequest(request, response, params);
            if (null == client) {
                return;
            }
		}
		
		AuthzAuthentication oauthAuthc = new SimpleAuthzAuthentication(params, client, userManager.getUserDetails(authc.getUserPrincipal()));
		
		//Generate token.
		callback.accept(tokenManager.createAccessToken(oauthAuthc));
    }
}
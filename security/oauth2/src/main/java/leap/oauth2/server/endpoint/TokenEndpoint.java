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
package leap.oauth2.server.endpoint;

import java.util.Map.Entry;

import leap.core.annotation.Inject;
import leap.lang.Strings;
import leap.lang.json.JsonWriter;
import leap.oauth2.OAuth2Params;
import leap.oauth2.OAuth2ResponseException;
import leap.oauth2.RequestOAuth2Params;
import leap.oauth2.server.OAuth2ServerErrorHandler;
import leap.oauth2.server.endpoint.token.GrantTypeHandler;
import leap.oauth2.server.token.OAuth2AccessToken;
import leap.web.App;
import leap.web.Handler;
import leap.web.Request;
import leap.web.Response;
import leap.web.exception.ResponseException;
import leap.web.route.Routes;

public class TokenEndpoint extends AbstractAuthzEndpoint implements Handler {
    
    protected @Inject OAuth2ServerErrorHandler errorHandler;
	
	@Override
    public void startEndpoint(App app, Routes routes) {
		if(config.isEnabled() && config.isTokenEndpointEnabled()) {
			sc.ignore(config.getTokenEndpointPath());
			
			routes.create()
				  .post(config.getTokenEndpointPath(), this)
				  .setCsrfEnabled(false)
				  .apply();
			
		}

	}

	@Override
    public void handle(Request request, Response response) throws Throwable {
		
		String grantType = request.getParameter("grant_type");
		if(Strings.isEmpty(grantType)) {
			errorHandler.invalidRequest(response, "'grant_type' required");
			return;
		}
		
		GrantTypeHandler handler = factory.tryGetBean(GrantTypeHandler.class, grantType);
		if(null == handler) {
			errorHandler.invalidRequest(response, "Unsupported grant type");
			return;
		}
		
		try{
			OAuth2Params params = new RequestOAuth2Params(request, grantType);
			
			handler.handleRequest(request, response, params,
									(token) -> handleGrantedToken(request, response, params, handler, token));
		}catch(OAuth2ResponseException e) {
			errorHandler.response(response, e.getStatus(), e.getError(), e.getMessage());
		}catch(ResponseException e) {
			throw e;
		}catch(Throwable e) {
			errorHandler.serverError(response, e.getMessage(), e);
		}
    }
	
	protected void handleGrantedToken(Request request, Response response, OAuth2Params params, GrantTypeHandler handler, OAuth2AccessToken token) {
		if(null == token) {
			errorHandler.serverError(response, "Access token did not returned by granter '" + handler.getClass().getSimpleName() + "'");
			return;
		}
		
		if(!handler.handleSuccess(request, response, params, token)) {
			handleDefaultSuccess(request, response, token);	
		}
	}
	
	protected void handleDefaultSuccess(Request request, Response response, OAuth2AccessToken token) {
		JsonWriter w = response.getJsonWriter();
		w.startObject()
		 .property("access_token", token.getToken())
		 .property("token_type", "bearer"); //TODO : supports other token type.
		
		int expiresIn = token.getExpiresIn() > 0 ? token.getExpiresIn() : config.getDefaultAccessTokenExpires();
		
		w.property("expires_in", expiresIn);
		
		if(null != token.getRefreshToken()) {
			w.property("refresh_token", token.getRefreshToken());
		}
		
		if(null != token.getExtendedParameters()) {
			for(Entry<String, Object> entry : token.getExtendedParameters().entrySet()) {
				w.property(entry.getKey(), entry.getValue());
			}
		}
		
		w.endObject();
	}

}
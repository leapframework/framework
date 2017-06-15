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
package leap.oauth2.as.endpoint;

import java.util.Map.Entry;

import leap.core.annotation.Inject;
import leap.lang.Strings;
import leap.lang.http.ContentTypes;
import leap.lang.json.JsonWriter;
import leap.oauth2.OAuth2Params;
import leap.oauth2.OAuth2ResponseException;
import leap.oauth2.RequestOAuth2Params;
import leap.oauth2.as.OAuth2AuthzServerErrorHandler;
import leap.oauth2.as.endpoint.token.DefaultGrantTokenManager;
import leap.oauth2.as.endpoint.token.GrantTypeHandler;
import leap.oauth2.as.endpoint.token.GrantTokenInterceptor;
import leap.oauth2.as.token.AuthzAccessToken;
import leap.oauth2.as.token.TokenAuthzProcessor;
import leap.web.App;
import leap.web.Handler;
import leap.web.Request;
import leap.web.Response;
import leap.web.exception.ResponseException;
import leap.web.route.Routes;

public class TokenEndpoint extends AbstractAuthzEndpoint implements Handler {

    protected @Inject OAuth2AuthzServerErrorHandler errorHandler;

    protected @Inject TokenAuthzProcessor[] processors;

    protected @Inject DefaultGrantTokenManager grantTokenManager;

	@Override
    public void startEndpoint(App app, Routes routes) {
		if(config.isEnabled()) {
			sc.ignore(config.getTokenEndpointPath());

			routes.create()
				  .post(config.getTokenEndpointPath(), this)
				  .disableCsrf().enableCors()
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

		GrantTypeHandler handler = grantTokenManager.getHandler(grantType);
		if(null == handler) {
			errorHandler.invalidRequest(response, "Unsupported grant type");
			return;
		}

		try{
			OAuth2Params params = new RequestOAuth2Params(request, grantType);

			AuthzAccessToken token = grantTokenManager.grantAccessToken(request,response,params,handler);
			if(token!=null){
				handleGrantedToken(request, response, params, handler, token);
			}

		}catch(OAuth2ResponseException e) {
			errorHandler.response(response, e.getStatus(), e.getError(), e.getMessage());
		}catch(ResponseException e) {
			throw e;
		}catch(Throwable e) {
			errorHandler.serverError(response, e.getMessage(), e);
		}
    }

	protected void handleGrantedToken(Request request, Response response, OAuth2Params params, GrantTypeHandler handler, AuthzAccessToken token) {
		if(null == token) {
			errorHandler.serverError(response, "Access token did not returned by granter '" + handler.getClass().getSimpleName() + "'");
			return;
		}

		if(processors != null){
			for (TokenAuthzProcessor processor : processors){
				if(!processor.process(request,response,params,handler,token)){
					return;
				}
			}
		}

		if(!handler.handleSuccess(request, response, params, token)) {
			handleDefaultSuccess(request, response, token);
		}
	}

	protected void handleDefaultSuccess(Request request, Response response, AuthzAccessToken token) {
		response.setContentType(ContentTypes.APPLICATION_JSON_UTF8);
		JsonWriter w = response.getJsonWriter();
		w.startObject()
		 .property("access_token", token.getToken())
		 .property("token_type", "bearer"); //TODO : supports other token type.

		int expiresIn = token.getExpiresInFormNow() > 0 ? token.getExpiresInFormNow() : config.getDefaultAccessTokenExpires();

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
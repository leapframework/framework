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
package leap.oauth2.server.endpoint.authorize;

import java.util.Map;

import leap.core.annotation.Inject;
import leap.core.validation.Validation;
import leap.lang.Result;
import leap.lang.Strings;
import leap.lang.http.QueryStringBuilder;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.net.Urls;
import leap.oauth2.server.OAuth2Errors;
import leap.oauth2.server.OAuth2Params;
import leap.oauth2.server.authc.AuthzAuthentication;
import leap.oauth2.server.OAuth2AuthzServerConfig;
import leap.oauth2.server.client.AuthzClient;
import leap.oauth2.server.client.AuthzClientManager;
import leap.web.Request;
import leap.web.Response;

public abstract class AbstractResponseTypeHandler implements ResponseTypeHandler {
    private static final Log log = LogFactory.get(AbstractResponseTypeHandler.class);

    protected @Inject OAuth2AuthzServerConfig config;
    protected @Inject AuthzClientManager      clientManager;
    protected @Inject Oauth2RedirectHandler[] handlers;

    @Override
    public Result<AuthzClient> validateRequest(Request request, Response response, OAuth2Params params) throws Throwable {
        Validation validation = request.getValidation();

        String clientId = params.getClientId();
        if(Strings.isEmpty(clientId)) {
            log.debug("error : client_id required");
            validation.addError(OAuth2Errors.ERROR_INVALID_REQUEST, "client_id required");
            request.forwardToView(config.getErrorView());
            return Result.intercepted();
        }

        String redirectUri = params.getRedirectUri();
        if(Strings.isEmpty(redirectUri)) {
            log.debug("error : redirect_uri required");
            validation.addError(OAuth2Errors.ERROR_INVALID_REQUEST, "redirect_uri required");
            request.forwardToView(config.getErrorView());
            return Result.intercepted();
        }

        AuthzClient client = clientManager.loadClientById(clientId);
        if(null == client) {
            log.debug("error : client_id {} not found", clientId);
            validation.addError(OAuth2Errors.ERROR_INVALID_REQUEST, "invalid client_id");
            request.forwardToView(config.getErrorView());
            return Result.intercepted();
        }

        if(!client.isEnabled()) {
            log.debug("error : client '{}' disabled", clientId);
            validation.addError(OAuth2Errors.ERROR_INVALID_REQUEST, "client disabled");
            request.forwardToView(config.getErrorView());
            return Result.intercepted();
        }

        if(!client.acceptsRedirectUri(redirectUri)) {
            log.debug("error : mismatch redirect_uri '{}' of client '{}'", redirectUri, clientId);
            validation.addError(OAuth2Errors.ERROR_INVALID_REQUEST, "invalid redirect_uri");
            request.forwardToView(config.getErrorView());
            return Result.intercepted();
        }

        return Result.of(client);
    }

    protected void sendSuccessRedirect(Request request, Response response, AuthzAuthentication authc, Map<String,String> qs) {
        OAuth2Params params = authc.getParams();

        String state = params.getState();
        if(!Strings.isEmpty(state)) {
            qs.put("state", state);
        }

        for(Oauth2RedirectHandler handler : handlers){
            if(!handler.onOauth2LoginSuccessRedirect(request,response,authc,qs)){
                return;
            }
        }

        QueryStringBuilder queryString =
                new QueryStringBuilder(request.getCharacterEncoding());
        for (Map.Entry<String, String> entry : qs.entrySet()) {
        	queryString.add(entry.getKey(), entry.getValue());
		}

        response.sendRedirect(Urls.appendQueryString(authc.getRedirectUri(), queryString.build()));
    }
}
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

import java.util.LinkedHashMap;
import java.util.Map;

import leap.core.BeanFactory;
import leap.core.annotation.Inject;
import leap.core.validation.Validation;
import leap.lang.Strings;
import leap.lang.net.Urls;
import leap.oauth2.OAuth2Params;
import leap.oauth2.OAuth2Errors;
import leap.oauth2.as.OAuth2AuthzServerConfig;
import leap.oauth2.as.client.AuthzClient;
import leap.oauth2.as.client.AuthzClientManager;
import leap.oauth2.as.token.AuthzAccessToken;
import leap.oauth2.as.token.AuthzTokenManager;
import leap.web.Endpoint;
import leap.web.Request;
import leap.web.Response;
import leap.web.security.SecurityConfigurator;

public abstract class AbstractAuthzEndpoint implements Endpoint {
    
    protected @Inject SecurityConfigurator    sc;
    protected @Inject OAuth2AuthzServerConfig config;
    protected @Inject AuthzClientManager      clientManager;
    protected @Inject AuthzTokenManager       tokenManager;
    protected @Inject BeanFactory             factory;

    protected AuthzClient validateClient(Request request, Response response, OAuth2Params params) throws Throwable {
        Validation validation = request.getValidation();
        
        String clientId = params.getClientId();
        if(Strings.isEmpty(clientId)) {
            validation.addError(OAuth2Errors.ERROR_INVALID_REQUEST, "client_id required");
            request.forwardToView(config.getErrorView());
            return null;
        }
        
        String redirectUri = params.getRedirectUri();
        if(Strings.isEmpty(redirectUri)) {
            validation.addError(OAuth2Errors.ERROR_INVALID_REQUEST, "redirect_uri required");
            request.forwardToView(config.getErrorView());
            return null;
        }
        
        AuthzClient client = clientManager.loadClientById(clientId);
        if(null == client) {
            validation.addError(OAuth2Errors.ERROR_INVALID_REQUEST, "invalid client_id");
            request.forwardToView(config.getErrorView());
            return null;
        }
        
        if(!client.isEnabled()) {
            validation.addError(OAuth2Errors.ERROR_INVALID_REQUEST, "client disabled");
            request.forwardToView(config.getErrorView());
            return null;
        }

        if(!client.acceptsRedirectUri(redirectUri)) {
            validation.addError(OAuth2Errors.ERROR_INVALID_REQUEST, "invalid redirect_uri");
            request.forwardToView(config.getErrorView());
            return null;         
        }
        
        return client;
    }
    
    protected void redirectToken(Request request, Response response, OAuth2Params params, AuthzAccessToken token) {
        Map<String, String> query = new LinkedHashMap<>(5);
        query.put("access_token", token.getToken());
        query.put("token_type", "bearer"); //TODO : supports other token type.
        
        if(token.getExpiresIn() > 0) {
            query.put("expires_in", String.valueOf(token.getExpiresIn()));
        }
        
        if(!Strings.isEmpty(token.getScope())) {
            query.put("scope", token.getScope());
        }
        
        if(!Strings.isEmpty(params.getState())) {
            query.put("state", params.getState());
        }
        
        String queryString = Urls.getQueryString(query);
        String redirectUrl = Urls.appendQueryString(params.getRedirectUri(), queryString);
        
        response.sendRedirect(redirectUrl);
    }
    
}

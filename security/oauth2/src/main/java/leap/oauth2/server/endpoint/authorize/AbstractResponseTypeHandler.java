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

import leap.core.annotation.Inject;
import leap.core.validation.Validation;
import leap.lang.Out;
import leap.lang.Result;
import leap.lang.Strings;
import leap.lang.http.QueryStringBuilder;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.net.Urls;
import leap.oauth2.OAuth2Errors;
import leap.oauth2.OAuth2Params;
import leap.oauth2.server.authc.OAuth2Authentication;
import leap.oauth2.server.OAuth2ServerConfig;
import leap.oauth2.server.client.OAuth2Client;
import leap.oauth2.server.client.OAuth2ClientManager;
import leap.web.Request;
import leap.web.Response;

public abstract class AbstractResponseTypeHandler implements ResponseTypeHandler {
    private static final Log log = LogFactory.get(AbstractResponseTypeHandler.class);
    
    protected @Inject OAuth2ServerConfig  config;
    protected @Inject OAuth2ClientManager clientManager;
    
    @Override
    public Result<OAuth2Client> validateRequest(Request request, Response response, OAuth2Params params) throws Throwable {
        Out<OAuth2Client> result = new Out<>();
        
        Validation validation = request.getValidation();
        
        String clientId = params.getClientId();
        if(Strings.isEmpty(clientId)) {
            log.debug("error : client_id required");
            validation.addError(OAuth2Errors.ERROR_INVALID_REQUEST, "client_id required");
            request.forwardToView(config.getErrorView());
            return result.err();
        }
        
        String redirectUri = params.getRedirectUri();
        if(Strings.isEmpty(redirectUri)) {
            log.debug("error : redirect_uri required");
            validation.addError(OAuth2Errors.ERROR_INVALID_REQUEST, "redirect_uri required");
            request.forwardToView(config.getErrorView());
            return result.err();
        }
        
        OAuth2Client client = clientManager.loadClientById(clientId);
        if(null == client) {
            log.debug("error : client_id {} not found", clientId);
            validation.addError(OAuth2Errors.ERROR_INVALID_REQUEST, "invalid client_id");
            request.forwardToView(config.getErrorView());
            return result.err();
        }
        
        if(!client.isEnabled()) {
            log.debug("error : client '{}' disabled", clientId);
            validation.addError(OAuth2Errors.ERROR_INVALID_REQUEST, "client disabled");
            request.forwardToView(config.getErrorView());
            return result.err();
        }

        if(!client.acceptsRedirectUri(redirectUri)) {
            log.debug("error : mismatch redirect_uri of client '{}'", clientId);
            validation.addError(OAuth2Errors.ERROR_INVALID_REQUEST, "invalid redirect_uri");
            request.forwardToView(config.getErrorView());
            return result.err();         
        }
        
        return result.ok(client);
    }
    
    protected void sendSuccessRedirect(Response response, OAuth2Authentication authc, QueryStringBuilder qs) {
        OAuth2Params params = authc.getParams();
        
        String state = params.getState();
        if(!Strings.isEmpty(state)) {
            qs.add("state", state);
        }
        
        response.sendRedirect(Urls.appendQueryString(authc.getRedirectUri(), qs.build()));
    }
}
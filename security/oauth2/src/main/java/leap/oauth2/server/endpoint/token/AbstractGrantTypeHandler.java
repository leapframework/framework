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
import leap.lang.Strings;
import leap.oauth2.OAuth2Errors;
import leap.oauth2.OAuth2Params;
import leap.oauth2.server.OAuth2ServerConfig;
import leap.oauth2.server.client.OAuth2Client;
import leap.oauth2.server.client.OAuth2ClientManager;
import leap.web.Request;
import leap.web.Response;

public abstract class AbstractGrantTypeHandler implements GrantTypeHandler {
    
    protected @Inject OAuth2ServerConfig  config;
    protected @Inject OAuth2ClientManager clientManager;
    
    protected OAuth2Client validateClient(Request request, Response response, OAuth2Params params) throws Throwable {
        String clientId = params.getClientId();
        if(Strings.isEmpty(clientId)) {
            OAuth2Errors.invalidRequest(response, "client_id required");
            return null;
        }
        
        String redirectUri = params.getRedirectUri();
        if(Strings.isEmpty(redirectUri)) {
            OAuth2Errors.invalidRequest(response, "redirect_uri required");
            return null;
        }
        
        String clientSecret = params.getClientSecret();
        if(Strings.isEmpty(clientSecret)) {
            OAuth2Errors.invalidRequest(response, "client_secret required");
            return null;
        }
        
        OAuth2Client client = clientManager.loadClientById(clientId);
        if(null == client) {
            OAuth2Errors.invalidGrant(response, "client not found");
            return null;
        }
        
        if(!client.isEnabled()) {
            OAuth2Errors.invalidGrant(response, "client diabled");
            return null;
        }

        if(!client.acceptsRedirectUri(redirectUri)){
            OAuth2Errors.invalidGrant(response, "redirect_uri invalid");
            return null;         
        }
        
        if(!Strings.equals(clientSecret, client.getSecret())) {
            OAuth2Errors.invalidGrant(response, "client_secret invalid");
            return null;
        }
        
        return client;
    }
    
    protected OAuth2Client validateClientSecret(Request request, Response response, OAuth2Params params) throws Throwable {
        String clientId = params.getClientId();
        if(Strings.isEmpty(clientId)) {
            OAuth2Errors.invalidRequest(response, "client_id required");
            return null;
        }
        
        String clientSecret = params.getClientSecret();
        if(Strings.isEmpty(clientSecret)) {
            OAuth2Errors.invalidRequest(response, "client_secret required");
            return null;
        }
        
        OAuth2Client client = clientManager.loadClientById(clientId);
        if(null == client) {
            OAuth2Errors.invalidGrant(response, "client not found");
            return null;
        }
        
        if(!client.isEnabled()) {
            OAuth2Errors.invalidGrant(response, "client diabled");
            return null;
        }
        
        if(!Strings.equals(clientSecret, client.getSecret())) {
            OAuth2Errors.invalidGrant(response, "client_secret invalid");
            return null;
        }

        return client;
    }

}

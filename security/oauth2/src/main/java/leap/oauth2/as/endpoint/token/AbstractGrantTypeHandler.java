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

import leap.core.annotation.Inject;
import leap.lang.Strings;
import leap.lang.codec.Base64;
import leap.oauth2.*;
import leap.oauth2.as.OAuth2AuthzServerConfig;
import leap.oauth2.as.client.*;
import leap.web.Request;
import leap.web.Response;

public abstract class AbstractGrantTypeHandler implements GrantTypeHandler {
    
    protected @Inject OAuth2AuthzServerConfig config;
    protected @Inject AuthzClientManager      clientManager;
    protected @Inject GrantTypeHandleFailHandler[] failHandlers;
    
    protected AuthzClient validateClient(Request request, Response response, OAuth2Params params, AuthzClientCredentials credentials) throws Throwable {
        String clientId = credentials.getClientId();
        if(Strings.isEmpty(clientId)) {
            handleError(request,response,new RequestOAuth2Params(request),OAuth2Errors.invalidRequestError("client_id required"));
            return null;
        }
        
        String redirectUri = params.getRedirectUri();
        if(Strings.isEmpty(redirectUri)) {
            handleError(request,response,new RequestOAuth2Params(request),OAuth2Errors.invalidRequestError("redirect_uri required"));
            return null;
        }
        
        String clientSecret = credentials.getClientSecret();
        if(Strings.isEmpty(clientSecret)) {
            handleError(request,response,new RequestOAuth2Params(request),OAuth2Errors.invalidRequestError("client_secret required"));
            return null;
        }
        AuthzClient client = clientManager.loadClientById(credentials.getClientId());
        if(client == null){
            handleError(request,response,new RequestOAuth2Params(request),OAuth2Errors.invalidGrantError("client not found"));
            return null;
        }
        if(!client.acceptsRedirectUri(redirectUri)){
            handleError(request,response,new RequestOAuth2Params(request),OAuth2Errors.invalidGrantError("redirect_uri invalid"));
            return null;         
        }
        
        return client;
    }
    
    protected AuthzClient validateClientSecret(Request request, Response response, AuthzClientCredentials credentials) throws Throwable {
        String clientId = credentials.getClientId();
        if(Strings.isEmpty(clientId)) {
            handleError(request,response,new RequestOAuth2Params(request),OAuth2Errors.invalidRequestError("client_id required"));
            return null;
        }
        
        String clientSecret = credentials.getClientSecret();
        if(Strings.isEmpty(clientSecret)) {
            handleError(request,response,new RequestOAuth2Params(request),OAuth2Errors.invalidRequestError("client_secret required"));
            return null;
        }
        AuthzClientAuthenticationContext context = new DefaultAuthzClientAuthenticationContext(request,response);
        AuthzClient client = clientManager.authenticate(context,credentials);
        if(!context.errors().isEmpty()){
            OAuth2Errors.invalidGrant(response, context.errors().getMessage());
        }
        return client;
    }

    protected AuthzClientCredentials extractClientCredentials(Request request, Response response,OAuth2Params params){
        String header = request.getHeader(OAuth2Constants.TOKEN_HEADER);
        if(header != null && !Strings.isEmpty(header)){
            if(!header.startsWith(OAuth2Constants.BASIC_TYPE)){
                handleError(request,response,params,OAuth2Errors.invalidRequestError("invalid Authorization header."));
                return null;
            }
            String base64Token = Strings.trim(header.substring(OAuth2Constants.BASIC_TYPE.length()));
            String token = Base64.decode(base64Token);
            String[] idAndSecret = Strings.split(token,":");
            if(idAndSecret.length != 2){
                handleError(request,response,params,OAuth2Errors.invalidRequestError("invalid Authorization header."));
                return null;
            }
            return new SamplingAuthzClientCredentials(idAndSecret[0],idAndSecret[1]);
        }
        String clientId = params.getClientId();
        String clientSecret = params.getClientSecret();
        if(Strings.isEmpty(clientId)){
            handleError(request,response,params,OAuth2Errors.invalidRequestError("client_id is required."));
            return null;
        }
        if(Strings.isEmpty(clientSecret)){
            handleError(request,response,params,OAuth2Errors.invalidRequestError("client_secret is required."));
            return null;
        }
        return new SamplingAuthzClientCredentials(clientId,clientSecret);
    }
    
    protected void handleError(Request request, Response response,OAuth2Params params,OAuth2Error error){
        if(!handleFail(request,response,params,error)){
            OAuth2Errors.response(response,error);
        }
    }

    @Override
    public boolean handleFail(Request request, Response response, OAuth2Params params, OAuth2Error error) {
        for (GrantTypeHandleFailHandler h : failHandlers){
            if(h.handle(request,response,params,error,this)){
                return true;
            }
        }
        return false;
    }
}

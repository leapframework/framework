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
import leap.core.i18n.MessageKey;
import leap.lang.Strings;
import leap.lang.codec.Base64;
import leap.oauth2.*;
import leap.oauth2.as.OAuth2AuthzServerConfig;
import leap.oauth2.as.client.*;
import leap.web.Request;
import leap.web.Response;

import java.util.UUID;
import java.util.function.Function;

import static leap.oauth2.Oauth2MessageKey.*;

public abstract class AbstractGrantTypeHandler implements GrantTypeHandler {
    
    protected @Inject OAuth2AuthzServerConfig config;
    protected @Inject AuthzClientManager      clientManager;
    protected @Inject GrantTypeHandleFailHandler[] failHandlers;
    
    protected AuthzClient validateClient(Request request, Response response, OAuth2Params params, AuthzClientCredentials credentials) throws Throwable {
        String clientId = credentials.getClientId();
        if(Strings.isEmpty(clientId)) {
            handleError(request,response,new RequestOAuth2Params(request),
                    getOauth2Error(key -> OAuth2Errors.invalidRequestError(request,key,"client_id required"),
                            INVALID_REQUEST_CLIENT_ID_REQUIRED));
            return null;
        }
        
        String redirectUri = params.getRedirectUri();
        if(Strings.isEmpty(redirectUri)) {
            handleError(request,response,new RequestOAuth2Params(request),
                    getOauth2Error(key -> OAuth2Errors.invalidRequestError(request,key,"redirect_uri required"),INVALID_REQUEST_REDIRECT_URI_REQUIRED));
            return null;
        }
        
        String clientSecret = credentials.getClientSecret();
        if(Strings.isEmpty(clientSecret)) {
            handleError(request,response,new RequestOAuth2Params(request),
                    getOauth2Error(key -> OAuth2Errors.invalidRequestError(request,key,"client_secret required"),INVALID_REQUEST_CLIENT_SECRET_REQUIRED));
            return null;
        }
        AuthzClient client = clientManager.loadClientById(credentials.getClientId());
        if(client == null){
            handleError(request,response,new RequestOAuth2Params(request),
                    getOauth2Error(key -> OAuth2Errors.invalidGrantError(request,key, "client not found"),ERROR_INVALID_GRANT_CLIENT_NOT_FOUND));
            return null;
        }
        if(!client.acceptsRedirectUri(redirectUri)){
            handleError(request,response,new RequestOAuth2Params(request),
                    getOauth2Error(key -> OAuth2Errors.invalidGrantError(request,key,"redirect_uri invalid"),ERROR_INVALID_GRANT_REDIRECT_URI_INVALID));
            return null;         
        }
        
        return client;
    }
    
    protected AuthzClient validateClientSecret(Request request, Response response, AuthzClientCredentials credentials) throws Throwable {
        String clientId = credentials.getClientId();
        if(Strings.isEmpty(clientId)) {
            handleError(request,response,new RequestOAuth2Params(request),
                    getOauth2Error(key -> OAuth2Errors.invalidRequestError(request,key,"client_id required"),INVALID_REQUEST_CLIENT_ID_REQUIRED));
            return null;
        }
        
        String clientSecret = credentials.getClientSecret();
        if(Strings.isEmpty(clientSecret)) {
            handleError(request,response,new RequestOAuth2Params(request),
                    getOauth2Error(key -> OAuth2Errors.invalidRequestError(request,key,"client_secret required"),INVALID_REQUEST_CLIENT_SECRET_REQUIRED));
            return null;
        }
        AuthzClientAuthenticationContext context = new DefaultAuthzClientAuthenticationContext(request,response);
        AuthzClient client = clientManager.authenticate(context,credentials);
        if(!context.errors().isEmpty()){
            handleError(request,response,new RequestOAuth2Params(request),
                    OAuth2Errors.invalidGrantError(request, Oauth2MessageKey.createRandomKey(),context.errors().getMessage()));
        }
        return client;
    }

    protected AuthzClientCredentials extractClientCredentials(Request request, Response response,OAuth2Params params){
        String header = request.getHeader(OAuth2Constants.TOKEN_HEADER);
        if(header != null && !Strings.isEmpty(header)){
            if(!header.startsWith(OAuth2Constants.BASIC_TYPE)){
                handleError(request,response,params,
                        getOauth2Error(key -> OAuth2Errors.invalidRequestError(request,key,"invalid Authorization header."),INVALID_REQUEST_INVALID_AUTHZ_HEADER));
                return null;
            }
            String base64Token = Strings.trim(header.substring(OAuth2Constants.BASIC_TYPE.length()));
            String token = Base64.decode(base64Token);
            String[] idAndSecret = Strings.split(token,":");
            if(idAndSecret.length != 2){
                handleError(request,response,params,
                        getOauth2Error(key -> OAuth2Errors.invalidRequestError(request,key,"invalid Authorization header."),INVALID_REQUEST_INVALID_AUTHZ_HEADER));
                return null;
            }
            return new SamplingAuthzClientCredentials(idAndSecret[0],idAndSecret[1]);
        }
        String clientId = params.getClientId();
        String clientSecret = params.getClientSecret();
        if(Strings.isEmpty(clientId)){
            handleError(request,response,params,
                    getOauth2Error(key -> OAuth2Errors.invalidRequestError(request, key,"client_id is required."),INVALID_REQUEST_CLIENT_ID_REQUIRED));
            return null;
        }
        if(Strings.isEmpty(clientSecret)){
            handleError(request,response,params,
                    getOauth2Error(key -> OAuth2Errors.invalidRequestError(request, key,"client_secret is required."),INVALID_REQUEST_CLIENT_ID_REQUIRED));
            return null;
        }
        return new SamplingAuthzClientCredentials(clientId,clientSecret);
    }
    
    protected void handleError(Request request, Response response,OAuth2Params params,OAuth2Error error){
        if(!handleFail(request,response,params,error)){
            OAuth2Errors.response(response,error);
        }
    }
    
    protected OAuth2Error getOauth2Error(Function<MessageKey,OAuth2Error> function, String messageKey, Object...args){
        MessageKey key = Oauth2MessageKey.getMessageKey(messageKey,args);
        return function.apply(key);
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

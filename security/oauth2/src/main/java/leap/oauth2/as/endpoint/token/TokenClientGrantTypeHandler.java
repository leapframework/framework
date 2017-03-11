/*
 * Copyright 2016 the original author or authors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *       http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package leap.oauth2.as.endpoint.token;

import leap.core.annotation.Inject;
import leap.core.security.Authentication;
import leap.core.security.token.jwt.JwtVerifier;
import leap.lang.Strings;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.oauth2.OAuth2Errors;
import leap.oauth2.OAuth2Params;
import leap.oauth2.as.OAuth2AuthzServerConfig;
import leap.oauth2.as.authc.AuthzAuthentication;
import leap.oauth2.as.authc.SimpleAuthzAuthentication;
import leap.oauth2.as.client.AuthzClient;
import leap.oauth2.as.client.AuthzClientManager;
import leap.oauth2.as.client.SamplingAuthzClientCredentials;
import leap.oauth2.as.token.AuthzAccessToken;
import leap.oauth2.as.token.AuthzTokenManager;
import leap.oauth2.as.token.SimpleAuthzAccessToken;
import leap.web.Request;
import leap.web.Response;
import leap.web.security.authc.AuthenticationManager;
import leap.web.security.user.UserDetails;
import leap.web.security.user.UserManager;

import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import static leap.oauth2.Oauth2MessageKey.*;

/**
 * Created by KAEL on 2016/6/13.
 * grant_type=token_client
 */
public class TokenClientGrantTypeHandler extends AbstractGrantTypeHandler {

    private Log log = LogFactory.get(TokenClientGrantTypeHandler.class);

    protected @Inject OAuth2AuthzServerConfig config;
    protected @Inject AuthzTokenManager tokenManager;
    protected @Inject AuthzClientManager clientManager;
    protected @Inject AuthenticationManager authenticationManager;
    protected @Inject UserManager userManager;


    @Override
    public void handleRequest(Request request, Response response, OAuth2Params params, Consumer<AuthzAccessToken> callback) throws Throwable {
        if(!config.isTokenClientEnabled()){
            handleError(request,response,params,
                    getOauth2Error(key -> OAuth2Errors.unsupportedGrantTypeError(request,key,null),ERROR_UNSUPPORTED_GRANT_TYPE_TYPE,"token_client"));
            return;
        }
        if(Strings.isEmpty(params.getAccessToken())){
            handleError(request,response,params,
                    getOauth2Error(key -> OAuth2Errors.invalidRequestError(request,key, "access_token is require"),INVALID_REQUEST_ACCESS_TOKEN_REQUIRED));
            return;
        }

        SamplingAuthzClientCredentials credentials = new SamplingAuthzClientCredentials(params.getClientId(),params.getClientSecret());

        AuthzClient client = validateClientSecret(request,response, credentials);
        if(client == null){
            return;
        }
        boolean isJwtToken = isJwtToken(request,response,params);

        AuthzAccessToken at;
        if(isJwtToken){
            at = authenticateJwtToken(request,response,params,client);
        }else{
            at = authenticateAccessToken(request,response,params,client);
        }
        if(at != null){
            callback.accept(at);
        }
    }

    @Override
    public boolean handleSuccess(Request request, Response response, OAuth2Params params, AuthzAccessToken token) {
        return false;
    }

    protected boolean isJwtToken(Request request, Response response, OAuth2Params params){
        return Strings.contains(params.getAccessToken(), ".");
    }

    protected AuthzAccessToken authenticateJwtToken(Request request, Response response, OAuth2Params params, AuthzClient client){
        JwtVerifier verifier = config.getJwtVerifier();
        if(verifier == null){
            handleError(request,response,params,
                    getOauth2Error(key -> OAuth2Errors.invalidRequestError(request,key, "not support jwt token because public key is undefined!"),INVALID_REQUEST_JWT_PUBLIC_KEY_UNDEFINED));
            return null;
        }
        String at = params.getAccessToken();
        Map<String, Object> claims = verifier.verify(at);

        Object username = claims.get("username");
        Object scope = claims.get("scope");
        Object expiresIn = claims.get("expires_in");
        Object expires = claims.get("expires");
        if(username == null){
            handleError(request,response,params,
                    getOauth2Error(key -> OAuth2Errors.invalidTokenError(request,key, "jwt token must contain username"),ERROR_INVALID_TOKEN_JWT_TOKEN_NOT_CONTAIN_USERNAME,at));
            return null;
        }
        if(expiresIn == null){
            handleError(request,response,params,
                    getOauth2Error(key -> OAuth2Errors.invalidTokenError(request, key,"jwt token must contain expires_in"),ERROR_INVALID_TOKEN_JWT_TOKEN_NOT_CONTAIN_EXPIRES_IN,at));
            return null;
        }
        String userLoginId = Objects.toString(username);
        String userScope = scope==null?"": Objects.toString(scope);
        Integer atExpiresIn = 0;
        Long atExpires = 0L;
        try {
            atExpiresIn = Integer.parseInt(Objects.toString(expiresIn));
            atExpires = Long.parseLong(Objects.toString(expires));
        } catch (NumberFormatException e) {
            log.debug("authenticate jwt token error",e);
            handleError(request,response,params,
                    getOauth2Error(key -> OAuth2Errors.invalidTokenError(request,key, "expires_in is not an integer or expires is not an long"),ERROR_INVALID_TOKEN_JWT_TOKEN_EXPIRES_IN_FORMAT_ERROR,expiresIn));
            return null;
        }
        Authentication authentication = userManager.createAuthenticationByUsername(userLoginId).get();
        if(authentication == null){
            handleError(request,response,params,
                    getOauth2Error(key -> OAuth2Errors.invalidRequestError(request,key,"invalid username"),INVALID_REQUEST_INVALID_USERNAME,userLoginId));
            return null;
        }
        
        if(System.currentTimeMillis() > atExpires){
            handleError(request,response,params,
                    getOauth2Error(key -> OAuth2Errors.invalidTokenError(request,key,"jwt_token is expired"),ERROR_INVALID_TOKEN_EXPIRED,at));
            return null;
        }
        
        SimpleAuthzAccessToken accessToken = new SimpleAuthzAccessToken();
        accessToken.setUsername(authentication.getUser().getLoginName());
        accessToken.setUserId(authentication.getUser().getIdAsString());
        accessToken.setExpiresIn(atExpiresIn);
        accessToken.setScope(userScope);
        return createAuthenticatedAccessToken(request,response,accessToken,params,client);
    }

    protected AuthzAccessToken authenticateAccessToken(Request request, Response response, OAuth2Params params, AuthzClient client){
        String at = params.getAccessToken();
        AuthzAccessToken accessToken = tokenManager.loadAccessToken(at);
        if(accessToken == null){
            handleError(request,response,params,
                    getOauth2Error(key -> OAuth2Errors.invalidTokenError(request,key,"invalid access_token"),ERROR_INVALID_TOKEN_INVALID,at));
            return null;
        }
        if(accessToken.isExpired()){
            handleError(request,response,params,
                    getOauth2Error(key -> OAuth2Errors.invalidTokenError(request,key,"access_token is expired"),ERROR_INVALID_TOKEN_EXPIRED,at));
            tokenManager.removeAccessToken(accessToken);
            return null;
        }
        if(!Strings.equals(accessToken.getClientId(), client.getId())){
            handleError(request,response,params,
                    getOauth2Error(key -> OAuth2Errors.invalidTokenError(request,key,"this access_token is not for the client:"+client.getId()),ERROR_INVALID_TOKEN_NOT_FOR_CLIENT,at,client.getId()));
            return null;
        }
        return createAuthenticatedAccessToken(request,response,accessToken,params,client);
    }

    protected AuthzAccessToken createAuthenticatedAccessToken(Request request, Response response, AuthzAccessToken accessToken,OAuth2Params params, AuthzClient client){
        UserDetails ud = userManager.loadUserDetails(accessToken.getUserId());
        if(ud == null){
            handleError(request,response,params,
                    getOauth2Error(key -> OAuth2Errors.invalidTokenError(request,key,"invalid user"),ERROR_INVALID_TOKEN_INVALID_USER,accessToken.getToken(),accessToken.getUserId()));
            return null;
        }
        AuthzAuthentication authz = new SimpleAuthzAuthentication(params,client,ud);
        AuthzAccessToken clientAuthAccessToken = tokenManager.createAccessToken(authz);
        return clientAuthAccessToken;
    }
}
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

import leap.core.BeanFactory;
import leap.core.annotation.Inject;
import leap.core.ioc.PostCreateBean;
import leap.core.security.Authentication;
import leap.core.security.token.jwt.RsaVerifier;
import leap.lang.Strings;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.oauth2.OAuth2Errors;
import leap.oauth2.OAuth2Params;
import leap.oauth2.as.OAuth2AuthzServerConfig;
import leap.oauth2.as.authc.AuthzAuthentication;
import leap.oauth2.as.authc.SimpleAuthzAuthentication;
import leap.oauth2.as.client.*;
import leap.oauth2.as.token.AuthzAccessToken;
import leap.oauth2.as.token.AuthzTokenManager;
import leap.oauth2.as.token.SimpleAuthzAccessToken;
import leap.web.Request;
import leap.web.Response;
import leap.web.security.authc.AuthenticationManager;
import leap.web.security.user.UserDetails;
import leap.web.security.user.UserManager;

import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Created by KAEL on 2016/6/13.
 * grant_type=token_client
 */
public class TokenClientGrantTypeHandler extends AbstractGrantTypeHandler implements PostCreateBean {

    private Log log = LogFactory.get(TokenClientGrantTypeHandler.class);

    protected @Inject OAuth2AuthzServerConfig config;
    protected @Inject AuthzTokenManager tokenManager;
    protected @Inject AuthzClientManager clientManager;
    protected @Inject AuthenticationManager authenticationManager;
    protected @Inject UserManager userManager;

    protected RsaVerifier verifier;

    @Override
    public void postCreate(BeanFactory factory) throws Throwable {
        PublicKey publicKey = config.getPublicKey();
        if(publicKey instanceof RSAPublicKey){
            verifier = new RsaVerifier((RSAPublicKey)publicKey);
        }
    }

    @Override
    public void handleRequest(Request request, Response response, OAuth2Params params, Consumer<AuthzAccessToken> callback) throws Throwable {
        if(!config.isTokenClientEnabled()){
            OAuth2Errors.unsupportedGrantType(response,null);
            return;
        }
        if(Strings.isEmpty(params.getAccessToken())){
            OAuth2Errors.invalidRequest(response, "access_token is require");
            return;
        }

        SamplingAuthzClientCredentials credentials = new SamplingAuthzClientCredentials(params.getClientId(),params.getClientSecret());

        AuthzClient client = validateClientSecret(request,response, credentials);
        if(client == null){
            return;
        }
        boolean isJwtToken =isJwtToken(request,response,params);

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
        if(verifier == null){
            OAuth2Errors.invalidRequest(response, "not support jwt token because public key is undefined!");
            return null;
        }
        String at = params.getAccessToken();
        Map<String, Object> claims = verifier.verify(at);

        Object username = claims.get("username");
        Object scope = claims.get("scope");
        Object expiresIn = claims.get("expires_in");
        Object expires = claims.get("expires");
        if(username == null){
            OAuth2Errors.invalidToken(response,"jwt token must contain username");
            return null;
        }
        if(expiresIn == null){
            OAuth2Errors.invalidToken(response,"jwt token must contain expires_in");
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
            OAuth2Errors.invalidToken(response,"expires_in is not an integer or expires is not an long");
            return null;
        }
        Authentication authentication = userManager.createAuthenticationByUsername(userLoginId).get();
        if(authentication == null){
            OAuth2Errors.invalidRequest(response, "invalid username");
            return null;
        }

        SimpleAuthzAccessToken accessToken = new SimpleAuthzAccessToken();
        accessToken.setUsername(authentication.getUser().getLoginName());
        accessToken.setUserId(authentication.getUser().getIdAsString());
        accessToken.setExpiresIn(atExpiresIn);
        accessToken.setScope(userScope);
        if(System.currentTimeMillis() > atExpires){
            OAuth2Errors.invalidToken(response, "jwt_token is expired");
            return null;
        }
        return createAuthenticatedAccessToken(request,response,accessToken,params,client);
    }

    protected AuthzAccessToken authenticateAccessToken(Request request, Response response, OAuth2Params params, AuthzClient client){
        String at = params.getAccessToken();
        AuthzAccessToken accessToken = tokenManager.loadAccessToken(at);
        if(accessToken == null){
            OAuth2Errors.invalidToken(response, "invalid access_token");
            return null;
        }
        if(accessToken.isExpired()){
            OAuth2Errors.invalidToken(response, "access_token is expired");
            tokenManager.removeAccessToken(accessToken);
            return null;
        }
        if(!Strings.equals(accessToken.getClientId(), client.getId())){
            OAuth2Errors.invalidToken(response, "this access_token is not for the client:"+client.getId());
            return null;
        }
        return createAuthenticatedAccessToken(request,response,accessToken,params,client);
    }

    protected AuthzAccessToken createAuthenticatedAccessToken(Request request, Response response, AuthzAccessToken accessToken,OAuth2Params params, AuthzClient client){
        UserDetails ud = userManager.loadUserDetails(accessToken.getUserId());
        if(ud == null){
            OAuth2Errors.invalidToken(response, "invalid user");
            return null;
        }
        AuthzAuthentication authz = new SimpleAuthzAuthentication(params,client,ud);
        AuthzAccessToken clientAuthAccessToken = tokenManager.createAccessToken(authz);
        return clientAuthAccessToken;
    }

}

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
package leap.oauth2.wac.auth;

import leap.core.annotation.Inject;
import leap.core.security.ClientPrincipal;
import leap.core.security.UserPrincipal;
import leap.core.security.token.jwt.MacSigner;
import leap.lang.Strings;
import leap.lang.intercepting.State;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.oauth2.OAuth2Params;
import leap.oauth2.rs.auth.ResClientPrincipal;
import leap.oauth2.wac.OAuth2WebAppConfig;
import leap.oauth2.wac.token.WacTokenManager;
import leap.web.Request;
import leap.web.Response;
import leap.web.security.authc.Authentication;
import leap.web.security.authc.AuthenticationManager;
import leap.web.security.authc.SimpleAuthentication;
import leap.web.security.login.LoginManager;
import leap.web.security.user.UserDetails;
import leap.web.security.user.UserManager;

import java.util.Map;

public class DefaultWacResponseHandler implements WacResponseHandler {
    
    private static final Log log = LogFactory.get(DefaultWacResponseHandler.class);
    
    protected @Inject OAuth2WebAppConfig    config;
    protected @Inject AuthenticationManager am;
    protected @Inject UserManager           um;
    protected @Inject LoginManager          sm;
    protected @Inject WacTokenManager       atm;

    @Override
    public State handleSuccessResponse(Request request, Response response, OAuth2Params params) throws Throwable {
        String idToken = params.getIdToken();
        if(!Strings.isEmpty(idToken)) {
            WacIdToken details = verifyIdToken(params, idToken);
            
            Authentication authc = authenticate(request, response, params, idToken, details);
            if(null == authc) {
                //TODO : 
                throw new IllegalStateException("Invalid authentication");
            }
            
            String code = params.getCode();
            if(!Strings.isEmpty(code)) {
                atm.fetchAndSaveAccessToken(request, authc, code);
            }
            
            login(request, response, authc);
            return State.INTERCEPTED;
        }
        
        return State.CONTINUE;
    }
    
    protected WacIdToken verifyIdToken(OAuth2Params params, String idToken) throws Throwable {
        
        MacSigner signer = new MacSigner(config.getClientSecret());
        
        Map<String, Object> claims = signer.verify(idToken);
        WacIdToken details = new WacIdToken();

        details.clientId = (String)claims.remove("aud");
        details.userId   = (String)claims.remove("sub");
        
        return details;
    }
    
    protected Authentication authenticate(Request request, Response response, OAuth2Params params, String idToken, WacIdToken details) throws Throwable {
        String clientId = details.getClientId();
        String userId   = details.getUserId();
        
        UserPrincipal   user   = null;
        ClientPrincipal client = null;
        
        if(!Strings.isEmpty(userId)) {
            UserDetails userDetails = um.loadUserDetails(userId);
            
            if(null == userDetails) {
                log.debug("The user id '{}' created with id token '{}' is not found", userId, idToken);
                return null;
            }else{
                user = userDetails;
            }
        }
        
        if(!Strings.isEmpty(clientId)) {
            client = new ResClientPrincipal(clientId);
        }
        
        SimpleAuthentication authc = new SimpleAuthentication(user, details);
        if(null != client) {
            authc.setClientPrincipal(client);
        }
        
        return authc;
    }

    protected void login(Request request, Response response, Authentication authc) throws Throwable {
        am.loginImmediately(request, response, authc);
        sm.handleLoginSuccess(request, response, authc);
    }
}
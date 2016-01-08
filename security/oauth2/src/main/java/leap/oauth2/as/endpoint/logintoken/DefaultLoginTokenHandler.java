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
package leap.oauth2.as.endpoint.logintoken;

import leap.core.annotation.Inject;
import leap.core.security.SecurityContext;
import leap.core.security.UserPrincipal;
import leap.lang.Strings;
import leap.lang.http.ContentTypes;
import leap.lang.json.JsonWriter;
import leap.oauth2.OAuth2Errors;
import leap.oauth2.OAuth2Params;
import leap.oauth2.as.authc.AuthzAuthentication;
import leap.oauth2.as.authc.SimpleAuthzAuthentication;
import leap.oauth2.as.client.AuthzClient;
import leap.oauth2.as.client.AuthzClientManager;
import leap.oauth2.as.endpoint.tokeninfo.TokenInfoHandler;
import leap.oauth2.as.token.AuthzAccessToken;
import leap.oauth2.as.token.AuthzLoginToken;
import leap.oauth2.as.token.AuthzTokenManager;
import leap.web.Request;
import leap.web.Response;
import leap.web.security.SecurityConfig;
import leap.web.security.user.SimpleUserDetailsPrincipal;
import leap.web.security.user.UserDetails;
import leap.web.security.user.UserManager;
import leap.web.security.user.UserStore;

import java.util.Map.Entry;

public class DefaultLoginTokenHandler implements LoginTokenHandler {

    protected @Inject SecurityConfig     sc;
    protected @Inject UserManager        um;
    protected @Inject AuthzTokenManager  tokenManager;
    protected @Inject AuthzClientManager clientManager;

    @Override
    public boolean handleLoginTokenRequest(Request request, Response response, OAuth2Params params) throws Throwable {
        String accessToken = params.getAccessToken();
        if(null != accessToken) {
            if(accessToken.isEmpty()) {
                OAuth2Errors.invalidRequest(response, "token required");
                return true;
            }            

            AuthzAccessToken at = tokenManager.loadAccessToken(accessToken);
            if(null == at) {
                OAuth2Errors.invalidRequest(response, "invalid token");
                return true;
            }else if(at.isExpired()) {
                OAuth2Errors.invalidRequest(response, "invalid token");
                return true;
            }

            if(at.isClientOnly()) {
                OAuth2Errors.invalidRequest(response, "'clientOnly' token not allowed");
                return true;
            }

            if(Strings.isEmpty(at.getClientId())){
                OAuth2Errors.invalidRequest(response, "'userOnly' token not allowed");
                return true;
            }

            //Authenticate user.
            UserPrincipal user = null;
            UserStore us = sc.getUserStore();
            UserDetails ud = us.findUserDetailsByIdString(at.getUserId());
            if(null == ud || !ud.isEnabled()) {
                OAuth2Errors.invalidGrant(response, "invalid user");
                return true;
            }
            user = new SimpleUserDetailsPrincipal(ud);

            //Authenticate client.
            AuthzClient client = clientManager.loadClientById(at.getClientId());
            if(null == client || !client.isEnabled()) {
                OAuth2Errors.invalidGrant(response, "invalid client");
                return true;
            }

            if(!client.isAllowLoginToken()) {
                OAuth2Errors.invalidGrant(response, "the client not allow use login token");
                return true;
            }

            //Creates login token
            AuthzLoginToken lt = tokenManager.createLoginToken(new SimpleAuthzAuthentication(params, client, um.getUserDetails(user)));

            //Response the login token
            writeTokenInfo(request, response, lt);
            return true;
        }
        
        return false;
    }
    
    protected void writeTokenInfo(Request request, Response response, AuthzLoginToken lt) {
        response.setContentType(ContentTypes.APPLICATION_JSON_UTF8);
        
        JsonWriter w = response.getJsonWriter();
        
        w.startObject()
         .property("login_token", lt.getToken());
        w.endObject();
    }

}

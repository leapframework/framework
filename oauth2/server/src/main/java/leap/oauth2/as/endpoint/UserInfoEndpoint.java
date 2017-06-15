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

import leap.core.annotation.Inject;
import leap.lang.Strings;
import leap.oauth2.OAuth2Errors;
import leap.oauth2.as.OAuth2AuthzServerConfig;
import leap.oauth2.as.endpoint.userinfo.UserInfoHandler;
import leap.oauth2.as.token.AuthzAccessToken;
import leap.oauth2.as.token.AuthzTokenManager;
import leap.oauth2.as.token.TokenExtractor;
import leap.web.*;
import leap.web.route.Routes;
import leap.web.security.user.UserDetails;
import leap.web.security.user.UserManager;

/**
 * Open ID Connect defined endpoint, see <a href="https://openid.net/specs/openid-connect-basic-1_0.html#UserInfo">UserInfo Endpoint</a>
 */
public class UserInfoEndpoint extends AbstractAuthzEndpoint implements Endpoint,Handler {

    protected @Inject OAuth2AuthzServerConfig config;
    protected @Inject AuthzTokenManager       tokenManager;
    protected @Inject TokenExtractor          tokenExtractor;
    protected @Inject UserManager             userManager;
    protected @Inject UserInfoHandler[]       handlers;

	@Override
    public void startEndpoint(App app, Routes routes) {
        if(config.isEnabled() && config.isUserInfoEnabled()) {
            sc.ignore(config.getUserInfoEndpointPath());

            routes.create()
                  .handle(config.getUserInfoEndpointPath(), this )
                  .enableCors().disableCsrf()
                  .apply();
        }
    }

	@Override
    public void handle(Request request, Response response) throws Throwable {
        String token = tokenExtractor.extractTokenFromRequest(request);
        if(Strings.isEmpty(token)) {
            OAuth2Errors.invalidRequest(request,response, null,"Invalid access token");
            return;
        }

        AuthzAccessToken at = tokenManager.loadAccessToken(token);
        if(null == at) {
            OAuth2Errors.invalidToken(request,response, null,"Invalid access token");
            return;
        }

        if(at.isClientOnly()) {
            OAuth2Errors.invalidToken(request,response, null,"Invalid access token");
            return;
        }

        String userid = at.getUserId();
        UserDetails userDetails = userManager.loadUserDetails(userid);
        if(null == userDetails) {
            OAuth2Errors.invalidToken(request,response,null, "User not found");
            return;
        }

        if(!userDetails.isEnabled()) {
            OAuth2Errors.invalidToken(request,response,null,"User disabled");
            return;
        }

        for(UserInfoHandler h : handlers) {
            if(h.handleUserInfoResponse(request, response, at, userDetails)) {
                return ;
            }
        }
    }
	
}
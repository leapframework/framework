/*
 *
 *  * Copyright 2013 the original author or authors.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */
package leap.oauth2.server.sso;

import leap.core.annotation.Inject;
import leap.core.security.UserPrincipal;
import leap.lang.Strings;
import leap.oauth2.server.authc.OAuth2Authentication;
import leap.oauth2.server.OAuth2ServerConfig;
import leap.oauth2.server.client.OAuth2Client;
import leap.web.App;
import leap.web.AppInitializable;
import leap.web.Request;
import leap.web.Response;
import leap.web.security.SecurityConfigurator;
import leap.web.security.SecurityInterceptor;
import leap.web.security.authc.Authentication;

import java.util.UUID;

public class DefaultSSOManager implements OAuth2SSOManager,AppInitializable,SecurityInterceptor {

    protected @Inject OAuth2ServerConfig   ac;
    protected @Inject SecurityConfigurator sc;

    @Override
    public void postAppInit(App app) throws Throwable {
        if(ac.isSingleLogoutEnabled()) {
            sc.interceptors().add(this);
        }
    }

    @Override
    public void onAuthenticated(Request request, Response response, OAuth2Authentication authc) throws Throwable {
        Authentication secAuthc = authc.getAuthentication();
        String token = secAuthc.getToken();
        if(null == token) {
            throw new IllegalStateException("The authentication token must be exists");
        }

        OAuth2SSOStore ss = ac.getSSOStore();

        OAuth2SSOSession session = ss.loadSessionByToken(authc.getUserDetails().getLoginName(), token);
        if(null == session) {
            //Creates a new sso session and save it.
            session = newSession(request, response, authc);
            OAuth2SSOLogin login = newLogin(request, response, authc, session, true);

            ss.saveSession(session, login);
        }else{
            //Creates a new login and save it in session.
            OAuth2SSOLogin login = newLogin(request, response, authc, session, false);
            ss.saveLogin(session, login);
        }
    }

    protected OAuth2SSOSession newSession(Request request, Response response, OAuth2Authentication authc) {
        SimpleOAuth2SSOSession session = new SimpleOAuth2SSOSession();

        UserPrincipal user = authc.getAuthentication().getUserPrincipal();

        session.setId(UUID.randomUUID().toString());
        session.setUserId(user.getIdAsString());
        session.setUsername(user.getLoginName());
        session.setToken(authc.getAuthentication().getToken());
        session.setExpiresIn(ac.getDefaultLoginSessionExpires());
        session.setCreated(System.currentTimeMillis());

        return session;
    }

    protected OAuth2SSOLogin newLogin(Request request, Response response, OAuth2Authentication authc, OAuth2SSOSession session, boolean initial) {

        SimpleOAuth2SSOLogin login = new SimpleOAuth2SSOLogin();

        login.setInitial(initial);
        login.setLoginTime(System.currentTimeMillis());
        login.setLogoutUri(authc.getParams().getLogoutUri());

        OAuth2Client client = authc.getClientDetails();
        if(null != client) {
            login.setClientId(client.getId());

            if(Strings.isEmpty(login.getLogoutUri())) {
                login.setLogoutUri(client.getLogoutUri());
            }
        }

        return login;
    }
}
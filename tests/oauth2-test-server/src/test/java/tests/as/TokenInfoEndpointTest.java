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

package tests.as;

import com.sun.jna.platform.win32.WinBase;
import leap.core.annotation.Inject;
import leap.lang.Threads;
import leap.lang.http.QueryString;
import leap.oauth2.server.QueryOAuth2Params;
import leap.oauth2.server.authc.SimpleAuthzAuthentication;
import leap.oauth2.server.client.AuthzClient;
import leap.oauth2.server.client.AuthzClientManager;
import leap.oauth2.server.client.SimpleAuthzClient;
import leap.oauth2.server.token.AuthzAccessToken;
import leap.oauth2.server.token.AuthzTokenManager;
import leap.web.security.user.UserDetails;
import leap.web.security.user.UserManager;
import leap.webunit.client.THttpResponse;
import org.junit.Test;
import tests.OAuth2TestBase;
import tests.TokenResponse;

import static app.Global.TEST_CLIENT_ID;

/**
 * @author kael.
 */
public class TokenInfoEndpointTest extends OAuth2TestBase {
    
    protected @Inject AuthzTokenManager tokenManager;
    protected @Inject AuthzClientManager clientManager;
    protected @Inject UserManager userManager;
    
    @Test
    public void testInvalidToken(){
        TokenResponse at = obtainAccessTokenByPassword(USER_ADMIN,PASS_ADMIN);
        AuthzAccessToken token = tokenManager.loadAccessToken(at.accessToken);
        tokenManager.removeAccessToken(token);

        String uri = serverContextPath + TOKENINFO_ENDPOINT + "?access_token=" + at.accessToken;
        THttpResponse resp = get(uri);
        resp.assert401();

        SimpleAuthzClient client = (SimpleAuthzClient)clientManager.loadClientById(TEST_CLIENT_ID);
        client.setAccessTokenExpires(1);
        UserDetails ud = (UserDetails)userManager.createAuthenticationByUsername(USER_ADMIN).get().getUser();
        SimpleAuthzAuthentication authc = new SimpleAuthzAuthentication(new QueryOAuth2Params(QueryString.EMPTY),client,ud);

        String eat = tokenManager.createAccessToken(authc).getToken();
        Threads.sleep(1000);
        uri = serverContextPath + TOKENINFO_ENDPOINT + "?access_token=" + eat;
        resp = get(uri);
        resp.assert401();
    }
}

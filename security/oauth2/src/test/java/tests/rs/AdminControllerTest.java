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
package tests.rs;

import app.Global;
import leap.core.security.token.jwt.JwtVerifier;
import leap.lang.Assert;
import leap.oauth2.TokenVerifierFactory;
import org.junit.Test;
import tested.models.User;
import tests.JwtTokenResponse;
import tests.OAuth2TestBase;
import tests.TokenResponse;

import java.util.Map;

public class AdminControllerTest extends OAuth2TestBase {

    @Override
    protected void doSetUp() throws Exception {
        serverContextPath = "/server";
    }

    @Test
    public void testClientOnlyAccessToken() {
        TokenResponse token = obtainAccessTokenByClient("client1", "client1_secret");
        
        withAccessToken(forGet("/resapp/book"), token.accessToken).send().assertNotOk();
        withAccessToken(forGet("/resapp/admin/hello"), token.accessToken).send().assertOk();
    }

    @Test
    public void testClientGrantedScope() {
        TokenResponse token1 = obtainAccessTokenByClient("client1", "client1_secret");
        TokenResponse token2 = obtainAccessTokenByClient("client2", "client2_secret");

        withAccessToken(forGet("/resapp/admin/status"), token1.accessToken).send().assertOk();
        withAccessToken(forGet("/resapp/admin/status"), token2.accessToken).send().assertFailure();

    }
    @Test
    public void testClientTokenGrantedScope(){
        TokenResponse token1 = obtainAccessTokenByPassword(USER_XIAOMING, PASS_XIAOMING);
        TokenResponse token2 = obtainAccessTokenByTokenClient(token1.accessToken,Global.TEST_CLIENT_ID,Global.TEST_CLIENT_SECRET);

        withAccessToken(forGet("/resapp/admin/test"), token2.accessToken).send().assertOk();
        withAccessToken(forGet("/resapp/admin/test"), token1.accessToken).send().assertFailure();
    }
    @Test
    public void testJwtTokenExpirseIn(){

        TokenResponse token1 = obtainAccessTokenByPassword(USER_XIAOMING, PASS_XIAOMING);
        TokenResponse token2 = obtainAccessTokenByTokenClient(token1.accessToken,Global.TEST_CLIENT_ID,Global.TEST_CLIENT_SECRET);
        JwtTokenResponse jwtTokenResponse = testJwtResponseAccessTokenInfo(token2);
        withAccessToken(forGet("/resapp/admin/test"), jwtTokenResponse.jwtToken).send().assertOk();
    }

    @Test
    public void testAccessTokenWithNotUser(){
        User user = new User();
        user.setLoginName("notuser");
        user.setPassword("notuser");
        user.create();
        TokenResponse token = obtainAccessTokenByPassword(user.getLoginName(), user.getPassword());
        Assert.notNull(token);
        user.delete();

        assertEquals("success",withAccessToken(forGet("/resapp/admin/allow_anonymous"), token.accessToken).send().getContent());
    }
    @Test
    public void testUrlJwtVerifier(){

        TokenResponse token = obtainAccessTokenByPassword(USER_XIAOMING, PASS_XIAOMING);
        assertFalse(token.isError());

        JwtTokenResponse jwtTokenResponse = testJwtResponseAccessTokenInfo(token);
        
        String publicKeyUrl = "https://localhost:8443/server/publickey";
        JwtVerifier verifier = TokenVerifierFactory.createNetPublicKeyRSAJwtVerifier(publicKeyUrl);
        Map<String, Object> claims = verifier.verify(jwtTokenResponse.jwtToken);
        assertNotEmpty(claims);
    }
    
    
}

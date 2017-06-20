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
package tests.resource;

import leap.core.security.SEC;
import leap.lang.Assert;
import org.junit.Test;
import tested.models.User;
import tests.OAuth2TestBase;
import tests.TokenResponse;

public class AdminControllerTest extends OAuth2TestBase {

    @Test
    public void testClientOnlyAccessToken() {
        TokenResponse token = obtainAccessTokenByClient("client1", "client1_secret");
        
        withAccessToken(useGet("/book"), token.accessToken).send().assertNotOk();
        withAccessToken(useGet("/admin/hello"), token.accessToken).send().assertOk();
    }

    @Test
    public void testClientGrantedScope() {
        TokenResponse token1 = obtainAccessTokenByClient("client1", "client1_secret");
        TokenResponse token2 = obtainAccessTokenByClient("client2", "client2_secret");

        withAccessToken(useGet("/admin/status"), token1.accessToken).send().assertOk();
        withAccessToken(useGet("/admin/status"), token2.accessToken).send().assertFailure();
    }

    @Test
    public void testClientTokenGrantedScope(){
        TokenResponse token1 = obtainAccessTokenByPassword(USER_XIAOMING, PASS_XIAOMING,TEST_CLIENT_ID,TEST_CLIENT_SECRET);
        TokenResponse token2 = obtainAccessTokenByPassword(USER_XIAOMING, PASS_XIAOMING,"app2","app2_secret");

        withAccessToken(useGet("/admin/test"), token1.accessToken).send().assertOk();
        withAccessToken(useGet("/admin/test"), token2.accessToken).send().assertFailure();
    }

    @Test
    public void testAccessTokenUserNotExists(){
        String username = "will_be_deleted";
        String password = "1";

        User user = new User();
        user.setLoginName(username);
        user.setPassword(SEC.encodePassword(password));
        user.create();

        TokenResponse token = obtainAccessTokenByPassword(user.getLoginName(), password);
        Assert.notNull(token.accessToken);

        user.delete();
        assertEquals("success",withAccessToken(useGet("/admin/allow_anonymous"), token.accessToken).send().getContent());
    }

}

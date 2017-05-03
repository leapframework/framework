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

import leap.lang.json.JsonObject;
import leap.webunit.client.THttpResponse;
import org.junit.Test;
import tests.OAuth2TestBase;
import tests.TokenResponse;

/**
 * @author kael.
 */
public class UserInfoEndpointTest extends OAuth2TestBase {
    @Test
    public void testGetUserinfo(){
        TokenResponse token = obtainAccessTokenByPassword(USER_ADMIN,PASS_ADMIN);
        THttpResponse response = withAccessToken(forGet("/oauth2/userinfo"),token.accessToken).send().assertSuccess();
        JsonObject json = response.getJson().asJsonObject();
        assertEquals(USER_ADMIN,json.getString("login_name"));
        assertEquals(testAccessTokenInfo(token).userId,json.getString("sub"));
    }
}

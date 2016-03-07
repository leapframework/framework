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

import org.junit.Test;
import tests.OAuth2TestBase;
import tests.TokenResponse;

public class AdminControllerTest extends OAuth2TestBase {

    @Override
    protected void setUp() throws Exception {
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
    
}

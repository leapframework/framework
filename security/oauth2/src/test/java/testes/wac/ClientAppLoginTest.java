/*
 * Copyright 2013 the original author or authors.
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
package testes.wac;

import org.junit.Test;
import testes.OAuth2TestBase;

public class ClientAppLoginTest extends OAuth2TestBase {

    @Test
    public void testSingleLogin() {
        logoutAuthzServer();

        String redirectUri = get("/clientapp1/").assertRecirect().getLocation();
        assertEquals(redirectUri, "https://localhost:8443/server/oauth2/authorize?" +
                                  "response_type=id_token&client_id=app1" +
                                  "&redirect_uri=https%3A%2F%2F127.0.0.1%3A8443%2Fclientapp1%2Foauth2_redirect%3Foauth2_redirect%3D1");


        redirectUri = get("/clientapp2/").assertRecirect().getLocation();
        assertEquals(redirectUri, "https://localhost:8443/server/oauth2/authorize?" +
                                  "response_type=code+id_token&client_id=app2" +
                                  "&redirect_uri=http%3A%2F%2Flocalhost%3A8080%2Fclientapp2%2Fauth_redirect%3F1%3D1%26oauth2_redirect%3D1");


    }


}

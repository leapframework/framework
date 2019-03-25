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
package tests.login;

import org.junit.Test;
import tests.OAuth2TestBase;

public class ClientAppLoginTest extends OAuth2TestBase {

    @Test
    public void testSingleLogin() {
        runHttp(() -> {

            if(isLogin()) {
                logoutServer();
            }

            //login without access token
            String redirectUrl1 = get("/app1").assertRedirect().getLocation();
            //todo: failed for UserDetailsLookup enabled
            //assertEquals(redirectUrl1, "http://localhost:8080/server/oauth2/authorize?response_type=id_token&client_id=app1&redirect_uri=http%3A%2F%2Flocalhost%3A8080%2Fapp1%2F%3Foauth2_redirect%3D1&logout_uri=http%3A%2F%2Flocalhost%3A8080%2Fapp1%2Flogout");

            //login with access token
            String redirectUrl2 = get("/app2").assertRedirect().getLocation();
            assertEquals(redirectUrl2, "http://localhost:8080/server/oauth2/authorize?response_type=code+id_token&client_id=app2&redirect_uri=http%3A%2F%2Flocalhost%3A8080%2Fapp2%2F%3Foauth2_redirect%3D1&logout_uri=http%3A%2F%2Flocalhost%3A8080%2Fapp2%2Flogout");

            loginServer();

            String redirectBackUrl1 = get(redirectUrl1).assertRedirect().getLocation();
            get("/app1/").assertNotOk();
            get(redirectBackUrl1);
            get("/app1/").assertOk();

            String redirectBackUrl2 = get(redirectUrl2).assertRedirect().getLocation();
            get("/app2/").assertNotOk();
            get(redirectBackUrl2);
            get("/app2/").assertOk();

        });

    }

}

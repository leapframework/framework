/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package app;

import app.models.Client;
import app.models.User;
import leap.core.security.SEC;
import leap.lang.net.Urls;
import leap.web.App;

import static tests.OAuth2TestData.*;

public class Global extends App {

    public static final String TEST_CLIENT_ID                   = "test";
    public static final String TEST_CLIENT_SECRET               = "test_secret";
    public static final String TEST_CLIENT_GRANTED_SCOPE		= "admin:test";
    public static final String TEST_CLIENT_REDIRECT_URI         = "/oauth2/redirect_uri";
    public static final String TEST_CLIENT_REDIRECT_URI_ENCODED = Urls.encode(TEST_CLIENT_REDIRECT_URI);

    @Override
    protected void init() throws Throwable {
        initUsers();
        initClients();
    }

    protected void initUsers() {
        User.deleteAll();

        //User : admin (ok)
        User admin = new User();
        admin.setLoginName(USER_ADMIN);
        admin.setPassword(SEC.encodePassword(PASS_ADMIN));
        admin.create();

        //User : xiaoming (ok)
        User test3 = new User();
        test3.setLoginName(USER_XIAOMING);
        test3.setPassword(SEC.encodePassword(PASS_XIAOMING));
        test3.create();

        //User : test1 (bad password)
        User test1 = new User();
        test1.setLoginName("test1");
        test1.setPassword("bad password");
        test1.create();

        //User : test2 (disabled)
        User test2 = new User();
        test2.setLoginName("test2");
        test2.setPassword("1");
        test2.setEnabled(false);
        test2.create();
    }

    protected void initClients() {
        Client.deleteAll();

        //Client web app1.
        Client app2 = new Client();
        app2.setId("app1");
        app2.setSecret("app1_secret");
        app2.setRedirectUriPattern("http*://*/clientapp1/oauth2_redirect");
        app2.create();

        //Client web app2.
        Client app3 = new Client();
        app3.setId("app2");
        app3.setSecret("app2_secret");
        app3.setRedirectUriPattern("http*://*/clientapp2/auth_redirect");
        app3.create();

        //Non web app client
        Client testClient = new Client();
        testClient.setId(app.Global.TEST_CLIENT_ID);
        testClient.setSecret(app.Global.TEST_CLIENT_SECRET);
        testClient.setGrantedScope(app.Global.TEST_CLIENT_GRANTED_SCOPE);
        testClient.setRedirectUri(app.Global.TEST_CLIENT_REDIRECT_URI);
        testClient.create();

        Client client1 = new Client();
        client1.setId("client1");
        client1.setSecret("client1_secret");
        client1.setGrantedScope("admin:status");
        client1.create();

        Client client2 = new Client();
        client2.setId("client2");
        client2.setSecret("client2_secret");
        client2.create();
    }
}

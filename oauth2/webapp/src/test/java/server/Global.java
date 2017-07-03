/*
 * Copyright 2014 the original author or authors.
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
package server;

import leap.core.annotation.Inject;
import leap.core.security.SEC;
import tests.OAuth2TestData;
import leap.oauth2.server.OAuth2AuthzServerConfigurator;
import leap.orm.dmo.Dmo;
import leap.web.App;
import leap.web.config.WebConfigurator;
import server.models.Client;
import tested.models.User;

/**
 * server : OAuth2 authorization server.
 */
public class Global extends App implements OAuth2TestData {
    
    protected @Inject OAuth2AuthzServerConfigurator oc;

    @Override
    protected void configure(WebConfigurator c) {
        oc.enable()
          .useJdbcStore().useRsaJwtVerifier();
    }
    
    @Override
    protected void init() throws Throwable {
        upgradeSchema();
        
        registerClients();
        
        registerUsers();
    }
    
    protected void upgradeSchema() {
        Dmo.get().cmdUpgradeSchema().execute();
    }
    
    protected void registerClients() {
        Client.deleteAll();

        //app.
        Client app = new Client();
        app.setId("app");
        app.setSecret("app_secret");
        app.setRedirectUriPattern("*");
        app.create();

        //Client web app1.
        Client app2 = new Client();
        app2.setId("app1");
        app2.setSecret("app1_secret");
        app2.setRedirectUriPattern("*");
        app2.create();

        //Client web app2.
        Client app3 = new Client();
        app3.setId("app2");
        app3.setSecret("app2_secret");
        app3.setRedirectUriPattern("*");
        app3.create();

        //Non web app client
        Client testClient = new Client();
        testClient.setId(TEST_CLIENT_ID);
        testClient.setSecret(TEST_CLIENT_SECRET);
        testClient.setGrantedScope(TEST_CLIENT_GRANTED_SCOPE);
        testClient.setRedirectUri(TEST_CLIENT_REDIRECT_URI);
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
    
    protected void registerUsers() {
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
}
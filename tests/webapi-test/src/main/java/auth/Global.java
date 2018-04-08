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
package auth;

import leap.core.annotation.Inject;
import leap.core.security.SEC;
import leap.oauth2.server.OAuth2AuthzServerConfigurator;
import leap.orm.dmo.Dmo;
import leap.web.App;
import leap.web.config.WebConfigurator;
import app.models.testing.User;
import auth.models.Client;

public class Global extends App implements AuthTestData {
    
    protected @Inject OAuth2AuthzServerConfigurator asc;

    @Override
    protected void configure(WebConfigurator c) {
        asc.enable().useJdbcStore();
    }
    
    @Override
    protected void init() throws Throwable {
        upgradeSchema();
        
        createClients();
        
        createUsers();
    }
    
    protected void upgradeSchema() {
        Dmo.get().cmdUpgradeSchema().execute();
    }
    
    protected void createClients() {
        Client.deleteAll();
        
        Client app = new Client();
        app.setId("app");
        app.setSecret("app_secret");
        app.setRedirectPattern("http*://*/oauth2_redirect");
        app.create();
        
        Client client1 = new Client();
        client1.setId("client1");
        client1.setSecret("client1_secret");
        client1.create();
    }
    
    protected void createUsers() {
        User.deleteAll();
        
        User admin = new User();
        admin.setLoginName(USERNAME);
        admin.setPassword(SEC.encodePassword(PASSWORD));
        admin.create();
        
        User test1 = new User();
        test1.setLoginName("test1");
        test1.setPassword("bad password");
        test1.create();
        
        User test2 = new User();
        test2.setLoginName("test2");
        test2.setPassword("1");
        test2.setEnabled(false);
        test2.create();
        
        User test3 = new User();
        test3.setLoginName(USERNAME1);
        test3.setPassword(SEC.encodePassword(PASSWORD1));
        test3.create();
    }
    
}
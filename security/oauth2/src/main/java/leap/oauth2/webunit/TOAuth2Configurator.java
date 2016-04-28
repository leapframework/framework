/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package leap.oauth2.webunit;

import leap.core.security.crypto.PasswordEncoder;
import leap.lang.path.PathPattern;
import leap.oauth2.as.OAuth2AuthzServerConfigurator;
import leap.oauth2.as.client.SimpleAuthzClient;
import leap.oauth2.as.store.AuthzInMemoryStore;
import leap.web.App;
import leap.web.security.SecurityConfigurator;
import leap.web.security.user.InMemoryUserStore;

import java.util.HashMap;
import java.util.Map;

public class TOAuth2Configurator {
    public static final PathPattern ANY_URI_PATTERN = new PathPattern() {
        @Override
        public String pattern() {
            return "*";
        }

        @Override
        public boolean matches(String path) {
            return true;
        }
    };

    private final SecurityConfigurator          sc;
    private final PasswordEncoder               pe;
    private final OAuth2AuthzServerConfigurator ac;

    private final Map<String, String>      users   = new HashMap<>();
    private final Map<String, TClientInfo> clients = new HashMap<>();

    public TOAuth2Configurator(App app) {
        this.sc = app.factory().getBean(SecurityConfigurator.class);
        this.pe = app.factory().getBean(PasswordEncoder.class);
        this.ac = app.factory().getBean(OAuth2AuthzServerConfigurator.class);
        initDefault();
    }

    public TOAuth2Configurator addUser(String username, String plainPassword) {
        users.put(username, plainPassword);
        return this;
    }

    public TOAuth2Configurator addClient(String clientId, String secret) {
        clients.put(clientId, new TClientInfo(clientId, secret));
        return this;
    }

    public void configure() {
        sc.enable();
        ac.enable();

        configureUsers();
        configureClients();
    }

    private void configureUsers() {
        InMemoryUserStore us = new InMemoryUserStore();
        sc.setUserStore(us);

        for(Map.Entry<String,String> user : users.entrySet()) {
            us.add(user.getKey(), pe.encode(user.getValue()));
        }
    }

    private void configureClients() {
        AuthzInMemoryStore store = ac.useInMemoryStore().inMemoryStore();

        for(TClientInfo ci : clients.values()) {
            SimpleAuthzClient client = new SimpleAuthzClient();
            client.setId(ci.id);
            client.setSecret(ci.secret);
            client.setRedirectUriPattern(ANY_URI_PATTERN);

            store.addClient(client);
        }
    }

    private void initDefault(){
        addUser(TOAuth2.DEFAULT_USERNAME,TOAuth2.DEFAULT_PASSWORD);
        addClient(TOAuth2.DEFAULT_CLIENT_ID, TOAuth2.DEFAULT_CLIENT_SECRET);
    }

    private static final class TClientInfo {
        private String id;
        private String secret;

        TClientInfo(String id, String secret) {
            this.id = id;
            this.secret = secret;
        }
    }
}

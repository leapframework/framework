/*
 * Copyright 2016 the original author or authors.
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
package leap.web.security.client;

import leap.core.security.ClientPrincipal;
import leap.core.security.Credentials;
import leap.core.security.UserPrincipal;
import leap.web.security.authc.AbstractAuthentication;

public class ClientOnlyAuthentication extends AbstractAuthentication {

    protected Credentials     credentials;
    protected ClientPrincipal client;

    public ClientOnlyAuthentication(Credentials credentials, ClientPrincipal client) {
        this.credentials = credentials;
        this.client      = client;
    }

    @Override
    public boolean isRememberMe() {
        return false;
    }

    @Override
    public Credentials getCredentials() {
        return credentials;
    }

    @Override
    public UserPrincipal getUser() {
        return null;
    }

    @Override
    public ClientPrincipal getClient() {
        return client;
    }

    @Override
    public String toString() {
        return "Client[id=" + client.getIdAsString() + "]";
    }
}

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
package leap.oauth2.server.store;

import leap.oauth2.server.client.OAuth2Client;
import leap.oauth2.server.client.OAuth2ClientStore;
import leap.oauth2.server.code.OAuth2AuthzCodeStore;
import leap.oauth2.server.sso.OAuth2SSOStore;
import leap.oauth2.server.token.OAuth2TokenStore;

public interface OAuth2InMemoryStore extends OAuth2ClientStore, OAuth2AuthzCodeStore, OAuth2TokenStore, OAuth2SSOStore {
    
    /**
     * Adds a client.
     */
    OAuth2InMemoryStore addClient(OAuth2Client client);
    
    /**
     * Adds a client. 
     */
    OAuth2InMemoryStore addClient(String clientId, String clientSecret, String redirectUri);
    
    /**
     * Removes the client from memory.
     * 
     * <p>
     * Returns the removed client if exists or <code>null</code> if not exists. 
     */
    OAuth2Client removeClient(String clientId);
    
}
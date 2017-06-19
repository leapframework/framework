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
package leap.oauth2.as.store;

import leap.oauth2.as.client.AuthzClient;
import leap.oauth2.as.client.AuthzClientStore;
import leap.oauth2.as.code.AuthzCodeStore;
import leap.oauth2.as.sso.AuthzSSOStore;
import leap.oauth2.as.token.AuthzTokenStore;

public interface AuthzInMemoryStore extends AuthzClientStore, AuthzCodeStore, AuthzTokenStore, AuthzSSOStore {
    
    /**
     * Adds a client.
     */
    AuthzInMemoryStore addClient(AuthzClient client);
    
    /**
     * Adds a client. 
     */
    AuthzInMemoryStore addClient(String clientId, String clientSecret, String redirectUri);
    
    /**
     * Removes the client from memory.
     * 
     * <p>
     * Returns the removed client if exists or <code>null</code> if not exists. 
     */
    AuthzClient removeClient(String clientId);
    
}
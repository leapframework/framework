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
package leap.oauth2.server.client;

import leap.core.annotation.Inject;
import leap.lang.Out;
import leap.lang.Strings;

public class DefaultOAuth2ClientAuthenticator implements OAuth2ClientAuthenticator {

    protected @Inject OAuth2ClientManager clientManager;
    
    @Override
    public boolean authenticate(OAuth2ClientCredentials credentials, Out<OAuth2Client> out) throws Throwable {
        String clientId     = credentials.getClientId();
        String clientSecret = credentials.getClientSecret();
        
        if(Strings.isEmpty(clientId) || Strings.isEmpty(clientSecret)) {
            return true;
        }
        
        OAuth2Client client = clientManager.loadClientById(clientId);
        if(null == client) {
            return true;
        }
        
        if(!Strings.equals(client.getSecret(), clientSecret)) {
            return false;
        }
        
        out.set(client);
        return true;
    }

}
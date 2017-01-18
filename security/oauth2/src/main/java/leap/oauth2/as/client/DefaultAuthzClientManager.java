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
package leap.oauth2.as.client;

import leap.core.annotation.Inject;
import leap.lang.Out;
import leap.lang.Result;
import leap.oauth2.OAuth2Errors;
import leap.oauth2.as.OAuth2AuthzServerConfig;

public class DefaultAuthzClientManager implements AuthzClientManager {

    protected @Inject OAuth2AuthzServerConfig    config;
    protected @Inject AuthzClientAuthenticator[] authenticators;
    
    @Override
    public AuthzClient authenticate(AuthzClientAuthenticationContext context, AuthzClientCredentials credentials) throws Throwable {

        AuthzClient client = loadClientById(credentials.getClientId());
        if(client == null){
            context.addError("invalid_clientId","client not found");
            return null;
        }
        if(!client.isEnabled()){
            context.addError("invalid_clientId","client diabled");
            return null;
        }
        for(AuthzClientAuthenticator a : authenticators) {
            if(a.authenticate(credentials, client)) {
                return client;
            }
        }
        context.addError("invalid_clientId","client_secret invalid");
        return null;
    }

    @Override
    public AuthzClient loadClientById(String clientId) {
	    return config.getClientStore().loadClient(clientId);
    }

}
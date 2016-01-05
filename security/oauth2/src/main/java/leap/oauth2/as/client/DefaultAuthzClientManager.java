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
import leap.oauth2.as.OAuth2AuthzServerConfig;

public class DefaultAuthzClientManager implements AuthzClientManager {

    protected @Inject OAuth2AuthzServerConfig    config;
    protected @Inject AuthzClientAuthenticator[] authenticators;
    
    @Override
    public Result<AuthzClient> authenticate(AuthzClientCredentials credentials) throws Throwable {
        Out<AuthzClient> client = new Out<AuthzClient>();

        for(AuthzClientAuthenticator a : authenticators) {
            if(a.authenticate(credentials, client)) {
                break;
            }
        }
        
        return client;
    }

    @Override
    public AuthzClient loadClientById(String clientId) {
	    return config.getClientStore().loadClient(clientId);
    }

}
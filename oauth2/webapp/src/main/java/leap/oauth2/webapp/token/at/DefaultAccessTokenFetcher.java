/*
 *
 *  * Copyright 2013 the original author or authors.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  
 */

package leap.oauth2.webapp.token.at;

import leap.core.annotation.Inject;
import leap.lang.http.client.HttpClient;
import leap.lang.http.client.HttpRequest;
import leap.oauth2.webapp.OAuth2Config;
import leap.oauth2.webapp.code.DefaultCodeVerifier;

/**
 * @author kael.
 */
public class DefaultAccessTokenFetcher extends DefaultCodeVerifier implements AccessTokenFetcher {
    
    protected @Inject HttpClient client;
    protected @Inject OAuth2Config config;
    
    @Override
    public AccessToken fetchTokenByClientCredentials(String clientId, String clientSecret) {
        if(null == config.getTokenUrl()) {
            throw new IllegalStateException("The tokenUrl must be configured");
        }
        HttpRequest request = client.request(config.getTokenUrl())
                .addFormParam("grant_type", "client_credentials");
        return fetchAccessToken(request);
    }
}

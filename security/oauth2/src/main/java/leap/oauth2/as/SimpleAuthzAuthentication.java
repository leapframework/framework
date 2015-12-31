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
package leap.oauth2.as;

import leap.oauth2.OAuth2Params;
import leap.oauth2.as.client.AuthzClient;
import leap.web.security.authc.Authentication;
import leap.web.security.user.UserDetails;

public class SimpleAuthzAuthentication implements AuthzAuthentication {

    protected AuthzClient    client;
    protected UserDetails    user;
    protected OAuth2Params   params;
    protected Authentication authc;
    
    public SimpleAuthzAuthentication(OAuth2Params params, AuthzClient client){
        this.params = params;
        this.client = client;
    }

    public SimpleAuthzAuthentication(OAuth2Params params, AuthzClient client, UserDetails user){
        this(params, client, user, null);
    }

    public SimpleAuthzAuthentication(OAuth2Params params, AuthzClient client, UserDetails user, Authentication authc){
        this.params = params;
        this.client = client;
        this.user = user;
        this.authc = authc;
    }

    @Override
    public AuthzClient getClient() {
        return client;
    }

    @Override
    public UserDetails getUser() {
        return user;
    }

    @Override
    public OAuth2Params getParams() {
        return params;
    }

    @Override
    public Authentication getAuthentication() {
        return authc;
    }
}

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
package leap.oauth2.server.authc;

import leap.oauth2.server.OAuth2Params;
import leap.oauth2.server.client.AuthzClient;
import leap.core.security.Authentication;
import leap.web.security.user.UserDetails;

public class SimpleAuthzAuthentication implements AuthzAuthentication {

    protected AuthzClient    clientDetails;
    protected UserDetails    userDetails;
    protected OAuth2Params   params;
    protected Authentication authc;
    
    public SimpleAuthzAuthentication(OAuth2Params params, AuthzClient client){
        this.params = params;
        this.clientDetails = client;
    }

    public SimpleAuthzAuthentication(OAuth2Params params, AuthzClient client, UserDetails user){
        this(params, client, user, null);
    }

    public SimpleAuthzAuthentication(OAuth2Params params, AuthzClient client, UserDetails user, Authentication authc){
        this.params = params;
        this.clientDetails = client;
        this.userDetails = user;
        this.authc = authc;
    }

    @Override
    public AuthzClient getClientDetails() {
        return clientDetails;
    }

    @Override
    public UserDetails getUserDetails() {
        return userDetails;
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

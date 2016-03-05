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
package leap.web.api.meta;

import leap.lang.Args;

import java.util.Map;

public class OAuth2ApiSecurityDef extends ApiSecurityDef {
    
    protected final String        authzEndpointUrl;
    protected final String        tokenEndpointUrl;
    protected final OAuth2Scope[] scopes;
    
    public OAuth2ApiSecurityDef(String authzEndpointUrl, String tokenEndpointUrl, OAuth2Scope[] scopes) {
        this(authzEndpointUrl,tokenEndpointUrl, scopes, null);
    }

    public OAuth2ApiSecurityDef(String authzEndpointUrl, String tokenEndpointUrl, OAuth2Scope[] scopes, Map<String, Object> attrs) {
        super(attrs);
        
        Args.notEmpty(authzEndpointUrl, "authorization endpoint url");
        Args.notEmpty(tokenEndpointUrl, "token endpoint url");
        
        this.authzEndpointUrl = authzEndpointUrl;
        this.tokenEndpointUrl = tokenEndpointUrl;
        this.scopes           = scopes;
    }

    /**
     * The url of authorization endpoint.
     */
    public String getAuthzEndpointUrl() {
        return authzEndpointUrl;
    }
    
    /**
     * The url of token endpoint.
     */
    public String getTokenEndpointUrl() {
        return tokenEndpointUrl;
    }

    /**
     * The oauth2 scopes of api.
     */
    public OAuth2Scope[] getScopes() {
        return scopes;
    }

}
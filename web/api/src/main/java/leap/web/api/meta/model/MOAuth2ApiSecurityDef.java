/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package leap.web.api.meta.model;

import leap.lang.Args;

import java.util.Map;

public class MOAuth2ApiSecurityDef extends MApiSecurityDef {
    
    protected final String authzEndpointUrl;
    protected final String tokenEndpointUrl;

    public MOAuth2ApiSecurityDef(String authzEndpointUrl, String tokenEndpointUrl) {
        this(authzEndpointUrl,tokenEndpointUrl, null);
    }

    public MOAuth2ApiSecurityDef(String authzEndpointUrl, String tokenEndpointUrl, Map<String, Object> attrs) {
        super(attrs);
        
        Args.notEmpty(authzEndpointUrl, "authorization endpoint url");
        Args.notEmpty(tokenEndpointUrl, "token endpoint url");
        
        this.authzEndpointUrl = authzEndpointUrl;
        this.tokenEndpointUrl = tokenEndpointUrl;
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

    @Override
    public boolean isOAuth2() {
        return true;
    }
}
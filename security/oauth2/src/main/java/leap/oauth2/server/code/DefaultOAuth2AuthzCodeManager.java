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
package leap.oauth2.server.code;

import leap.core.annotation.Inject;
import leap.oauth2.server.authc.OAuth2Authentication;
import leap.oauth2.server.OAuth2ServerConfig;
import leap.oauth2.server.client.OAuth2Client;
import leap.web.security.user.UserDetails;

public class DefaultOAuth2AuthzCodeManager implements OAuth2AuthzCodeManager {
    
    protected @Inject OAuth2ServerConfig       config;
    protected @Inject OAuth2AuthzCodeGenerator codeGenerator;

    @Override
    public OAuth2AuthzCode createAuthorizationCode(OAuth2Authentication authc) {
        //Geneate code string
        String codeString = codeGenerator.generateAuthorizationCode(authc);
        
        //Creates code object.
        SimpleOAuth2AuthzCode code = new SimpleOAuth2AuthzCode();
        code.setCode(codeString);
        code.setExpiresIn(config.getDefaultAuthorizationCodeExpires());
        code.setCreated(System.currentTimeMillis());
        
        OAuth2Client client = authc.getClientDetails();
        if(null != client) {
            code.setClientId(client.getId());    
        }

        UserDetails user = authc.getUserDetails();
        code.setUserId(user.getId().toString());
        
        //Store it.
        config.getCodeStore().saveAuthorizationCode(code);
        
        return code;
    }

    @Override
    public OAuth2AuthzCode consumeAuthorizationCode(String code) {
        return config.getCodeStore().removeAuthorizationCode(code);
    }

    @Override
    public void removeAuthorizationCode(OAuth2AuthzCode code) {
        config.getCodeStore().removeAuthorizationCode(code.getCode());
    }
    
}
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
import leap.oauth2.server.authc.AuthzAuthentication;
import leap.oauth2.server.OAuth2AuthzServerConfig;
import leap.oauth2.server.client.AuthzClient;
import leap.oauth2.server.sso.AuthzSSOSession;
import leap.web.security.user.UserDetails;

public class DefaultAuthzCodeManager implements AuthzCodeManager {

    protected @Inject OAuth2AuthzServerConfig config;
    protected @Inject AuthzCodeGenerator      codeGenerator;

    @Override
    public AuthzCode createAuthorizationCode(AuthzAuthentication authc, AuthzSSOSession session) {
    	AuthzCode code=genAuthorizationCode(authc,session);

        //Store it.
        config.getCodeStore().saveAuthorizationCode(code);

        return code;
    }

    protected SimpleAuthzCode genAuthorizationCode(AuthzAuthentication authc, AuthzSSOSession session){
    	//Geneate code string
        String codeString = codeGenerator.generateAuthorizationCode(authc);

        //Creates code object.
        SimpleAuthzCode code = new SimpleAuthzCode();
        code.setCode(codeString);
        
        if(session != null){
            code.setSessionId(session.getId());
        }
        
        code.setExpiresIn(config.getDefaultAuthorizationCodeExpires());
        code.setCreated(System.currentTimeMillis());
        AuthzClient client = authc.getClientDetails();
        if(null != client) {
            code.setClientId(client.getId());
        }

        UserDetails user = authc.getUserDetails();
        code.setUserId(user.getId().toString());

        return code;
    }

    @Override
    public AuthzCode consumeAuthorizationCode(String code) {
        return config.getCodeStore().removeAndLoadAuthorizationCode(code);
    }

    @Override
    public void removeAuthorizationCode(AuthzCode code) {
        config.getCodeStore().removeAndLoadAuthorizationCode(code.getCode());
    }

}
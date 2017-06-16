/*
 * Copyright 2014 the original author or authors.
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
package resapp;

import leap.core.annotation.Inject;
import leap.core.security.token.jwt.JwtVerifier;
import leap.oauth2.TokenVerifierFactory;
import leap.oauth2.rs.OAuth2ResServerConfigurator;
import leap.web.App;
import leap.web.config.WebConfigurator;

/**
 * resource server only.
 */
public class Global extends App {
    
    protected @Inject OAuth2ResServerConfigurator rsc;

    @Override
    protected void configure(WebConfigurator c) {
        String publicKeyUrl = "https://localhost:8443/server/publickey";

        JwtVerifier verifier = TokenVerifierFactory.createNetPublicKeyRSAJwtVerifier(publicKeyUrl);

        rsc.enable()
                .useJwtVerifier(verifier)
                .useRemoteAuthorizationServer().setResourceServerId("resource_server_id")
                .setRemoteTokenInfoEndpointUrl("https://localhost:8443/server/oauth2/tokeninfo")
                .setRemoteUserInfoEndpointUrl("https://localhost:8443/server/oauth2/userinfo");
    }

}
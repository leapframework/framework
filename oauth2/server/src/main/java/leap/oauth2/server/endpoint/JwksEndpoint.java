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

package leap.oauth2.server.endpoint;

import leap.core.annotation.ConfigProperty;
import leap.core.annotation.Configurable;
import leap.core.annotation.Inject;
import leap.core.annotation.M;
import leap.core.security.token.jwt.JwtSigner;
import leap.core.validation.annotations.NotNull;
import leap.oauth2.server.endpoint.jwks.JwkToken;
import leap.oauth2.server.endpoint.jwks.JwkWriter;
import leap.oauth2.server.endpoint.jwks.JwksToken;
import leap.web.*;
import leap.web.route.Routes;

import java.util.stream.Stream;

/**
 * support jwk specification, compatible spring oauth2 config.
 *
 * @author kael.
 * @see <a href="https://docs.spring.io/spring-security-oauth2-boot/docs/current/reference/htmlsingle/#boot-features-security-oauth2-resource-server">OAuth2 Autoconfig</a>
 * <p>
 * this endpoint will be call when use spring <code>security.oauth2.resource.jwt.key-uri</code> configuration.
 */
@Configurable(prefix = "oauth2.as.jwks")
public class JwksEndpoint extends AbstractAuthzEndpoint implements Endpoint, Handler {
    @ConfigProperty(key = "jwksPath", defaultValue = "/oauth2/token_keys")
    private String[] jwksPath;
    
    protected @Inject @M JwksToken jwksToken;
    
    @Override
    public void startEndpoint(App app, Routes routes) throws Throwable {
        if (config.isEnabled()) {
            Stream.of(jwksPath).distinct().forEach(s -> {
                sc.ignore(s);
                routes.create()
                        .handle(s, this).disableCsrf().enableCors()
                        .apply();
            });
        }
    }

    @Override
    public void handle(Request request, Response response) throws Throwable {
        JwkWriter.create(request).write(jwksToken);
    }

}

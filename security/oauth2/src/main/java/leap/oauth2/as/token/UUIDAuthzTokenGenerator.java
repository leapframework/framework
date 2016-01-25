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
package leap.oauth2.as.token;

import java.util.UUID;

import leap.oauth2.OAuth2Constants;
import leap.oauth2.as.authc.AuthzAuthentication;

public class UUIDAuthzTokenGenerator implements AuthzRefreshTokenGenerator,AuthzAccessTokenGenerator, AuthzLoginTokenGenerator {

    @Override
    public String generateAccessToken(AuthzAuthentication authc) {
        return generateUUID();
    }

    @Override
    public String generateRefreshToken(AuthzAuthentication authc) {
        return generateUUID();
    }

    @Override
    public String generateLoginToken(AuthzAuthentication authc) {
        return OAuth2Constants.V3TOKEN_PREFIX+generateUUID();
    }

    protected String generateUUID() {
        return UUID.randomUUID().toString();
    }

}
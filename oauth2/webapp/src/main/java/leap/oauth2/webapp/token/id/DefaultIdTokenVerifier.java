/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package leap.oauth2.webapp.token.id;

import leap.core.annotation.Inject;
import leap.core.security.SimpleUserPrincipal;
import leap.core.security.token.TokenVerifyException;
import leap.core.security.token.jwt.JWT;
import leap.core.security.token.jwt.MacSigner;
import leap.oauth2.webapp.OAuth2Params;
import leap.oauth2.webapp.OAuth2Config;

import java.util.Map;

public class DefaultIdTokenVerifier implements IdTokenVerifier {

    protected @Inject OAuth2Config config;

    @Override
    public IdToken verifyIdToken(OAuth2Params params, String token) throws TokenVerifyException {
        MacSigner signer = new MacSigner(config.getClientSecret());

        Map<String, Object> claims = signer.verify(token);
        SimpleIdToken idToken = new SimpleIdToken(token);

        idToken.setClientId((String)claims.remove(JWT.CLAIM_AUDIENCE));
        idToken.setUserId((String)claims.remove(JWT.CLAIM_SUBJECT));

        SimpleUserPrincipal user = new SimpleUserPrincipal();
        user.setId(idToken.getUserId());
        user.setName((String)claims.remove("name"));
        user.setLoginName((String)claims.remove("login_name"));

        //todo: more details user properties ?

        idToken.setUserInfo(user);

        return idToken;
    }

}
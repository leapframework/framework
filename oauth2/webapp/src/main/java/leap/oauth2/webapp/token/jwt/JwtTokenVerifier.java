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

package leap.oauth2.webapp.token.jwt;

import leap.core.AppConfigException;
import leap.core.annotation.Inject;
import leap.core.security.token.TokenVerifyException;
import leap.core.security.token.jwt.JWT;
import leap.core.security.token.jwt.JwtVerifier;
import leap.core.security.token.jwt.RsaVerifier;
import leap.lang.Strings;
import leap.lang.http.client.HttpClient;
import leap.lang.http.client.HttpResponse;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.security.RSA;
import leap.oauth2.webapp.OAuth2Config;
import leap.oauth2.webapp.OAuth2InternalServerException;
import leap.oauth2.webapp.token.SimpleTokenInfo;
import leap.oauth2.webapp.token.Token;
import leap.oauth2.webapp.token.TokenInfo;
import leap.oauth2.webapp.token.TokenVerifier;
import leap.web.security.SecurityConfig;

import java.security.interfaces.RSAPublicKey;
import java.util.Map;

public class JwtTokenVerifier implements TokenVerifier {

    private static final Log log = LogFactory.get(JwtTokenVerifier.class);

    protected @Inject SecurityConfig sc;
    protected @Inject OAuth2Config   config;
    protected @Inject HttpClient     httpClient;

    private volatile JwtVerifier verifier;

    @Override
    public TokenInfo verifyToken(Token token) throws TokenVerifyException {
        if(null == verifier) {
            if(Strings.isEmpty(config.getPublicKeyUrl())) {
                throw new AppConfigException("publicKeyUrl must be configured");
            }
            refreshJwtVerifier();
        }

        return verify(verifier, token.getToken());
    }

    protected void refreshJwtVerifier() {
        log.info("Fetching public key from server, url '{}' ...", config.getPublicKeyUrl());
        HttpResponse response = httpClient.request(config.getPublicKeyUrl()).get();
        if(!response.isOk()) {
            throw new OAuth2InternalServerException("Error fetching public key from server, status " + response.getStatus() + "");
        }

        String       encoded   = response.getString();
        RSAPublicKey publicKey = RSA.decodePublicKey(encoded);

        verifier = new RsaVerifier(publicKey);
    }

    protected TokenInfo verify(JwtVerifier verifier, String token) throws TokenVerifyException {
        Map<String,Object> jwtDetail;

        try {
            jwtDetail = verifier.verify(token);
        }catch (TokenVerifyException e) {
            refreshJwtVerifier();
            jwtDetail = verifier.verify(token);
        }

        SimpleTokenInfo tokenInfo = new SimpleTokenInfo();

        String userId   = (String)jwtDetail.get(JWT.CLAIM_SUBJECT);
        tokenInfo.setUserId(userId);
        tokenInfo.setScope((String)jwtDetail.get("scope"));
        tokenInfo.setClientId((String)jwtDetail.get(JWT.CLAIM_AUDIENCE));

        tokenInfo.setCreated(System.currentTimeMillis());
        Object exp = jwtDetail.get(JWT.CLAIM_EXPIRATION_TIME);
        if (null != exp && exp instanceof Number) {
            long expirationTimeSecond = ((Number) exp).longValue();
            long nowTimeInSecond = System.currentTimeMillis()/1000L;
            tokenInfo.setExpiresIn((int)(expirationTimeSecond-nowTimeInSecond));
        }
        tokenInfo.setClaims(jwtDetail);
        return tokenInfo;
    }

}

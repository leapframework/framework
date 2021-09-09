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
import leap.lang.New;
import leap.lang.Strings;
import leap.lang.annotation.Nullable;
import leap.lang.codec.Base64;
import leap.lang.http.client.HttpClient;
import leap.lang.http.client.HttpResponse;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.security.RSA;
import leap.oauth2.webapp.OAuth2Config;
import leap.oauth2.webapp.OAuth2InternalServerException;
import leap.oauth2.webapp.token.*;
import leap.web.security.SecurityConfig;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.JsonWebKeySet;
import org.jose4j.lang.JoseException;
import java.security.interfaces.RSAPublicKey;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.jose4j.jwk.JsonWebKey.*;

public class JwtTokenVerifier implements TokenVerifier {

    private static final Log log = LogFactory.get(JwtTokenVerifier.class);

    protected @Inject SecurityConfig  sc;
    protected @Inject OAuth2Config    config;
    protected @Inject HttpClient      httpClient;
    protected @Inject TokenInfoLookup tokenInfoLookup;
    
    protected @Inject @Nullable JwksSelector selector;

    private volatile JwtVerifier verifier;

    @Override
    public TokenInfo verifyToken(Token token) throws TokenVerifyException {
        if (config.isDecryptJwt()) {
            if (null == verifier) {
                refreshJwtVerifier(token.getToken());
            }
            return verify(verifier, token.getToken());
        }
        return tokenInfoLookup.lookupByAccessToken(token.getToken());
    }

    protected void refreshJwtVerifier(String token) {
        if (!Strings.isEmpty(config.getJwksUrl())) {
            refreshJwtVerifierFromJwks(token, config.getJwksUrl());
        } else {
            if (Strings.isEmpty(config.getPublicKeyUrl())) {
                throw new AppConfigException("publicKeyUrl must be configured");
            }

            log.info("Fetching public key from server, url '{}' ...", config.getPublicKeyUrl());
            HttpResponse response = httpClient.request(config.getPublicKeyUrl()).get();
            if (!response.isOk()) {
                throw new OAuth2InternalServerException("Error fetching public key from server, status " + response.getStatus() + "");
            }

            String encoded = response.getString();
            RSAPublicKey publicKey = RSA.decodePublicKey(encoded);
            verifier = new RsaVerifier(publicKey);
        }
    }

    protected void refreshJwtVerifierFromJwks(String token, String jwksUrl) {
        log.info("Fetching jwks from url '{}' ...", jwksUrl);
        HttpResponse response = httpClient.request(jwksUrl).get();
        if (!response.isOk()) {
            throw new OAuth2InternalServerException("Error fetching jwks from server, status " + response.getStatus() + "");
        }

        try {
            JsonWebKey jwk = null;
            final List<JsonWebKey> jwks = new JsonWebKeySet(response.getString()).getJsonWebKeys();
            if (jwks.size() != 1) {
                if (Strings.isNotEmpty(config.getJwksKeyId())){
                    Optional<JsonWebKey> optional = jwks.stream().filter(jsonWebKey -> Strings.equals(config.getJwksKeyId(),jsonWebKey.getKeyId())).findAny();
                    if (optional.isPresent()){
                        jwk = optional.get();
                    }
                }
                if (null == jwk){
                    if (null != selector){
                        Optional<JsonWebKey> optional = jwks.stream().filter(jsonWebKey -> selector.test(jwkToMap(jsonWebKey))).findAny();
                        if (optional.isPresent()){
                            jwk = optional.get();
                        }
                    }
                }
                if (null == jwk){
                    throw new OAuth2InternalServerException("server response multi jwks, but jwt verifier can not select ensure one.");
                }
            }else {
                jwk = jwks.get(0);
            }
            
            if (!(jwk.getKey() instanceof RSAPublicKey)) {
                throw new OAuth2InternalServerException("Non RSA public key '" + jwk.getKey() + "' not supported");
            }

            verifier = new RsaVerifier((RSAPublicKey) jwk.getKey());
        } catch (JoseException e) {
            throw new OAuth2InternalServerException("Invalid jwks : " + e.getMessage());
        }
    }

    protected Map<String, Object> jwkToMap(JsonWebKey jwk){
        Map<String, Object> map = New.hashMap();
        map.put(KEY_TYPE_PARAMETER, jwk.getKeyType());
        map.put(USE_PARAMETER, jwk.getUse());
        map.put(KEY_ID_PARAMETER, jwk.getKeyId());
        map.put(ALGORITHM_PARAMETER, jwk.getAlgorithm());
        map.put(KEY_OPERATIONS, jwk.getKeyOps());
        map.put("key", Base64.encode(jwk.getKey().getEncoded()));
        return map;
    }
    
    protected TokenInfo verify(JwtVerifier verifier, String token) throws TokenVerifyException {
        Map<String, Object> jwtDetail;

        try {
            jwtDetail = verifier.verify(token);
        } catch (TokenVerifyException e) {
            refreshJwtVerifier(token);
            jwtDetail = verifier.verify(token);
        }

        return createTokenInfo(jwtDetail);
    }

    protected TokenInfo createTokenInfo(Map<String, Object> jwtDetail) {
        SimpleTokenInfo tokenInfo = new SimpleTokenInfo();

        String userId = (String) jwtDetail.get(JWT.CLAIM_SUBJECT);
        tokenInfo.setUserId(userId);
        tokenInfo.setScope((String) jwtDetail.get("scope"));
        tokenInfo.setClientId((String) jwtDetail.get("client_id"));

        /* todo:
        if(Strings.isEmpty(tokenInfo.getClientId())) {
            tokenInfo.setClientId((String)jwtDetail.get(JWT.CLAIM_AUDIENCE));
        }
        */

        tokenInfo.setCreated(System.currentTimeMillis());
        Object exp = jwtDetail.get(JWT.CLAIM_EXPIRATION_TIME);
        if (null != exp && exp instanceof Number) {
            long expirationTimeSecond = ((Number) exp).longValue();
            long nowTimeInSecond = System.currentTimeMillis() / 1000L;
            tokenInfo.setExpiresIn((int) (expirationTimeSecond - nowTimeInSecond));
        }
        tokenInfo.setClaims(jwtDetail);
        return tokenInfo;
    }
}

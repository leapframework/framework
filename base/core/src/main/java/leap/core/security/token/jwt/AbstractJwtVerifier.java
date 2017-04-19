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
package leap.core.security.token.jwt;

import java.util.Map;

import leap.core.security.token.TokenExpiredException;
import leap.core.security.token.TokenVerifyException;
import leap.core.security.token.TokenVerifyException.ErrorCode;
import leap.lang.Args;
import leap.lang.Strings;
import leap.lang.json.JSON;
import leap.lang.json.JsonValue;

public abstract class AbstractJwtVerifier implements JwtVerifier {

    @Override
    public Map<String, Object> verify(String token) throws TokenVerifyException {
        Args.notEmpty(token, "token");

        String[] parts = Strings.split(token, '.');
        if (parts.length < 2 || parts.length > 3) {
            throw new TokenVerifyException(ErrorCode.INVALID_TOKEN, "Invalid jwt token, wrong number of parts: " + parts.length);
        }

        String content;
        String payload;
        String signature;

        if (parts.length == 2) {
            content = parts[0];
            payload = parts[0];
            signature = parts[1];
        } else {
            content = parts[0] + "." + parts[1];
            payload = parts[1];
            signature = parts[2];
        }

        if (payload.isEmpty() || signature.isEmpty()) {
            throw new TokenVerifyException(ErrorCode.INVALID_TOKEN, "Invalid jwt token, both payload and signature parts must not be empty");
        }

        return verify(content, payload, signature);
    }

    protected Map<String, Object> verify(String content, String payload, String signature) {
        if (!verifySignature(content, signature)) {
            throw new TokenVerifyException(ErrorCode.INVALID_SIGNATURE, "Signature verification failed");
        }

        JsonValue json;
        try {
            json = JSON.parse(JWT.base64UrlDeocodeToString(payload));
        } catch (Exception e) {
            throw new TokenVerifyException(ErrorCode.INVALID_PAYLOAD, "Parse payload as json object failed, " + e.getMessage());
        }

        if (!json.isMap()) {
            throw new TokenVerifyException(ErrorCode.INVALID_PAYLOAD, "The payload must be json object '{..}'");
        }

        //get claims
        Map<String, Object> claims = json.asMap();

        //verify expiration
        verifyExpiration(claims);

        return claims;
    }
    
    protected void verifyExpiration(Map<String, Object> claims) {
        Object exp = claims.get(JWT.CLAIM_EXPIRATION_TIME);
        if (null != exp && exp instanceof Long) {
            long expirationTimeSecond = (Long) exp;
            long nowTimeInSecond = System.currentTimeMillis()/1000L;
            if(expirationTimeSecond <= 0 || nowTimeInSecond >= expirationTimeSecond){
                throw new TokenExpiredException("Token expired");
            }
        }
    }

    protected abstract boolean verifySignature(String content, String signed);
}

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
package leap.oauth2.as.openid;

import java.util.LinkedHashMap;
import java.util.Map;

import leap.core.annotation.Inject;
import leap.core.security.token.jwt.JWT;
import leap.core.security.token.jwt.JwtSigner;
import leap.core.security.token.jwt.MacSigner;
import leap.lang.New;
import leap.lang.Strings;
import leap.oauth2.OAuth2Params;
import leap.oauth2.as.authc.AuthzAuthentication;
import leap.oauth2.as.OAuth2AuthzServerConfig;
import leap.oauth2.as.client.AuthzClient;
import leap.web.security.user.UserDetails;

public class JwtIdTokenGenerator implements IdTokenGenerator {

    protected @Inject OAuth2AuthzServerConfig config;

    @Override
    public String generateIdToken(AuthzAuthentication authc) {
        return generateIdToken(authc,New.hashMap());
    }

    @Override
    public String generateIdToken(AuthzAuthentication authc, Map<String, Object> extend) {
        return generateIdToken(authc, extend, config.getDefaultIdTokenExpires());
    }

    @Override
    public String generateIdToken(AuthzAuthentication authc, Map<String, Object> extend, int expiresIn) {
        JwtSigner           signer = getJwtSigner(authc, expiresIn);
        Map<String, Object> claims = getJwtClaims(authc, extend, expiresIn);
        
        return signer.sign(claims);
    }
    
    protected JwtSigner getJwtSigner(AuthzAuthentication authc, int expires) {
        AuthzClient client  = authc.getClientDetails();
        
        return new MacSigner(client.getSecret(), expires);
    }
    
    protected Map<String, Object> getJwtClaims(AuthzAuthentication authc, Map<String, Object> extend, int expiresIn) {
        OAuth2Params params = authc.getParams();
        AuthzClient client = authc.getClientDetails();
        UserDetails user   = authc.getUserDetails();
        
        Map<String, Object> claims = new LinkedHashMap<String, Object>();
        
        /* Example claims in Open ID Connnect.
          {
           "iss": "http://server.example.com",
           "sub": "248289761001",
           "aud": "s6BhdRkqt3",
           "nonce": "n-0S6_WzA2Mj",
           "exp": 1311281970,
           "iat": 1311280970,
           "name": "Jane Doe",
           "given_name": "Jane",
           "family_name": "Doe",
           "gender": "female",
           "birthdate": "0000-10-31",
           "email": "janedoe@example.com",
           "picture": "http://example.com/janedoe/me.jpg"
          }
         */
        
        claims.put(JWT.CLAIM_AUDIENCE, client.getId());
        claims.put(JWT.CLAIM_SUBJECT,  user.getId().toString());
        claims.put(JWT.CLAIM_EXPIRATION_TIME, System.currentTimeMillis()/1000L+expiresIn);
        claims.put("name",             user.getName());
        claims.put("login_name",       user.getLoginName());
        
        //TODO : other user properties

        String nonce = params.getNonce();
        if(!Strings.isEmpty(nonce)) {
            claims.put(OAuth2Params.NONCE, nonce);
        }
        
        if(extend != null){
            extend.forEach((s, o) -> claims.put(s,o));
        }
        
        return claims;
    }

}
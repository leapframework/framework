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

import leap.core.annotation.Inject;
import leap.core.security.token.TokenVerifyException;
import leap.core.security.token.jwt.JwtVerifier;
import leap.oauth2.webapp.OAuth2Config;
import leap.oauth2.webapp.token.AccessToken;
import leap.oauth2.webapp.token.AccessTokenDetails;
import leap.oauth2.webapp.token.AccessTokenStore;
import leap.oauth2.webapp.token.SimpleAccessTokenDetails;
import leap.web.security.SecurityConfig;
import leap.web.security.user.UserDetails;

import java.security.interfaces.RSAPublicKey;
import java.util.Map;
import java.util.Objects;

public class JwtAccessTokenStore implements AccessTokenStore {

    protected         RSAPublicKey   publicKey;
    protected @Inject SecurityConfig sc;
    protected @Inject OAuth2Config   rsc;

    @Override
    public AccessTokenDetails loadAccessTokenDetails(AccessToken token) {
        JwtVerifier verifier = rsc.getJwtVerifier();
        if(verifier == null){
            throw new TokenVerifyException(TokenVerifyException.ErrorCode.VERIFY_FAILED, "the jwt verifier must be specified!");
        }
        Map<String,Object> jwtDetail = verifier.verify(token.getToken());
        SimpleAccessTokenDetails resAccessTokenDetails = new SimpleAccessTokenDetails(token.getToken());
        
        Object userId = jwtDetail.remove("user_id");
        UserDetails ud;
        if(userId != null){
            ud = sc.getUserStore().loadUserDetailsById(userId);
        }else{
            String username = Objects.toString(jwtDetail.remove("username"));
            ud = sc.getUserStore().loadUserDetailsByLoginName(username);
        }
        if(ud == null){
            return null;
        }
        resAccessTokenDetails.setUserId(ud==null?null:ud.getIdAsString());
        resAccessTokenDetails.setScope((String)jwtDetail.remove("scope"));
        resAccessTokenDetails.setClientId((String)jwtDetail.remove("client_id"));
        //TODO How to ensure is expired?
        resAccessTokenDetails.setCreated(System.currentTimeMillis());
        try {
            Object expiresIn = jwtDetail.get("expires_in");
            if(expiresIn == null){
                //todo:
                throw new IllegalStateException("'expires_in' not found in jwt token");
            }else{
                int second = expiresIn instanceof Integer?(Integer)expiresIn:Integer.parseInt(expiresIn.toString());
                resAccessTokenDetails.setExpiresIn(second * 1000);
            }
        } catch (NumberFormatException e) {
            //todo :
            throw new IllegalStateException("Invalid expires_in : " + e.getMessage(), e);
        }
        return resAccessTokenDetails;
    }

    @Override
    public void removeAccessToken(AccessToken token) {
        //Do nothing
    }

    public RSAPublicKey getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(RSAPublicKey publicKey) {
        this.publicKey = publicKey;
    }
}

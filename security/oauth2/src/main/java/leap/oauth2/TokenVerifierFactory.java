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

package leap.oauth2;

import leap.core.security.token.TokenVerifyException;
import leap.core.security.token.jwt.JwtVerifier;
import leap.core.security.token.jwt.RsaVerifier;
import leap.lang.Assert;
import leap.lang.Strings;
import leap.lang.http.client.HttpClient;
import leap.lang.http.client.HttpResponse;
import leap.lang.http.client.JdkHttpClient;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.security.RSA;

import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Map;

/**
 * Created by kael on 2017/1/12.
 */
public class TokenVerifierFactory {
    public static <T extends RSAPublicKey> JwtVerifier createRSAJwtVerifier(T publicKey){
        return new RsaVerifier(publicKey);
    }
    public static <T extends RSAPublicKey> JwtVerifier createRSAJwtVerifier(PublicKeyGetter<T> getter){
        return new RsaVerifier(getter.getPublicKey());
    }
    public static JwtVerifier createNetPublicKeyRSAJwtVerifier(String url){
        return new PublicKeyGetterRSAJwtVerifier(new NetPublicKeyGetter(url),2);
    }
    
    interface PublicKeyGetter<T extends PublicKey>{
        T getPublicKey();
    }
    
    protected static class NetPublicKeyGetter implements PublicKeyGetter<RSAPublicKey>{
        
        private final String url;
        private final HttpClient client;
        private String publicKeyStr;
        
        public NetPublicKeyGetter(String url) {
            this.url = url;
            this.client = new JdkHttpClient();
        }

        @Override
        public RSAPublicKey getPublicKey() {
            publicKeyStr = getPublicKeyStr();
            return RSA.decodePublicKey(publicKeyStr);
        }
        
        protected String getPublicKeyStr() {
            HttpResponse response = client.request(url).get();
            if(!response.is2xx()){
                throw new RuntimeException("get public key from url error,error status:"+response.getStatus()
                        +", error message:"+response.getString());
            }
            String publicKeyStr = response.getString();
            if(Strings.isEmpty(publicKeyStr)){
                throw new RuntimeException("get public key from url error, the public key is empty!");
            }
            return publicKeyStr;
        }
    }
    
    protected static class PublicKeyGetterRSAJwtVerifier implements JwtVerifier{
        
        protected final Log log = LogFactory.get(PublicKeyGetterRSAJwtVerifier.class);
        
        protected JwtVerifier verifier;
        protected int repeatCount;
        protected PublicKeyGetter<RSAPublicKey> getter;

        public PublicKeyGetterRSAJwtVerifier(
                PublicKeyGetter<RSAPublicKey> getter, int repeatCount) {
            Assert.notNull(getter,"public key getter can not be null.");
            this.getter = getter;
            try {
                this.verifier = getVerifier();
            } catch (Throwable throwable) {
                log.warn("create verifier error",throwable);
            }
            this.repeatCount = repeatCount;
        }

        @Override
        public Map<String, Object> verify(String token) throws TokenVerifyException {
            if(this.verifier == null){
                this.verifier = getVerifier();
            }
            Map<String, Object> claims = null;
            TokenVerifyException error = null;
            for(int i = 0; i < this.repeatCount; i ++){
                try {
                    claims = this.verifier.verify(token);
                    break;
                }catch (TokenVerifyException t){
                    log.info("verifier token error for " + i + " times.");
                    error = t;
                    this.verifier = getVerifier();
                }
            }
            if(claims == null && error != null){
                throw error;
            }
            
            return claims;
        }
        
        public void setRepeatCount(int count){
            this.repeatCount = count;
        }
        
        protected JwtVerifier getVerifier(){
            return new RsaVerifier(this.getter.getPublicKey());
        }
        
    }
}

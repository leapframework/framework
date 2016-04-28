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
package leap.lang.security;

import java.io.Serializable;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class RSA {
    
    public static final String      ALGORITHM        = "RSA";
    public static final int         DEFAULT_KEY_SIZE = 1024;
    
    private static KeyPairGenerator g;
    private static KeyFactory       f;
    
    static {
        try {
            g = KeyPairGenerator.getInstance(ALGORITHM);
            g.initialize(DEFAULT_KEY_SIZE);
            
            f = KeyFactory.getInstance(ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e.getMessage(), e); 
        }
    }
    
    /**
     * Generates a new {@link RsaKeyPair}.
     */
    public static RsaKeyPair generateKeyPair() {
        try {
            KeyPair kp = g.generateKeyPair();
            return new RsaKeyPair((RSAPrivateKey)kp.getPrivate(), (RSAPublicKey)kp.getPublic());
        } catch (Exception e) {
            throw new RuntimeException("Error generating key pair using alg '" + ALGORITHM + "' : " + e.getMessage(), e);
        }
    }
    
    public static String encodePrivateKey(RSAPrivateKey key) {
        return new String(java.util.Base64.getMimeEncoder().encode(key.getEncoded()));
    }
    
    public static String encodePublicKey(RSAPublicKey key) {
        return new String(Base64.getMimeEncoder().encode(key.getEncoded()));
    }
    
    public static RSAPrivateKey decodePrivateKey(String base64){
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(Base64.getMimeDecoder().decode(base64));
        try {
            return (RSAPrivateKey)f.generatePrivate(spec);
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException(e.getMessage(),e);
        }
    }
    
    public static RSAPublicKey decodePublicKey(String base64){
        X509EncodedKeySpec spec = new X509EncodedKeySpec(Base64.getMimeDecoder().decode(base64));
        try {
            return (RSAPublicKey)f.generatePublic(spec);
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException(e.getMessage(),e);
        }
    }
    
    public static final class RsaKeyPair implements Serializable {

        private static final long serialVersionUID = 8336210331967156219L;
        
        private final RSAPrivateKey privateKey;
        private final RSAPublicKey  publicKey;
        
        public RsaKeyPair(RSAPrivateKey privateKey, RSAPublicKey publicKey) {
            this.privateKey = privateKey;
            this.publicKey  = publicKey;
        }

        public RSAPrivateKey getPrivateKey() {
            return privateKey;
        }

        public RSAPublicKey getPublicKey() {
            return publicKey;
        }
        
        public String getBase64PrivateKey() {
            return encodePrivateKey(privateKey);
        }
        
        public String getBase64PublicKey() {
            return encodePublicKey(publicKey);
        }
    }

    protected RSA() {
        
    }
    
}
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

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import leap.junit.TestBase;
import leap.lang.security.RSA.RsaKeyPair;

import org.junit.Test;

public class RsaTest extends TestBase {

    @Test
    public void testRsa() {
        
        RsaKeyPair kp = RSA.generateKeyPair();
        
        String privateKeyBase64 = kp.getBase64PrivateKey();
        String publicKeyBase64  = kp.getBase64PublicKey();
        
        RSAPrivateKey privateKey = RSA.decodePrivateKey(privateKeyBase64);
        RSAPublicKey  publicKey  = RSA.decodePublicKey(publicKeyBase64);
        
        assertEquals(privateKeyBase64, RSA.encodePrivateKey(privateKey));
        assertEquals(publicKeyBase64,  RSA.encodePublicKey(publicKey));
        
    }
    
}
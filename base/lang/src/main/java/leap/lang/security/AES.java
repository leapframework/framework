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

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import leap.lang.Randoms;
import leap.lang.codec.Base64;

public class AES {
    
    public static final String ALGORITHM    = "AES";
    public static final int    DEFAULT_SIZE = 256;
    
    private static final KeyGenerator g;
    static {
        try {
            g = KeyGenerator.getInstance(ALGORITHM);
            g.init(DEFAULT_SIZE, new SecureRandom(Randoms.nextString(20).getBytes()));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
        
    }
    
    public static SecretKey generateKey() {
        return g.generateKey();
    }
    
    public static String encodeSecretKey(SecretKey key) {
        return Base64.encode(key.getEncoded());
    }
    
    public static SecretKey decodeSecretKey(String encoded) {
        return new SecretKeySpec(Base64.decodeToBytes(encoded), ALGORITHM);
    }
    
    protected AES() {
        
    }

}

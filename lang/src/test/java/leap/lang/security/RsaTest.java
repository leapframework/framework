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

import leap.junit.TestBase;
import leap.lang.security.RSA.RsaKeyPair;
import org.junit.Test;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

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

    @Test
    public void testDecodeOpenssl() {

        String privateKeyStringPkcs8 = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAMMBI6MhZ7EumdeI\n" +
                "k0kXakvo3M98GgXS0oSva20CQn/t/e5QeGPY0iFDTVa706Md9eMK9BJcxaqpzX47\n" +
                "0bVTAHZeyVtY3ARkUzCZB0/lWV4ZsktHfA6XcjN2CCTEEnVD2tdSVW2yUr8Kwy2T\n" +
                "CRASDR5kMfVV4leooGUSGw1yRggZAgMBAAECgYEAkTsC+JaDbHEhgGUmFFSNBPXr\n" +
                "pdduZhHqWYlv/2KkdjSgXuGtl+c+MCm4qrT+XMiOfUaGGjrfuEf4VGBmwZ2MhE8p\n" +
                "DuHYzJo4qxbEPYMkx5Ka4bT5LqHZTkcPJATFPySrbHrtztOeDj4XnCh6zjM0zJ5Y\n" +
                "/52ogeS1NrqonBqiupECQQDhAaoKceuovr12dwFS3K5LDX+nlJRIcfpIzejsUkbE\n" +
                "FyO1o9mjfkh+lWSpp8yFxCC8dUfHT9STr0hyFqegPElFAkEA3d2FlGztMG4rq6YZ\n" +
                "ISsxRBH3kpvfxY1tQx91PRIqaJGBvcxVg92zPGYf5KYxyIifkMmF4tOH1SRTiPK7\n" +
                "6i5uxQJAZDiAi8x4Qh5Ld6PENCtVetqVXIhij+4spBcLQ4/WM2t3HWXQ//C0y6Ux\n" +
                "RBwYjhBw4GCAyzc0oV92rlKG8WlI3QJAbz4PKCillM7onpwdpX/ep97KX3xAavFK\n" +
                "g4lWY1SkZuHuR5gYmJGkuPgLrb2W4JGAUDx7IQwm4zvflp7+kaDOcQJAQl/Reuou\n" +
                "yna44mbvMCLWuW4tA04NH/RNXwTtesFsvwcJYtXlLid2ifIadRMGdzs9bGCNF0Kb\n" +
                "rG8n6FjfxjWLXA==";

        String publicKeyString = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDDASOjIWexLpnXiJNJF2pL6NzP\n" +
                "fBoF0tKEr2ttAkJ/7f3uUHhj2NIhQ01Wu9OjHfXjCvQSXMWqqc1+O9G1UwB2Xslb\n" +
                "WNwEZFMwmQdP5VleGbJLR3wOl3IzdggkxBJ1Q9rXUlVtslK/CsMtkwkQEg0eZDH1\n" +
                "VeJXqKBlEhsNckYIGQIDAQAB";

        RSA.decodePrivateKey(privateKeyStringPkcs8);
        RSA.decodePublicKey(publicKeyString);
    }
    
}
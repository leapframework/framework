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
package leap.web.security.jwt;

import java.util.HashMap;
import java.util.Map;

import leap.core.security.token.jwt.MacSigner;
import leap.core.security.token.jwt.RsaSigner;
import leap.core.security.token.jwt.RsaVerifier;
import leap.junit.TestBase;
import leap.lang.Randoms;
import leap.lang.security.RSA;
import leap.lang.security.RSA.RsaKeyPair;

import org.junit.Test;

import com.auth0.jwt.JWTSigner;

public class JwtTokenTest extends TestBase {
	
	@Test
	public void testHS256Sign() {
		String secret = Randoms.nextString(10);
		
		JWTSigner signer0 = new JWTSigner(secret);
		MacSigner signer1 = new MacSigner(secret);

		Map<String, Object> claims = new HashMap<String, Object>();
		claims.put("aaa", "bbb");
		claims.put("exp", System.currentTimeMillis() + 1000L * 1000);
		
		String token0 = signer0.sign(claims);
		String token1 = signer1.sign(claims);
		
		assertEquals(token0, token1);
	}

	@Test
	public void testHS256SignAndVerify() {
		String secret = Randoms.nextString(10);
		MacSigner jwt = new MacSigner(secret);
		
		Map<String, Object> claims = new HashMap<String, Object>();
		claims.put("aaa", "bbb");
		claims.put("exp", System.currentTimeMillis() + 1000L * 1000);

		String token = jwt.sign(claims);
		
		Map<String, Object> decodedClaims = jwt.verify(token);
		
		assertEquals(claims.get("aaa"),decodedClaims.get("aaa"));
		assertEquals(claims.get("exp"),decodedClaims.get("exp"));
	}
	
    @Test
    public void testRS256Sign() {
        RsaKeyPair kp = RSA.generateKeyPair();
        
        RsaSigner signer = new RsaSigner(kp.getPrivateKey());
        
        Map<String, Object> claims = new HashMap<String, Object>();
        claims.put("aaa", "bbb");
        claims.put("exp", System.currentTimeMillis() + 1000L * 1000);
        
        String token = signer.sign(claims);
        
        assertNotEmpty(token);
    }
    
    @Test
    public void testRS256SignAndVerify() {
        
        RsaKeyPair kp = RSA.generateKeyPair();
        
        RsaSigner   signer   = new RsaSigner(kp.getPrivateKey());
        RsaVerifier verifier = new RsaVerifier(kp.getPublicKey());
        
        Map<String, Object> claims = new HashMap<String, Object>();
        claims.put("aaa", "bbb");
        claims.put("exp", System.currentTimeMillis() + 1000L * 1000);
        
        String token = signer.sign(claims);
        
        Map<String, Object> decodedClaims = verifier.verify(token);
        
        assertEquals(claims.get("aaa"),decodedClaims.get("aaa"));
        assertEquals(claims.get("exp"),decodedClaims.get("exp"));
    }
}
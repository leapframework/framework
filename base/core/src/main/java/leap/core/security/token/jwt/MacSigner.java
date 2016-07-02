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

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import leap.core.security.token.TokenSignatureException;
import leap.core.security.token.TokenVerifyException;
import leap.lang.Args;

public class MacSigner extends AbstractJwtSigner implements JwtVerifier {
	
	public static final String ALG_HMACSHA256 = "HMACSHA256";
	
    protected final String     jwtAlgorithm;
    protected final String     macAlgorithm;
    protected final SecretKey  secretKey;

	protected JwtVerifier verifier;
	
	public MacSigner(String secret) {
		this(JWT.ALG_HS256, ALG_HMACSHA256, new SecretKeySpec(secret.getBytes(), ALG_HMACSHA256));
	}
	
    public MacSigner(String secret, int defaultExpires) {
        this(JWT.ALG_HS256, ALG_HMACSHA256, new SecretKeySpec(secret.getBytes(), ALG_HMACSHA256));
        this.defaultExpires = defaultExpires;
    }
	
	public MacSigner(String jwtAlgorithm, String macAlgorithm, SecretKey secretKey) {
		Args.notEmpty(jwtAlgorithm,"jwt algorithm");
		Args.notEmpty(macAlgorithm,"mac algorithm");
		Args.notNull(secretKey,"secret key");
		this.jwtAlgorithm = jwtAlgorithm;
		this.macAlgorithm = macAlgorithm;
		this.secretKey    = secretKey;
	}

	@Override
    protected String getJwtAlgorithm() {
	    return jwtAlgorithm;
    }

	@Override
	protected byte[] signToBytes(String payload) {
		return sign(macAlgorithm,secretKey,payload);
	}
	
    @Override
    public Map<String, Object> verify(String token) throws TokenVerifyException {
        if(null == verifier) {
            verifier = new Verifier();
        }
        return verifier.verify(token);
    }

    protected class Verifier extends AbstractJwtVerifier {
        @Override
        protected boolean verifySignature(String content, String signature) {
            return sign(content).equals(signature);
        }
    }
	
	static byte[] sign(String alg, SecretKey key, String data) {
		try {
	        Mac mac = Mac.getInstance(alg);
	        mac.init(key);
	        return mac.doFinal(data.getBytes());
        } catch (Exception e) {
        	throw new TokenSignatureException("Error signing data using algorithm '" + alg + ", " + e.getMessage(), e);
        }
	}

}

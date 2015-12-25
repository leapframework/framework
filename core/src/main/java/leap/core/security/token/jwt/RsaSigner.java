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

import java.security.GeneralSecurityException;
import java.security.Signature;
import java.security.interfaces.RSAPrivateKey;

import leap.core.security.token.TokenSignatureException;
import leap.lang.Args;

public class RsaSigner extends AbstractJwtSigner {
	
	public static final String ALG_SHA256_WITH_RSA = "SHA256withRSA";
	
	protected final String        jwtAlgorithm;
	protected final String        rsaAlgorithm;
	protected final RSAPrivateKey privateKey;
	
	public RsaSigner(RSAPrivateKey privateKey) {
		this(JWT.ALG_RS256, ALG_SHA256_WITH_RSA, privateKey);
	}
	
    public RsaSigner(RSAPrivateKey privateKey, int defaultExpires) {
        this(JWT.ALG_RS256, ALG_SHA256_WITH_RSA, privateKey);
        this.defaultExpires = defaultExpires;
    }
	
	public RsaSigner(String jwtAlgorithm, String rsaAlgorithm, RSAPrivateKey privateKey) {
		Args.notEmpty(jwtAlgorithm,"jwt algorithm");
		Args.notEmpty(rsaAlgorithm,"rsa algorithm");
		Args.notNull(privateKey,"private key");
		this.jwtAlgorithm = jwtAlgorithm;
		this.rsaAlgorithm = rsaAlgorithm;
		this.privateKey   = privateKey;
	}

	@Override
    protected String getJwtAlgorithm() {
	    return jwtAlgorithm;
    }

	@Override
	protected byte[] signToBytes(String payload) {
		return sign(rsaAlgorithm,privateKey,payload);
	}
	
	static byte[] sign(String alg, RSAPrivateKey key, String data) {
        try {
            Signature signature = Signature.getInstance(alg);
            signature.initSign(key);
            signature.update(data.getBytes());
            return signature.sign();
        } catch (GeneralSecurityException e) {
            throw new TokenSignatureException("Error signing data using algorithm '" + alg + ", " + e.getMessage(), e);
        }
	}

}
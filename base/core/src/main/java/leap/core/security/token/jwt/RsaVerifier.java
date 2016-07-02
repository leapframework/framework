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
import java.security.interfaces.RSAPublicKey;

import leap.lang.Args;

public class RsaVerifier extends AbstractJwtVerifier {
    
    protected RSAPublicKey publicKey;
    protected String       rsaAlgorithm;
    
    public RsaVerifier(RSAPublicKey publicKey) {
        this(publicKey, RsaSigner.ALG_SHA256_WITH_RSA);
    }
    
    public RsaVerifier(RSAPublicKey publicKey, String rsaAlgorithm) {
        Args.notNull(publicKey,"private key");
        Args.notEmpty(rsaAlgorithm,"rsa algorithm");
        this.publicKey    = publicKey;
        this.rsaAlgorithm = rsaAlgorithm;
    }

    @Override
    protected boolean verifySignature(String content, String signed) {
        try {
            byte[] signedData  = JWT.base64UrlDeocode(signed);
            byte[] contentData = content.getBytes();
            
            Signature signature = Signature.getInstance(rsaAlgorithm);
            signature.initVerify(publicKey);
            signature.update(contentData);

            return signature.verify(signedData);
        } catch (GeneralSecurityException e) {
            return false;
        }
    }

}
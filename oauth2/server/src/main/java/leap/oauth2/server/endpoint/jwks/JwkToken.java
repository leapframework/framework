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

package leap.oauth2.server.endpoint.jwks;

/**
 * @author kael.
 * @see <a href="https://tools.ietf.org/html/rfc7517#section-4">RFC7517#section-4</a>
 * @see <a href="https://auth0.com/docs/tokens/reference/jwt/jwks-properties">auth0#JSON Web Key Set Properties</a>
 * @see <a href="https://tools.ietf.org/html/rfc3447">RFC3447</a>
 */
public interface JwkToken {
    /**
     * The family of cryptographic algorithms used with the key.
     * <p>
     * example: RSA
     * <p>
     * {@link leap.core.validation.annotations.NotEmpty}
     */
    String getKty();
    
    /**
     * value of key
     *
     * {@link leap.core.validation.annotations.NotEmpty}
     */
    String getValue();

    /**
     * The unique identifier for the key.
     * <p>
     * {@link leap.core.validation.annotations.NotEmpty}
     */
    String getKid();
    
    /**
     * How the key was meant to be used; sig represents the signature.
     * <p>
     * options:
     * <ul>
     *     <li>sig</li>
     *     <li>enc</li>
     * </ul>
     * <p>
     * {@link leap.lang.annotation.Nullable}
     */
    default String getUse() {
        return null;
    }

    /**
     * The "key_ops" (key operations) parameter identifies the operation(s)
     * for which the key is intended to be used.  The "key_ops" parameter is
     * intended for use cases in which public, private, or symmetric keys
     * may be present.
     * <p>
     * Its value is an array of key operation values.  Values defined by
     * this specification are:
     * <p>
     * o  "sign" (compute digital signature or MAC)
     * o  "verify" (verify digital signature or MAC)
     * o  "encrypt" (encrypt content)
     * o  "decrypt" (decrypt content and validate decryption, if applicable)
     * o  "wrapKey" (encrypt key)
     * o  "unwrapKey" (decrypt key and validate decryption, if applicable)
     * o  "deriveKey" (derive key)
     * o  "deriveBits" (derive bits not to be used as a key)
     * <p>
     * {@link leap.lang.annotation.Nullable}
     */
    default String getKeyOps() {
        return null;
    }
    
    /**
     * The specific cryptographic algorithm used with the key.
     * <p>
     * {@link leap.lang.annotation.Nullable}
     */
    default String getAlg(){
        return null;
    }

    /**
     * The "x5u" (X.509 URL) parameter is a URI that refers to a resource for an X.509 public key certificate or certificate chain.
     * <p>
     * {@link leap.lang.annotation.Nullable}
     * <p>
     * {@link leap.lang.annotation.Nullable}
     */
    default String getX5u(){
        return null;
    }

    /**
     * The x.509 certificate chain. The first entry in the array is the certificate to use for token verification; the other certificates can be used to verify this first certificate.
     * <p>
     * {@link leap.lang.annotation.Nullable}
     */
    default String getX5c(){
        return null;
    }

    /**
     * The thumbprint of the x.509 cert (SHA-1 thumbprint).
     * <p>
     * {@link leap.lang.annotation.Nullable}
     */
    default String getX5t(){
        return null;
    }
    
    /**
     * The modulus for the RSA public key.
     * 
     * {@link leap.lang.annotation.Nullable}
     */
    default String getN(){
        return null;
    }

    /**
     * The exponent for the RSA public key.
     * 
     * {@link leap.lang.annotation.Nullable}
     */
    default String getE(){
        return null;
    }
}

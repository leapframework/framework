/*
 * Copyright 2011 the original author or authors.
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
package leap.core.security.crypto;

import leap.lang.Bytes;
import leap.lang.Strings;
import leap.lang.codec.Digester;
import leap.lang.codec.Hex;
import leap.lang.codec.Utf8;

/**
 * A standard {@code PasswordEncoder} implementation that uses SHA-256 hashing with 1024 iterations and a
 * random 8-byte random salt value. It uses an additional system-wide secret value to provide additional protection.
 * <p>
 * The digest algorithm is invoked on the concatenated bytes of the salt, secret and password.
 * <p>
 * If you are developing a new system, {@link org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder} is
 * a better choice both in terms of security and interoperability with other languages.
 *
 * @author Keith Donald
 * @author Luke Taylor
 */
public final class StandardPasswordEncoder implements PasswordEncoder {

    private final Digester digester;

    private final byte[] secret;

    private final BytesKeyGenerator saltGenerator;

    /**
     * Constructs a standard password encoder with no additional secret value.
     */
    public StandardPasswordEncoder() {
        this("");
    }

    /**
     * Constructs a standard password encoder with a secret value which is also included in the
     * password hash.
     *
     * @param secret the secret key used in the encoding process (should not be shared)
     */
    public StandardPasswordEncoder(String secret) {
        this("SHA-256", secret);
    }

    public String encode(String rawPassword) {
        return encode(rawPassword, saltGenerator.generateKey());
    }

    public boolean matches(String rawPassword, String encodedPassword) {
        byte[] digested = decode(encodedPassword);
        byte[] salt = Bytes.subArray(digested, 0, saltGenerator.getKeyLength());
        return matches(digested, digest(rawPassword, salt));
    }

    // internal helpers

    private StandardPasswordEncoder(String algorithm, String secret) {
    	if(null == secret){
    		secret = Strings.EMPTY;
    	}
        this.digester = new Digester(algorithm, DEFAULT_ITERATIONS);
        this.secret = Utf8.encode(secret);
        this.saltGenerator = KeyGenerators.secureRandom();
    }

    private String encode(String rawPassword, byte[] salt) {
        byte[] digest = digest(rawPassword, salt);
        return new String(Hex.encode(digest));
    }

    private byte[] digest(String rawPassword, byte[] salt) {
        byte[] digest = digester.digest(Bytes.concat(salt, secret, Utf8.encode(rawPassword)));
        return Bytes.concat(salt, digest);
    }

    private byte[] decode(String encodedPassword) {
        return Hex.decode(encodedPassword);
    }

    /**
     * Constant time comparison to prevent against timing attacks.
     */
    private boolean matches(byte[] expected, byte[] actual) {
        if (expected.length != actual.length) {
            return false;
        }

        int result = 0;
        for (int i = 0; i < expected.length; i++) {
            result |= expected[i] ^ actual[i];
        }
        return result == 0;
    }

    private static final int DEFAULT_ITERATIONS = 1024;
}

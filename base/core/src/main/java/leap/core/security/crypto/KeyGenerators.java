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

import java.security.SecureRandom;

//from spring-security-crypto
/**
 * Factory for commonly used key generators.
 * Public API for constructing a {@link BytesKeyGenerator} or {@link StringKeyGenerator}.
 * @author Keith Donald
 */
class KeyGenerators {

    /**
     * Create a {@link BytesKeyGenerator} that uses a {@link SecureRandom} to generate keys of 8 bytes in length.
     */
    public static BytesKeyGenerator secureRandom() {
        return new SecureRandomBytesKeyGenerator();
    }

    /**
     * Create a {@link BytesKeyGenerator} that uses a {@link SecureRandom} to generate keys of a custom length.
     * @param keyLength the key length in bytes, e.g. 16, for a 16 byte key.
     */
    public static BytesKeyGenerator secureRandom(int keyLength) {
        return new SecureRandomBytesKeyGenerator(keyLength);
    }

    // internal helpers

    private KeyGenerators() {
    }
}

/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tests.core.crypto;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import leap.core.security.crypto.StandardPasswordEncoder;

import org.junit.Test;

//from spring-security-crypto
public class StandardPasswordEncoderTests {

    private StandardPasswordEncoder encoder = new StandardPasswordEncoder("secret");

    @Test
    public void matches() {
        String result = encoder.encode("password");
        assertFalse(result.equals("password"));
        assertTrue(encoder.matches("password", result));
    }

    @Test
    public void matchesLengthChecked() {
        String result = encoder.encode("password");
        assertFalse(encoder.matches("password", result.substring(0,result.length()-2)));
    }

    @Test
    public void notMatches() {
        String result = encoder.encode("password");
        assertFalse(encoder.matches("bogus", result));
    }

}

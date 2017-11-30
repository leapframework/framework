/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package leap.lang.convert;

import leap.junit.TestBase;
import org.junit.Test;

public class StringConvertTest extends TestBase {

    @Test
    public void testStringToBoolean() {
        assertEquals(Boolean.TRUE, Converts.convert("true", Boolean.TYPE));
        assertEquals(Boolean.TRUE, Converts.convert("true", Boolean.class));
    }

}

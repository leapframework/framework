/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tests;

import app.TestFilter;
import org.junit.Test;

import java.util.Map;

public class SimpleTest extends AbstractTest {

    @Test
    public void testSimpleFilter() {
        Map<String, Object> state = TestFilter.state;

        client.get("/?p=1").assertSuccess();
        assertEquals(Boolean.TRUE, state.get("action_executed"));
        assertEquals("1", state.get("p"));

        client.get("/?p=2").assertSuccess();
        assertEquals("2", state.get("p"));
    }

}

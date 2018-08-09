/*
 *  Copyright 2018 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package tests.api1;

import leap.lang.New;
import leap.lang.json.JsonValue;
import org.junit.Test;
import tests.ApiTestCase;

import java.util.List;

public class Api1Test extends ApiTestCase {

    @Test
    public void testListString() {
        doTestListString("/api1/sample/list_string1");
        doTestListString("/api1/sample/list_string2");
    }

    private void doTestListString(String path) {
        JsonValue json = postJson(path, New.arrayList("1", "2")).getJson();

        assertTrue(json.isArray());

        List<String> ids = json.asList();
        assertEquals("1", ids.get(0));
        assertEquals("2", ids.get(1));
    }

}

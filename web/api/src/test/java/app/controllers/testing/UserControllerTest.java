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

package app.controllers.testing;

import leap.webunit.WebTestBase;
import org.junit.Test;

import java.util.Map;

public class UserControllerTest extends WebTestBase {

    @Test
    public void testSelectExclude() {
        Map<String,Object> user = get("/testing/user/safe").decodeJsonArray(Map.class)[0];
        assertFalse(user.containsKey("password"));

        user = get("/testing/user").decodeJsonArray(Map.class)[0];
        assertTrue(user.containsKey("password"));
    }

}

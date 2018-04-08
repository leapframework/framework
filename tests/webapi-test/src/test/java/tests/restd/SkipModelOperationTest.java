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

package tests.restd;

import org.junit.Test;
import tests.ApiTestCase;

public class SkipModelOperationTest extends ApiTestCase {

    @Test
    public void testSkipFindOperationIfExists() {
        get("/restd/model3/1").assertStatusEquals(501);
    }

    @Test
    public void testSkipCreateOperationIfExists() {
        post("/restd/model3").assertStatusEquals(501);
    }

    @Test
    public void testSkipUpdateOperationIfExists() {
        patch("/restd/model3/1").assertStatusEquals(501);
    }

    @Test
    public void testSkipDeleteOperationIfExists() {
        delete("/restd/model3/1").assertStatusEquals(501);
    }

    @Test
    public void testSkipQueryOperationIfExists() {
        get("/restd/model3").assertStatusEquals(501);
    }
}

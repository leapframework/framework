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

package app.interceptors;

import leap.web.WebTestCase;
import org.junit.Test;

public class RoutesInterceptorTest extends WebTestCase {

    @Test
    public void testExternalRouter() {
        get("/test_router").assert404();

        get("/test_router/simple/action1?p=1").assertContentEquals("1");
        get("/test_router/restful/action1?p=2").assert404();
        get("/test_router/restful/action2?p=3").assertContentEquals("3");
        get("/test_router/action1?p=4").assertContentEquals("4");

        //not found
        get("/test_router/404").assertContentEquals("404");
        get("/test_router/404?not_found=1").assertContentEquals("not found");
    }

}

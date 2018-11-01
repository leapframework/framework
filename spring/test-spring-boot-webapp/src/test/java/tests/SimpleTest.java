/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package tests;

import leap.webunit.client.OkTHttpClient;
import leap.webunit.client.THttpClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import tapp.Application;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class SimpleTest {

    protected final THttpClient client = new OkTHttpClient(8080);

    @Test
    public void testIndex() {
        client.get("/").assertContentContains("<h1>hello</h1>");
        client.get("/index").assertContentContains("<h1>hello</h1>");
    }

    @Test
    public void testHello() {
        client.get("/hello").assertContentContains("<p>Hello</p>");
        client.get("/hello.html").assertContentContains("<p>Hello</p>");
    }

    @Test
    public void testOAuth2View() {
        client.get("/oauth2/login").assertSuccess();
    }

    @Test
    public void testStatic() {
        client.get("/t.js").assertContentContains("function _(){}");
        client.get("/static/t.js").assertContentContains("function _(){}");
    }
}
/*
 * Copyright 2017 the original author or authors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *       http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tests;

import app.models.api.RestApi;
import leap.lang.json.JSON;
import leap.webunit.WebTestBase;
import leap.webunit.client.THttpResponse;
import org.junit.Test;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.Map;
import java.util.UUID;

public class JsonpCallbackTest extends WebTestBase {
    @Test
    public void testGetJsonpCallbackWithHeader() throws ScriptException {
        RestApi api1 = new RestApi();
        api1.setPublished(true);
        api1.setName(UUID.randomUUID().toString());
        api1.create();
        THttpResponse response = useGet("/api/restapi")
                .addQueryParam("callback","func")
                .addQueryParam("total","true").send();
        String content = response.getContent();
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName( "JavaScript" );
        String func = "function func(a,b){" +
                "var JSON = Java.type('leap.lang.json.JSON');" +
                "var result = JSON.encode(b);" +
                "return result" +
                "};";
        Object obj = engine.eval(func+content);
        Map<String, Object> headers = JSON.decodeMap(obj.toString());
        assertNotEmpty(headers);
        assertEquals(headers.size(),1);
        assertTrue(headers.containsKey("X-Total-Count"));
        headers.forEach((s, o) -> assertEquals(response.getHeader(s),o));
    }
}

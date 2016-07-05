/*
 * Copyright 2015 the original author or authors.
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
package leap.lang.http;

import java.util.List;
import java.util.Map;

import org.junit.Test;

import leap.junit.TestBase;

public class QueryStringParserTest extends TestBase {
    
    @Test
    public void testEmpty() {
        Map<String, List<String>> params = QueryStringParser.parseMap(null);
        assertTrue(params.isEmpty());
        
        params = QueryStringParser.parseMap("");
        assertTrue(params.isEmpty());
    }
    
    @Test
    public void testSingle() {
        Map<String, List<String>> params = QueryStringParser.parseMap("a=b&c=d%20e");
        assertEquals(2, params.size());
        assertEquals("b", params.get("a").get(0));
        assertEquals("d e",params.get("c").get(0));
    }
    
    @Test
    public void testMulti() {
        Map<String, List<String>> params = QueryStringParser.parseMap("a=b&c=d&a=e");
        assertEquals(2, params.size());
        assertEquals("b", params.get("a").get(0));
        assertEquals("e", params.get("a").get(1));
        assertEquals("d",params.get("c").get(0));
    }
    
    @Test
    public void testEmptyValue() {
        Map<String, List<String>> params = QueryStringParser.parseMap("a=&b=&c&d");
        assertEquals(4, params.size());
        assertEquals("", params.get("a").get(0));
        assertEquals("", params.get("b").get(0));
        assertEquals("", params.get("c").get(0));
        assertEquals("", params.get("d").get(0));
    }

}

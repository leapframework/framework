/*
 * Copyright 2013 the original author or authors.
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
package leap.lang.collection;

import java.util.HashMap;
import java.util.Map;

import leap.junit.TestBase;
import leap.junit.contexual.ContextualProvider;
import leap.junit.contexual.ContextualRule;
import leap.lang.reflect.Reflection;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.Description;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class CaseInsensitiveMapTest extends TestBase {
	
	private static final Map<String, Class<? extends CaseSensitiveMap>> maps = new HashMap<String, Class<? extends CaseSensitiveMap>>();
	
	static {
		maps.put(SimpleCaseInsensitiveMap.class.getSimpleName(), SimpleCaseInsensitiveMap.class);
		maps.put(WrappedCaseInsensitiveMap.class.getSimpleName(), WrappedCaseInsensitiveMap.class);
	}

	private CaseSensitiveMap<Object> map;
	
	@Rule
	public final ContextualRule rule = new ContextualRule(new ContextualProvider() {
		@Override
		public Iterable<String> names(Description description) {
			return maps.keySet();
		}
		
		@Override
		public void finishTests(Description description) throws Exception {
			
		}
		
		@Override
		public void beforeTest(Description description, String name) throws Exception {
			map = Reflection.newInstance(maps.get(name));
	        map.put("one", "1");
	        map.put("two", "2");
		}
		
		@Override
		public void afterTest(Description description, String name) throws Exception {
			map = null;
		}
	}, false);
	
    @Test
    public void testContainsKey() {
        assertTrue("1", map.containsKey("ONE"));
        assertTrue("2", map.containsKey("one"));
        assertTrue("3", !map.containsKey("onex"));
    }

    @Test
    public void testGet() {
        assertEquals("1", "1", map.get("ONE"));
        assertEquals("2", "1", map.get("One"));
        assertEquals("3", null, map.get("hoge"));
    }

    @Test
    public void testPut() {
        assertEquals("1", "1", map.put("One", "11"));
        assertEquals("2", "11", map.get("one"));
    }

    @Test
    public void testRemove() {
        assertEquals("1", "1", map.remove("ONE"));
        assertEquals("2", 1, map.size());
        assertEquals("3", null, map.remove("dummy"));
    }

    @Test
    public void testPutAll() {
        Map m = new HashMap();
        m.put("three", "3");
        m.put("four", "4");
        map.putAll(m);
        assertEquals("1", "3", map.get("THREE"));
        assertEquals("2", "4", map.get("FOUR"));
        assertEquals("3", 4, map.size());
    }
}

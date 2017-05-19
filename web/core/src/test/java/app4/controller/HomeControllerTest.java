/*
 * Copyright 2016 the original author or authors.
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

/*
 * Copyright 2014 the original author or authors.
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
package app4.controller;

import leap.lang.json.JSON;
import leap.web.WebTestCase;
import org.junit.Test;

import java.util.Map;

public class HomeControllerTest extends WebTestCase {
	@Test
	public void testGetMapWithLowerUnderscore(){
		String json = get("/app4/mvc/map").getContent();
		Map<String, Object> map = JSON.decode(json);
		assertEquals("userId", map.get("user_id"));
		assertEquals("userName", ((Map<String, Object>)map.get("user_property")).get("user_name"));
	}

	@Test
	public void testGetBeanWithJsonProcessorDefined(){
		String json = get("/app4/mvc/bean1origin").getContent();
		Map<String, Object> map = JSON.decode(json);
		assertEquals("test", map.get("prop1"));
		assertNull(map.get("prop2"));

		json = get("/app4/mvc/bean1").getContent();
		map = JSON.decode(json);
		assertEquals("test", map.get("prop1"));
		assertEquals("", map.get("prop2"));

		json = get("/app4/mvc/bean1array").getContent();
		map = JSON.decodeArray(json, Map.class)[0];
		assertEquals("test", map.get("prop1"));
		assertEquals("", map.get("prop2"));

	}

	@Test
	public void testMultiSlashPath() {
		String json = get("/app4/mvc//map").getContent();
		Map<String, Object> map = JSON.decode(json);
		assertEquals("userId", map.get("user_id"));
		assertEquals("userName", ((Map<String, Object>)map.get("user_property")).get("user_name"));
	}
}
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
package leap.lang.yaml;

import java.util.Map;

import leap.junit.TestBase;

import org.junit.Test;

public class YamlTest extends TestBase {

	@Test
	public void testParseSimpleMap() {
		String yaml = "p: value";
		YamlObject o = YAML.parse(yaml).asYamlObject();
		assertEquals("value",o.get("p"));
	}
	
	@Test
	public void testParseScalar() {
		String yaml = "x";
		YamlScalar s = YAML.parse(yaml).asYamlScalar();
		assertEquals(s.raw(), "x");
	}
	
	@Test
	public void testParseSimpleCollection() {
		String yaml = "- v";
		YamlCollection c = YAML.parse(yaml).asYamlCollection();
		assertEquals(1,c.size());
		assertEquals("v",c.get(0));
	}
	
	@Test
	public void testParseNestedMap() {
		String yaml = "p1: v1\np2: v2\np3:\n np1: nv1\n np2: nv2";
		YamlObject o = YAML.parse(yaml).asYamlObject();
		
		Map<String, Object> np = o.get("p3");
		assertEquals("nv1", np.get("np1"));
	}
}

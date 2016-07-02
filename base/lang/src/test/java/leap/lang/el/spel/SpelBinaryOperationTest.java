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
package leap.lang.el.spel;

import java.util.Map;

import leap.lang.New;

import org.junit.Test;

public class SpelBinaryOperationTest extends SpelTestCase {

	@Test
	public void testStringAdd() {
		assertEquals("1" + "2",eval("'1' + '2'"));
		assertEquals(3L,eval("'1' + 2"));
		assertEquals(3L,eval("1 + '2'"));
	}
	
	@Test
	public void testNullAdd() {
		assertEquals(new Long(0), eval("a + b"));
	}
	
	@Test
	public void testCharEq() {
		Map<String, Object> vars = New.hashMap("c",new Character('s'));
		assertEquals(Boolean.TRUE,eval("c == 's'", vars));
	}
}

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

import leap.lang.New;
import leap.lang.expression.ExpressionException;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class SpelExpressionTest extends SpelTestCase {

	@Test
	public void testCompositeExpression(){
		assertEquals("a",SPEL.createCompositeExpression("a").getValue());
		assertEquals("a1",SPEL.createCompositeExpression("a${v}").getValue(New.<String,Object>hashMap("v","1")));
		assertEquals("a${1",SPEL.createCompositeExpression("a\\${${v}").getValue(New.<String,Object>hashMap("v","1")));
		assertEquals("1",SPEL.createCompositeExpression("${v}").getValue(New.<String,Object>hashMap("v","1")));
		assertEquals("a1b1",SPEL.createCompositeExpression("a${v}b${v}").getValue(New.<String,Object>hashMap("v","1")));
		assertEquals("a1b11",SPEL.createCompositeExpression("a${v}b${v}1").getValue(New.<String,Object>hashMap("v","1")));
		assertEquals("1/edit",SPEL.createCompositeExpression("${id}/edit").getValue(New.<String,Object>hashMap("id","1")));
		try {
	        SPEL.createCompositeExpression("a${v}b${v}${xx");
	        fail("Should throw exception");
        } catch (ExpressionException e) {

        }
	}
	
	@Test
	public void testArrayItem() {
		int[] a = new int[]{1,2,3,4,5};
		
		Map<String, Object> m = new HashMap<>();
		m.put("a", a);
		m.put("s", "str");
		
		Map<String, Object> vars = new HashMap<>();
		vars.put("a", a);
		vars.put("m", m);
		vars.put("index", 0);
		vars.put("p", "s");
		
		assertEquals(1, eval("a[0]",vars));
		assertEquals(1, eval("a[index]",vars));
		assertEquals(2, eval("a[index + 1]",vars));
		assertEquals(1, eval("m['a'][0]",vars));
		assertEquals("str", eval("m['s']",vars));
		assertEquals("str", eval("m[p]",vars));
	}

    @Test
    public void testProperty() {
        Map<String,Object> m = New.hashMap("a","b");
        assertEquals("b", eval("['a']", m));
    }
	
	@Test
	public void testStaticField() {
		assertEquals(Boolean.TRUE,eval("T(java.lang.Boolean).TRUE"));
		assertEquals(Boolean.TRUE,eval("T(Boolean).TRUE"));
		assertEquals(Boolean.TRUE,eval("T(Strings).isEmpty('')"));
	}
	
	@Test
	public void testVariableSpecialCharacters() {
		eval("$name");
		eval("_name");
	}
}

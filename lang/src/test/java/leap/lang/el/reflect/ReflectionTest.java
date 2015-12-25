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
package leap.lang.el.reflect;

import java.lang.reflect.Method;

import leap.junit.TestBase;
import leap.lang.reflect.Reflection;

import org.junit.Test;

public class ReflectionTest extends TestBase {

	@Test
	public void testParameterName() {
		Method m = Reflection.getMethod(this.getClass(), "forParameterNameTest");
		
		String[] names = Reflection.getParameterNames(m);
		assertEquals(2, names.length);
		assertEquals("p1",names[0]);
		assertEquals("val",names[1]);
	}
	
	protected static void forParameterNameTest(String p1,int val) {
		
	}
	
}

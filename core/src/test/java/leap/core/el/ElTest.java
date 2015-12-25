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
package leap.core.el;

import java.util.Date;

import org.junit.Test;

import leap.core.junit.AppTestBase;
import leap.lang.expression.Expression;

public class ElTest extends AppTestBase {

	@Test
	public void testEnv() {
		Expression e = EL.createExpression("${env.now}");
		Object now = e.getValue();
		assertTrue(now instanceof Date);
	}
	
	@Test
	public void testFunction(){
		String clsName = ElTest.class.getName();
		assertEquals(Boolean.TRUE,EL.eval("classes:isPresent('" + clsName + "')"));
	}
	
}
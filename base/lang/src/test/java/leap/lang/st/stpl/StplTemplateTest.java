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
package leap.lang.st.stpl;

import static leap.lang.st.stpl.StplTemplate.render;

import java.util.HashMap;

import leap.junit.TestBase;
import leap.lang.New;

import org.junit.Test;

public class StplTemplateTest extends TestBase {
	
	@Test
	public void testSimpleStplTemplate(){
		assertEquals("test", render("test",new HashMap<String,Object>()));
		assertEquals("test", render("$var$",New.<String,Object>hashMap("var", "test")));
		assertEquals("test1", render("$var$1",New.<String,Object>hashMap("var", "test")));
		assertEquals("test1", render("$var$1{?$var1$}",New.<String,Object>hashMap("var", "test")));
		assertEquals("test1test2", render("$var$1{?$var1$}",New.<String,Object>hashMap("var", "test","var1","test2")));
	}
	
}

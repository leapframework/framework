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

import org.junit.Test;

public class SpelLiteralTest extends SpelTestCase {

	@Test
	public void testString(){
		assertEquals("s1", eval("'s1'"));
		assertEquals("s1", eval("\"s1\""));
	}
	
	@Test
	public void testHexCharacter() {
		assertEquals(0x0A, eval("0x0A"));
	}

	@Test
	public void testOctalEscapes() {
		assertEquals("\344", eval("'\\344'"));
	}

	@Test
	public void testOctalEscapes2() {
		assertEquals("\7", eval("'\\7'"));
	}

	@Test
	public void testOctalEscapes3() {
		assertEquals("\777", eval("'\\777'"));
	}

	@Test
	public void testUniHex1() {
		assertEquals("\uFFFF::", eval("'\\uFFFF::'"));
	}

	@Test
	public void testNumLiterals() {
		assertEquals(1e1f, eval("1e1f"));
	}

	@Test
	public void testNumLiterals2() {
		assertEquals(2.f, eval("2.f"));
	}

	@Test
	public void testNumLiterals4() {
		assertEquals(3.14f, eval("3.14f"));
	}

	@Test
	public void testNumLiterals5() {
		assertEquals(1e1, eval("1e1"));
	}

	@Test
	public void testNumLiterals6() {
		assertEquals(2., eval("2."));
	}

	@Test
	public void testNumLiterals8() {
		assertEquals(1e-9d, eval("1e-9d"));
	}

	@Test
	public void testNumLiterals9() {
		//long 
		assertEquals(0x400921FB54442D18L, eval("0x400921FB54442D18L"));
	}
}

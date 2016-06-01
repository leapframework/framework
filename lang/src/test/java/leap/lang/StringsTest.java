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
package leap.lang;

import leap.junit.concurrent.ConcurrentTestCase;

import org.junit.Test;

public class StringsTest extends ConcurrentTestCase {

	@Test
	public void testIsEmpty() {
		assertEquals(true, Strings.isEmpty(null));
		assertEquals(true, Strings.isEmpty(""));
		assertEquals(false, Strings.isEmpty(" "));
		assertEquals(false, Strings.isEmpty("foo"));
		assertEquals(false, Strings.isEmpty("  foo  "));
	}
	
	@Test
	public void testIsNotEmpty() {
		assertEquals(false, Strings.isNotEmpty(null));
		assertEquals(false, Strings.isNotEmpty(""));
		assertEquals(true, Strings.isNotEmpty(" "));
		assertEquals(true, Strings.isNotEmpty("foo"));
		assertEquals(true, Strings.isNotEmpty("  foo  "));
	}
	
	@Test
	public void testIsNotBlank() {
		assertTrue(Strings.isNotBlank(" x "));
		assertFalse(Strings.isNotBlank(" "));
		assertFalse(Strings.isNotBlank(null));
		assertFalse(Strings.isNotBlank(""));
	}
	
	@Test
	public void testEqualsIgnoreCase(){
		assertTrue(Strings.equalsIgnoreCase(null, null));
	}
	
	@Test
	public void testCamel() {
		assertEquals("HelloWorld",Strings.upperCamel("hello_world",'_'));
		assertEquals("HelloWorld",Strings.upperCamel("hello_world_",'_'));
		
		assertEquals("helloWorld",Strings.lowerCamel("hello_world",'_'));
		assertEquals("helloWorld",Strings.lowerCamel("hello_world_",'_'));
	}
	
	@Test
	public void testSplitWhitespaces() {
		assertArrayEquals(Strings.splitWhitespaces("1"), new String[]{"1"});
		assertArrayEquals(Strings.splitWhitespaces(" 1"), new String[]{"1"});
		assertArrayEquals(Strings.splitWhitespaces("   1"), new String[]{"1"});
		assertArrayEquals(Strings.splitWhitespaces("   1 "), new String[]{"1"});
		assertArrayEquals(Strings.splitWhitespaces("   1   "), new String[]{"1"});
		
		assertArrayEquals(Strings.splitWhitespaces("1 2"), new String[]{"1","2"});
		assertArrayEquals(Strings.splitWhitespaces(" 1 2"), new String[]{"1","2"});
		assertArrayEquals(Strings.splitWhitespaces(" 1 2 "), new String[]{"1","2"});
		assertArrayEquals(Strings.splitWhitespaces("  1 2  "), new String[]{"1","2"});
		assertArrayEquals(Strings.splitWhitespaces("  1  2  "), new String[]{"1","2"});
	}

    @Test
    public void testRight() {
        assertEquals("a", Strings.right("a", 10));
        assertEquals("cde", Strings.right("abcde", 3));
    }
}
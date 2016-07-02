/*
 * Copyright 2012 the original author or authors.
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
package leap.lang.convert;

import leap.junit.concurrent.ConcurrentTestCase;

import org.junit.Test;

public class CharacterConverterTest extends ConcurrentTestCase {

	@Test
	public void testConvertToString() {
        assertEquals("Character Test", "N", Converts.convert(new Character('N'),String.class));
        assertEquals("char Test", "N", Converts.convert('N',String.class));
    }

	@Test
    public void testConvertToCharacter() {
        assertEquals("Character Test", new Character('N'), Converts.convert(new Character('N'),Character.class));
        assertEquals("String Test",    new Character('F'), Converts.convert("FOO",Character.class));
        assertEquals("Integer Test",   new Character('3'), Converts.convert(new Integer(321),Character.class));
    }
}

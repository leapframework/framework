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
package leap.lang.convert;

import java.sql.Timestamp;
import java.time.LocalDate;

import junit.framework.TestCase;
import leap.lang.Dates;
import leap.lang.time.DateFormats;

import org.junit.Test;

public class DateConverterTest extends TestCase{

	@Test
	public void testStringToTimestamp(){
		Timestamp t = Converts.convert("2012-12-21",Timestamp.class);
		
		String dt = Dates.format(t,DateFormats.DATE_PATTERN);
		
		assertEquals("2012-12-21", dt);
	}
	
	@Test
	public void testStringToLocalDate() {
		LocalDate d = Converts.convert("2014-11-01",LocalDate.class);
		assertEquals(LocalDate.of(2014, 11, 1),d);

        assertEquals("2014-11-01", Converts.toString(d));
	}
	
}

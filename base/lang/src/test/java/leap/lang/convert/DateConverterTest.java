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

import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import junit.framework.TestCase;
import leap.lang.Dates;
import leap.lang.time.DateFormats;

import leap.lang.time.StopWatch;
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

    @Test
    public void testIntegerToTimestamp() {
        Timestamp timestamp = Converts.convert(-28800000, Timestamp.class);
        assertNotNull(timestamp);
    }

    @Test
    public void testStringTimeToDate() {
        Time d = Converts.convert("01:59:50", Time.class);
        assertNotNull(d);

//        StopWatch sw = StopWatch.startNew();
//        for(int i=0;i<10000;i++) {
//            //Dates.parse("01:59:50", DateFormats.TIME_PATTERN);
//            //Converts.convert("01:59:50", Date.class);
//            DateTimeFormatter.ofPattern(DateFormats.TIME_PATTERN).withZone(ZoneId.systemDefault()).parse("01:59:50");
//
//        }
//        System.out.println(sw.getElapsedMilliseconds());

        assertEquals("01:59:50", Converts.toString(d));
    }

}

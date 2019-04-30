/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tests.core.el.function;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import leap.core.el.EL;
import leap.core.el.function.TimeFuncs;
import leap.core.junit.AppTestBase;
import leap.lang.DateTimes;
import leap.lang.Dates;
import leap.lang.New;

import org.junit.Test;

public class TimeFuncsTest extends AppTestBase {
	
	@Test
	public void testFormatSqlTimestamp(){
		LocalDateTime ldt = LocalDateTime.now();
		Timestamp timestamp = java.sql.Timestamp.valueOf(ldt);
		String date = Dates.format(timestamp);
		assertEquals(ldt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")),date);
	}
	@Test
	public void testFormatSqlDate(){
		LocalDate date = LocalDate.now();
		java.sql.Date d = java.sql.Date.valueOf(date);
		String f = Dates.format(d);
		assertEquals(date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),f);
	}
	
	@Test
	public void testFormat() {
		Date d = Dates.parse("2014-11-01 10:02:03");
		
		Instant   i  = d.toInstant();
		LocalTime lt = DateTimes.toLocalTime(i);
		LocalDate ld = DateTimes.toLocalDate(i);
		LocalDateTime ldt = DateTimes.toLocalDateTime(i);
		
		assertEquals("11.01", TimeFuncs.format(d,  "MM.dd"));
		assertEquals("11.01",TimeFuncs.format(ld, "MM.dd"));
		assertEquals("11.01",TimeFuncs.format(i,  "MM.dd"));
		assertEquals("11.01",TimeFuncs.format(ldt,"MM.dd"));
		assertEquals("10:02",TimeFuncs.format(lt, "HH:mm"));
		
		assertEquals("11.01",EL.eval("times:fmt(t,'MM.dd')",New.hashMap("t", d)));
	}
	
	@Test
	public void testHour() {
		Date d = Dates.parse("2014-11-01 10:02:03");
		
		Instant   i  = d.toInstant();
		LocalTime lt = DateTimes.toLocalTime(i);
		LocalDateTime ldt = DateTimes.toLocalDateTime(i);
		
		assertEquals(10,TimeFuncs.hour(d));
		assertEquals(10,TimeFuncs.hour(i));
		assertEquals(10,TimeFuncs.hour(lt));
		assertEquals(10,TimeFuncs.hour(ldt));
		
		assertEquals(10,EL.eval("times:hour(t)",New.hashMap("t", d)));
	}
}

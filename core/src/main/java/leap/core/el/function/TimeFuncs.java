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
package leap.core.el.function;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.Temporal;
import java.util.Calendar;
import java.util.Date;

import leap.lang.DateTimes;
import leap.lang.Dates;


public class TimeFuncs {
	
	public static String format(Object t,String pattern) {
		if(null == t){
			return "";
		}
		
		if(t instanceof Date) {
			return Dates.format((Date)t, pattern);
		}
		
		if(t instanceof LocalDateTime) {
			return ((LocalDateTime) t).format(DateTimeFormatter.ofPattern(pattern));
		}
		
		if(t instanceof LocalDate) {
			return ((LocalDate) t).format(DateTimeFormatter.ofPattern(pattern));
		}
		
		if(t instanceof Instant) {
			return DateTimes.toLocalDateTime((Instant)t).format(DateTimeFormatter.ofPattern(pattern));
		}
		
		if(t instanceof LocalTime) {
			return ((LocalTime) t).format(DateTimeFormatter.ofPattern(pattern));
		}
		
		throw new IllegalStateException(t + " is not a valid or supported time");
	}
	
	public static int hour(Object t) {
		if(t instanceof Date) {
			return Dates.toCalendar((Date)t).get(Calendar.HOUR_OF_DAY);
		}
		if(t instanceof Instant) {
			return DateTimes.toLocalDateTime((Instant)t).getHour();
		}
		if(t instanceof Temporal) {
			return ((Temporal) t).get(ChronoField.HOUR_OF_DAY);
		}
		throw new IllegalStateException(t + " is not a valid or supported time");
	}
	
	public static int minute(Object t) {
		if(t instanceof Date) {
			return Dates.toCalendar((Date)t).get(Calendar.MINUTE);
		}
		if(t instanceof Instant) {
			return DateTimes.toLocalDateTime((Instant)t).getMinute();
		}
		if(t instanceof Temporal) {
			return ((Temporal) t).get(ChronoField.MINUTE_OF_HOUR);
		}
		throw new IllegalStateException(t + " is not a valid or supported time");
	}
	
	protected TimeFuncs() {
		
	}

}

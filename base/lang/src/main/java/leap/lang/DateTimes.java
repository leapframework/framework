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
package leap.lang;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;

import leap.lang.time.DateFormats;

/**
 * Date&Time utils for new Java8 Date & Time API.
 */
public class DateTimes {
	
	public static LocalDate tryParseLocalDate(CharSequence text) {
		if(null == text || text.length() == 0){
			return null;
		}
		
		LocalDate d = tryParseLocalDate(text,DateTimeFormatter.ISO_LOCAL_DATE);
		if(d == null) {
			d = tryParseLocalDate(text, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
			if(d == null) {
				Date ld = Dates.tryParse(text.toString(),DateFormats.DATETIME_FORMATTER);
				if(null != ld) {
					return toLocalDate(ld);
				}
			}
		}
		
		return d;
	}

	public static LocalDate tryParseLocalDate(CharSequence text,DateTimeFormatter formatter) {
		try {
	        return LocalDate.parse(text, formatter);
        } catch (DateTimeParseException e) {
        	return null;
        }
	}
	
	public static LocalTime tryParseLocalTime(CharSequence text) {
		if(null == text || text.length() == 0){
			return null;
		}

		LocalTime t = tryParseLocalTime(text, DateTimeFormatter.ISO_LOCAL_TIME);
		if(null == t){
			t = tryParseLocalTime(text, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
			
			if(null == t) {
				Date d = Dates.tryParse(text.toString(), DateFormats.DATETIME_FORMATTER);
				if(null != d) {
					return toLocalTime(d);
				}
			}
		}
		return t;
	}
		
	public static LocalTime tryParseLocalTime(CharSequence text,DateTimeFormatter formatter) {
		try {
	        return LocalTime.parse(text, formatter);
        } catch (DateTimeParseException e) {
        	return null;
        }
	}
	
	public static LocalDateTime tryParseLocalDateTime(CharSequence text) {
		if(null == text || text.length() == 0){
			return null;
		}

		LocalDateTime dt = tryParseLocalDateTime(text, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
		if(dt == null) {
			String s = text.toString();
			
			Date d = Dates.tryParse(s,DateFormats.DATETIME_FORMATTER);
			if(null != d){
				return toLocalDateTime(d);
			}

			d = Dates.tryParse(s,DateFormats.DATE_FORMATTER);
			if(null != d){
				return toLocalDateTime(d);
			}
		}
		return dt;
	}
	
	public static LocalDateTime tryParseLocalDateTime(CharSequence text,DateTimeFormatter formatter) {
		try {
	        return LocalDateTime.parse(text, formatter);
        } catch (DateTimeParseException e) {
        	return null;
        }
	}
	
	public static Instant tryParseInstant(CharSequence text) {
		if(null == text || text.length() == 0){
			return null;
		}
		
		try{
			return Instant.parse(text);
		}catch(DateTimeParseException e) {
			Date d = Dates.tryParse(text.toString());
			if(null != d){
				return d.toInstant();
			}
		}
		
		return null;
	}
	
	public static LocalDate toLocalDate(Date d) {
		return LocalDateTime.ofInstant(d.toInstant(), ZoneId.systemDefault()).toLocalDate();
	}
	
	public static LocalDate toLocalDate(Instant i) {
		return LocalDateTime.ofInstant(i, ZoneId.systemDefault()).toLocalDate();
	}
	
	public static LocalTime toLocalTime(Date d) {
		return LocalDateTime.ofInstant(d.toInstant(), ZoneId.systemDefault()).toLocalTime();
	}
	
	public static LocalTime toLocalTime(Instant i) {
		return LocalDateTime.ofInstant(i, ZoneId.systemDefault()).toLocalTime();
	}
	
	public static LocalDateTime toLocalDateTime(Date d) {
		return LocalDateTime.ofInstant(d.toInstant(), ZoneId.systemDefault());
	}
	
	public static LocalDateTime toLocalDateTime(Instant i) {
		return LocalDateTime.ofInstant(i, ZoneId.systemDefault());
	}
	
	protected DateTimes() {
		
	}
}

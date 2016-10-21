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

import java.sql.Time;
import java.sql.Timestamp;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

import leap.lang.DateTimes;
import leap.lang.time.DateFormats;

public class DateTimeConverters {
	
	public static class LocalDateConverter extends AbstractDateTimeConverter<LocalDate> {

		@Override
        public String convertToString(LocalDate value) throws Throwable {
	        return value.format(DateTimeFormatter.ISO_LOCAL_DATE);
        }
		
		@Override
        protected LocalDate convertFromString(CharSequence cs) {
			return DateTimes.tryParseLocalDate(cs);
        }

		@Override
        protected Instant convertToInstant(LocalDate value) {
			return Instant.ofEpochMilli(value.toEpochDay());
        }

		@Override
        protected LocalDate convertFromInstant(Instant instant) {
	        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).toLocalDate();
        }
	}

    public static class LocalTimeConverter extends AbstractDateTimeConverter<LocalTime> {

        @Override
        public String convertToString(LocalTime value) throws Throwable {
            return value.toString();
        }

        @Override
        protected LocalTime convertFromString(CharSequence cs) {
            return DateTimes.tryParseLocalTime(cs);
        }

        @Override
        protected Instant convertToInstant(LocalTime value) {
            return null;
        }

        @Override
        protected LocalTime convertFromInstant(Instant instant) {
            return LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).toLocalTime();
        }
    }

    public static class LocalDateTimeConverter extends AbstractDateTimeConverter<LocalDateTime> {

        @Override
        public String convertToString(LocalDateTime value) throws Throwable {
            return value.toString();
        }

        @Override
        protected LocalDateTime convertFromString(CharSequence cs) {
            return DateTimes.tryParseLocalDateTime(cs);
        }

        @Override
        protected Instant convertToInstant(LocalDateTime value) {
            return value.toInstant(ZoneOffset.UTC);
        }

        @Override
        protected LocalDateTime convertFromInstant(Instant instant) {
            return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        }
    }

	public static class DateTimeConverter extends AbstractDateConverter<Date> implements Converter<Date> {
		
		public DateTimeConverter() {
		    this.patterns = DateFormats.DEFAULT_PATTERNS;
	    }

		@Override
	    protected Date convertFrom(Class<?> targetType, Date date) {
		    return date;
	    }

		@Override
	    protected Date convertFrom(Class<?> targetType, Calendar calendar) {
		    return new Date(calendar.getTimeInMillis());
	    }

		@Override
	    protected Date convertFrom(Class<?> targetType, Long time) {
		    return new Date(time);
	    }
	}
	
	public static class CalendarConverter extends AbstractDateConverter<Calendar> implements Converter<Calendar> {
		
		public CalendarConverter() {
		    this.patterns = DateFormats.DEFAULT_PATTERNS;
	    }

		@Override
        protected Calendar convertFrom(Class<?> targetType, Date date) {
	        Calendar c = Calendar.getInstance();
	        c.setTime(date);
	        return c;
        }

		@Override
        protected Calendar convertFrom(Class<?> targetType, Calendar calendar) {
	        return calendar;
        }

		@Override
        protected Calendar convertFrom(Class<?> targetType, Long time) {
	        Calendar c = Calendar.getInstance();
	        c.setTime(new Date(time));
	        return c;
        }
	}
	
	public static class SqlDateConverter extends AbstractDateConverter<java.sql.Date> {
		
		public SqlDateConverter() {
	        this.patterns = DateFormats.DEFAULT_PATTERNS;
        }

		@Override
	    protected java.sql.Date convertFrom(Class<?> targetType, Calendar calendar) {
		    return new java.sql.Date(calendar.getTimeInMillis());
	    }

		@Override
	    protected java.sql.Date convertFrom(Class<?> targetType, java.util.Date date) {
		    return new java.sql.Date(date.getTime());
	    }

		@Override
	    protected java.sql.Date convertFrom(Class<?> targetType, Long time) {
		    return new java.sql.Date(time);
	    }

	}
	
	public static class SqlTimeConverter extends AbstractDateConverter<Time> {
		
		public SqlTimeConverter() {
	        this.patterns = DateFormats.DEFAULT_PATTERNS;
        }
		
		@Override
	    protected Time convertFrom(Class<?> targetType, Calendar calendar) {
		    return new java.sql.Time(calendar.getTimeInMillis());
	    }

		@Override
	    protected Time convertFrom(Class<?> targetType, Date date) {
		    return new java.sql.Time(date.getTime());
	    }

		@Override
	    protected Time convertFrom(Class<?> targetType, Long time) {
			return new java.sql.Time(time);
		}
	}
	
	public static class SqlTimestampConverter extends AbstractDateConverter<Timestamp> {
		
		public SqlTimestampConverter() {
	        this.patterns = DateFormats.DEFAULT_PATTERNS;
        }
		
		@Override
	    protected Timestamp convertFrom(Class<?> targetType, Calendar calendar) {
		    return new Timestamp(calendar.getTimeInMillis());
	    }

		@Override
	    protected Timestamp convertFrom(Class<?> targetType, Date date) {
		    return new Timestamp(date.getTime());
	    }

		@Override
	    protected Timestamp convertFrom(Class<?> targetType, Long time) {
		    return new Timestamp(time);
	    }
	}
	
	public static class InstantConverter extends AbstractDateConverter<Instant> {

		@Override
        protected Instant convertFrom(Class<?> targetType, Date date) {
	        // TODO Auto-generated method stub
	        return null;
        }

		@Override
        protected Instant convertFrom(Class<?> targetType, Calendar calendar) {
	        // TODO Auto-generated method stub
	        return null;
        }

		@Override
        protected Instant convertFrom(Class<?> targetType, Long time) {
	        // TODO Auto-generated method stub
	        return null;
        }

	}
	
	protected DateTimeConverters(){
		
	}
}

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
package leap.lang.time;

import leap.lang.Args;
import leap.lang.Strings;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;



public class DateFormats {
	
	private static final Map<String,  DateFormat> patternFormats  = new ConcurrentHashMap<String,  DateFormat>();
	private static final Map<Class<?>,String>     defaultPatterns = new ConcurrentHashMap<Class<?>,String>();
    private static final Map<String, DateTimeFormatter> formatters = new ConcurrentHashMap<>();

    public static final String DATE_PATTERN          = "yyyy-MM-dd";
	public static final String TIME_PATTERN          = "HH:mm:ss";
	public static final String DATETIME_PATTERN      = "yyyy-MM-dd HH:mm:ss";
	public static final String TIMESTAMP_PATTERN     = "yyyy-MM-dd HH:mm:ss.SSS";
	public static final String RFC3339_DATE_PATTERN  = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    public static final String ISO8601_DATE_PATTERN1 = "yyyy-MM-dd'T'HH:mm:ssZ";
    public static final String ISO8601_DATE_PATTERN2 = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";

    public static final String[] DEFAULT_PATTERNS = new String[]{
        DATE_PATTERN,
        TIME_PATTERN,
        DATETIME_PATTERN,
        TIMESTAMP_PATTERN,
        RFC3339_DATE_PATTERN,
        ISO8601_DATE_PATTERN1,
        ISO8601_DATE_PATTERN2
    };
    
    public static final ConcurrentDateFormat DATE_FORMAT      = new ConcurrentDateFormat(DATE_PATTERN);
    public static final ConcurrentDateFormat TIME_FORMAT      = new ConcurrentDateFormat(TIME_PATTERN);
    public static final ConcurrentDateFormat DATETIME_FORMAT  = new ConcurrentDateFormat(DATETIME_PATTERN);
    public static final ConcurrentDateFormat TIMESTAMP_FORMAT = new ConcurrentDateFormat(TIMESTAMP_PATTERN);

    public static final DateTimeFormatter DATE_FORMATTER      = DateTimeFormatter.ofPattern(DATE_PATTERN).withZone(ZoneId.systemDefault());
    public static final DateTimeFormatter TIME_FORMATTER      = DateTimeFormatter.ofPattern(TIME_PATTERN).withZone(ZoneId.systemDefault());
    public static final DateTimeFormatter DATETIME_FORMATTER  = DateTimeFormatter.ofPattern(DATETIME_PATTERN).withZone(ZoneId.systemDefault());
    public static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern(TIMESTAMP_PATTERN).withZone(ZoneId.systemDefault());

    static {
    	patternFormats.put(DATE_PATTERN,          DATE_FORMAT);
    	patternFormats.put(TIME_PATTERN,          TIME_FORMAT);
    	patternFormats.put(DATETIME_PATTERN,      DATETIME_FORMAT);
    	patternFormats.put(TIMESTAMP_PATTERN,     TIMESTAMP_FORMAT);
        patternFormats.put(RFC3339_DATE_PATTERN,  new ConcurrentDateFormat(RFC3339_DATE_PATTERN));
    	patternFormats.put(ISO8601_DATE_PATTERN1, new ConcurrentDateFormat(ISO8601_DATE_PATTERN1));
        patternFormats.put(ISO8601_DATE_PATTERN2, new ConcurrentDateFormat(ISO8601_DATE_PATTERN2));

        formatters.put(DATE_PATTERN,          DATE_FORMATTER);
        formatters.put(TIME_PATTERN,          TIME_FORMATTER);
        formatters.put(DATETIME_PATTERN,      DATETIME_FORMATTER);
        formatters.put(TIMESTAMP_PATTERN,     TIMESTAMP_FORMATTER);
        formatters.put(RFC3339_DATE_PATTERN,  DateTimeFormatter.ofPattern(RFC3339_DATE_PATTERN).withZone(ZoneId.systemDefault()));
        formatters.put(ISO8601_DATE_PATTERN1, DateTimeFormatter.ofPattern(ISO8601_DATE_PATTERN1).withZone(ZoneId.systemDefault()));
        formatters.put(ISO8601_DATE_PATTERN2, DateTimeFormatter.ofPattern(ISO8601_DATE_PATTERN2).withZone(ZoneId.systemDefault()));

        defaultPatterns.put(Timestamp.class,       TIMESTAMP_PATTERN);
    	defaultPatterns.put(Time.class, 	       TIME_PATTERN);
    	defaultPatterns.put(Date.class, 	   	   DATE_PATTERN);
    	defaultPatterns.put(java.util.Date.class,  DATETIME_PATTERN);
    }

    private static final ZoneOffset SYSTEM_DEFAULT_ZONE_OFFSET = ZoneId.systemDefault().getRules().getOffset(Instant.now());

    public static ZoneOffset systemDefaultZoneOffset() {
        return SYSTEM_DEFAULT_ZONE_OFFSET;
    }

	/**
	 * Get a date/time formatter using the supplied pattern.
	 * 
	 * @param pattern the pattern to format date/time.
	 * @return the formatter using the pattern.  
	 */
	public static DateFormat getFormat(String pattern) {
        DateFormat format = patternFormats.get(pattern);
        
        if(null == format){
        	format = new ConcurrentDateFormat(pattern);
            
            patternFormats.put(pattern,format);
        }
        
        return format;
	}

    public static DateFormat getFormat(String pattern, String zone) {
        if(Strings.isEmpty(zone)) {
            return getFormat(pattern);
        }
        String key = pattern + "$$$" + zone;
        DateFormat format = patternFormats.get(key);

        if(null == format){
            format = new ConcurrentDateFormat(pattern, TimeZone.getTimeZone(zone));
            patternFormats.put(pattern,format);
        }

        return format;
    }
	
	/**
	 * get a date/time formatter using the pattern of the supplied type.
	 * the type can be {@link Timestamp}, {@link Time}, {@link Date}, {@link java.util.Date}.
	 * @param type the type to represent the pattern.
	 * @return the formatter using the pattern.
	 */
	public static DateFormat getFormat(Class<?> type) {
		return getFormat(getPattern(type));
	}

    public static DateTimeFormatter getFormatter(String pattern) {
        return getFormatter(pattern, ZoneId.systemDefault());
    }

    public static DateTimeFormatter getFormatter(String pattern, String zone) {
        return getFormatter(pattern, Strings.isEmpty(zone) ? ZoneId.systemDefault() : ZoneId.of(zone));
    }

    public static DateTimeFormatter getFormatter(String pattern, ZoneId zoneId) {
        Args.notNull(zoneId);

        String key = pattern + "$$$" + zoneId.getId();
        DateTimeFormatter format = formatters.get(key);

        if(null == format){
            format = DateTimeFormatter.ofPattern(pattern).withZone(zoneId);
            formatters.put(key,format);
        }

        return format;
    }

    public static DateTimeFormatter getFormatter(Class<?> type) {
        return getFormatter(getPattern(type));
    }
	
	/**
	 * get the pattern string according to the supplied type.
	 * return pattern string "yyyy-MM-dd HH:mm:ss" if there's no pattern string matches the type.
	 * @param type the type to get the matched pattern string.
	 * @return the pattern string according to the supplied type.
	 */
	public static String getPattern(Class<?> type) {
		String pattern = defaultPatterns.get(type);
		
		return null == pattern ? DATETIME_PATTERN : pattern;
	}

    protected DateFormats(){

    }
}
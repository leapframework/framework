/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

import leap.lang.time.DateFormats;

import java.text.DateFormat;
import java.text.ParsePosition;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQueries;
import java.util.Calendar;
import java.util.Date;

/**
 * <p>
 * A suite of utilities surrounding the use of the {@link java.util.Calendar} and {@link java.util.Date} object.
 * </p>
 */
public class Dates {

    private static final LocalDate LD_1970_01_02 = LocalDate.parse("1970-01-02", DateFormats.DATE_FORMATTER);
    private static final LocalTime LT_00_00_00   = LocalTime.of(0, 0, 0);

    /**
     * Number of milliseconds in a standard second.
     */
    public static final long MILLIS_ONE_SECOND = 1000;

    /**
     * Number of milliseconds in a standard minute.
     */
    public static final long MILLIS_ONE_MINUTE = 60 * MILLIS_ONE_SECOND;

    /**
     * Number of milliseconds in a standard hour.
     */
    public static final long MILLIS_ONE_HOUR = 60 * MILLIS_ONE_MINUTE;

    /**
     * Number of milliseconds in a standard day.
     */
    public static final long MILLIS_ONE_DAY = 24 * MILLIS_ONE_HOUR;

    /**
     * Number of milliseconds in a standard year.
     */
    public static final long MILLIS_ONE_YEAR = MILLIS_ONE_DAY * 365;

    protected Dates() {

    }

    /**
     * Formats a Date into a date string using pattern "yyyy-MM-dd" as default.
     *
     * @param date the date value to be formatted into a date string.
     * @return the formatted date string.
     */
    public static String format(Date date) {
        return DateFormats.getFormatter(date.getClass()).format(date.toInstant());
    }

    /**
     * Formats a Date into a date string using the supplied pattern.
     *
     * @param date    the date value to be formatted into a date string.
     * @param pattern the pattern used to format.
     * @return the formatted date/time string.
     */
    public static String format(Date date, String pattern) {
        return DateFormats.getFormatter(pattern).format(date.toInstant());
    }

    /**
     * parses a string representing a date/time by trying a variety of different parsers.
     * <p>
     * if no parse patterns match, throw {@link IllegalArgumentException}.
     *
     * @param string the string to be parsed.
     * @return the parsed {@link Date}.
     * @throws IllegalArgumentException if no parse patterns matches.
     */
    public static Date parse(String string) throws IllegalArgumentException {
        return parse(string, DateFormats.DEFAULT_PATTERNS, false);
    }

    /**
     * parses a string representing a date/time by trying a variety of different parsers.
     * <p>
     * if no parse patterns match, return null instead of throw {@link IllegalArgumentException}.
     *
     * @param string the string to be parsed.
     * @return the parsed {@link Date} or null if no patterns matches.
     */
    public static Date tryParse(String string) {
        return parse(string, DateFormats.DEFAULT_PATTERNS, true);
    }

    /**
     * parses a string representing a date/time by trying all the supplied patterns.
     * <p>
     * if no parse patterns match, throw {@link IllegalArgumentException}.
     *
     * @param string the string to be parsed.
     * @return the parsed {@link Date}.
     * @throws IllegalArgumentException if no parse patterns matches.
     */
    public static Date parse(String string, String... patterns) throws IllegalArgumentException {
        return parse(string, patterns, false);
    }

    /**
     * parses a string representing a date/time by trying all the supplied patterns.
     * <p>
     * if no parse patterns match, return null instead of throw {@link IllegalArgumentException}.
     *
     * @param string the string to be parsed.
     * @return the parsed {@link Date} or null if no patterns matches.
     */
    public static Date tryParse(String string, String... patterns) {
        return parse(string, patterns, true);
    }

    public static Date tryParse(String string, DateTimeFormatter formatter) {
        return parse(string, formatter, true);
    }

    /**
     * @deprecated use {@link #tryParse(String, DateTimeFormatter)} instead.
     */
    @Deprecated
    public static Date tryParse(String string, DateFormat format) {
        return parse(string, format, true);
    }

    public static Calendar toCalendar(Date d) {
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        return c;
    }

    /**
     * @param field the given calendar field, see {@link Calendar}
     */
    public static Date zero(Date date, int field) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(field, 0);
        return c.getTime();
    }

    private static Date parse(String string, String[] patterns, boolean returnNull) throws IllegalArgumentException {
        if (string == null || patterns == null) {
            throw new IllegalArgumentException("Date and Patterns must not be null");
        }

        for (String pattern : patterns) {
            leap.lang.time.DateFormat format = DateFormats.getFormat1(pattern);
            if(!format.matches(string)) {
                continue;
            }

            DateTimeFormatter formatter = format.getFormatter();

            Date date = tryParse(formatter, string, new ParsePosition(0));
            if (null != date) {
                return date;
            }
        }

        if (!returnNull) {
            throw new IllegalArgumentException("Unable to parse the date: " + string);
        }

        return null;
    }

    private static Date parse(String string, DateFormat format, boolean returnNull) throws IllegalArgumentException {
        ParsePosition pos = new ParsePosition(0);

        pos.setIndex(0);

        String str2 = string;

        Date date = format.parse(str2, pos);

        if (date != null && pos.getIndex() == str2.length()) {
            return date;
        }

        if (!returnNull) {
            throw new IllegalArgumentException("Unable to parse the date: " + string);
        }

        return null;
    }

    private static Date parse(String string, DateTimeFormatter format, boolean returnNull) throws IllegalArgumentException {
        Date date = tryParse(format, string, new ParsePosition(0));
        if (date != null) {
            return date;
        }

        if (!returnNull) {
            throw new IllegalArgumentException("Unable to parse the date: " + string);
        }
        return null;
    }

    private static Date tryParse(DateTimeFormatter formatter, String s, ParsePosition pp) {
        TemporalAccessor ta = null;
        try {
            if (null == pp) {
                ta = formatter.parse(s);
            } else {
                ta = formatter.parse(s, pp);
            }

        } catch (DateTimeParseException e) {
            //log.debug(e.getMessage(), e);
        }

        if (ta != null && (null == pp || pp.getIndex() == s.length())) {
            Instant instant = null;
            try {
                if(ta.isSupported(ChronoField.INSTANT_SECONDS) && ta.isSupported(ChronoField.NANO_OF_SECOND)) {
                    instant = Instant.from(ta);
                }
            } catch (DateTimeException e) {
            }

            if(null == instant) {
                LocalDate ld = ta.query(TemporalQueries.localDate());
                LocalTime lt = ta.query(TemporalQueries.localTime());

                if(ld == null && lt == null) {
                    throw new IllegalArgumentException("Invalid date time '" + s + "'");
                }

                if(ld == null) {
                    ld = LD_1970_01_02;
                }

                if(lt == null) {
                    lt = LT_00_00_00;
                }

                instant = LocalDateTime.of(ld, lt).toInstant(DateFormats.systemDefaultZoneOffset());
            }

            return Date.from(instant);
        }

        return null;
    }
}
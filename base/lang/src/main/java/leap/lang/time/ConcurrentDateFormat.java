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
package leap.lang.time;

import java.text.AttributedCharacterIterator;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * A concurrent DateFormat class wrapped the {@link SimpleDateFormat}.
 * 
 * <p/>
 * 
 * This implementation simply to add {@code synchronized} keywork in all methods.
 */
public class ConcurrentDateFormat extends DateFormat {

    private static final long serialVersionUID = -1237664759165921526L;
    
    private SimpleDateFormat format;
    
    /**
     * Constructs a SimpleDateFormat using the given pattern and the default date format symbols
     * for the default locale, and the parsing is not lenient as default. 
     * Note: This constructor may not support all locales. 
     * For full coverage, use the factory methods in the {@link DateFormats} class.
     * 
     * @param pattern the pattern describing the date and time format
     */
    public ConcurrentDateFormat(String pattern){
        this.format = new SimpleDateFormat(pattern);
        this.format.setLenient(false);
    }

    public ConcurrentDateFormat(String pattern, TimeZone zone){
        this.format = new SimpleDateFormat(pattern);
        this.format.setLenient(false);
        this.format.setTimeZone(zone);
    }
    
    @Override
    public synchronized Object clone() {
        return format.clone();
    }

    @Override
    public synchronized boolean equals(Object obj) {
        return format.equals(obj);
    }

    @Override
    public synchronized StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition fieldPosition) {
        return format.format(date, toAppendTo, fieldPosition);
    }

    @Override
    public synchronized Calendar getCalendar() {
        return format.getCalendar();
    }

    @Override
    public synchronized NumberFormat getNumberFormat() {
        return format.getNumberFormat();
    }

    @Override
    public synchronized TimeZone getTimeZone() {
        return format.getTimeZone();
    }

    @Override
    public synchronized int hashCode() {
        return format.hashCode();
    }

    @Override
    public synchronized boolean isLenient() {
        return format.isLenient();
    }

    @Override
    public synchronized Date parse(String source, ParsePosition pos) {
        return format.parse(source, pos);
    }

    @Override
    public synchronized Date parse(String source) throws ParseException {
        return format.parse(source);
    }

    @Override
    public synchronized Object parseObject(String source, ParsePosition pos) {
        return format.parseObject(source, pos);
    }

    @Override
    public synchronized void setCalendar(Calendar newCalendar) {
        format.setCalendar(newCalendar);
    }

    @Override
    public synchronized void setLenient(boolean lenient) {
        format.setLenient(lenient);
    }

    @Override
    public synchronized void setNumberFormat(NumberFormat newNumberFormat) {
        format.setNumberFormat(newNumberFormat);
    }

    @Override
    public synchronized void setTimeZone(TimeZone zone) {
        format.setTimeZone(zone);
    }

    @Override
    public synchronized AttributedCharacterIterator formatToCharacterIterator(Object obj) {
        return format.formatToCharacterIterator(obj);
    }

    @Override
    public synchronized Object parseObject(String source) throws ParseException {
        return format.parseObject(source);
    }
}
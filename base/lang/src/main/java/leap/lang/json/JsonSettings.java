/*
 * Copyright 2010 the original author or authors.
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
package leap.lang.json;

import leap.lang.beans.BeanProperty;
import leap.lang.naming.NamingStyle;
import leap.lang.naming.NamingStyles;
import leap.lang.time.DateFormats;
import java.text.DateFormat;
import java.time.format.DateTimeFormatter;
import java.util.function.Predicate;

public class JsonSettings {

    public static JsonSettings MAX = new Builder().build();

    public static JsonSettings MIN = new Builder().setKeyQuoted(false)
            .setIgnoreEmpty(true)
            .setIgnoreNull(true).build();

    private final boolean                 keyQuoted;
    private final boolean                 ignoreNull;
    private final boolean                 ignoreFalse;
    private final boolean                 ignoreEmptyString;
    private final boolean                 ignoreEmptyArray;
    private final boolean                 nullToEmptyString;
    private final boolean                 htmlEscape;
    private final NamingStyle             namingStyle;
    private final DateFormat              dateFormat;
    private final DateTimeFormatter       dateTimeFormatter;
    private final Predicate<BeanProperty> propertyFilter;
    private final Predicate<Object>       beanFilter;

    public JsonSettings(boolean keyQuoted, boolean ignoreNull, boolean ignoreFalse,
                        boolean ignoreEmptyString, boolean ignoreEmptyArray,
                        NamingStyle namingStyle, DateFormat dateFormat) {
        this(keyQuoted, ignoreNull, ignoreFalse, ignoreEmptyString, ignoreEmptyArray, false, false, namingStyle, dateFormat);
    }

    public JsonSettings(boolean keyQuoted, boolean ignoreNull, boolean ignoreFalse,
                        boolean ignoreEmptyString, boolean ignoreEmptyArray, boolean nullToEmptyString, boolean htmlEscape,
                        NamingStyle namingStyle, DateFormat dateFormat) {
        this.keyQuoted = keyQuoted;
        this.ignoreNull = ignoreNull;
        this.ignoreFalse = ignoreFalse;
        this.ignoreEmptyString = ignoreEmptyString;
        this.ignoreEmptyArray = ignoreEmptyArray;
        this.namingStyle = namingStyle;
        this.dateFormat = dateFormat;
        this.dateTimeFormatter = null;
        this.nullToEmptyString = nullToEmptyString;
        this.htmlEscape = htmlEscape;
        this.propertyFilter = null;
        this.beanFilter = null;
    }

    public JsonSettings(boolean keyQuoted, boolean ignoreNull, boolean ignoreFalse,
                        boolean ignoreEmptyString, boolean ignoreEmptyArray, boolean nullToEmptyString, boolean htmlEscape,
                        NamingStyle namingStyle, DateFormat dateFormat, DateTimeFormatter dateTimeFormatter,
                        Predicate<BeanProperty> propertyFilter, Predicate<Object> beanFilter) {
        this.keyQuoted = keyQuoted;
        this.ignoreNull = ignoreNull;
        this.ignoreFalse = ignoreFalse;
        this.ignoreEmptyString = ignoreEmptyString;
        this.ignoreEmptyArray = ignoreEmptyArray;
        this.namingStyle = namingStyle;
        this.dateFormat = dateFormat;
        this.dateTimeFormatter = dateTimeFormatter;
        this.nullToEmptyString = nullToEmptyString;
        this.htmlEscape = htmlEscape;
        this.propertyFilter = propertyFilter;
        this.beanFilter = beanFilter;
    }

    public boolean isKeyQuoted() {
        return keyQuoted;
    }

    public boolean isIgnoreNull() {
        return ignoreNull;
    }

    public boolean isIgnoreFalse() {
        return ignoreFalse;
    }

    public boolean isIgnoreEmptyString() {
        return ignoreEmptyString;
    }

    public boolean isIgnoreEmptyArray() {
        return ignoreEmptyArray;
    }

    public NamingStyle getNamingStyle() {
        return this.namingStyle;
    }

    @Deprecated
    public DateFormat getDateFormat() {
        return dateFormat;
    }

    public DateTimeFormatter getDateTimeFormatter() {
        return dateTimeFormatter;
    }

    public boolean isNullToEmptyString() {
        return nullToEmptyString;
    }

    public boolean isHtmlEscape() {
        return htmlEscape;
    }

    public Predicate<Object> getBeanFilter() {
        return beanFilter;
    }

    public Predicate<BeanProperty> getPropertyFilter() {
        return propertyFilter;
    }

    public static final class Builder {

        private boolean                 keyQuoted         = true;
        private boolean                 ignoreNull        = false;
        private boolean                 ignoreFalse       = false;
        private boolean                 ignoreEmptyString = false;
        private boolean                 ignoreEmptyArray  = false;
        private boolean                 nullToEmptyString = false;
        private boolean                 htmlEscape        = false;
        private NamingStyle             namingStyle       = NamingStyles.RAW;
        private DateFormat              dateFormat        = null;
        private DateTimeFormatter       dateTimeFormatter;
        private Predicate<BeanProperty> propertyFilter;
        private Predicate<Object>       beanFilter;

        public Builder() {
            super();
        }

        public boolean isKeyQuoted() {
            return keyQuoted;
        }

        public Builder setKeyQuoted(boolean keyQuoted) {
            this.keyQuoted = keyQuoted;
            return this;
        }

        public boolean isIgnoreNull() {
            return ignoreNull;
        }

        public Builder setIgnoreNull(boolean ignoreNull) {
            this.ignoreNull = ignoreNull;
            return this;
        }

        public boolean isIgnoreFalse() {
            return ignoreFalse;
        }

        public Builder setIgnoreFalse(boolean ignoreFalse) {
            this.ignoreFalse = ignoreFalse;
            return this;
        }

        public Builder setIgnoreEmpty(boolean ignoreEmpty) {
            this.ignoreEmptyString = ignoreEmpty;
            this.ignoreEmptyArray = ignoreEmpty;
            return this;
        }

        public boolean isIgnoreEmptyString() {
            return ignoreEmptyString;
        }

        public void setIgnoreEmptyString(boolean ignoreEmptyString) {
            this.ignoreEmptyString = ignoreEmptyString;
        }

        public boolean isIgnoreEmptyArray() {
            return ignoreEmptyArray;
        }

        public void setIgnoreEmptyArray(boolean ignoreEmptyArray) {
            this.ignoreEmptyArray = ignoreEmptyArray;
        }

        public Builder ignoreEmpty() {
            return setIgnoreEmpty(true);
        }

        public Builder ignoreNull() {
            return setIgnoreNull(true);
        }

        public NamingStyle getNamingStyle() {
            return namingStyle;
        }

        public Builder setNamingStyle(NamingStyle namingStyle) {
            this.namingStyle = namingStyle;
            return this;
        }

        public DateFormat getDateFormat() {
            return dateFormat;
        }

        @Deprecated
        public Builder setDateFormat(String dateFormat) {
            this.dateFormat = null == dateFormat ? null : DateFormats.getFormat(dateFormat);
            return this;
        }

        public DateTimeFormatter getDateTimeFormatter() {
            return dateTimeFormatter;
        }

        public Builder setDateTimeFormatter(DateTimeFormatter formatter) {
            this.dateTimeFormatter = formatter;
            return this;
        }

        public Builder setDateTimeFormatter(String pattern) {
            return setDateTimeFormatter(pattern, null);
        }

        public Builder setDateTimeFormatter(String pattern, String zone) {
            this.dateTimeFormatter = null == pattern ? null : DateFormats.getFormatter(pattern, zone);
            return this;
        }

        public boolean isNullToEmptyString() {
            return nullToEmptyString;
        }

        public Builder setNullToEmptyString(boolean nullToEmptyString) {
            this.nullToEmptyString = nullToEmptyString;
            return this;
        }

        public boolean isHtmlEscape() {
            return htmlEscape;
        }

        public Builder setHtmlEscape(boolean htmlEscape) {
            this.htmlEscape = htmlEscape;
            return this;
        }

        public Predicate<BeanProperty> getPropertyFilter() {
            return propertyFilter;
        }

        public Builder setPropertyFilter(Predicate<BeanProperty> propertyFilter) {
            this.propertyFilter = propertyFilter;
            return this;
        }

        public Predicate<Object> getBeanFilter() {
            return beanFilter;
        }

        public Builder setBeanFilter(Predicate<Object> beanFilter) {
            this.beanFilter = beanFilter;
            return this;
        }

        public Builder setSettings(JsonSettings settings) {
            this.keyQuoted = settings.keyQuoted;
            this.ignoreNull = settings.ignoreNull;
            this.ignoreFalse = settings.ignoreFalse;
            this.ignoreEmptyString = settings.ignoreEmptyString;
            this.ignoreEmptyArray = settings.ignoreEmptyArray;
            this.nullToEmptyString = settings.nullToEmptyString;
            this.htmlEscape = settings.htmlEscape;
            this.namingStyle = settings.namingStyle;
            this.dateFormat = settings.dateFormat;
            this.dateTimeFormatter = settings.dateTimeFormatter;
            this.propertyFilter = settings.propertyFilter;
            this.beanFilter = settings.beanFilter;
            return this;
        }

        public JsonSettings build() {
            return new JsonSettings(keyQuoted, ignoreNull, ignoreFalse,
                    ignoreEmptyString, ignoreEmptyArray, nullToEmptyString, htmlEscape,
                    namingStyle, dateFormat, dateTimeFormatter, propertyFilter, beanFilter);
        }

    }
}
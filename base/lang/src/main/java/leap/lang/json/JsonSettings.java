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

import leap.lang.naming.NamingStyle;
import leap.lang.naming.NamingStyles;
import leap.lang.time.DateFormats;

import java.text.DateFormat;
import java.time.format.DateTimeFormatter;

public class JsonSettings {
	
	public static JsonSettings MAX = new Builder().build();
	
	public static JsonSettings MIN = new Builder().setKeyQuoted(false)
												  .setIgnoreEmpty(true)
												  .setIgnoreNull(true).build();

    private final boolean           keyQuoted;
    private final boolean           ignoreNull;
    private final boolean           ignoreFalse;
    private final boolean           ignoreEmptyString;
    private final boolean           ignoreEmptyArray;
    private final boolean           nullToEmptyString;
    private final NamingStyle       namingStyle;
    private final DateFormat        dateFormat;
    private final DateTimeFormatter dateFormatter;

    public JsonSettings(boolean keyQuoted, boolean ignoreNull, boolean ignoreFalse,
                        boolean ignoreEmptyString, boolean ignoreEmptyArray,
                        NamingStyle namingStyle, DateFormat dateFormat) {
        this(keyQuoted, ignoreNull, ignoreFalse, ignoreEmptyString, ignoreEmptyArray, false, namingStyle, dateFormat);
    }

    public JsonSettings(boolean keyQuoted, boolean ignoreNull, boolean ignoreFalse,
                        boolean ignoreEmptyString, boolean ignoreEmptyArray, boolean nullToEmptyString,
                        NamingStyle namingStyle, DateFormat dateFormat) {
		this.keyQuoted         = keyQuoted;
		this.ignoreNull        = ignoreNull;
        this.ignoreFalse       = ignoreFalse;
        this.ignoreEmptyString = ignoreEmptyString;
        this.ignoreEmptyArray  = ignoreEmptyArray;
		this.namingStyle       = namingStyle;
        this.dateFormat        = dateFormat;
        this.dateFormatter     = null;
        this.nullToEmptyString = nullToEmptyString;
	}

    public JsonSettings(boolean keyQuoted, boolean ignoreNull, boolean ignoreFalse,
                        boolean ignoreEmptyString, boolean ignoreEmptyArray, boolean nullToEmptyString,
                        NamingStyle namingStyle, DateFormat dateFormat, DateTimeFormatter dateFormatter) {
        this.keyQuoted         = keyQuoted;
        this.ignoreNull        = ignoreNull;
        this.ignoreFalse       = ignoreFalse;
        this.ignoreEmptyString = ignoreEmptyString;
        this.ignoreEmptyArray  = ignoreEmptyArray;
        this.namingStyle       = namingStyle;
        this.dateFormat        = dateFormat;
        this.dateFormatter     = dateFormatter;
        this.nullToEmptyString = nullToEmptyString;
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

    public NamingStyle getNamingStyle(){
		return this.namingStyle;
	}

    @Deprecated
    public DateFormat getDateFormat() {
        return dateFormat;
    }

    public DateTimeFormatter getDateFormatter() {
        return dateFormatter;
    }

    public boolean isNullToEmptyString() {
        return nullToEmptyString;
    }

    public static final class Builder {

        private boolean     keyQuoted   = true;
        private boolean     ignoreNull  = false;
        private boolean     ignoreFalse = false;
        private boolean     ignoreEmptyString = false;
        private boolean     ignoreEmptyArray  = false;
        private boolean     nullToEmptyString = false;
        private NamingStyle namingStyle = NamingStyles.RAW;
        private DateFormat  dateFormat  = null;
        private DateTimeFormatter dateFormatter;

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
            this.ignoreEmptyArray  = ignoreEmpty;
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

        public DateTimeFormatter getDateFormatter() {
            return dateFormatter;
        }

        public Builder setDateFormatter(String dateFormat) {
            this.dateFormatter = dateFormatter;
            return this;
        }

        public Builder setDateFormatter(String dateFormat, String zone) {
            this.dateFormatter = null == dateFormat ? null : DateFormats.getFormatter(dateFormat, zone);
            return this;
        }


        public boolean isNullToEmptyString() {
            return nullToEmptyString;
        }

        public Builder setNullToEmptyString(boolean nullToEmptyString) {
            this.nullToEmptyString = nullToEmptyString;
            return this;
        }

        public Builder setSettings(JsonSettings settings) {
            this.keyQuoted = settings.keyQuoted;
            this.ignoreNull = settings.ignoreNull;
            this.ignoreFalse = settings.ignoreFalse;
            this.ignoreEmptyString = settings.ignoreEmptyString;
            this.ignoreEmptyArray = settings.ignoreEmptyArray;
            this.nullToEmptyString = settings.nullToEmptyString;
            this.namingStyle = settings.namingStyle;
            this.dateFormat = settings.dateFormat;
            this.dateFormatter = settings.dateFormatter;
            return this;
        }

        public JsonSettings build(){
			return new JsonSettings(keyQuoted, ignoreNull, ignoreFalse,
                                    ignoreEmptyString, ignoreEmptyArray, nullToEmptyString,
                                    namingStyle, dateFormat, dateFormatter);
		}

    }
}
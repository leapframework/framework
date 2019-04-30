/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package leap.lang.time;

import java.time.format.DateTimeFormatter;

class DateFormatImpl implements DateFormat {

    private final DateTimeFormatter formatter;
    private final int minLength;
    private final int maxLength;

    public DateFormatImpl(DateTimeFormatter formatter) {
        this(formatter, -1, -1);
    }

    public DateFormatImpl(DateTimeFormatter formatter, int length) {
        this(formatter, length, length);
    }

    public DateFormatImpl(DateTimeFormatter formatter, int minLength, int maxLength) {
        this.formatter = formatter;
        this.minLength = minLength;
        this.maxLength = maxLength;
    }

    @Override
    public boolean matches(String s) {
        int len = s.length();

        if(minLength > 0 && len < minLength) {
            return false;
        }

        if(maxLength > 0 && len > maxLength) {
            return false;
        }

        return true;
    }

    @Override
    public DateTimeFormatter getFormatter() {
        return formatter;
    }

}

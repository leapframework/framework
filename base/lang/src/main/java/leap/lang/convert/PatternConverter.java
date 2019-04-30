/*
 * Copyright 2017 the original author or authors.
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

package leap.lang.convert;

import leap.lang.Out;

import java.lang.reflect.Type;
import java.util.regex.Pattern;

public class PatternConverter extends AbstractConverter<Pattern> {

    @Override
    public boolean convertFrom(Object value, Class<?> targetType, Type genericType, Out<Object> out, ConvertContext context) throws Throwable {
        if(value instanceof CharSequence) {
            out.set(Pattern.compile(value.toString()));
            return true;
        }
        return false;
    }

    @Override
    public String convertToString(Pattern value) throws Throwable {
        return value.pattern();
    }

}

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

import java.lang.reflect.Type;

import leap.lang.Out;
import leap.lang.Strings;

public interface Converter<T> {

    /**
     * Converts the value to the target type.
     *
     * <p/>
     * Returns <code>true</code> if the converter can converts the value to the target type,
     *
     * and sets the converted value to the out object.
     *
     * <p/>
     * Returns <code>false</code> if the converter cannot converts the value to the target type,
     *
     * that means the converter does not supports both the value type and the target type.
     */
    boolean convertFrom(Object value, Class<?> targetType, Type genericType, Out<Object> out) throws Throwable;

    /**
     * Converts the value to the target type.
     *
     * <p/>
     * Returns <code>true</code> if the converter can converts the value to the target type,
     *
     * and sets the converted value to the out object.
     *
     * <p/>
     * Returns <code>false</code> if the converter cannot converts the value to the target type,
     *
     * that means the converter does not supports both the value type and the target type.
     */
    default boolean convertFrom(Object value, Class<?> targetType, Type genericType, Out<Object> out, ConvertContext context) throws Throwable {
        return convertFrom(value, targetType, genericType, out);
    }

    /**
     * todo : doc
     */
	boolean convertTo(T value,Class<?> targetType,Type genericType,Out<Object> out) throws Throwable;

    /**
     * todo : doc
     */
    default boolean convertTo(T value, Class<?> targetType, Type genericType, Out<Object> out, ConvertContext context) throws Throwable {
        return convertTo(value, targetType, genericType, out);
    }

    /**
     * Converts the value to {@link String}.
     */
	default String convertToString(T value) throws Throwable {
        return null == value ? Strings.EMPTY : value.toString();
    }

}
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

package leap.lang.accessor;

import leap.lang.Objects2;
import leap.lang.convert.Converts;

import java.math.BigDecimal;

public interface IndexedGetter {

    /**
     * Returns the indexed value (may be null).
     *
     * @param index 0-based index.
     *
     * @throws IndexOutOfBoundsException
     */
    <T> T get(int index) throws IndexOutOfBoundsException;

    /**
     * Returns the indexed value (may be null).
     *
     * @param index 0-based index.
     *
     * @throws IndexOutOfBoundsException
     */
    default <T> T get(int index, Class<T> type) throws IndexOutOfBoundsException {
        Object v = get(index);
        return Objects2.isEmpty(v) ? null : Converts.convert(v, type);
    }

    /**
     * Returns the indexed value, return default value if the value is null or empty.
     *
     * @param index 0-based index.
     *
     * @throws IndexOutOfBoundsException
     */
    default <T> T get(int index, Class<T> type, T defaultValue) throws IndexOutOfBoundsException {
        Object v = get(index);
        return Objects2.isEmpty(v) ? defaultValue : Converts.convert(v, type);
    }

    /**
     * Returns the indexed value as {@link String}.
     */
    default String getString(int index) {
        return get(index, String.class);
    }

    /**
     * Returns the indexed value as {@link Short}.
     */
    default Short getShort(int index) {
        return get(index, Short.class);
    }

    /**
     * Returns the indexed value as <code>short</code>, returns default value if the value is null.
     */
    default short getShort(int index, short defaultValue) {
        return get(index, Short.class, defaultValue);
    }

    /**
     * Returns the indexed value as {@link Integer}.
     */
    default Integer getInteger(int index) {
        return get(index, Integer.class);
    }

    /**
     * Returns the indexed value as <code>int</code>, returns default value if the value is null.
     */
    default int getInteger(int index, int defaultValue) {
        return get(index, Integer.class, defaultValue);
    }

    /**
     * Returns the indexed value as {@link Long}.
     */
    default Long getLong(int index) {
        return get(index, Long.class);
    }

    /**
     * Returns the indexed value as <code>long</code>, returns default value if the value is null.
     */
    default long getLong(int index, long defaultValue) {
        return get(index, Long.class, defaultValue);
    }

    /**
     * Returns the indexed value as {@link Float}.
     */
    default Float getFloat(int index) {
        return get(index, Float.class);
    }

    /**
     * Returns the indexed value as <code>float</code>, returns default value if the value is null.
     */
    default float getFloat(int index, float defaultValue) {
        return get(index, Float.class, defaultValue);
    }

    /**
     * Returns the indexed value as {@link Double}.
     */
    default Double getDouble(int index) {
        return get(index, Double.class);
    }

    /**
     * Returns the indexed value as <code>double</code>, returns default value if the value is null.
     */
    default double getDouble(int index, double defaultValue) {
        return get(index, Double.class, defaultValue);
    }

    /**
     * Returns the indexed value as {@link BigDecimal}.
     */
    default BigDecimal getDecimal(int index) {
        return get(index, BigDecimal.class);
    }
    
}

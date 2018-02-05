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

import leap.lang.Named;
import leap.lang.Objects2;
import leap.lang.convert.Converts;
import leap.lang.exception.ObjectNotFoundException;

import java.math.BigDecimal;

public interface NamedGetter extends ObjectPropertyGetter {

    /**
     * Returns true if the name exists.
     */
    boolean contains(String name);

    /**
     * Checks the name must be exists.
     *
     * @throws ObjectNotFoundException if the name not exists.
     */
    default NamedGetter mustContains(String name) throws ObjectNotFoundException {
        if(!contains(name)) {
            throw new ObjectNotFoundException("The name '" + name + "' not exists!");
        }
        return this;
    }

    @Override
    default Object getProperty(String name) {
        return get(name);
    }

    /**
     * Returns the named value (may be null).
     *
     * <p/>
     * Returns <code>null</code> if the name not exists.
     */
    <T> T get(String name);

    /**
     * Returns the named value (may be null).
     *
     * @throws ObjectNotFoundException if the name not exists.
     */
    default <T> T mustGet(String name) {
        return mustContains(name).get(name);
    }

    /**
     * Returns the named value (may be null).
     *
     * <p/>
     * Returns <code>null</code> if the name not exists.
     */
    default <T> T get(Named named) {
        return get(named.getName());
    }

    /**
     * Returns the named value (may be null).
     *
     * <p/>
     * Returns <code>null</code> if the name not exists.
     */
    default <T> T get(String name, Class<T> type) {
        Object v = get(name);
        return Objects2.isEmpty(v) ? null : Converts.convert(v, type);
    }

    /**
     * Returns the named value (may be null).
     *
     * <p/>
     * Returns <code>null</code> if the name not exists.
     */
    default <T> T get(Named named, Class<T> type) {
        Object v = get(named.getName());
        return Objects2.isEmpty(v) ? null : Converts.convert(v, type);
    }

    /**
     * Returns the named value, returns default value if the value is null or empty.
     *
     * @throws ObjectNotFoundException if the value not exists.
     */
    default <T> T get(String name, Class<T> type, T defaultValue) {
        Object v = get(name);
        return Objects2.isEmpty(v) ? defaultValue : Converts.convert(v, type);
    }

    /**
     * Returns the named value as {@link String}.
     */
    default String getString(String name) {
        return get(name, String.class);
    }

    /**
     * Returns the named value as {@link Short}.
     */
    default Short getShort(String name) {
        return get(name, Short.class);
    }

    /**
     * Returns the named value as <code>short</code>, returns default value if the value is null.
     */
    default short getShort(String name, short defaultValue) {
        return get(name, Short.class, defaultValue);
    }

    /**
     * Returns the named value as {@link Integer}.
     */
    default Integer getInteger(String name) {
        return get(name, Integer.class);
    }

    /**
     * Returns the named value as <code>int</code>, returns default value if the value is null.
     */
    default int getInteger(String name, int defaultValue) {
        return get(name, Integer.class, defaultValue);
    }

    /**
     * Returns the named value as {@link Long}.
     */
    default Long getLong(String name) {
        return get(name, Long.class);
    }

    /**
     * Returns the named value as <code>long</code>, returns default value if the value is null.
     */
    default long getLong(String name, long defaultValue) {
        return get(name, Long.class, defaultValue);
    }

    /**
     * Returns the named value as {@link Float}.
     */
    default Float getFloat(String name) {
        return get(name, Float.class);
    }

    /**
     * Returns the named value as <code>float</code>, returns default value if the value is null.
     */
    default float getFloat(String name, float defaultValue) {
        return get(name, Float.class, defaultValue);
    }

    /**
     * Returns the named value as {@link Double}.
     */
    default Double getDouble(String name) {
        return get(name, Double.class);
    }

    /**
     * Returns the named value as <code>double</code>, returns default value if the value is null.
     */
    default double getDouble(String name, double defaultValue) {
        return get(name, Double.class, defaultValue);
    }

    /**
     * Returns the named value as {@link BigDecimal}.
     */
    default BigDecimal getDecimal(String name) {
        return get(name, BigDecimal.class);
    }
}

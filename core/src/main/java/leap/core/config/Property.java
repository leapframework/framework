/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package leap.core.config;

import leap.lang.convert.Converts;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A wrapper interface for configuration property.
 *
 * @param <T> the type of property value.
 */
public interface Property<T> {

    static <T> Property<T> of(Function<String, T> converter) {
        return new ConvertibleProperty<>(converter);
    }

    static <T> Property<T> of(Function<String, T> converter, T defaultValue) {
        return new ConvertibleProperty<>(converter, defaultValue);
    }

    /**
     * Returns the property value. May be null.
     */
    T get();

    /**
     * Sets the value.
     */
    void set(T value);

    /**
     * Sets the value by converting from string.
     */
    void convert(String s);

    /**
     * Converts the property value to the given type.
     */
    default <T1> T1 get(Class<T1> type) {
        return Converts.convert(get(), type);
    }

    /**
     * Returns true if the value is null.
     */
    default boolean isNull() {
        return null == get();
    }

    /**
     * Called when changed.
     */
    default void onChanged(Consumer<T> callback) {

    }

}
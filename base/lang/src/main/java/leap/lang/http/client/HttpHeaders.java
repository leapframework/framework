/*
 * Copyright 2016 the original author or authors.
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
package leap.lang.http.client;

import java.util.List;
import java.util.function.BiConsumer;

public interface HttpHeaders {

    /**
     * Empty headers.
     */
    HttpHeaders EMPTY = name -> null;

    /**
     * Returns true if the header exists.
     */
    default boolean exists(String name) { return get(name) != null; }

    /**
     * Returns a list contains all the values of header.
     *
     * <p/>
     * Returns null if the header is not exists.
     */
    List<String> get(String name);

    /**
     * Adds a value to the given header.
     */
    default void add(String name, String value) {throw new IllegalStateException("Cannot add header");}

    /**
     * Sets the value of the given header,
     *
     * the value(s) will be override if the header already has value(s) exists.
     */
    default void set(String name, String value) {throw new IllegalStateException("Cannot set header");}

    /**
     * For each all the header values.
     *
     * <p/>
     * First argument is the name of header.
     *
     * Second argument is the value of header.
     */
    default void forEach(BiConsumer<String, String> consumer) {}

}

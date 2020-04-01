/*
 *  Copyright 2020 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package leap.db.support;

import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Json column support.
 */
public interface JsonColumnSupport {

    /**
     * Returns the native data type of json column.
     */
    default String getNativeType() {
        return "json";
    }

    /**
     * todo: doc
     */
    default boolean isInsertByKeys() {
        return false;
    }

    /**
     * Apply the insert expr to the insert consumer.
     */
    default void applyInsertExpr(String column, String[] keys, Function<String, String> nameToParam, BiConsumer<String, String> insert) {
        insert.accept(column, nameToParam.apply(column));
    }

    /**
     * todo: doc
     */
    default boolean isUpdateByKeys() {
        return true;
    }

    /**
     * Returns the update expr.
     */
    default String getUpdateExpr(String column, String[] keys, Function<String, String> nameToParam) {
        throw new IllegalStateException("Not implemented");
    }
}
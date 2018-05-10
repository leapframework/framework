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

package leap.orm.sql;

import leap.lang.params.Params;
import leap.orm.metadata.MetadataContext;

import java.io.IOException;

public interface SqlTagProcessor {

    /**
     * Prepares the sql tag.
     */
    default void prepareTag(MetadataContext context, Sql sql, SqlTag tag) {

    }

    /**
     * Returns true if the processor supports {@link #toFragment(SqlContext, Sql, SqlTag, Params)}.
     */
    default boolean supportsToFragment() {
        return false;
    }

    /**
     * Returns a not null string if just returns a sql fragment.
     */
    default String toFragment(SqlContext context, Sql sql, SqlTag tag, Params params) {
        return null;
    }

    /**
     * Process the tag and returns the result sql content.
     */
    default String processTag(SqlContext context, Sql sql, SqlTag tag, Params params) {
        return null;
    }
}

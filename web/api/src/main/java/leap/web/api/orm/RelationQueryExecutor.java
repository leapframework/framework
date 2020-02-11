/*
 *  Copyright 2018 the original author or authors.
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
 */

package leap.web.api.orm;

import leap.web.api.mvc.params.QueryOptions;
import leap.web.api.mvc.params.QueryOptionsBase;

import java.util.function.Supplier;

public interface RelationQueryExecutor {

    /**
     * Find one or null record for many-to-one relation.
     */
    default QueryOneResult queryOne(Object id, QueryOptionsBase options) {
        return queryOne(id, options, null);
    }

    /**
     * Find one or null record for many-to-one relation.
     */
    QueryOneResult queryOne(Object id, QueryOptionsBase options, Supplier<QueryOneResult> func);

    /**
     * Query records for *-to-many relation.
     */
    default QueryListResult queryList(Object id, QueryOptions options) {
        return queryList(id, options, null);
    }

    /**
     * Query records for *-to-many relation.
     */
    QueryListResult queryList(Object id, QueryOptions options, Supplier<QueryListResult> func);

}
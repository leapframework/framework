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

package leap.orm.event;

import leap.orm.query.QueryContext;
import leap.orm.value.EntityWrapper;

public interface LoadEntityEvent extends EntityEvent, Iterable<LoadEntityEvent.Row> {

    /**
     * One row of load entity event.
     */
    interface Row {
        /**
         * The id in row, may be null.
         */
        Object getId();

        /**
         * The row data.
         */
        EntityWrapper getEntity();
    }

    /**
     * Returns the {@link QueryContext}
     */
    QueryContext getQueryContext();

    /**
     * Returns true if the event is triggered by 'find' (find by id) operation.
     */
    boolean isFind();

    /**
     * Returns true if the event is triggered by 'query' operation.
     */
    boolean isQuery();

    /**
     * Is one row ?
     */
    default boolean isOne() {
        return size() == 1;
    }

    /**
     * Is no row(s) ?
     */
    default boolean isEmpty() {
        return size() == 0;
    }

    /**
     * Returns the size of rows.
     */
    int size();

}
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

import java.util.Collections;
import java.util.Set;

public interface SqlMetadata {

    /**
     * Returns <code>true</code> if the sql is a select statement.
     */
    default boolean isSelect() {
        return false;
    }

    /**
     * Returns <code>true</code> if the sql is an insert statement.
     */
    default boolean isInsert() {
        return false;
    }

    /**
     * Returns <code>true</code> if the sql is an update statement.
     */
    default boolean isUpdate() {
        return false;
    }

    /**
     * Returns <code>true</code> if the sql is a delete statement.
     */
    default boolean isDelete() {
        return false;
    }

    /**
     * Returns <code>true</code> if cannot determinate the type of sql.
     */
    default boolean isUnknown() {
        return ! (isSelect() || isInsert() || isUpdate() || isDelete());
    }

    /**
     * Returns all the parameter names.
     */
    default Set<Param> getParameters() {
        return Collections.emptySet();
    }

    interface Param {

        String getName();

        boolean isRequired();

    }
}

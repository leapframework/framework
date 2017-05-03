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

package leap.orm.metadata;

import leap.lang.exception.ObjectExistsException;
import leap.orm.sql.SqlCommand;
import leap.orm.sql.SqlFragment;

public interface SqlRegistry {

    /**
     * Adds a new {@link SqlFragment} object to this metadata.
     *
     * <p>
     * Throws {@link ObjectExistsException} if the key exists.
     */
    void addSqlFragment(String key, SqlFragment fragment) throws ObjectExistsException;

    /**
     * Returns the {@link SqlFragment} for the given fragment key.
     *
     * <p>
     * Returns <code>null</code> if sql fragment does not exists.
     */
    SqlFragment tryGetSqlFragment(String key);

    /**
     * Adds a new {@link SqlCommand} object to this metadata.
     *
     * <p>
     * Throws {@link ObjectExistsException} if the key exists.
     */
    void addSqlCommand(String key, String dbType, SqlCommand cmd) throws ObjectExistsException;

    /**
     * Returns the {@link SqlCommand} for the given command key.
     *
     * <p>
     * Returns <code>null</code> if sql command does not exists.
     */
    SqlCommand tryGetSqlCommand(String key, String dbType);

    /**
     * Removes an exists {@link SqlCommand} object from this metadata.
     *
     * <p>
     * Returns the removed {@link SqlCommand} if the key mapped to an exists {@link SqlCommand}.
     *
     * <p>
     * Returns <code>null<code> if there was no mapping for the key.
     */
    SqlCommand removeSqlCommand(String key, String dbType);

}

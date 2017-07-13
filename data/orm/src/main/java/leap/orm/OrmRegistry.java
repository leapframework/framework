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

package leap.orm;

import leap.lang.Emptiable;
import leap.lang.exception.ObjectExistsException;
import leap.lang.exception.ObjectNotFoundException;

import java.util.Collection;

public interface OrmRegistry extends Emptiable {

    /**
     * Returns the collection of all context objects.
     */
    Collection<OrmContext> contexts();

    /**
     * Register a {@link OrmContext}.
     *
     * @throws ObjectExistsException if a context with the same name already exists.
     */
    default void registerContext(OrmContext context) throws ObjectExistsException {
        registerContext(context, false);
    }

    /**
     * Register a {@link OrmContext}.
     *
     * @throws ObjectExistsException if a context with the same name already exists.
     */
    void registerContext(OrmContext context, boolean _default) throws ObjectExistsException;

    /**
     * Removes the context with the given name and returns the removed context..
     *
     * <p/>
     * Do nothing if the context does not exists.
     */
    OrmContext removeContext(String name);

    /**
     * Returns null if no default context.
     */
    OrmContext getDefaultContext();

    /**
     * Returns null if not exists.
     */
    OrmContext findContext(String name);

    /**
     * Returns the context.
     *
     * @throws ObjectNotFoundException if not exists.
     */
    OrmContext getContext(String name) throws ObjectNotFoundException;
}

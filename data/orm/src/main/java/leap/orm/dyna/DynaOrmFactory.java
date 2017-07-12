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

package leap.orm.dyna;

import leap.lang.exception.ObjectExistsException;

import javax.sql.DataSource;

public interface DynaOrmFactory {

    /**
     * Creates a new {@link DynaOrmContext}.
     *
     * @throws ObjectExistsException if the name already exists.
     */
    DynaOrmContext createDynaContext(String name, DataSource ds) throws ObjectExistsException;

    /**
     * Destroy the context, clearing all resources.
     *
     * <p/>
     * Notice: The {@link DataSource} will not be destroyed.
     */
    void destroyDynaContext(DynaOrmContext context);

}
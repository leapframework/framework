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

import leap.db.Db;
import leap.lang.exception.ObjectExistsException;

import javax.sql.DataSource;

public interface DynaOrmFactory {

    String UNNAMED = "unnamed";

    /**
     * Creates a new unnamed {@link DynaOrmContext}.
     *
     * <p/>
     * The name of created {@link DynaOrmContext} is {@link #UNNAMED}.
     */
    DynaOrmContext createDynaContext(DataSource ds);

    /**
     * Creates a new named {@link DynaOrmContext}.
     */
    DynaOrmContext createDynaContext(String name, DataSource ds);

    /**
     * Creates a new unnamed {@link DynaOrmContext}.
     *
     * <p/>
     * The name of created {@link DynaOrmContext} is {@link #UNNAMED}.
     */
    DynaOrmContext createDynaContext(Db db);

    /**
     * Creates a new named {@link DynaOrmContext}.
     */
    DynaOrmContext createDynaContext(String name, Db db);

    /**
     * Creates a new named {@link DynaOrmContext} and register it to {@link leap.orm.OrmRegistry}.
     *
     * @throws ObjectExistsException if the name already exists in the {@link leap.orm.OrmRegistry}.
     */
    DynaOrmContext createDynaContext(String name, DataSource ds, boolean register) throws ObjectExistsException;

    /**
     * Destroy the context, clearing all resources.
     *
     * <p/>
     * Notice: The {@link DataSource} will not be destroyed.
     */
    void destroyDynaContext(DynaOrmContext context);

}
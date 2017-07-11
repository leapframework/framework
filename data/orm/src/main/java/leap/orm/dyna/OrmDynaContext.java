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

import leap.orm.OrmContext;
import leap.orm.dao.Dao;
import leap.orm.mapping.EntityMapping;
import leap.orm.metadata.MetadataException;

import javax.sql.DataSource;

public interface OrmDynaContext extends OrmContext {

    /**
     * Returns the {@link Dao} for data access operation.
     */
    Dao getDao();

    /**
     * Adds a new {@link EntityMapping} to this context.
     */
    void addEntity(EntityMapping em) throws MetadataException;

    /**
     * Destroy this context, clearing all resources.
     *
     * <p/>
     * Notice: The {@link DataSource} will not be destroyed.
     */
    void destroy();

}
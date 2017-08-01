/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package leap.orm.mapping;

import leap.core.AppContext;
import leap.db.Db;
import leap.orm.OrmConfig;
import leap.orm.OrmContext;
import leap.orm.OrmMetadata;
import leap.orm.metadata.MetadataContext;
import leap.orm.metadata.OrmMetadataManager;
import leap.orm.naming.NamingStrategy;

public interface MappingConfigContext extends MetadataContext {

    /**
     * Returns the {@link OrmContext}.
     */
	OrmContext getOrmContext();

    @Override
    default String getName() {
        return getOrmContext().getName();
    }

    @Override
    default Db getDb() {
        return getOrmContext().getDb();
    }

    @Override
    default OrmConfig getConfig() {
        return getOrmContext().getConfig();
    }

    @Override
    default AppContext getAppContext() {
        return getOrmContext().getAppContext();
    }

    @Override
    default OrmMetadata getMetadata() {
        return getOrmContext().getMetadata();
    }

    @Override
    default MappingStrategy getMappingStrategy() {
        return getOrmContext().getMappingStrategy();
    }

    @Override
    default NamingStrategy getNamingStrategy() {
        return getOrmContext().getNamingStrategy();
    }

    @Override
    default OrmMetadataManager getMetadataManager() {
        return getOrmContext().getMetadataManager();
    }

    /**
     * Returns all the {@link EntityMappingBuilder} in this context.
     */
	Iterable<EntityMappingBuilder> getEntityMappings();

    /**
     * Returns the {@link EntityMappingBuilder} in this context.
     *
     * @throws MappingNotFoundException if entity mapping not exists.
     */
	default EntityMappingBuilder getEntityMapping(String entityName) throws MappingNotFoundException {
        EntityMappingBuilder emb = tryGetEntityMapping(entityName);
        if(null == emb) {
            throw new MappingNotFoundException("Entity mapping '" + entityName + "' not found");
        }
        return emb;
    }

    /**
     * Returns the {@link EntityMappingBuilder} in this context.
     *
     * @throws MappingNotFoundException if entity mapping not exists.
     */
	default EntityMappingBuilder getEntityMapping(Class<?> entityClass) throws MappingNotFoundException {
        EntityMappingBuilder emb = tryGetEntityMapping(entityClass);
        if(null == emb) {
            throw new MappingNotFoundException("Entity mapping '" + entityClass + "' not found");
        }
        return emb;
    }

    /**
     * Returns the {@link EntityMappingBuilder} in this context of <code>null</code> if not exists.
     */
	EntityMappingBuilder tryGetEntityMapping(String entityName);

    /**
     * Returns the {@link EntityMappingBuilder} in this context of <code>null</code> if not exists.
     */
    EntityMappingBuilder tryGetEntityMapping(Class<?> entityClass);

    /**
     * Adds a new {@link EntityMappingBuilder} to this context.
     */
    void addEntityMapping(EntityMappingBuilder emb) throws MappingExistsException;

}
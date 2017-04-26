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

package leap.orm.mapping;

import leap.core.annotation.Inject;
import leap.db.model.DbSchema;
import leap.db.model.DbTable;
import leap.orm.metadata.MetadataException;

public class SchemaMapper implements Mapper {

    protected @Inject MappingStrategy strategy;

    @Override
    public void postMappings(MappingConfigContext context) throws MetadataException {
        if(!context.getConfig().isAutoMappingTables()) {
            return;
        }

        //Default schema.
        DbSchema schema = context.getDb().getMetadata().getSchema();
        for(DbTable table : schema.getTables()) {
            if(isTableExists(context, table)) {
                continue;
            }

            createEntityMappingByTable(context, table);
        }

        //todo : relations

    }

    protected void createEntityMappingByTable(MappingConfigContext context, DbTable table) {
        EntityMappingBuilder emb = strategy.createEntityMappingByTable(context, table);
        context.addEntityMapping(emb);
    }

    protected boolean isTableExists(MappingConfigContext context, DbTable table) {

        for(EntityMappingBuilder emb : context.getEntityMappings()) {

            if(emb.getTableName().equalsIgnoreCase(table.getName())) {
                return true;
            }

        }

        return false;
    }

}

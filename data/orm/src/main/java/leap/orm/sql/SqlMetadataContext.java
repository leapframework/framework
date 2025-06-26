/*
 * Copyright 2025 the original author or authors.
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
package leap.orm.sql;

import java.util.Map;
import leap.core.AppContext;
import leap.db.Db;
import leap.lang.Strings;
import leap.lang.annotation.Nullable;
import leap.orm.OrmConfig;
import leap.orm.OrmMetadata;
import leap.orm.mapping.EntityMapping;
import leap.orm.mapping.MappingStrategy;
import leap.orm.metadata.MetadataContext;
import leap.orm.metadata.OrmMetadataManager;
import leap.orm.naming.NamingStrategy;

public class SqlMetadataContext implements MetadataContext {

    protected final MetadataContext context;
    protected final String defaultAlias;
    protected final Map<String, EntityMapping> aliasMappings;

    public SqlMetadataContext(MetadataContext context, String defaultAlias, Map<String, EntityMapping> aliasMappings) {
        this.context = context;
        this.defaultAlias = defaultAlias;
        this.aliasMappings = aliasMappings;
    }

    @Nullable
    public String getDefaultAlias() {
        return defaultAlias;
    }

    @Nullable
    public EntityMapping getEntityMapping(String alias) {
        if (Strings.isEmpty(alias)) {
            if (null == defaultAlias) {
                return null;
            }
            return aliasMappings.get(defaultAlias);
        }
        return aliasMappings.get(alias);
    }

    @Override
    public boolean isPrimary() {
        return context.isPrimary();
    }

    @Override
    public String getName() {
        return context.getName();
    }

    @Override
    public Db getDb() {
        return context.getDb();
    }

    @Override
    public OrmConfig getConfig() {
        return context.getConfig();
    }

    @Override
    public SqlMappings getSqlMappings() {
        return context.getSqlMappings();
    }

    @Override
    public AppContext getAppContext() {
        return context.getAppContext();
    }

    @Override
    public OrmMetadata getMetadata() {
        return context.getMetadata();
    }

    @Override
    public MappingStrategy getMappingStrategy() {
        return context.getMappingStrategy();
    }

    @Override
    public NamingStrategy getNamingStrategy() {
        return context.getNamingStrategy();
    }

    @Override
    public OrmMetadataManager getMetadataManager() {
        return context.getMetadataManager();
    }
}

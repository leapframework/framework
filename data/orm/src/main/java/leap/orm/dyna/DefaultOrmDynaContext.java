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

import leap.core.AppContext;
import leap.db.Db;
import leap.orm.OrmConfig;
import leap.orm.OrmMetadata;
import leap.orm.command.CommandFactory;
import leap.orm.dao.Dao;
import leap.orm.dmo.Dmo;
import leap.orm.event.EntityEventHandler;
import leap.orm.mapping.MappingStrategy;
import leap.orm.metadata.OrmMetadataManager;
import leap.orm.naming.NamingStrategy;
import leap.orm.parameter.ParameterStrategy;
import leap.orm.query.QueryFactory;
import leap.orm.reader.EntityReader;
import leap.orm.reader.RowReader;
import leap.orm.sql.SqlFactory;

import javax.sql.DataSource;

public class DefaultOrmDynaContext implements DynaOrmContext {

    protected final String      name;
    protected final Db          db;
    protected final OrmMetadata metadata;

    protected AppContext         appContext;
    protected OrmConfig          config;
    protected Dao                dao;
    protected Dmo                dmo;
    protected OrmMetadataManager metadataManager;
    protected MappingStrategy    mappingStrategy;
    protected NamingStrategy     namingStrategy;
    protected ParameterStrategy  parameterStrategy;
    protected CommandFactory     commandFactory;
    protected SqlFactory         sqlFactory;
    protected QueryFactory       queryFactory;
    protected EntityReader       entityReader;
    protected RowReader          rowReader;
    protected EntityEventHandler eventHandler;

    public DefaultOrmDynaContext(String name, Db db, OrmMetadata md) {
        this.name     = name;
        this.db       = db;
        this.metadata = md;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public AppContext getAppContext() {
        return appContext;
    }

    @Override
    public OrmConfig getConfig() {
        return config;
    }

    @Override
    public Db getDb() {
        return db;
    }

    @Override
    public DataSource getDataSource() {
        return db.getDataSource();
    }

    @Override
    public OrmMetadata getMetadata() {
        return metadata;
    }

    @Override
    public Dao getDao() {
        return dao;
    }

    @Override
    public Dmo getDmo() {
        return dmo;
    }

    @Override
    public MappingStrategy getMappingStrategy() {
        return mappingStrategy;
    }

    @Override
    public NamingStrategy getNamingStrategy() {
        return namingStrategy;
    }

    @Override
    public OrmMetadataManager getMetadataManager() {
        return metadataManager;
    }

    @Override
    public ParameterStrategy getParameterStrategy() {
        return parameterStrategy;
    }

    @Override
    public CommandFactory getCommandFactory() {
        return commandFactory;
    }

    @Override
    public SqlFactory getSqlFactory() {
        return sqlFactory;
    }

    @Override
    public QueryFactory getQueryFactory() {
        return queryFactory;
    }

    @Override
    public EntityReader getEntityReader() {
        return entityReader;
    }

    @Override
    public RowReader getRowReader() {
        return rowReader;
    }

    @Override
    public EntityEventHandler getEntityEventHandler() {
        return eventHandler;
    }

    public void setAppContext(AppContext appContext) {
        this.appContext = appContext;
    }

    public void setConfig(OrmConfig config) {
        this.config = config;
    }


    public void setDao(Dao dao) {
        this.dao = dao;
    }

    public void setDmo(Dmo dmo) {
        this.dmo = dmo;
    }

    public void setMetadataManager(OrmMetadataManager metadataManager) {
        this.metadataManager = metadataManager;
    }

    public void setMappingStrategy(MappingStrategy mappingStrategy) {
        this.mappingStrategy = mappingStrategy;
    }

    public void setNamingStrategy(NamingStrategy namingStrategy) {
        this.namingStrategy = namingStrategy;
    }

    public void setParameterStrategy(ParameterStrategy parameterStrategy) {
        this.parameterStrategy = parameterStrategy;
    }

    public void setCommandFactory(CommandFactory commandFactory) {
        this.commandFactory = commandFactory;
    }

    public void setSqlFactory(SqlFactory sqlFactory) {
        this.sqlFactory = sqlFactory;
    }

    public void setQueryFactory(QueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    public void setEntityReader(EntityReader entityReader) {
        this.entityReader = entityReader;
    }

    public void setRowReader(RowReader rowReader) {
        this.rowReader = rowReader;
    }

    public void setEventHandler(EntityEventHandler eventHandler) {
        this.eventHandler = eventHandler;
    }
}

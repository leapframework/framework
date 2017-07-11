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
import leap.core.annotation.Inject;
import leap.db.Db;
import leap.orm.OrmConfig;
import leap.orm.OrmMetadata;
import leap.orm.command.CommandFactory;
import leap.orm.dao.Dao;
import leap.orm.event.EntityEventHandler;
import leap.orm.mapping.EntityMapping;
import leap.orm.mapping.MappingStrategy;
import leap.orm.metadata.OrmMetadataManager;
import leap.orm.naming.NamingStrategy;
import leap.orm.parameter.ParameterStrategy;
import leap.orm.query.QueryFactory;
import leap.orm.reader.EntityReader;
import leap.orm.reader.RowReader;
import leap.orm.sql.SqlFactory;

import javax.sql.DataSource;

public class DefaultOrmDynaContext implements OrmDynaContext {

    protected final String      name;
    protected final Db          db;
    protected final OrmMetadata metadata;

    protected @Inject AppContext         appContext;
    protected @Inject OrmConfig          config;
    protected @Inject OrmMetadataManager omm;
    protected @Inject MappingStrategy    mappingStrategy;
    protected @Inject NamingStrategy     namingStrategy;
    protected @Inject ParameterStrategy  parameterStrategy;
    protected @Inject CommandFactory     commandFactory;
    protected @Inject SqlFactory         sqlFactory;
    protected @Inject QueryFactory       queryFactory;
    protected @Inject EntityReader       entityReader;
    protected @Inject RowReader          rowReader;
    protected @Inject EntityEventHandler eventHandler;

    protected Dao dao;

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
    public MappingStrategy getMappingStrategy() {
        return mappingStrategy;
    }

    @Override
    public NamingStrategy getNamingStrategy() {
        return namingStrategy;
    }

    @Override
    public OrmMetadataManager getMetadataManager() {
        return omm;
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

    @Override
    public Dao getDao() {
        return dao;
    }

    public void setDao(Dao dao) {
        this.dao = dao;
    }

    @Override
    public void addEntity(EntityMapping em) {
        omm.createEntity(this, em);
    }

    @Override
    public void destroy() {

    }
}

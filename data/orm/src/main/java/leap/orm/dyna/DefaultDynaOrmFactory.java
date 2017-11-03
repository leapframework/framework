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
import leap.core.BeanFactory;
import leap.core.annotation.Inject;
import leap.db.Db;
import leap.db.DbFactory;
import leap.lang.Strings;
import leap.lang.exception.ObjectExistsException;
import leap.orm.OrmConfig;
import leap.orm.OrmContext;
import leap.orm.OrmMetadata;
import leap.orm.OrmRegistry;
import leap.orm.command.CommandFactory;
import leap.orm.dao.Dao;
import leap.orm.dao.DefaultDao;
import leap.orm.dmo.DefaultDmo;
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
import leap.orm.sql.SqlMetadata;

import javax.sql.DataSource;

public class DefaultDynaOrmFactory implements DynaOrmFactory {

    protected @Inject BeanFactory        bf;
    protected @Inject AppContext         appContext;
    protected @Inject OrmConfig          config;
    protected @Inject OrmRegistry        registry;
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

    @Override
    public DynaOrmContext createDynaContext(DataSource ds) {
        return createDynaContext(UNNAMED, ds);
    }

    @Override
    public DynaOrmContext createDynaContext(String name, DataSource ds) {
        return createDynaContext(name, DbFactory.createInstance(ds));
    }

    @Override
    public DynaOrmContext createDynaContext(Db db) {
        return createDynaContext(null, db);
    }

    public DynaOrmContext createDynaContext(String name, Db db) {
        if(Strings.isEmpty(name)) {
            name = UNNAMED;
        }

        final OrmMetadata md = omm.createMetadata();

        DefaultOrmDynaContext context = new DefaultOrmDynaContext(name, db, md);

        context.setAppContext(appContext);
        context.setConfig(config);
        context.setMetadataManager(omm);
        context.setMappingStrategy(mappingStrategy);
        context.setNamingStrategy(namingStrategy);
        context.setParameterStrategy(parameterStrategy);
        context.setCommandFactory(commandFactory);
        context.setSqlFactory(sqlFactory);
        context.setQueryFactory(queryFactory);
        context.setEntityReader(entityReader);
        context.setRowReader(rowReader);
        context.setEventHandler(eventHandler);

        Dao dao = bf.inject(new DefaultDao(context));
        context.setDao(dao);

        Dmo dmo = bf.inject(new DefaultDmo(context));
        context.setDmo(dmo);

        return context;
    }

    @Override
    public DynaOrmContext createDynaContext(String name, DataSource ds, boolean register) throws ObjectExistsException {
        if(!register) {
            return createDynaContext(name, ds);
        }

        if(registry.findContext(name) != null) {
            throw new ObjectExistsException("The context '" + name + "' already exists in registry!");
        }

        DynaOrmContext context = createDynaContext(name, ds);

        registry.registerContext(context);

        return context;
    }

    @Override
    public void destroyDynaContext(DynaOrmContext context) {
        OrmContext registered = registry.findContext(context.getName());
        if(registered == context) {
            registry.removeContext(context.getName());
        }
    }

}
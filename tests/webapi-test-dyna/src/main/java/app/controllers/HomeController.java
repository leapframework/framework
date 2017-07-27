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

package app.controllers;

import app.models.Entity1;
import leap.core.annotation.Inject;
import leap.core.ds.DataSourceProps;
import leap.core.ds.DataSourceManager;
import leap.lang.Try;
import leap.orm.dmo.Dmo;
import leap.orm.dyna.DynaOrmContext;
import leap.orm.dyna.DynaOrmFactory;
import leap.web.api.config.ApiConfigurator;
import leap.web.api.config.model.RestdConfig;
import leap.web.api.dyna.DynaApi;
import leap.web.api.dyna.DynaApiCreator;
import leap.web.api.dyna.DynaApiFactory;

import javax.sql.DataSource;

public class HomeController {

    private @Inject DataSourceManager dsm;
    private @Inject DynaOrmFactory    dyf;
    private @Inject DynaApiFactory    daf;

    private DataSource     dataSource;
    private DynaOrmContext ormContext;
    private DynaApi        api;

    public void createApi() {
        if(null != api) {
            throw new IllegalStateException("Api already created");
        }

        createDataSource();
        createOrmContext();
        doCreateApi();
    }

    public void destroyApi() {
        if(null == api) {
            throw new IllegalStateException("Api not created yet");
        }

        daf.destroyDynaApi(api);
        dyf.destroyDynaContext(ormContext);
        dsm.destroyDataSource(dataSource);

        api = null;
    }

    protected void doCreateApi() {
        DynaApiCreator  creator = daf.createDynaApi("test", "/test");
        ApiConfigurator cfg     = creator.configurator();

        RestdConfig rc = new RestdConfig();
        rc.setDataSourceName("test");
        cfg.setRestdConfig(rc);

        api = creator.create();
    }

    protected void createDataSource() {
        DataSourceProps.Builder dsc = new DataSourceProps.Builder();
        dsc.setDefault(true);
        dsc.setDriverClassName("org.h2.Driver");
        dsc.setJdbcUrl("jdbc:h2:./target/test;DB_CLOSE_ON_EXIT=FALSE");
        dsc.setUsername("sa");

        Try.throwUnchecked(() -> {
            dataSource = dsm.createDataSource("test", dsc.build());
        });
    }

    protected void createOrmContext() {
        //create orm context
        ormContext = dyf.createDynaContext("test", dataSource, true);

        //create entities.
        createEntities();
    }

    protected void createEntities() {
        Dmo dmo = ormContext.getDmo();
        dmo.cmdCreateEntity(Entity1.class).setTableName("table1").setCreateTable(true).execute();
    }

}

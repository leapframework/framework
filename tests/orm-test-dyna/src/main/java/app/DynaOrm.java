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

package app;

import app.models.Entity1;
import leap.core.annotation.Bean;
import leap.core.annotation.Inject;
import leap.core.ds.DataSourceConfig;
import leap.core.ds.DataSourceManager;
import leap.lang.Try;
import leap.orm.dmo.Dmo;
import leap.orm.dyna.DynaOrmContext;
import leap.orm.dyna.DynaOrmFactory;

import javax.sql.DataSource;

@Bean(lazyInit = false)
public class DynaOrm {

    public static final String NAME = "test";

    private @Inject DataSourceManager dsm;
    private @Inject DynaOrmFactory    dyf;

    private DataSource     testDataSource;
    private DynaOrmContext testOrmContext;

    public DynaOrm() {

    }

    public DynaOrmContext createTestContext() {
        //create data source.
        createTestDataSource();

        //create orm context.
        createTestOrmContext();

        return testOrmContext;
    }

    public void destroyTestContext() {
        //destroy data source.
        dsm.destroyDataSource(testDataSource);

        //destroy orm context
        dyf.destroyDynaContext(testOrmContext);
    }

    protected void createTestDataSource() {
        DataSourceConfig.Builder dsc = new DataSourceConfig.Builder();
        dsc.setDefault(true);
        dsc.setDriverClassName("org.h2.Driver");
        dsc.setJdbcUrl("jdbc:h2:./target/test;DB_CLOSE_ON_EXIT=FALSE");
        dsc.setUsername("sa");

        Try.throwUnchecked(() -> {
            testDataSource = dsm.createDataSource(NAME, dsc.build());
        });
    }

    protected void createTestOrmContext() {
        //create orm context
        testOrmContext = dyf.createDynaContext(NAME,testDataSource, true);

        //create entities.
        createEntities();
    }

    protected void createEntities() {
        Dmo dmo = testOrmContext.getDmo();

        dmo.cmdCreateEntity(Entity1.class).setTableName("table1").setCreateTable(true).execute();
    }
}
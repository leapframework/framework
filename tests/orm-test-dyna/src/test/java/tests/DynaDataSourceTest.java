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

package tests;

import app.models.Entity2;
import leap.core.annotation.Inject;
import leap.core.junit.AppTestBase;
import leap.orm.OrmContext;
import leap.orm.dao.Dao;
import leap.orm.dyna.DynaDataSource;
import leap.orm.dyna.DynaOrmFactory;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.sql.DataSource;

public class DynaDataSourceTest extends AppTestBase {

    private static @Inject(name = "db1") DataSource db1;
    private static @Inject(name = "db2") DataSource db2;

    private static @Inject DynaOrmFactory ormFactory;
    private static OrmContext ormContext;

    @BeforeClass
    public static void init() {
        DynaDataSource dds = new DynaDataSource();

        //init by db1
        dds.exec(db1, () -> {
            ormContext = ormFactory.createDynaContext(dds);
            ormContext.getMetadataManager().loadPackage(ormContext, "app");
        });
    }

    @Test
    public void testSimpleDynaDataSource() {
        DynaDataSource dds = (DynaDataSource)ormContext.getDataSource();
        assertNull(dds.getCurrentDataSource());

        //test mapping.
        ormContext.getMetadata().getEntityMapping(Entity2.class);

        Dao dao = ormContext.getDao();

        //test db1 insert
        dds.exec(db1, () -> {
            assertEquals(0, dao.count(Entity2.class));
            dao.insert(new Entity2());
            assertEquals(1, dao.count(Entity2.class));
        });
        assertNull(dds.getCurrentDataSource());

        //test db2 insert
        dds.exec(db2, () -> {
            assertEquals(0, dao.count(Entity2.class));
            dao.insert(new Entity2());
            assertEquals(1, dao.count(Entity2.class));
        });
        assertNull(dds.getCurrentDataSource());

        //test db1 delete
        dds.exec(db1, () -> {
            assertEquals(1, dao.count(Entity2.class));
            dao.deleteAll(Entity2.class);
            assertEquals(0, dao.count(Entity2.class));
        });
        assertNull(dds.getCurrentDataSource());

        //test db2 delete
        dds.exec(db2, () -> {
            assertEquals(1, dao.count(Entity2.class));
            dao.deleteAll(Entity2.class);
            assertEquals(0, dao.count(Entity2.class));
        });
        assertNull(dds.getCurrentDataSource());
    }
}

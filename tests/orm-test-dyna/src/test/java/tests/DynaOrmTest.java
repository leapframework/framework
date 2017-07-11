/*
 *
 *  * Copyright 2013 the original author or authors.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  
 */

package tests;

import app.DynaOrm;
import leap.core.annotation.Inject;
import leap.core.ds.DataSourceManager;
import leap.core.junit.AppTestBase;
import leap.orm.OrmMetadata;
import leap.orm.dao.Dao;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class DynaOrmTest extends AppTestBase {

    protected @Inject DynaOrm           dynaOrm;
    protected @Inject DataSourceManager dsm;

    protected Dao         dao;
    protected OrmMetadata md;

    @Override
    protected void setUp() throws Exception {
        dynaOrm.createTestContext();
        //dao = Dao.get("test");
        //md  = dao.getOrmContext().getMetadata();
    }

    @Override
    protected void tearDown() throws Exception {
        dynaOrm.destroyTestContext();
    }

    @Test
    public void testDynaCreatedDataSource() throws SQLException {
        DataSource ds = dsm.getDefaultDataSource();
        assertNotNull(ds);

        try(Connection connection = ds.getConnection()) {}
    }

    @Test
    public void testDynaCreatedEntityMapping() {

    }
}

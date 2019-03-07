/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package tests;

import leap.orm.annotation.SqlKey;
import leap.orm.dao.Dao;
import leap.orm.dao.DaoCommand;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import pkg0.Application;
import pkg0.Entity0;
import pkg1.Entity1;
import pkg2.Entity2;
import pkg3.Entity3;
import pkg4.Entity4;
import pkg5_.Entity5;

import javax.sql.DataSource;

import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertNotSame;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class SimpleTest {

    @Autowired
    private DataSource dataSource1;

    @Autowired
    @Qualifier("secondary")
    private DataSource dataSource2;

    @Autowired
    private Dao dao1;

    @Autowired
    @Qualifier("secondary")
    private Dao dao2;

    @Autowired
    @SqlKey("test")
    private DaoCommand testCommand1;

    @Autowired
    @Qualifier("test")
    private DaoCommand testCommand2;

    @Test
    public void testEntities() {
        dao1.findOrNull(Entity0.class, "1");
        dao1.findOrNull(Entity1.class, "1");
        dao1.findOrNull(Entity2.class, "1");
        dao1.findOrNull(Entity3.class, "1");
        dao1.findOrNull(Entity4.class, "1");
        dao1.findOrNull(Entity5.class, "1");
    }

    @Test
    public void testMultiDao() {
        assertNotSame(dataSource1, dataSource2);

        assertNotSame(dao1, dao2);
        assertNotSame(dao1.getOrmContext().getDataSource(), dao2.getOrmContext().getDataSource());
    }

    @Test
    public void testDaoCommandAutowired() {
        assertNotNull(testCommand1);
        assertNotNull(testCommand2);
    }
}
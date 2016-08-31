/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package leap.orm.dao.query;

import leap.core.annotation.Inject;
import leap.junit.contexual.Contextual;
import leap.lang.Confirm;
import leap.lang.New;
import leap.orm.OrmTestCase;
import leap.orm.annotation.SqlKey;
import leap.orm.dao.DaoCommand;
import leap.orm.sql.SqlNotFoundException;
import leap.orm.tested.model.ECodeModel;
import leap.orm.tested.model.ECodeModel1;
import org.junit.Test;

import javax.sql.DataSource;

public class WhereColumnTest extends OrmTestCase {
    @Inject
    @SqlKey(value = "testSingleWhereColumn.ECodeModel.all",datasource = "h2")
    private DaoCommand selectAllCommand;
    @Inject
    @SqlKey(value = "testSqlDatasource",datasource = "h2")
    private DaoCommand testSqlDatasource;
    @Test
    public void setTestSqlDatasource(){
        if(dao.getOrmContext().getDataSource()!=factory.getBean(DataSource.class,"h2")){
            boolean exception = false;
            try {
                dao.createNamedQuery("testSqlDatasource");
            } catch (SqlNotFoundException e) {
                exception = true;
            }
            assertTrue(exception);
        }
        if(dao.getOrmContext().getDataSource()==factory.getBean(DataSource.class,"h2")){
            DataSource ds = testSqlDatasource.dao().getOrmContext().getDataSource();
            assertTrue(factory.getBean(DataSource.class,"h2") == ds);
        }
    }

    @Test
    public void testSingleWhereQuery() {
        Confirm.execute( () -> dmo.truncate(ECodeModel.class));

        ECodeModel o1 = new ECodeModel("1").create();
        ECodeModel o2 = new ECodeModel("2").set("ecode","t1").create();

        assertEquals(1,ECodeModel.where("1=1").count());
        assertEquals("1",ECodeModel.<ECodeModel>where("1=1").first().getName());

        assertEquals("1",ECodeModel.<ECodeModel>where("name = ?", "1").first().getName());
        assertNull(ECodeModel.<ECodeModel>where("name = :name", New.hashMap("ecode",null)).orderBy("name asc").firstOrNull());

        assertEquals(1,ECodeModel.where("ecode = ?", "t1").count());
        assertEquals("2",ECodeModel.<ECodeModel>where("ecode = ?", "t1").first().getName());

        assertEquals(1, ECodeModel.where("1=1").update(New.hashMap("name", "11")));
        assertEquals("11",ECodeModel.<ECodeModel>where("1=1").first().getName());

        assertEquals(1, ECodeModel.where("ecode = ?", "t1").update(New.hashMap("name", "22")));
        assertEquals("22",ECodeModel.<ECodeModel>where("ecode = ?", "t1").first().getName());

        assertEquals(1, ECodeModel.where("1=1").delete());
        assertNull(ECodeModel.<ECodeModel>where("1=1").firstOrNull());

        assertEquals(1, ECodeModel.where("ecode = ?", "t1").delete());
        assertNull(ECodeModel.<ECodeModel>where("ecode = ?", "t1").firstOrNull());
    }

    @Test
    @Contextual("h2")
    public void testSingleNamedQuery() {
        Confirm.execute( () -> dmo.truncate(ECodeModel.class));

        ECodeModel o1 = new ECodeModel("1").create();
        ECodeModel o2 = new ECodeModel("2").set("ecode","t1").create();

        assertEquals(1, ECodeModel.query("testSingleWhereColumn.ECodeModel.all").orderBy("ecode asc").list().size());
    }

    @Test
    @Contextual("h2")
    public void testComplexQuery() {
        ECodeModel.query("testSingleWhereColumn.ECodeModel.complexQuery").list();
    }

    @Test
    @Contextual("h2")
    public void testLeftJoinWithDynamicClause() {
        ECodeModel.query("testSingleWhereColumn.ECodeModel.leftJoinDynamic").list();
    }

    @Test
    @Contextual("h2")
    public void testWhereWithDynamicClause() {
        ECodeModel.query("testSingleWhereColumn.ECodeModel.whereDynamic").list();
    }

    @Test
    @Contextual("h2")
    public void testWhereWithDynamicLeftJoin() {
        ECodeModel.query("testSingleWhereColumn.ECodeModel.whereWithDynamicLeftJoin").list();
    }

    @Test
    @Contextual("h2")
    public void testWhereWithCommaJoin() {
        ECodeModel.query("testSingleWhereColumn.ECodeModel.whereWithCommaJoin").list();
    }

    @Test
    public void testLeftJoinAndWhereQuery() {
        ECodeModel.deleteAll();
        ECodeModel1.deleteAll();

        new ECodeModel("1").id("1").set("ecode","t1").create();
        new ECodeModel1("2").id("1").set("ecode","t1").create();
        new ECodeModel1("3").id("1").set("ecode","t2").create();

        long count =
            dao.createSqlQuery("select * from ECodeModel t1 left join ECodeModel1 t2 on t1.id = t2.id")
               .count();

        assertEquals(0, count);
    }

    @Test
    public void testInnerJoinAndLeftJoinQuery() {
        ECodeModel.deleteAll();
        ECodeModel1.deleteAll();

        new ECodeModel("2").id("1").set("ecode","t1").create();
        new ECodeModel1("2").id("1").set("ecode","t2").create();

        String sql =
            "select * from ECodeModel t1 " +
            "inner join ECodeModel1 t2 on t1.id = t2.id " +
            "left join EcodeModel1 t3 on t1.id = t3.id";

        dao.createSqlQuery(sql).count();
    }

    @Test
    public void testUnionQuery() {
        ECodeModel.deleteAll();
        ECodeModel1.deleteAll();

        new ECodeModel("1").id("1").set("ecode","t").create();
        new ECodeModel("2").id("2").set("ecode","t1").create();
        new ECodeModel1("3").id("1").set("ecode","t").create();
        new ECodeModel1("4").id("2").set("ecode","t1").create();

        String sql = "select * from ECodeModel t1 union select * from ECodeModel1 t2";

        assertEquals(2,dao.createSqlQuery(sql).count());

        sql = "select * from (" +
                    "select * from ECodeModel t1 union select * from ECodeModel1 t2" +
                ") t";

        assertEquals(2, dao.createSqlQuery(sql).count());
    }
}

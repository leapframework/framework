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

package leap.orm.dao;

import leap.junit.contexual.Contextual;
import leap.orm.OrmTestCase;
import org.junit.Test;

public class ShardingTest extends OrmTestCase {

    @Test
    @Contextual("h2")
    public void testSimpleSharding() {
//        //delete all
//        ShardingModel.where("type = 'a'").delete();
//        ShardingModel.where("type = 'b'").delete();
//
//        //insert
//        Object id1 = new ShardingModel().set("value","1").set("type", "a").create().id();
//        assertEquals("1",db.queryForString("select value_ from sharding_model_a where id_ = ?", new Object[]{id1}));
//
//        Object id2 = new ShardingModel().set("value","2").set("type", "b").create().id();
//        assertEquals("2",db.queryForString("select value_ from sharding_model_b where id_ = ?", new Object[]{id2}));
//
//        //update
//        new ShardingModel().id(id1).set("type","a").set("value", "2").update();
//        assertEquals("2",db.queryForString("select value_ from sharding_model_a where id_ = ?", new Object[]{id1}));
//
//        new ShardingModel().id(id2).set("type","b").set("value", "3").update();
//        assertEquals("3",db.queryForString("select value_ from sharding_model_b where id_ = ?", new Object[]{id2}));
//
//        //select
//        ShardingModel m1 = ShardingModel.<ShardingModel>where("type = 'a'").firstOrNull();
//        assertEquals("2", m1.getValue());
//
//        ShardingModel m2 = ShardingModel.<ShardingModel>where("type = 'b'").firstOrNull();
//        assertEquals("3", m2.getValue());
//
//        //delete
//        ShardingModel.where("id = :id and type = :type", New.hashMap("id", id1, "type", "a")).delete();
//        assertNull(ShardingModel.<ShardingModel>where("type = 'a'").firstOrNull());
//
//        ShardingModel.where("type = :type and id = :id", New.hashMap("id", id2, "type", "b")).delete();
//        assertNull(ShardingModel.<ShardingModel>where("type = 'b'").firstOrNull());
    }

    //todo
    public void testIndexParamsSharding() {

    }

    //todo
    public void testJoinQueryWithSingleSharding() {

    }

    //todo
//    @Test
//    @Contextual("h2")
//    public void testJoinQueryWithMultiSharding() {
//        String sql =
//                "select t1.*,t2.* from ShardingModel t1 join ShardingModel1 t2 on t1.value = t2.value " +
//                "where t1.type = 'a' and t2.type = 'b'";
//
//        dao.createSqlQuery(sql).firstOrNull();
//    }
}
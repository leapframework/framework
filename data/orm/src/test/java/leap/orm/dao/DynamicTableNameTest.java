/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package leap.orm.dao;

import leap.core.value.Record;
import leap.lang.Dates;
import leap.orm.OrmTestCase;
import leap.orm.tested.model.sharding.CustomerOrder;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DynamicTableNameTest extends OrmTestCase {

    @Test
    public void testCreateAndDelete() {
        CustomerOrder order = new CustomerOrder();
        order.setName("name1");
        order.setCreatedAt(Dates.parse("2007-10-07"));
        order.create();

        List<Record> re = dao.createSqlQuery("select * from customer_order_2007 where id_ = ?", order.getId()).list();
        assertNotEmpty(re);
        assertEquals(1, re.size());
        assertEquals("name1", re.get(0).getString("name"));

        Map<String, Object> params = new HashMap<>();
        params.put("id", order.getId());
        params.put("createdAt", Dates.parse("2007-01-01"));
        CustomerOrder.deleteAll("id = :id", params);

        re = dao.createSqlQuery("select * from customer_order_2007 where id_ = ?", order.getId()).list();
        assertEquals(0, re.size());
    }

    @Test
    public void testSqlQuery() {
        CustomerOrder order = new CustomerOrder();
        order.setName("name1");
        order.setCreatedAt(Dates.parse("2017-10-07"));
        order.create();

        boolean exist = dao.createSqlQuery("select * from CustomerOrder where id = :id").param("id", order.getId()).exists();
        assertFalse(exist);

        exist = dao.createSqlQuery("select * from CustomerOrder where id = :id").param("id", order.getId())
                .param("createdAt", Dates.parse("2017-09-09")).exists();
        assertTrue(exist);

        Map<String, Object> params = new HashMap<>();
        params.put("id", order.getId());
        params.put("createdAt", Dates.parse("2017-01-01"));
        CustomerOrder.deleteAll("id = :id", params);
    }

    @Test
    public void testBatchUpdate() {
        boolean exist = dao.createSqlQuery("select * from CustomerOrder where name = :name").param("name", "name1")
                .param("createdAt", Dates.parse("2017-09-09")).exists();
        assertFalse(exist);

        exist = dao.createSqlQuery("select * from CustomerOrder where name = :name").param("name", "name2")
                .param("createdAt", Dates.parse("2007-09-09")).exists();
        assertFalse(exist);

        exist = dao.createSqlQuery("select * from CustomerOrder where name = :name").param("name", "name3")
                .param("createdAt", Dates.parse("2017-09-09")).exists();
        assertFalse(exist);

        List<CustomerOrder> list = new ArrayList<>();
        list.add(new CustomerOrder("name1", Dates.parse("2017-10-07")));
        list.add(new CustomerOrder("name2", Dates.parse("2007-10-07")));
        list.add(new CustomerOrder("name3", Dates.parse("2017-10-07")));
        dao.batchInsert(list);

        exist = dao.createSqlQuery("select * from CustomerOrder where name = :name").param("name", "name1")
                .param("createdAt", Dates.parse("2017-09-09")).exists();
        assertTrue(exist);

        exist = dao.createSqlQuery("select * from CustomerOrder where name = :name").param("name", "name2")
                .param("createdAt", Dates.parse("2007-09-09")).exists();
        assertTrue(exist);

        exist = dao.createSqlQuery("select * from CustomerOrder where name = :name").param("name", "name3")
                .param("createdAt", Dates.parse("2017-09-09")).exists();
        assertTrue(exist);

        Map<String, Object> params = new HashMap<>();
        params.put("createdAt", Dates.parse("2017-01-01"));
        CustomerOrder.deleteAll("1 = 1", params);
        params.put("createdAt", Dates.parse("2007-01-01"));
        CustomerOrder.deleteAll("1 = 1", params);
    }

}

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

package leap.orm.dao.query;

import leap.junit.contexual.ContextualIgnore;
import leap.orm.OrmTestCase;
import leap.orm.query.CriteriaQuery;
import leap.orm.query.Query;
import leap.orm.sql.ast.SqlQuery;
import leap.orm.tested.model.petclinic.Owner;
import org.junit.Test;

@ContextualIgnore
public class CountQueryTest extends OrmTestCase {

    @Override
    protected void doSetUp() throws Exception {
        deleteAll(Owner.class);

        Owner o1 = new Owner();
        Owner o2 = new Owner();
        Owner o3 = new Owner();
        Owner o4 = new Owner();
        Owner o5 = new Owner();

        o1.setFullName("a", "0");
        o2.setFullName("a", "1");
        o3.setFullName("b", "0");
        o4.setFullName("b", "1");
        o5.setFullName("c", "0");

        o1.save();
        o2.save();
        o3.save();
        o4.save();
        o5.save();
    }

    @Test
    public void testCriteriaQueryCountWithGroupBy() {
        CriteriaQuery<Owner> query = dao.createCriteriaQuery(Owner.class);
        assertEquals(5, query.count());

        //group by
        query.select("firstName");
        query.groupBy("firstName");
        assertEquals(3, query.count());
    }

    @Test
    public void testCriteriaQueryCountWithDistinct() {
        CriteriaQuery<Owner> query = dao.createCriteriaQuery(Owner.class);
        query.distinct().select("firstName");
        assertEquals(3, query.list().size());
        assertEquals(3, query.count());
    }

    @Test
    public void testSqlQueryCountWithGroupBy() {
        Query<Owner> query = dao.createSqlQuery(Owner.class, "select firstName from owner order by firstName group by firstName");
        assertEquals(3, query.count());
    }

    @Test
    public void testSqlQueryCountWithDistinct() {
        Query<Owner> query = dao.createSqlQuery(Owner.class, "select distinct firstName from owner order by firstName");
        assertEquals(3, query.count());
    }
}

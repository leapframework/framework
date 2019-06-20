/*
 *
 *  * Copyright 2019 the original author or authors.
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

package leap.orm.dao.query;

import leap.core.value.Record;
import leap.junit.contexual.Contextual;
import leap.orm.OrmTestCase;
import leap.orm.tested.model.petclinic.Owner;
import org.junit.Test;

public class SqlQueryTest extends OrmTestCase {

    @Test
    @Contextual("mysql")
    public void testQuotedColumnAlias() {
        deleteAll(Owner.class);

        new Owner().setFullName("a", "0").save();

        Record record = dao.createSqlQuery("select `first_name` as `myFirstName` from owner").single();
        assertEquals("a", record.getString("myFirstName"));
    }

    @Test
    @Contextual("h2")
    public void testCountQueryWithCountColumn() {
        dao.createSqlQuery("select count(*) from TestCountColumn").scalar().getInteger();
    }
}

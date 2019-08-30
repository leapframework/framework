/*
 *  Copyright 2019 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package leap.orm.mapping;

import leap.junit.contexual.Contextual;
import leap.orm.OrmTestCase;
import leap.orm.sql.SqlMappings;
import org.junit.Test;

public class SqlMappingsTest extends OrmTestCase {

    @Test
    @Contextual("h2")
    public void testSqlFunctionMapping() {
        final SqlMappings mappings = dao.getOrmContext().getSqlMappings();

        try {
            dao.createSqlQuery("select __count__(0) from dual").count();
            fail();
        } catch (Exception e) {
        }

        mappings.addFunction("__count__", "h2", "count");
        try {
            dao.createSqlQuery("select __count__(1) from dual").count();
        } catch (Exception e) {
            fail();
        }

        try {
            dao.createSqlQuery("select __count__ (1) from dual").count();
        } catch (Exception e) {
            fail();
        }

        try {
            dao.createSqlQuery("select __count__ (1), __count__(2) from dual").count();
        } catch (Exception e) {
            fail();
        }
    }

}

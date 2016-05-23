/*
 * Copyright 2013 the original author or authors.
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
 */
package leap.db;

import leap.core.AppContext;
import leap.junit.contexual.Contextual;
import org.junit.Test;

public class DbTestCaseTest extends DbTestCase {
	
	@Test
	public void testDbTestCaseNonContextual(){
		assertNotNull(db);
		assertNotNull(metadata);
		assertNotNull(dialect);
		assertSame(db, defaultDb);
	}

	@Test
	@Contextual
	public void testDbTestCaseContextual(){
		assertNotNull(db);
		assertNotNull(metadata);
		assertNotNull(dialect);
	}

    @Test
    @Contextual("not_exists_db")
    public void testNotExistsDb() {

    }

    @Test
    public void testInitBeans() {
        Db db = AppContext.factory().getBean(Db.class);
        assertNotNull(db);
    }

    /*
    @Test
    @Contextual("mysql")
    public void testFlyway() {

        Flyway flyway = new Flyway();

        flyway.setDataSource(db.getDataSource());

        final String sqls = Resources.getResource("classpath:/test/mysql_script.sql").getContent();

        db.execute(conn -> {

            JdbcTemplate jt = new JdbcTemplate(conn,0);

            SqlScript script = new SqlScript(sqls, new MySQLDbSupport(conn));

            script.execute(jt);
        });

    }
    */
}

/*
 * Copyright 2014 the original author or authors.
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
package leap.db.ddl;

import leap.db.DbTestCase;
import leap.db.change.ColumnDefinitionChange;
import leap.db.change.SchemaChanges;
import leap.db.model.*;
import leap.db.platform.oracle.OraclePlatform;
import leap.junit.contexual.Contextual;

import leap.lang.exception.NestedSQLException;
import org.junit.Test;

public class ColumnTest extends DbTestCase {

	@Test
	@Contextual
	public void testCommentColumnComment() {
		if(!db.getDialect().supportsColumnComment()){
			return;
		}
		
		DbColumnBuilder col1 = DbColumnBuilder.varchar("col1", 100).setComment("comm");
		DbColumnBuilder col2 = DbColumnBuilder.varchar("col2", 100);
		DbColumnBuilder col3 = DbColumnBuilder.varchar("col3", 100).setComment("comm");

		DbTableBuilder table = new DbTableBuilder("test_column_comment")
								.addColumn(col1)
								.addColumn(col2);
		//test column comment for create table
		db.cmdCreateTable(table.build()).execute();
		
		DbTable t1 = db.getMetadata().tryGetTable(table.getTableName());
		assertNotNull(t1);
		
		DbColumn c1 = t1.findColumn(col1.getName());
		assertNotNull(c1);
		assertEquals(col1.getComment(), c1.getComment());
		
		//test column comment for alter table( add column )
		db.cmdCreateColumn(table.getTableName(), col3.build()).execute();
		t1 = db.getMetadata().tryGetTable(t1);
		DbColumn c3 = t1.findColumn(col3.getName());
		assertEquals(col3.getComment(), c3.getComment());
		//test column comment for alter column
		col2.setComment("comm");
		SchemaChanges changes = db.getComparator().compareTable(table.build(), t1);
		assertNotNull(changes.firstOrNull(ColumnDefinitionChange.class));
		changes.applyChanges();
		DbColumn c2 = db.getMetadata().tryGetTable(t1).findColumn(col2.getName());
		assertEquals(col2.getComment(),c2.getComment());
	}

	@Test
	@Contextual("mysql")
	public void testAddColumnWithSpecialSchemaName() {
		DbSchemaObjectName name = new DbSchemaObjectName("", "test-schema", "test");
		DbColumnBuilder c = DbColumnBuilder.varchar("col1", 100).setComment("comm");

		String sql = dialect.getCreateColumnSqls(name, c.build()).get(0);
		assertTrue(sql.contains("`test-schema`.test"));
	}
}

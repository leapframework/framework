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
import leap.db.change.SchemaChanges;
import leap.db.model.DbColumnBuilder;
import leap.db.model.DbTable;
import leap.db.model.DbTableBuilder;
import leap.junit.contexual.Contextual;

import org.junit.Test;

public class TableTest extends DbTestCase {

	@Test
	@Contextual()
	public void testSimpleCreateDropTable(){
		DbTable tableTobeCreate = new DbTableBuilder("simple_create_drop_table")
										.addPrimaryKey(DbColumnBuilder.guid("id_"))
										.addColumn(DbColumnBuilder.varchar("name", 150).notNull().unique())
										.addColumn(DbColumnBuilder.bigint("bigint_"))
										.addColumn(DbColumnBuilder.integer("integer_"))
										.addColumn(DbColumnBuilder.timestamp("timestamp_"))
										.build();
		
		if(db.checkTableExists(tableTobeCreate)){
			assertTrue(db.cmdDropTable(tableTobeCreate).execute().success());
		}
		
		assertTrue(db.cmdCreateTable(tableTobeCreate).execute().success());

		DbTable tableCreated = db.getMetadata().tryGetTable(tableTobeCreate.getName());
		assertNotNull(tableCreated);
		SchemaChanges changes = db.getComparator().compareTable(tableTobeCreate, tableCreated);
		assertTrue(changes.isEmpty());
		
		assertTrue(db.cmdDropTable(tableTobeCreate).execute().success());
		assertFalse(db.checkTableExists(tableTobeCreate));
	}
	
}

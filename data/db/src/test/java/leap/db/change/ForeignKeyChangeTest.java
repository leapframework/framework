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
package leap.db.change;

import leap.db.DbTestCase;
import leap.db.model.DbColumnBuilder;
import leap.db.model.DbForeignKeyBuilder;
import leap.db.model.DbForeignKeyColumn;
import leap.db.model.DbTable;
import leap.db.model.DbTableBuilder;
import leap.junit.contexual.Contextual;

import org.junit.Test;

public class ForeignKeyChangeTest extends DbTestCase {
	
	@Test
	@Contextual
	public void testAddForeignKeyChange() {
		DbColumnBuilder colId  = DbColumnBuilder.integer("id").primaryKey();
		DbColumnBuilder colPid = DbColumnBuilder.integer("pid");

		DbTableBuilder table = new DbTableBuilder("test_addfk_change")
									.addPrimaryKey(colId)
									.addColumn(colPid);
		
		//create table
		db.cmdCreateTable(table.build()).execute();

		try{
			DbTable targetTable = db.getMetadata().tryGetTable(table.getName());
			assertNotNull(targetTable);
			assertEquals(0,targetTable.getForeignKeys().length);
			
			//add foreignkey
			DbForeignKeyBuilder fk = new DbForeignKeyBuilder();
			fk.setName("fk_test_addfk_change");
			fk.setForeignTable(targetTable);
			fk.addColumn(new DbForeignKeyColumn("pid", "id"));
			table.addForeignKey(fk);

			//compare changes
			SchemaChanges changes = db.getComparator().compareTable(table.build(), targetTable);
			assertEquals(1,changes.size());
			assertTrue(changes.get(0) instanceof AddForeignKeyChange);
			
			//apply changes
			changes.applyChanges();
			
			//refresh target table
			targetTable = db.getMetadata().tryGetTable(targetTable);
			assertEquals(1,targetTable.getForeignKeys().length);
			
			//compares again (should be no changes)
			changes = db.getComparator().compareTable(table.build(), targetTable);
			assertTrue(changes.isEmpty());
		}finally{
			//drop table
			db.cmdDropTable(table.getTableName()).execute();
		}
	}
}
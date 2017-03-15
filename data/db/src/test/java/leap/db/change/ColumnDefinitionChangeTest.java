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
import leap.db.model.DbTable;
import leap.db.model.DbTableBuilder;
import leap.junit.contexual.Contextual;
import org.junit.Test;

public class ColumnDefinitionChangeTest extends DbTestCase {
	
	@Test
	@Contextual
	public void testColumnUniquePropertyChange() {
		DbColumnBuilder col = DbColumnBuilder.varchar("col1", 100);

		DbTableBuilder table = new DbTableBuilder("test_unique_change")
								.addColumn(col);
		
		//create table with non-unique column
		db.cmdCreateTable(table.build()).execute();

		//change the column's unique property to true
		col.setUnique(true);
		
		//compares table
		DbTable targetTable = db.getMetadata().tryGetTable(table.getTableName());
		assertNotNull(targetTable);
		
		SchemaChanges changes = db.getComparator().compareTable(table.build(), targetTable);
		assertEquals(1, changes.size());
		assertTrue(changes.get(0) instanceof ColumnDefinitionChange);

		ColumnDefinitionChange change = (ColumnDefinitionChange)changes.get(0);
		assertTrue(change.isUniqueChanged());
		
		//apply the changes
		changes.applyChanges();
		
		targetTable = db.getMetadata().tryGetTable(table.getTableName());
		changes = db.getComparator().compareTable(table.build(), targetTable);
		assertNull(changes.firstOrNull(ColumnDefinitionChange.class));
		
		//drop column
		db.cmdDropTable(table.getTableName()).execute();
	}
}
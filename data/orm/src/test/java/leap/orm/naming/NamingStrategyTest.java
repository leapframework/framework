/*
 * Copyright 2015 the original author or authors.
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
package leap.orm.naming;

import org.junit.Test;

import leap.orm.Orm;
import leap.orm.junit.OrmTestBase;

public class NamingStrategyTest extends OrmTestBase {
	
	
	@Test
	public void testTableNameOfEntity() {
		NamingStrategy ns = Orm.context().getNamingStrategy();
		
		assertTrue(ns.isTableOfEntity("table_", "Table"));
		assertTrue(ns.isTableOfEntity("table", "Table"));
		
		assertTrue(ns.isTableOfEntity("table_name_", "TableName"));
		assertTrue(ns.isTableOfEntity("table_name", "TableName"));
	}
	
	@Test
	public void testColumnNameOfField() {
		NamingStrategy ns = Orm.context().getNamingStrategy();
		
		assertTrue(ns.isColumnOfField("column_", "Column"));
		assertTrue(ns.isColumnOfField("column", "Column"));
		
		assertTrue(ns.isColumnOfField("column_name_", "ColumnName"));
		assertTrue(ns.isColumnOfField("column_name", "ColumnName"));
	}

}

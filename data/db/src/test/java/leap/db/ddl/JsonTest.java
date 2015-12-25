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
import leap.db.model.DbColumnBuilder;
import leap.db.model.DbTable;
import leap.db.model.DbTableBuilder1;

import org.junit.Test;

public class JsonTest extends DbTestCase {

	@Test
	public void testJsonTable() {
		DbTable t = new DbTableBuilder1("t_json").addColumn(DbColumnBuilder.varchar("c1", 100)).build();
		
		String json = t.toJson();
		
		System.out.println(json);
		
		DbTable t1 = DbTable.fromJson(json);
		
		assertTrue(db.getComparator().compareTable(t, t1).isEmpty());
	}
	
}

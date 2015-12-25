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
package leap.db;

import leap.db.model.DbSchema;
import leap.db.model.DbSchemaName;
import leap.junit.contexual.Contextual;

import org.junit.Test;

public class DbPlatformTest extends DbTestCase {

	@Test
	public void testDisplayInfo() {
		DbPlatform p = DbPlatforms.get("mysql");
		assertNotNull(p.getTitle());
	}
	
	@Test
	@Contextual
	public void testSimpleGetAllCreatedDbInstances(){
		assertNotNull(db);
		assertNotNull(db.getName());
		assertNotNull(db.getDataSource());
		assertNotNull(db.getDialect());
		assertNotNull(db.getMetadata());
		assertNotNull(db.getMetadata().getDefaultSchemaName());
		assertNotNull(db.getComparator());
		
		DbSchemaName[] schemaNames = db.getMetadata().getExtraSchemaNames();
		assertNotNull(schemaNames);
		
		DbSchema schema = db.getMetadata().getSchema();
		assertNotNull(schema);
	}
	
}
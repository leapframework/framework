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

import java.sql.Types;
import java.util.function.Consumer;

import leap.db.DbTestCase;
import leap.db.model.DbColumnBuilder;
import leap.db.model.DbTable;
import leap.db.model.DbTableBuilder;
import leap.junit.contexual.Contextual;
import leap.lang.Arrays2;
import leap.lang.Out;
import leap.lang.convert.Converts;

import org.junit.Test;

public class PrimaryKeyTest extends DbTestCase {

	@Test
	@Contextual
	public void testAutoIncrement(){
		if(dialect.supportsAutoIncrement()){
			DbTableBuilder tb = new DbTableBuilder("test_identity");
			
			tb.addColumn(new DbColumnBuilder("id_",Types.INTEGER).setAutoIncrement(true).setPrimaryKey(true).build());
			tb.addColumn(DbColumnBuilder.varchar("col1_", 100).build());
			
			DbTable table = tb.build();
			
			if(db.checkTableExists(table)){
				db.cmdDropTable(table).execute();
			}
			
			db.cmdCreateTable(table).execute();	
			
			final Out<Object> id = new Out<Object>();
			
			db.executeUpdate("insert into test_identity(col1_) values(?)", 
							 new Object[]{"str"}, 
							 Arrays2.EMPTY_INT_ARRAY,
							 dialect.getAutoIncrementIdHandler(new Consumer<Object>() {
								@Override
								public void accept(Object input) {
									id.set(input);
								}
							} ));
			
			assertEquals(1,Converts.toInt(id.getValue()));
			
			db.cmdDropTable(table).execute();
		}
	}
	
}

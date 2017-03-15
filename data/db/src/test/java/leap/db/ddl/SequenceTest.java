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

import java.util.function.Consumer;

import leap.db.DbTestCase;
import leap.db.model.DbColumnBuilder;
import leap.db.model.DbSequence;
import leap.db.model.DbSequenceBuilder;
import leap.db.model.DbTable;
import leap.db.model.DbTableBuilder;
import leap.junit.contexual.Contextual;
import leap.lang.Arrays2;
import leap.lang.Out;
import leap.lang.convert.Converts;

import org.junit.Test;

public class SequenceTest extends DbTestCase {
	
	@Test
	@Contextual
	public void testSimpleCreateAndDropSequence(){
		DbSequence sequence = new DbSequenceBuilder("tested_sequence").build();
		
		DbTable table = new DbTableBuilder("tested_sequence_table")
								.addColumn(DbColumnBuilder.integer("id_").setPrimaryKey(true))
								.build();
		
		if(dialect.supportsSequence()){
			if(db.checkSequenceExists(sequence)){
				db.cmdDropSequence(sequence).execute();
			}
			
			if(db.checkTableExists(table)){
				db.cmdDropTable(table).execute();
			}
			
			assertFalse(db.checkSequenceExists(sequence));
			db.cmdCreateSequence(sequence).execute();
			assertTrue(db.checkSequenceExists(sequence));
			db.cmdCreateTable(table).execute();
			
			final String sql = "insert into tested_sequence_table(id_) values(" + 
							   dialect.getNextSequenceValueSqlString("tested_sequence") + 
							   ")";
			
			final Out<Object> id = new Out<Object>();
			
			if(dialect.supportsCurrentSequenceValue()) {
				db.executeUpdate(sql,Arrays2.EMPTY_OBJECT_ARRAY,Arrays2.EMPTY_INT_ARRAY,
						 dialect.getInsertedSequenceValueHandler("tested_sequence", input -> id.set(input)));
				DbSequence[] sequences = db.getMetadata().getSchema().getSequences();
				for(DbSequence seq : sequences){
					if(seq.getName().equalsIgnoreCase(sequence.getName())){
						sequence = seq;
						break;
					}
				}
				long current = sequence.getStart() == null?1:sequence.getStart()+1;
				assertEquals(current, Converts.toInt(id.getValue()));
			}else{
				db.executeUpdate(sql);
			}
			
			db.cmdDropTable(table).execute();
			db.cmdDropSequence(sequence).execute();
			assertFalse(db.checkSequenceExists(sequence));
		}else{
			try {
	            db.cmdCreateSequence(sequence);
	            fail("should throw IllegalStateException");
            } catch (IllegalStateException e) {
            	
            }
		}
	}

}

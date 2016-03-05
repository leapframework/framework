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
package leap.orm.dmo;

import leap.junit.contexual.Contextual;
import leap.lang.beans.BeanProperty;
import leap.lang.beans.BeanType;
import leap.orm.OrmTestCase;
import leap.orm.annotation.NonColumn;
import leap.orm.mapping.EntityMapping;
import leap.orm.mapping.FieldMapping;
import leap.orm.tested.CreationEntity;

import org.junit.Test;

public class DmoTest extends OrmTestCase {
	
	@Test
	@Contextual
	public void testCreateEntity(){
		EntityMapping em = metadata.tryGetEntityMapping(CreationEntity.class);
		assertNull(em);
		assertTrue(dmo.cmdCreateEntity(CreationEntity.class).setDropTableIfExists(true).execute(true));
		em = metadata.getEntityMapping(CreationEntity.class);
		assertAllPropertiesMapped(CreationEntity.class,em);
		assertEquals(100,em.getFieldMapping("string1").getColumn().getLength());

		assertFalse(em.getKeyFieldMappings().length == 0);
		assertEquals(1, em.getTable().getPrimaryKeyColumnNames().length);
		metadata.removeEntityMapping(em);
		db.cmdDropTable(em.getTable()).execute();
	}

	//TODO : ERROR - DmoTest.testCreateTable:51 » Metadata Entity's primary key(s) must not be empt
	/*
	@Test
	public void testCreateTable() {
	    EntityMapping em = metadata.tryGetEntityMapping(CreationEntity.class);
	    if(null == em) {
	        dmo.cmdCreateEntity(CreationEntity.class).execute();
	        em = metadata.getEntityMapping(CreationEntity.class);
	    }
	    if(db.checkTableExists(em.getTable())) {
	        db.cmdDropTable(em.getTable()).execute();
	    }
	    
	    assertTrue(dmo.createTableIfNotExists(CreationEntity.class));
	    assertFalse(dmo.createTableIfNotExists(CreationEntity.class));
	    assertTrue(db.checkTableExists(em.getTable()));
	    
	    metadata.removeEntityMapping(em);
	}
	*/
	
	private void assertAllPropertiesMapped(Class<?> cls,EntityMapping em){
		for(BeanProperty bp : BeanType.of(cls).getProperties()){
			if(bp.isAnnotationPresent(NonColumn.class)) {
				continue;
			}
			
			boolean mapped = false;
			for(FieldMapping pm : em.getFieldMappings()){
				if(pm.getBeanProperty() == bp){
					mapped = true;
					break;
				}
			}
			
			if(!mapped){
				fail("ConfigProperty '" + bp.getName() + "' not mapped in class '" + cls.getName() + "'");
			}
		}
	}

}
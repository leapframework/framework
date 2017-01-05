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
package leap.orm.mapping;

import leap.core.metamodel.ReservedMetaFieldName;
import leap.lang.beans.BeanType;
import leap.lang.jdbc.JdbcTypes;
import leap.orm.OrmTestCase;
import leap.orm.domain.FieldDomain;
import leap.orm.tested.DomainEntity;
import leap.orm.tested.TestedEntity;
import leap.orm.tested.TestedTableName;
import leap.orm.tested.model.DateMappingEntity;
import leap.orm.tested.model.ECodeModel;
import leap.orm.tested.model.ModelConfigTable;
import leap.orm.tested.model.ModelNotDefaultTable;
import leap.orm.tested.model.petclinic.Owner;
import leap.orm.tested.model.relation.RelationEntity1;

import org.junit.Test;

public class MappingTest extends OrmTestCase {

	@Test
	public void testDefaultMappingLoader(){
		EntityMapping em = metadata.tryGetEntityMapping(TestedEntity.class);
		
		assertNotNull(em);
		assertEquals(BeanType.of(TestedEntity.class).getProperties().length - 1,em.getFieldMappings().length);
	}
	
	@Test
	public void testTableAnnotation() {
		EntityMapping em = metadata.getEntityMapping(TestedTableName.class);
		assertEquals("t_table_name", em.getTableName());
	}
	
	@Test
	public void testDomainAnnotation() {
		EntityMapping em = metadata.getEntityMapping(DomainEntity.class);
		
		FieldMapping fm = em.getFieldMapping("test");
		assertNotNull(fm.getDomain());
		assertEquals("test",fm.getDomain().getName());
		assertEquals(100,fm.getColumn().getLength());
		
		fm = em.getFieldMapping("test1");
		assertNotNull(fm.getDomain());
		assertEquals("test1",fm.getDomain().getName());
		assertEquals(101,fm.getColumn().getLength());
		
		fm = em.getFieldMapping("test2");
		assertFalse(fm.isUpdate());
	}
	@Test
	public void testNonDomainAnnotation(){
		EntityMapping em = metadata.getEntityMapping(DomainEntity.class);
		FieldMapping createdAt = em.getFieldMapping("createdAt");
		FieldMapping updatedAt = em.getFieldMapping("updatedAt");
		FieldDomain createdAtDomain = metadata.domains().getFieldDomain("createdAt");
		FieldDomain updatedAtDomain = metadata.domains().getFieldDomain("updatedAt");
		assertNotEquals(createdAt.getInsertValue(),createdAtDomain.getInsertValue());
		assertEquals(updatedAt.getInsertValue(),updatedAtDomain.getInsertValue());
	}

	@Test
	public void testManyToManyRelation() {
		EntityMapping em = metadata.getEntityMapping(RelationEntity1.class);
		assertNotEmpty(em.getRelationMappings());
	}
	
	@Test
	public void testNonColumn() {
		EntityMapping em = metadata.tryGetEntityMapping(TestedEntity.class);
		assertNull(em.tryGetFieldMapping("nonColumn"));
	}
	
	@Test
	public void testReservedMetaFieldName() {
		EntityMapping em = metadata.getEntityMapping(Owner.class);
		
		assertNotNull(em.tryGetFieldMappingByMetaName(ReservedMetaFieldName.CREATED_AT));
		assertNotNull(em.tryGetFieldMappingByMetaName(ReservedMetaFieldName.UPDATED_AT));
	}
	
	@Test
	public void testDateMapping() {
	    EntityMapping em = metadata.getEntityMapping(DateMappingEntity.class);
	    
	    FieldMapping fm1 = em.getFieldMapping("timestamp1");
	    assertEquals(JdbcTypes.TIMESTAMP_TYPE_NAME,fm1.getColumn().getTypeName());
	    
        FieldMapping fm2 = em.getFieldMapping("timestamp2");
        assertEquals(JdbcTypes.TIMESTAMP_TYPE_NAME,fm2.getColumn().getTypeName());	    
	}

    @Test
    public void testGlobalField() {
        EntityMapping em = metadata.getEntityMapping(ECodeModel.class);

        FieldMapping ecode = em.getFieldMapping("ecode");
        assertNotNull(ecode);
        assertTrue(ecode.isWhere());
    }

    @Test
    public void testModelNotDefaultTable() {
        EntityMapping em = metadata.getEntityMapping(ModelNotDefaultTable.class);
        assertEquals("m_not_default_table", em.getTableName());
    }

    @Test
    public void testModelConfigTable() {
        EntityMapping em = metadata.getEntityMapping(ModelConfigTable.class);
        assertEquals("m_config_table", em.getTableName());
    }
}

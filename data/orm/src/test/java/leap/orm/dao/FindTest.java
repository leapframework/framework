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
package leap.orm.dao;

import leap.core.exception.RecordNotFoundException;
import leap.orm.OrmTestCase;
import leap.orm.tested.model.petclinic.Owner;

import org.junit.Test;

public class FindTest extends OrmTestCase {
    
    @Test
    public void tetFind() {
        deleteAll(Owner.class);
        
        Object id1 = new Owner().setFullName("a","b").save().id();

        assertNotNull(dao.find(Owner.class, id1));
        try {
            dao.find(Owner.class, -1);
            fail("Should throw RecordNotFoundException");
        } catch (RecordNotFoundException e) {
            
        }
        
        try {
            dao.find("Owner", -1);
            fail("Should throw RecordNotFoundException");
        } catch (RecordNotFoundException e) {
            
        }
        
        try {
            dao.find("Owner", Owner.class, -1);
            fail("Should throw RecordNotFoundException");
        } catch (RecordNotFoundException e) {
            
        }
        
        try {
            dao.find(Owner.metamodel(), Owner.class, -1);
            fail("Should throw RecordNotFoundException");
        } catch (RecordNotFoundException e) {
            
        }        
    }
    
    @Test
    public void tetFindOrNull() {
        deleteAll(Owner.class);
        
        Object id1 = new Owner().setFullName("a","b").save().id();

        assertNotNull(dao.find(Owner.class, id1));
        assertNull(dao.findOrNull(Owner.class, -1));
        assertNull(dao.findOrNull("Owner", -1));
        assertNull(dao.findOrNull("Owner", Owner.class, -1));
        assertNull(dao.findOrNull(Owner.metamodel(),Owner.class, -1));
    }
	
    @Test
    public void testFindList() {
        deleteAll(Owner.class);
        
        Object id1 = new Owner().setFullName("a","b").save().id();
        Object id2 = new Owner().setFullName("c","d").save().id();

        Object[] ids = new Object[]{id1,id2};
        
        assertEquals(2,dao.findList(Owner.class, ids).size());
        assertEquals(2,dao.findList("Owner", ids).size());
        assertEquals(2,dao.findList("Owner", Owner.class, ids).size());
        assertEquals(2,Owner.findList(ids).size());
        assertEquals(1,dao.findList(Owner.class, new Object[]{id1}).size());
        
        try {
            dao.findList(Owner.class, new Object[]{-1});
            fail("Should throw RecordNotFoundException");
        } catch (RecordNotFoundException e) {
            
        }
    }
    
    @Test
    public void testFindListIfExiss() {
        deleteAll(Owner.class);
        
        Object id1 = new Owner().setFullName("a","b").save().id();
        Object id2 = new Owner().setFullName("c","d").save().id();

        Object[] ids = new Object[]{id1,id2};
        
        assertEquals(2,dao.findListIfExists(Owner.class, ids).size());
        assertEquals(2,dao.findListIfExists("Owner", ids).size());
        assertEquals(2,dao.findListIfExists("Owner", Owner.class, ids).size());
        assertEquals(2,Owner.findListIfExists(ids).size());
        assertEquals(1,dao.findListIfExists(Owner.class, new Object[]{id1}).size());
        assertEquals(0,dao.findListIfExists(Owner.class, new Object[]{-1}).size());
        assertEquals(0,Owner.findListIfExists(new Object[]{-1}).size());
        assertEquals(1,Owner.findListIfExists(new Object[]{id1,-1}).size());
    }
	
}

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
package leap.orm.model;

import java.util.List;

import leap.orm.OrmTestCase;
import leap.orm.tested.model.ModelWithGuid;
import leap.orm.tested.model.ModelWithId;
import leap.orm.tested.model.ModelWithId1;
import leap.orm.tested.model.ModelWithId2;
import leap.orm.tested.model.ModelWithId3;

import org.junit.Test;

public class ModelIdTest extends OrmTestCase {

	@Test
	public void testModelWithId(){
		ModelWithId m = new ModelWithId();
		m.setField1("hello");
		m.save();

		Object  id1 = m.id();
		Integer id2 = m.getId();
		
		assertNotNull(id1);
		assertSame(id1, id2);
		
		m = ModelWithId.find(m.id());
		assertEquals(id1,m.id());
	}
	
	@Test
	public void testInsertUpdateEmptyString1() {
	    ModelWithId m = new ModelWithId();
	    m.setField1("");
	    m.save();
	    
	    ModelWithId m1 = ModelWithId.find(m.id());
	    assertEquals("", m1.getField1());
	    m1.setField1(null);
	    m1.update();
	    
	    ModelWithId m2 = ModelWithId.find(m.id());
	    assertNull(m2.getField1());
	}
	
    @Test
    public void testInsertUpdateEmptyString2() {
        ModelWithId m = new ModelWithId();
        m.setField1(null);
        m.save();

        ModelWithId m1 = ModelWithId.find(m.id());
        assertNull(m1.getField1());
        m1.setField1("");
        m1.update();

        ModelWithId m2 = ModelWithId.find(m.id());
        assertEquals("",m2.getField1());
    }
	
	@Test
	public void testModelWithGuid(){
		ModelWithGuid m = new ModelWithGuid();
		m.setField1("hello");
		m.save();

		Object id1 = m.id();
		String id2 = m.getId();
		
		assertNotNull(id1);
		assertSame(id1, id2);
		
		m = ModelWithGuid.find(m.id());
		assertEquals(id1,m.id());
	}
	
	@Test
	public void testModelWithId1(){
		deleteAll(ModelWithId1.class);
		ModelWithId1 m = new ModelWithId1();
		m.setField1("hello");
		m.save();

		Object  id1 = m.id();
		Integer id2 = m.getId();
		
		assertNotNull(id1);
		assertSame(id1, id2);
		
		m = ModelWithId1.find(m.id());
		assertEquals(id1,m.id());
	}
	
	@Test
	public void testModelWithId2() {
		deleteAll(ModelWithId2.class);
		
		ModelWithId2 m = new ModelWithId2();
		
		m.setModelId("1");
		m.setField1("a");
		m.create();

		m = ModelWithId2.find(m.id());
		assertEquals("1", m.id());
	}
	
	@Test
	public void testModelWithId3() {
		deleteAll(ModelWithId3.class);
		
		ModelWithId3 m = new ModelWithId3();
		m.setId1("a");
		m.setId2("b");
		m.setField1("f");
		m.create();
		
		m = ModelWithId3.find(m.id());
		assertEquals("a",m.getId1());
		assertEquals("b",m.getId2());
		assertEquals("f",m.getField1());
		
		m.setField1("f-1");
		m.update();
		m = ModelWithId3.find(m.id());
		assertEquals("f-1",m.getField1());
		
		m.delete();
		assertNull(ModelWithId3.findOrNull(m.id()));
	}
	
	@Test
	public void testFindListWithCompositeId() {
		deleteAll(ModelWithId3.class);
		
		ModelWithId3 m1 = new ModelWithId3();
		m1.setId1("a");
		m1.setId2("b");
		m1.setField1("f");
		m1.create();
		
		ModelWithId3 m2 = new ModelWithId3();
		m2.setId1("c");
		m2.setId2("d");
		m2.setField1("f");
		m2.create();
		
		ModelWithId3 m3 = new ModelWithId3();
		m3.setId1("a");
		m3.setId2("d");
		m3.setField1("f");
		m3.create();
		
		Object id1 = new Object[]{m1.getId1(),m1.getId2()};
		Object id2 = new Object[]{m2.getId1(),m2.getId2()};
		
		List<ModelWithId3> models = ModelWithId3.findList(new Object[]{id1,id2});
		assertEquals(2,models.size());
	}
}
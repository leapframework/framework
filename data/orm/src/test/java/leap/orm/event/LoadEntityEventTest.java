/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package leap.orm.event;

import leap.core.value.Record;
import leap.orm.OrmTestCase;
import leap.orm.tested.model.event.EventModel;
import leap.orm.tested.model.event.TestingListener;
import org.junit.Test;

import java.util.List;

public class LoadEntityEventTest extends OrmTestCase {

    @Test
    public void testFind() {
        EventModel m1 = new EventModel();
        m1.create();
        assertNull(m1.getCol3());

        EventModel m2 = EventModel.find(m1.id());
        assertEquals("_ok",   m2.getCol3());
        assertEquals("extra", m2.get("col4"));

        LoadEntityEvent e = TestingListener.lastLoadEntityEvent;
        assertTrue(e.isFind());
        assertFalse(e.isQuery());
        assertEquals(m1.id(), e.iterator().next().getId());
        assertNotNull(e.getQueryContext());

        Bean bean = dao.find(EventModel.metamodel(), Bean.class, m1.id());
        assertEquals("_ok", bean.getCol3());

        m1.delete();
    }

    @Test
    public void testCriteriaQuery() {
        deleteAll(EventModel.class);

        EventModel m1 = new EventModel();
        m1.create();

        EventModel m2 = new EventModel();
        m2.create();

        List<EventModel> list = EventModel.<EventModel>where(" 1 = 1").list();

        assertEquals("_ok",   list.get(0).getCol3());
        assertEquals("extra", list.get(0).get("col4"));
        assertEquals("_ok",   list.get(1).getCol3());
        assertEquals("extra", list.get(1).get("col4"));

        LoadEntityEvent e = TestingListener.lastLoadEntityEvent;
        assertFalse(e.isFind());
        assertTrue(e.isQuery());
        assertEquals(list.size(), e.size());
        assertNotNull(e.getQueryContext());
    }

    @Test
    public void testSqlQuery() {
        deleteAll(EventModel.class);

        EventModel m1 = new EventModel();
        m1.create();

        EventModel m2 = new EventModel();
        m2.create();

        List<Record> list = dao.createSqlQuery(EventModel.metamodel(), "select * from EventModel").list();
        assertEquals("_ok",   list.get(0).get("col3"));
        assertEquals("extra", list.get(0).get("col4"));
        assertEquals("_ok",   list.get(1).get("col3"));
        assertEquals("extra", list.get(1).get("col4"));

        LoadEntityEvent e = TestingListener.lastLoadEntityEvent;
        assertFalse(e.isFind());
        assertTrue(e.isQuery());
        assertEquals(list.size(), e.size());
    }

    static final class Bean {

        protected String col1;
        protected String col2;
        protected String col3;

        public String getCol1() {
            return col1;
        }

        public void setCol1(String col1) {
            this.col1 = col1;
        }

        public String getCol2() {
            return col2;
        }

        public void setCol2(String col2) {
            this.col2 = col2;
        }

        public String getCol3() {
            return col3;
        }

        public void setCol3(String col3) {
            this.col3 = col3;
        }
    }
}

/*
 * Copyright 2017 the original author or authors.
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

import leap.orm.OrmTestCase;
import leap.orm.tested.model.event.EventModel;
import leap.orm.tested.model.event.TestingListener;
import org.junit.Test;

public class DeleteEntityEventTest extends OrmTestCase {

    @Test
    public void testPreDeleteNoTrans() {
        TestingListener.PRE_DELETE_CONTEXT.get().set(0);

        EventModel m1 = new EventModel();
        m1.create().delete();

        assertEquals(2, TestingListener.PRE_DELETE_CONTEXT.get().get());
    }

    @Test
    public void testPostDeleteNoTransWithError() {
        EventModel m1 = new EventModel();
        m1.create();

        try {
            TestingListener.POST_DELETE_CONTEXT.get().set(1);
            m1.delete();

            fail();
        }catch (Exception e) {
            assertNull(EventModel.findOrNull(m1.id()));
        }
    }

    @Test
    public void testPostDeleteInTransWithError() {
        EventModel m1 = new EventModel();
        m1.create();

        try {
            TestingListener.POST_DELETE_CONTEXT.get().set(2);
            m1.delete();

            fail();
        }catch (Exception e) {
            assertNotNull(EventModel.findOrNull(m1.id()));
        }
    }

}

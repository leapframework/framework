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
import org.junit.Test;

public class CreateEntityEventTest extends OrmTestCase {

    @Test
    public void testPreCreateNoTrans() {
        EventModel m1 = new EventModel();
        m1.create();

        assertEquals("Test1", m1.getCol1());
        assertEquals("Test2", m1.getCol2());

        EventModel m2 = EventModel.find(m1.id());
        assertEquals(m1.getCol1(), m2.getCol1());
        assertEquals(m1.getCol2(), m2.getCol2());

        m1.delete();
    }

}

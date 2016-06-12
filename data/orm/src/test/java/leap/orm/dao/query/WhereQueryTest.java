/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package leap.orm.dao.query;

import leap.lang.New;
import leap.orm.OrmTestCase;
import leap.orm.tested.model.ECodeModel;
import org.junit.Test;

public class WhereQueryTest extends OrmTestCase {

    @Test
    public void testSingleWhereField() {
        ECodeModel.deleteAll();

        ECodeModel o1 = new ECodeModel("1").create();
        ECodeModel o2 = new ECodeModel("2").set("ecode","t1").create();

        assertEquals(1,ECodeModel.where("1=1").count());
        assertEquals("1",ECodeModel.<ECodeModel>where("1=1").first().getName());

        assertEquals("1",ECodeModel.<ECodeModel>where("name = ?", "1").first().getName());
        assertNull(ECodeModel.<ECodeModel>where("name = :name", New.hashMap("ecode",null)).orderBy("name asc").firstOrNull());

        assertEquals(1,ECodeModel.where("ecode = ?", "t1").count());
        assertEquals("2",ECodeModel.<ECodeModel>where("ecode = ?", "t1").first().getName());

        assertEquals(1, ECodeModel.where("1=1").update(New.hashMap("name", "11")));
        assertEquals("11",ECodeModel.<ECodeModel>where("1=1").first().getName());

        assertEquals(1, ECodeModel.where("ecode = ?", "t1").update(New.hashMap("name", "22")));
        assertEquals("22",ECodeModel.<ECodeModel>where("ecode = ?", "t1").first().getName());

        assertEquals(1, ECodeModel.where("1=1").delete());
        assertNull(ECodeModel.<ECodeModel>where("1=1").firstOrNull());

        assertEquals(1, ECodeModel.where("ecode = ?", "t1").delete());
        assertNull(ECodeModel.<ECodeModel>where("ecode = ?", "t1").firstOrNull());
    }

}

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

package leap.orm.dao;

import leap.orm.OrmTestCase;
import leap.orm.tested.model.petclinic.Owner;
import org.junit.Ignore;
import org.junit.Test;

public class TagTest extends OrmTestCase {

    @Test
    public void testNopTag() {
        deleteAll(Owner.class);

        new Owner().setFullName("f1","l1").create();

        assertEquals(0,dao.createSqlQuery("select * from owners where @nop ( 1=0 )").count());
        assertEquals(1,dao.createSqlQuery("select * from owners where @nop ( 1=1 )").count());
    }

    /* the feature of optional tag has been removed.
    @Test
    public void testOptionalTag() {
        deleteAll(Owner.class);

        new Owner().setFullName("f1","l1").create();

        assertEquals(1,dao.createSqlQuery("select * from owners @not_exists{? err }").count());
    }
    */
}

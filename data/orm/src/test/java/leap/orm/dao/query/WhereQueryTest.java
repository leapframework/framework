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

import leap.orm.OrmTestCase;
import leap.orm.tested.model.ECodeModel;
import org.junit.Test;

public class WhereQueryTest extends OrmTestCase {

    @Test
    public void testSingleWhereField() {
        ECodeModel.deleteAll();

        new ECodeModel("1").create();
        new ECodeModel("2").set("ecode","t1").create();

        assertEquals(1,ECodeModel.where("1=1").count());
    }

}

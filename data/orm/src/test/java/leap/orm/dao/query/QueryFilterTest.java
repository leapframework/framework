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

package leap.orm.dao.query;

import leap.orm.OrmTestCase;
import leap.orm.tested.model.FilteredModel;
import org.junit.Test;

import java.util.List;

public class QueryFilterTest extends OrmTestCase {

    @Test
    public void testFilteredModel() {
        deleteAll(FilteredModel.class);

        FilteredModel m1 = new FilteredModel();
        m1.num     = 11;
        m1.create();

        FilteredModel m2 = new FilteredModel();
        m2.num     = 10;
        m2.create();

        //query
        List<FilteredModel> list = FilteredModel.findAll();
        assertEquals(1, list.size());
        assertEquals(m1.id, list.get(0).id);

        assertEquals(1,FilteredModel.query("select * from FilteredModel order by id").list().size());
    }

}

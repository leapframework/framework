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

import leap.junit.contexual.Contextual;
import leap.orm.OrmTestCase;
import leap.orm.tested.model.NoneMappingColumnsModel;
import org.junit.Test;

import java.util.Map;

@Contextual("h2")
public class NoneMappingColumnsTest extends OrmTestCase {

    @Test
    public void testFind() {
        NoneMappingColumnsModel row = new NoneMappingColumnsModel().create();

        //find one
        Map<String,Object> result = NoneMappingColumnsModel.find(row.id).getProperties();
        assertTrue(result.containsKey("col2"));
        assertFalse(result.containsKey("col3"));

        //find all
        result = NoneMappingColumnsModel.findAll().get(0).getProperties();
        assertTrue(result.containsKey("col2"));
        assertFalse(result.containsKey("col3"));

        row.delete();
    }

    @Test
    public void testCriteriaQuery() {
        NoneMappingColumnsModel row = new NoneMappingColumnsModel().create();

        Map<String,Object> result = NoneMappingColumnsModel.where("id = ?", row.id).first().getProperties();
        assertTrue(result.containsKey("col2"));
        assertFalse(result.containsKey("col3"));

        row.delete();
    }
}

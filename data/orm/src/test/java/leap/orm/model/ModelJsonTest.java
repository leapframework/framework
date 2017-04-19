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

package leap.orm.model;

import leap.lang.New;
import leap.lang.json.JSON;
import leap.orm.OrmTestCase;
import leap.orm.tested.model.json.JsonModel;
import org.junit.Test;

import java.util.Map;

public class ModelJsonTest extends OrmTestCase {

    @Test
    public void testIgnoreField() {
        JsonModel m = new JsonModel();

        m.setName("test");
        m.setIgnoredField("1");

        String json = JSON.stringify(m);

        Map<String,Object> map = JSON.decode(json);
        assertEquals("test", map.get("name"));
        assertFalse(map.containsKey("ignoredField"));
    }

    @Test
    public void testBeanList() {
        JsonModel m = new JsonModel();

        m.setBeanList(New.arrayList(new JsonModel.Bean("a")));

        m.create();

        JsonModel loaded = JsonModel.find(m.id());
        assertEquals(1, loaded.getBeanList().size());
        assertEquals("a", loaded.getBeanList().get(0).getProp1());

        m.delete();
    }

}

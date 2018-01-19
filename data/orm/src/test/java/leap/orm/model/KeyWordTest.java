/*
 *
 *  * Copyright 2013 the original author or authors.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  
 */

package leap.orm.model;

import leap.orm.OrmTestCase;
import leap.orm.tested.model.keyword.KeyWordModel;
import org.junit.Test;

/**
 * @author kael
 */
public class KeyWordTest extends OrmTestCase {

    @Test
    public void testKeyWordCrud(){

        KeyWordModel.deleteAll();

        //create
        KeyWordModel keyWordModel = new KeyWordModel();
        keyWordModel.setTable("table");
        keyWordModel.setType("type");
        keyWordModel.setWhen("when");
        Object id = keyWordModel.create().id();

        //find
        keyWordModel = KeyWordModel.find(id);
        assertEquals("when", keyWordModel.getWhen());

        //query
        KeyWordModel.where("when = ?", "when").first();

        //update
        keyWordModel.update();

        //delete
        keyWordModel.delete();
    }
    
}

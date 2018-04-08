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

package leap.orm.reader;

import leap.orm.OrmTestCase;
import leap.orm.tested.model.ReaderTestModel;
import org.junit.Test;

import java.util.UUID;

/**
 * @author kael.
 * 
 * select
 *      reader_id as reader_id,         -> reader_id/readerId
 *      reader_name,                    -> readerName
 *      'other_field' as other_field    -> other_field 
 * from ReaderTestModel
 * 
 * will convert to lowerCamel and alias name when use alias
 * 
 */
public class DefaultEntityReaderTest extends OrmTestCase {
    @Test
    public void testSqlKeyReader(){
        ReaderTestModel.deleteAll();
        ReaderTestModel p = new ReaderTestModel();
        p.setReaderId(UUID.randomUUID().toString());
        p.setReaderName("name");
        p.create();

        ReaderTestModel m = ReaderTestModel.<ReaderTestModel>query("reader.ReaderTestModel").single();

        assertEquals(p.getReaderId(),m.getReaderId());
        assertEquals(p.getReaderName(),m.getReaderName());

        ReaderTestModel m1 = ReaderTestModel.<ReaderTestModel>query("reader.ReaderTestModel.alias").single();
        assertEmpty(m1.getString("reader_name"));
        assertEquals(p.getReaderName(),m1.getReaderName());

        assertEquals(p.getReaderId(),m1.getReaderId());
        assertEquals(p.getReaderId(),m1.getString("reader_id"));
        assertNotEmpty(m1.getString("other_field"));
    }
    
}

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

import leap.junit.contexual.Contextual;
import leap.orm.OrmTestCase;
import leap.orm.config.OrmModelClassConfig;
import leap.orm.config.OrmModelsConfigs;
import leap.orm.tested.OrmConfigTableModel;
import org.junit.Test;

import java.util.UUID;

/**
 * @author kael.
 */
public class OrmConfigClassTableTest extends OrmTestCase {
    @Test
    @Contextual("h2")
    public void testOrmConfigTable(){
        OrmConfigTableModel.deleteAll();
        OrmModelsConfigs configs = config.getExtension(OrmModelsConfigs.class);
        String ds = OrmConfigTableModel.dao().getOrmContext().getName();
        String className = OrmConfigTableModel.class.getName();
        OrmModelClassConfig classConfig = configs.getModelsConfig(ds).getClasses().get(className);
        assertEquals(classConfig.getTableName(),OrmConfigTableModel.metamodel().getTableName());

        OrmConfigTableModel.newInstance().id(UUID.randomUUID().toString()).set("name","name").insert();
        int count = dao.queryForInteger("select count(*) from "+classConfig.getTableName()).intValue();
        assertEquals(1,count);
    }
}

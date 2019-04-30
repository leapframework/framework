/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package tests;

import pkg0.Application;
import pkg0.Entity0;
import leap.orm.dao.Dao;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import pkg1.Entity1;
import pkg2.Entity2;
import pkg3.Entity3;
import pkg4.Entity4;
import pkg4.Service4;
import pkg5_.Entity5;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class SimpleTest {

    private @Autowired Dao dao;
    private @Autowired Service4 service4;

    @Test
    public void testEntitiesAtWeb() {
        dao.findOrNull(Entity0.class, "1");
        service4.getDao().findOrNull(Entity1.class, "1");
        dao.findOrNull(Entity2.class, "1");
        dao.findOrNull(Entity3.class, "1");
        dao.findOrNull(Entity4.class, "1");
        dao.findOrNull(Entity5.class, "1");
    }
}
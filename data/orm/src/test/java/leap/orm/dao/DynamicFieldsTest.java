/*
 *  Copyright 2020 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package leap.orm.dao;

import leap.db.model.DbColumnBuilder;
import leap.lang.New;
import leap.lang.meta.MTypes;
import leap.orm.OrmTestCase;
import leap.orm.mapping.EntityMapping;
import leap.orm.mapping.FieldMapping;
import leap.orm.mapping.FieldMappingBuilder;
import leap.orm.tested.EmdEntity;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class DynamicFieldsTest extends OrmTestCase {

    @Test
    public void testSimpleCRUD() {
        dao.deleteAll(EmdEntity.class);

        final DynamicImpl dynamic = new DynamicImpl();
        EntityMapping.withDynamic(dynamic, () -> {
            addField(dynamic, "x1", String.class);
            addField(dynamic, "x2", Integer.class);

            //create and find
            dao.insert(EmdEntity.class, New.hashMap("id", "1", "x1", "a"));
            EmdEntity record = dao.find(EmdEntity.class, "1");
            assertEquals("a", record.get("x1"));

            //update and find
            dao.update(EmdEntity.class, "1", New.hashMap("x2", 100));
            record = dao.find(EmdEntity.class, "1");
            assertEquals("a", record.get("x1"));
            assertEquals(new Integer(100), record.get("x2"));

            record = dao.createCriteriaQuery(EmdEntity.class).select("x1").firstOrNull();
            assertEquals("a", record.get("x1"));
        });
    }

    protected void addField(EntityMapping.Dynamic dynamic, String name, Class<?> type) {
        FieldMappingBuilder f = new FieldMappingBuilder(name, type);
        f.setDataType(MTypes.getMType(type));
        f.setEmbedded(true);

        DbColumnBuilder c = f.getColumn();
        c.setName(name);
        c.setTypeCode(f.getDataType().asSimpleType().getJdbcType().getCode());

        dynamic.getFieldMappings().add(f.build());
    }

    protected static final class DynamicImpl implements EntityMapping.Dynamic {
        protected List<FieldMapping> fieldMappings = new ArrayList<>();

        @Override
        public List<FieldMapping> getFieldMappings() {
            return fieldMappings;
        }

        public void setFieldMappings(List<FieldMapping> fieldMappings) {
            this.fieldMappings = fieldMappings;
        }
    }
}

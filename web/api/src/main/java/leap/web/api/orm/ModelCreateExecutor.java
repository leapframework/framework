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

package leap.web.api.orm;

import leap.core.value.Record;
import leap.orm.mapping.RelationProperty;

import java.util.Map;

public interface ModelCreateExecutor {

    class Created {
        private Object id;
        private Record record;

        public Created() {

        }

        public Created(Object id, Record record) {
            this.id = id;
            this.record = record;
        }

        public Object getId() {
            return id;
        }

        public void setId(Object id) {
            this.id = id;
        }

        public Record getRecord() {
            return record;
        }

        public void setRecord(Record record) {
            this.record = record;
        }
    }

    interface Creation {
        /**
         * The properties of entity.
         */
        Map<String, Object> getProperties();

        /**
         * The relation properties of entity.
         */
        Map<RelationProperty, Object[]> getRelationProperties();

        /**
         * Returns the properties combined with {@link #getProperties()} & {@link #getRelationProperties()}
         */
        Map<String, Object> getCombinedProperties();
    }

    interface CreateHandler {

        Created createOne(ModelExecutionContext context, Creation creation);

    }

    ModelCreateExecutor withHandler(CreateHandler handler);

    default CreateOneResult createOne(Object request) {
        return createOne(request, null, null);
    }

    default CreateOneResult createOne(Object request, Object id) {
        return createOne(request, id, null);
    }

    CreateOneResult createOne(Object request, Object id, Map<String, Object> extraProperties);

}

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

package leap.orm.mapping;

import leap.lang.collection.WrappedCaseInsensitiveMap;

import java.util.HashMap;
import java.util.Map;

public class UnionSettings {

    protected String                          typeField;
    protected String                          idField;
    protected Map<String, UnionEntityMapping> entities      = WrappedCaseInsensitiveMap.create();
    protected Map<String, UnionEntityMapping> typeEntityMap = new HashMap<>();

    public String getTypeField() {
        return typeField;
    }

    public void setTypeField(String typeField) {
        this.typeField = typeField;
    }

    public String getIdField() {
        return idField;
    }

    public void setIdField(String idField) {
        this.idField = idField;
    }

    public Map<String, UnionEntityMapping> getEntities() {
        return entities;
    }

    public void setEntities(Map<String, UnionEntityMapping> entities) {
        this.entities = entities;
    }

    public UnionEntityMapping getEntityByType(String type) {
        return typeEntityMap.get(type);
    }

    void initTypeAndEntityMap() {
        typeEntityMap.clear();
        for(UnionEntityMapping e : entities.values()) {
            if(typeEntityMap.containsKey(e.getType())) {
                throw new IllegalStateException("Found duplicated union type '" + e.getType() + "'");
            }
            typeEntityMap.put(e.getType(), e);
        }
    }

    public static class UnionEntityMapping {
        protected String                         entityName;
        protected String                         type;
        protected String                         idField;
        protected Map<String, UnionFieldMapping> fields = WrappedCaseInsensitiveMap.create();

        public String getEntityName() {
            return entityName;
        }

        public void setEntityName(String entityName) {
            this.entityName = entityName;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getIdField() {
            return idField;
        }

        public void setIdField(String idField) {
            this.idField = idField;
        }

        public UnionFieldMapping getField(String name) {
            return fields.get(name);
        }

        public Map<String, UnionFieldMapping> getFields() {
            return fields;
        }

        public void setFields(Map<String, UnionFieldMapping> fields) {
            this.fields = fields;
        }
    }

    public static class UnionFieldMapping {
        private final String name;

        public UnionFieldMapping(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}

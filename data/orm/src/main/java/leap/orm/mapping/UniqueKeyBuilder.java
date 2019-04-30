/*
 *  Copyright 2018 the original author or authors.
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
 */

package leap.orm.mapping;

import java.util.ArrayList;
import java.util.List;

public class UniqueKeyBuilder {

    protected String       name;
    protected List<String> fields = new ArrayList<>();

    public UniqueKeyBuilder() {

    }

    public UniqueKeyBuilder(String name) {
        this.name = name;
    }

    public UniqueKeyBuilder(String name, List<String> fields) {
        this.name = name;
        this.fields.addAll(fields);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getFields() {
        return fields;
    }

    public void setFields(List<String> fields) {
        this.fields = fields;
    }

    public void addField(String field) {
        fields.add(field);
    }

    public boolean containsField(String field) {
        return fields.stream().anyMatch(name -> name.equalsIgnoreCase(field));
    }
}

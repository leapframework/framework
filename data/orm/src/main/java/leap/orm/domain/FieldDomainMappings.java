/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package leap.orm.domain;

import leap.lang.Strings;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

class FieldDomainMappings {
	
	private String  entityName;
    private Pattern entityPattern;
    private Map<String, Domain> fields  = new HashMap<>();
    private Map<String, Domain> aliases = new HashMap<>();

    public boolean hasEntityPattern() {
        return null != entityPattern;
    }

    public boolean hasEntityName() {
        return !Strings.isEmpty(entityName);
    }

    public boolean hasEntity() {
        return hasEntityPattern() || hasEntityName();
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public Pattern getEntityPattern() {
        return entityPattern;
    }

    public void setEntityPattern(Pattern entityPattern) {
        this.entityPattern = entityPattern;
    }

    public Domain getField(String name) {
        return fields.get(name.toLowerCase());
    }

    public Map<String, Domain> getFields() {
        return fields;
    }

    public Map<String, Domain> getAliases() {
        return aliases;
    }

    public void addField(Domain domain) {
        fields.put(domain.getName().toLowerCase(), domain);
    }

    public void addAlias(String field, String alias) {
        Domain domain = getField(field);
        if(null == domain) {
            throw new DomainConfigException("The field '" + field + "' not found in {entityName=" + entityName + ", entityPattern=" + entityPattern + "}");
        }
        aliases.put(alias.toLowerCase(), domain);
    }

    public Domain mapping(String entityName, String fieldName) {
        if(hasEntityName() && !this.entityName.equalsIgnoreCase(entityName)) {
            return null;
        }

        if(hasEntityPattern() && !entityPattern.matcher(entityName).matches()) {
            return null;
        }

        String key = fieldName.toLowerCase();
        Domain domain = fields.get(key);
        if(null == domain) {
            domain = aliases.get(key);
        }
        return domain;
    }
}
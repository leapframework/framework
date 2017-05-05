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

package leap.web.api.config.model;

import leap.web.api.config.ApiConfigException;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class ModelConfigImpl implements ModelConfig {

    protected String name;
    protected String className;

    protected Map<String, Property> properties = new LinkedHashMap<>();

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    @Override
    public Set<Property> getProperties() {
        return new LinkedHashSet<>(properties.values());
    }

    public Property getProperty(String name) {
        return properties.get(name.toLowerCase());
    }

    public void addProperty(Property p) {
        if(properties.containsKey(p.getName().toLowerCase())) {
            throw new ApiConfigException("Found duplicated property '" + p.getName() + "' of model " + this);
        }
        properties.put(p.getName().toLowerCase(), p);
    }

    @Override
    public String toString() {
        return "(name=" + name + ",class=" + className + ")";
    }

    public static class PropertyImpl implements Property,ConfigWithDocument {
        protected String name;
        protected String title;
        protected String summary;
        protected String description;

        public PropertyImpl(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        @Override
        public String getSummary() {
            return summary;
        }

        public void setSummary(String summary) {
            this.summary = summary;
        }

        @Override
        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }
}

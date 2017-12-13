/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package leap.web.api.meta.model;

import leap.lang.Strings;

import java.util.Map;

public class MApiModel extends MApiNamedWithDesc {

    protected final boolean        entity;
    protected final String         baseName;
    protected final Class<?>[]     javaTypes;
    protected final MApiProperty[] properties;
    protected final MApiExtension  extension;

    public MApiModel(boolean entity, String baseName, String name, String title, String summary, String description,
                     Class<?>[] javaTypes, MApiProperty[] properties, Map<String, Object> attrs, MApiExtension extension) {
        super(name, title, summary, description, attrs);

        this.entity = entity;
        this.baseName = baseName;
        this.javaTypes = javaTypes;
        this.properties = properties;
        this.extension = extension;
    }

    public boolean isEntity() {
        return entity;
    }

    public boolean hasBaseModel() {
        return !Strings.isEmpty(baseName);
    }

    /**
     * The name of base model.
     */
    public String getBaseName() {
        return baseName;
    }

    /**
     * Optional.
     */
    public Class<?>[] getJavaTypes() {
        return javaTypes;
    }

    public MApiProperty[] getProperties() {
        return properties;
    }

    public MApiProperty tryGetProperty(String name) {
        return tryGetProperty(name, false);
    }

    public MApiProperty tryGetProperty(String name, boolean ignoreCause) {
        for (MApiProperty p : properties) {
            if (ignoreCause ? p.getName().equalsIgnoreCase(name) : p.getName().equals(name)) {
                return p;
            }
        }
        return null;
    }

    public MApiExtension getExtension() {
        return extension;
    }
}

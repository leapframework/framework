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

import leap.lang.Arrays2;
import leap.lang.Builders;
import leap.lang.Strings;
import leap.lang.meta.MComplexType;
import leap.lang.meta.MProperty;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class MApiModelBuilder extends MApiNamedWithDescBuilder<MApiModel> {

    protected String       baseName;
    protected MComplexType type;
    protected boolean      entity;
    protected MApiExtension extension;

    protected Set<Class<?>>                    javaTypes  = new LinkedHashSet<>();
    protected Map<String, MApiPropertyBuilder> properties = new LinkedHashMap<>();

    public MApiModelBuilder() {
        super();
    }

    public MApiModelBuilder(MComplexType type) {
        this(type, null);
    }

    public MApiModelBuilder(MComplexType type, String name) {
        this.type = type;
        this.name = Strings.isEmpty(name) ? type.getName() : name;
        this.title = type.getTitle();
        this.summary = type.getSummary();
        this.description = type.getDescription();
        this.entity = type.isEntity();

        if(null != type.getJavaType()) {
            this.javaTypes.add(type.getJavaType());
        }

        for (MProperty mp : type.getProperties()) {
            addProperty(new MApiPropertyBuilder(mp));
        }
    }

    public boolean isEntity() {
        return entity;
    }

    public void setEntity(boolean entity) {
        this.entity = entity;
    }

    /**
     * The name of base model.
     */
    public String getBaseName() {
        return baseName;
    }

    public void setBaseName(String baseName) {
        this.baseName = baseName;
    }

    public Set<Class<?>> getJavaTypes() {
        return javaTypes;
    }

    public void addJavaType(Class<?> c) {
        javaTypes.add(c);
    }

    public Map<String, MApiPropertyBuilder> getProperties() {
        return properties;
    }

    public void addProperty(MApiPropertyBuilder p) {
        properties.put(p.getName(), p);
    }

    public MApiPropertyBuilder removeProperty(String name) {
        return properties.remove(name);
    }

    public MApiExtension getExtension() {
        return extension;
    }

    public void setExtension(MApiExtension extension) {
        this.extension = extension;
    }

    @Override
    public MApiModel build() {
        return new MApiModel(entity, baseName, name, title, summary, description, javaTypes.toArray(Arrays2.EMPTY_CLASS_ARRAY),
                Builders.buildArray(properties.values(), new MApiProperty[properties.size()]), attrs, extension);
    }

}

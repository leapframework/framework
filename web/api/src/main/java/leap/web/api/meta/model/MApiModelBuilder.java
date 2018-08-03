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
import leap.lang.Extensible;
import leap.lang.Strings;
import leap.lang.beans.BeanProperty;
import leap.lang.meta.MComplexType;
import leap.lang.meta.MComplexTypeBuilder;
import leap.lang.meta.MProperty;
import leap.web.api.annotation.ApiModel;
import leap.web.api.annotation.ApiProperty;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MApiModelBuilder extends MApiNamedWithDescBuilder<MApiModel> implements Extensible {

    protected final Map<Class<?>, Object> extensions = new HashMap<>();

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

        boolean annotatedOnly = false;

        if(null != type.getJavaType()) {
            this.javaTypes.add(type.getJavaType());

            ApiModel a = type.getJavaType().getAnnotation(ApiModel.class);
            if(null != a) {
                this.name = Strings.firstNotEmpty(a.name(), a.value(), this.name);
                this.description = Strings.firstNotEmpty(a.desc(), this.description);
                annotatedOnly = a.explicitProperties();
            }
        }

        for (MProperty mp : type.getProperties()) {
            if(annotatedOnly) {
                BeanProperty bp = mp.getBeanProperty();
                if(null != bp && !bp.isAnnotationPresent(ApiProperty.class)) {
                    continue;
                }
            }
            addProperty(new MApiPropertyBuilder(mp));
        }
    }

    public MComplexTypeBuilder toMComplexType() {
        MComplexTypeBuilder ct = new MComplexTypeBuilder();
        ct.setName(name);
        ct.setTitle(title);
        ct.setSummary(summary);
        ct.setDescription(description);
        ct.setEntity(entity);

        for(MApiPropertyBuilder p : properties.values()) {
            ct.addProperty(p.toMProperty());
        }

        return ct;
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

    public MApiPropertyBuilder getProperty(String name) {
        return properties.get(name);
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
    public Map<Class<?>, Object> getExtensions() {
        return extensions;
    }

    @Override
    public final <T> T getExtension(Class<?> type) {
        return (T)extensions.get(type);
    }

    @Override
    public final <T> void setExtension(Class<T> type, Object extension) {
        extensions.put(type, extension);
    }

    @Override
    public <T> T removeExtension(Class<?> type) {
        return (T)extensions.remove(type);
    }

    @Override
    public MApiModel build() {
        MApiModel m =
                new MApiModel(entity, baseName, name, title, summary, description, javaTypes.toArray(Arrays2.EMPTY_CLASS_ARRAY),
                              Builders.buildArray(properties.values(), new MApiProperty[properties.size()]), attrs, extension);

        m.getExtensions().putAll(this.extensions);

        return m;
    }

}

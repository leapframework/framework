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

import leap.lang.Builders;
import leap.lang.meta.MComplexType;
import leap.lang.meta.MProperty;

import java.util.LinkedHashMap;
import java.util.Map;

public class MApiModelBuilder extends MApiNamedWithDescBuilder<MApiModel> {

    protected String       baseName;
    protected MComplexType type;
    protected Class<?>     javaType;
    protected Map<String, MApiPropertyBuilder> properties = new LinkedHashMap<>();

    public MApiModelBuilder() {
        super();
    }

    public MApiModelBuilder(MComplexType type) {
        this.type = type;
        this.name = type.getName();
        this.title = type.getTitle();
        this.summary = type.getSummary();
        this.description = type.getDescription();
        this.javaType = type.getJavaType();

        for (MProperty mp : type.getProperties()) {
            addProperty(new MApiPropertyBuilder(mp));
        }
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

    public Class<?> getJavaType() {
        return javaType;
    }

    public void setJavaType(Class<?> javaType) {
        this.javaType = javaType;
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

    @Override
    public MApiModel build() {
        return new MApiModel(baseName, name, title, summary, description, javaType,
                Builders.buildArray(properties.values(), new MApiProperty[properties.size()]), attrs);
    }

}

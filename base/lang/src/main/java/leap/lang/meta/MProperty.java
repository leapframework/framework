/*
 * Copyright 2015 the original author or authors.
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
package leap.lang.meta;

import leap.lang.Args;
import leap.lang.Strings;
import leap.lang.beans.BeanProperty;

public class MProperty extends ImmutableMNamedWithDesc {

    protected final MType        type;
    protected final BeanProperty beanProperty;
    protected final Boolean      required;
    protected final String       defaultValue;
    protected final String[]     enumValues;
    protected final boolean      fixedLength;
    protected final Integer      length;
    protected final Integer      precision;
    protected final Integer      scale;
    protected final boolean      discriminator;
    protected final Boolean      creatable;
    protected final Boolean      updatable;
    protected final Boolean      sortable;
    protected final Boolean      filterable;
    protected final boolean      reference;

    public MProperty(String name, String title, String summary, String description,
                     MType type, BeanProperty beanProperty,
                     Boolean required, String defaultValue, String[] enumValues,
                     boolean fixedLength,
                     Integer length, Integer precision, Integer scale,
                     boolean discriminator,
                     Boolean creatable, Boolean updatable, Boolean sortable, Boolean filterable,
                     boolean reference) {
        super(name, title, summary, description);

        Args.notNull(type, "type");

        this.type = type;
        this.beanProperty = beanProperty;
        this.required = required;
        this.defaultValue = Strings.trimToNull(defaultValue);
        this.fixedLength = fixedLength;
        this.enumValues = enumValues;
        this.length = length;
        this.precision = precision;
        this.scale = scale;
        this.discriminator = discriminator;
        this.creatable = creatable;
        this.updatable = updatable;
        this.sortable = sortable;
        this.filterable = filterable;
        this.reference = reference;
    }

    public MType getType() {
        return type;
    }

    /**
     * Optional.
     */
    public BeanProperty getBeanProperty() {
        return beanProperty;
    }

    public Boolean getRequired() {
        return required;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public String[] getEnumValues() {
        return enumValues;
    }

    public boolean isFixedLength() {
        return fixedLength;
    }

    public Integer getLength() {
        return length;
    }

    public Integer getPrecision() {
        return precision;
    }

    public Integer getScale() {
        return scale;
    }

    public boolean isDiscriminator() {
        return discriminator;
    }

    public Boolean getCreatable() {
        return creatable;
    }

    public Boolean getUpdatable() {
        return updatable;
    }

    public Boolean getSortable() {
        return sortable;
    }

    public Boolean getFilterable() {
        return filterable;
    }

    /**
     * Returns true if this property is a reference property.
     */
    public boolean isReference() {
        return reference;
    }

    /**
     * Returns the referenced type name.
     */
    public String getRefTypeName() {
        return type.isCollectionType() ?
                type.asCollectionType().getElementType().asTypeRef().getRefTypeName() :
                type.asTypeRef().getRefTypeName();
    }

    @Override
    public String toString() {
        return "MProperty[name:" + name + ", kind:" + type.getTypeKind() + "]";
    }
}

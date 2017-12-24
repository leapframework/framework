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
import leap.lang.beans.BeanProperty;
import leap.lang.enums.Bool;
import leap.lang.meta.MProperty;
import leap.web.api.annotation.ApiProperty;

public class MApiPropertyBuilder extends MApiParameterBaseBuilder<MApiProperty> {

    protected MProperty    metaProperty;
    protected BeanProperty beanProperty;
    protected boolean      identity;
    protected boolean      unique;
    protected boolean      discriminator;
    protected boolean      reference;
    protected Boolean      readOnly;
    protected Boolean      creatable;
    protected Boolean      updatable;
    protected Boolean      sortable;
    protected Boolean      filterable;
    protected Boolean      expandable;
    protected MApiExtension extension;

    public MApiPropertyBuilder() {
	    super();
    }

    public MApiPropertyBuilder(MProperty mp) {
        super();
        this.setMProperty(mp);
    }

	public void setMProperty(MProperty mp) {
        this.metaProperty = mp;
        this.beanProperty = mp.getBeanProperty();
		this.name  = mp.getName();
		this.title = mp.getTitle();
		this.summary = mp.getSummary();
		this.description = mp.getDescription();
        this.metaProperty = mp;
		this.type = mp.getType();
		this.defaultValue = mp.getDefaultValue();
        this.enumValues = mp.getEnumValues();
		this.required =  mp.getRequired();
        this.identity = mp.isIdentity();
        this.unique = mp.isUnique();
        this.reference = mp.isReference();
        this.discriminator = mp.isDiscriminator();
        this.creatable = mp.getCreatable();
        this.updatable = mp.getUpdatable();
        this.sortable = mp.getSortable();
        this.filterable = mp.getFilterable();

        if(null != beanProperty) {
            ApiProperty a = beanProperty.getAnnotation(ApiProperty.class);
            if(null != a) {
                this.name = Strings.firstNotEmpty(a.name(), a.value(), this.name);

                this.description = Strings.firstNotEmpty(a.desc(), this.description);

                if(null == this.required) {
                    this.required = a.required();
                }

                if(a.readOnly()) {
                    this.readOnly = true;
                }
            }
        }
	}

    @Override
    public void setDescription(String description) {
        super.setDescription(description);
    }

    @Override
    public void trySetDescription(String description) {
        super.trySetDescription(description);
    }

    public MProperty getMetaProperty() {
        return metaProperty;
    }

    public void setMetaProperty(MProperty metaProperty) {
        this.metaProperty = metaProperty;
    }

    public BeanProperty getBeanProperty() {
        return beanProperty;
    }

    public void setBeanProperty(BeanProperty beanProperty) {
        this.beanProperty = beanProperty;
    }

    public boolean isIdentity() {
        return identity;
    }

    public void setIdentity(boolean identity) {
        this.identity = identity;
    }

    public boolean isUnique() {
        return unique;
    }

    public void setUnique(boolean unique) {
        this.unique = unique;
    }

    public boolean isReference() {
        return reference;
    }

    public void setReference(boolean reference) {
        this.reference = reference;
    }

    public boolean isDiscriminator() {
        return discriminator;
    }

    public void setDiscriminator(boolean discriminator) {
        this.discriminator = discriminator;
    }

    public Boolean getReadOnly() {
        return readOnly;
    }

    public void setReadOnly(Boolean readOnly) {
        this.readOnly = readOnly;
    }

    public Boolean getCreatable() {
        return creatable;
    }

    public void setCreatable(Boolean creatable) {
        this.creatable = creatable;
    }

    public Boolean getUpdatable() {
        return updatable;
    }

    public void setUpdatable(Boolean updatable) {
        this.updatable = updatable;
    }

    public Boolean getSortable() {
        return sortable;
    }

    public void setSortable(Boolean sortable) {
        this.sortable = sortable;
    }

    public Boolean getFilterable() {
        return filterable;
    }

    public void setFilterable(Boolean filterable) {
        this.filterable = filterable;
    }

    /**
     * For reference property.
     */
    public Boolean getExpandable() {
        return expandable;
    }

    public void setExpandable(Boolean expandable) {
        this.expandable = expandable;
    }

    public MApiExtension getExtension() {
        return extension;
    }

    public void setExtension(MApiExtension extension) {
        this.extension = extension;
    }

    @Override
    public MApiProperty build() {
	    return new MApiProperty(name, title, summary, description, metaProperty, beanProperty,
                                type, format, identity, unique, reference, discriminator, password, required,
                                defaultValue, enumValues,
	    					    null == validation ? null : validation.build(), attrs,
                                readOnly, creatable, updatable, sortable, filterable, expandable, extension);
    }
}
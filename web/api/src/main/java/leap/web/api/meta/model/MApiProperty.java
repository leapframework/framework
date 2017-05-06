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

import leap.lang.beans.BeanProperty;
import leap.lang.meta.MProperty;
import leap.lang.meta.MType;

import java.util.Map;

public class MApiProperty extends MApiParameterBase {

    protected final MProperty    metaProperty;
    protected final BeanProperty beanProperty;
    protected final boolean      discriminator;
    protected final Boolean      creatable;
    protected final Boolean      updatable;
    protected final Boolean      sortable;
    protected final Boolean      filterable;

	public MApiProperty(String name, String title, String summary, String description,
                        MProperty metaProperty,BeanProperty beanProperty,
                        MType type, String format, boolean discriminator, boolean password, Boolean required,
                        String defaultValue, String[] enumValues,
                        MApiValidation validation, Map<String, Object> attrs,
                        Boolean creatable, Boolean updatable, Boolean sortable, Boolean filterable) {
	    super(name, title, summary, description, type, format, false, password, required, defaultValue, enumValues, validation, attrs);

        this.discriminator = discriminator;
        this.metaProperty = metaProperty;
        this.beanProperty = beanProperty;
        this.creatable = creatable;
        this.updatable = updatable;
        this.sortable = sortable;
        this.filterable = filterable;
    }

    public MProperty getMetaProperty() {
        return metaProperty;
    }

    /**
     * Optional.
     */
    public BeanProperty getBeanProperty() {
        return beanProperty;
    }

    public boolean isDiscriminator() {
        return discriminator;
    }

    public boolean isReference() {
        return null != metaProperty && metaProperty.isReference();
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

    public boolean isCreatableExplicitly() {
        return null != creatable && creatable;
    }

    public boolean isUpdatableExplicitly() {
        return null != updatable && updatable;
    }

    public boolean isSortableExplicitly() {
        return null != sortable && sortable;
    }

    public boolean isFilterableExplicitly() {
        return null != filterable && filterable;
    }

    public boolean isNotCreatableExplicitly() {
        return null != creatable && !creatable;
    }

    public boolean isNotUpdatableExplicitly() {
        return null != updatable && !updatable;
    }

    public boolean isNotSortableExplicitly() {
        return null != sortable && !sortable;
    }

    public boolean isNotFilterableExplicitly() {
        return null != filterable && !filterable;
    }
}
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
import leap.lang.meta.MProperty;
import leap.web.api.meta.desc.ModelDesc;

public class MApiPropertyBuilder extends MApiParameterBaseBuilder<MApiProperty> {

    protected MProperty              property;
    protected Boolean                creatable;
    protected Boolean                updatable;
    protected Boolean                sortable;
    protected Boolean                filterable;
	
	public MApiPropertyBuilder() {
	    super();
    }

    public MApiPropertyBuilder(MProperty mp) {
        super();
        this.setMProperty(mp);
    }

	public void setMProperty(MProperty mp) {
        this.property = mp;
		this.name  = mp.getName();
		this.title = mp.getTitle();
		this.summary = mp.getSummary();
		this.description = mp.getDescription();
        this.property = mp;
		this.type = mp.getType();
		this.defaultValue = mp.getDefaultValue();
        this.enumValues = mp.getEnumValues();
		this.required =  mp.getRequired();
        this.creatable = mp.getCreatable();
        this.updatable = mp.getUpdatable();
        this.sortable = mp.getSortable();
        this.filterable = mp.getFilterable();
	}

    public MProperty getProperty() {
        return property;
    }

    public void setProperty(MProperty property) {
        this.property = property;
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

    @Override
    public MApiProperty build() {
	    return new MApiProperty(name, title, summary, description, property, type, format, password, required,
                                defaultValue, enumValues,
	    					    null == validation ? null : validation.build(), attrs,
                                creatable, updatable, sortable, filterable);
    }
}
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

import leap.lang.Buildable;
import leap.lang.expression.Expression;
import leap.lang.jdbc.JdbcType;

public class FieldDomainBuilder implements Buildable<FieldDomain> {
	
	protected Object       source;
	protected EntityDomain entityDomain;
	protected String       name;
	protected String       defaultColumnName;
	protected JdbcType     type;
	protected Integer      length;
	protected Integer      precision;
	protected Integer      scale;
	protected Boolean      nullable;
	protected String       defaultValue;
	protected Boolean	   insert;
	protected Boolean	   update;
	protected Expression   insertValue;
	protected Expression   updateValue;
	protected boolean	   autoMapping;
	protected Integer      sortOrder;
	protected boolean      unnamed;
	
	public FieldDomainBuilder(){
		
	}
	
	public FieldDomainBuilder(Object source){
		this.source = source;
	}
	
	public Object getSource() {
		return source;
	}

	public FieldDomainBuilder setSource(Object source) {
		this.source = source;
		return this;
	}
	
	public EntityDomain getEntityDomain() {
		return entityDomain;
	}

	public FieldDomainBuilder setEntityDomain(EntityDomain entityDomain) {
		this.entityDomain = entityDomain;
		return this;
	}

	public String getName() {
		return name;
	}

	public FieldDomainBuilder setName(String name) {
		this.name = name;
		return this;
	}
	
	public String getDefaultColumnName() {
        return defaultColumnName;
    }

    public FieldDomainBuilder setDefaultColumnName(String defaultColumnName) {
        this.defaultColumnName = defaultColumnName;
        return this;
    }

    public JdbcType getType() {
		return type;
	}

	public FieldDomainBuilder setType(JdbcType type) {
		this.type = type;
		return this;
	}

	public Integer getLength() {
		return length;
	}

	public FieldDomainBuilder setLength(Integer length) {
		this.length = length;
		return this;
	}

	public Integer getPrecision() {
		return precision;
	}

	public FieldDomainBuilder setPrecision(Integer precision) {
		this.precision = precision;
		return this;
	}

	public Integer getScale() {
		return scale;
	}

	public FieldDomainBuilder setScale(Integer scale) {
		this.scale = scale;
		return this;
	}

	public Boolean getNullable() {
		return nullable;
	}

	public FieldDomainBuilder setNullable(Boolean nullabe) {
		this.nullable = nullabe;
		return this;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public FieldDomainBuilder setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
		return this;
	}
	
	public Boolean getInsert() {
		return insert;
	}

	public FieldDomainBuilder setInsert(Boolean insert) {
		this.insert = insert;
		return this;
	}

	public Boolean getUpdate() {
		return update;
	}

	public FieldDomainBuilder setUpdate(Boolean update) {
		this.update = update;
		return this;
	}
	
	public Expression getInsertValue() {
		return insertValue;
	}

	public FieldDomainBuilder setInsertValue(Expression insertValue) {
		this.insertValue = insertValue;
		return this;
	}

	public Expression getUpdateValue() {
		return updateValue;
	}

	public FieldDomainBuilder setUpdateValue(Expression updateValue) {
		this.updateValue = updateValue;
		return this;
	}
	
	public boolean isAutoMapping() {
		return autoMapping;
	}

	public FieldDomainBuilder setAutoMapping(boolean autoMapping) {
		this.autoMapping = autoMapping;
		return this;
	}
	
    public Integer getSortOrder() {
        return sortOrder;
    }

    public FieldDomainBuilder setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
        return this;
    }
    
    public boolean isUnnamed() {
        return unnamed;
    }

    public FieldDomainBuilder setUnnamed(boolean unnamed) {
        this.unnamed = unnamed;
        return this;
    }

    @Override
	public FieldDomain build() {
		return new FieldDomain(source, entityDomain, name, defaultColumnName, type, length, precision, scale, 
				 				  nullable, defaultValue,insert,update,
				 				  insertValue,updateValue,autoMapping, sortOrder);
	}
}

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
import leap.lang.Strings;
import leap.lang.expression.Expression;
import leap.lang.jdbc.JdbcType;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class DomainBuilder implements Buildable<Domain> {
	
	protected Object       source;
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
	protected Float        sortOrder;
	protected boolean      unnamed;
    protected boolean	   autoMapping;
    protected boolean      override;
    protected List<String> aliases = new ArrayList<>();
	
	public DomainBuilder(){
		
	}

	public DomainBuilder(Object source){
		this.source = source;
	}

    public DomainBuilder tryUpdateFrom(Domain domain) {
        if(Strings.isEmpty(this.defaultColumnName)) {
            this.defaultColumnName = domain.getDefaultColumnName();
        }

        if(null == this.type) {
            this.type = domain.getType();
        }

        if(null == this.length) {
            this.length = domain.getLength();
        }

        if(null == this.precision) {
            this.precision = domain.getPrecision();
            this.scale = domain.getScale();
        }

        if(null == this.nullable) {
            this.nullable = domain.getNullable();
        }

        if(Strings.isEmpty(this.defaultValue)) {
            this.defaultValue = domain.getDefaultValue();
        }

        if(null == this.insert) {
            this.insert = domain.getInsert();
        }

        if(null == this.insertValue) {
            this.insertValue = domain.getInsertValue();
        }

        if(null == this.update) {
            this.update = domain.getUpdate();
        }

        if(null == this.updateValue) {
            this.updateValue = domain.getUpdateValue();
        }

        if(null == this.sortOrder) {
            this.sortOrder = domain.getSortOrder();
        }

        return this;
    }
	
	public Object getSource() {
		return source;
	}

	public DomainBuilder setSource(Object source) {
		this.source = source;
		return this;
	}
	
	public String getName() {
		return name;
	}

	public DomainBuilder setName(String name) {
		this.name = name;
		return this;
	}
	
	public String getDefaultColumnName() {
        return defaultColumnName;
    }

    public DomainBuilder setDefaultColumnName(String defaultColumnName) {
        this.defaultColumnName = defaultColumnName;
        return this;
    }

    public JdbcType getType() {
		return type;
	}

	public DomainBuilder setType(JdbcType type) {
		this.type = type;
		return this;
	}

	public Integer getLength() {
		return length;
	}

	public DomainBuilder setLength(Integer length) {
		this.length = length;
		return this;
	}

	public Integer getPrecision() {
		return precision;
	}

	public DomainBuilder setPrecision(Integer precision) {
		this.precision = precision;
		return this;
	}

	public Integer getScale() {
		return scale;
	}

	public DomainBuilder setScale(Integer scale) {
		this.scale = scale;
		return this;
	}

	public Boolean getNullable() {
		return nullable;
	}

	public DomainBuilder setNullable(Boolean nullabe) {
		this.nullable = nullabe;
		return this;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public DomainBuilder setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
		return this;
	}
	
	public Boolean getInsert() {
		return insert;
	}

	public DomainBuilder setInsert(Boolean insert) {
		this.insert = insert;
		return this;
	}

	public Boolean getUpdate() {
		return update;
	}

	public DomainBuilder setUpdate(Boolean update) {
		this.update = update;
		return this;
	}
	
	public Expression getInsertValue() {
		return insertValue;
	}

	public DomainBuilder setInsertValue(Expression insertValue) {
		this.insertValue = insertValue;
		return this;
	}

	public Expression getUpdateValue() {
		return updateValue;
	}

	public DomainBuilder setUpdateValue(Expression updateValue) {
		this.updateValue = updateValue;
		return this;
	}
	
	public boolean isAutoMapping() {
		return autoMapping;
	}

	public DomainBuilder setAutoMapping(boolean autoMapping) {
		this.autoMapping = autoMapping;
		return this;
	}
	
    public Float getSortOrder() {
        return sortOrder;
    }

    public DomainBuilder setSortOrder(Float sortOrder) {
        this.sortOrder = sortOrder;
        return this;
    }
    
    public boolean isUnnamed() {
        return unnamed;
    }

    public DomainBuilder setUnnamed(boolean unnamed) {
        this.unnamed = unnamed;
        return this;
    }

    public List<String> getAliases() {
        return aliases;
    }

    public DomainBuilder addAliases(List<String> aliases) {
        if(null != aliases) {
            this.aliases.addAll(aliases);
        }
        return this;
    }

    public boolean isOverride() {
        return override;
    }

    public DomainBuilder setOverride(boolean override) {
        this.override = override;
        return this;
    }

    @Override
	public Domain build() {
		return new Domain(source, name, defaultColumnName, type, length, precision, scale,
				 				  nullable, defaultValue,insert,update,
				 				  insertValue,updateValue,sortOrder,autoMapping);
	}
}

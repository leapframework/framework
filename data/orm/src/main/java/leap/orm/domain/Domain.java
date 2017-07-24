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

import leap.lang.*;
import leap.lang.annotation.Nullable;
import leap.lang.expression.Expression;
import leap.lang.jdbc.JdbcType;

import java.util.regex.Pattern;

public class Domain implements Sourced,Named {
	
	private final Object       source;
	private final String       name;
	private final String       defaultColumnName;
	private final JdbcType     type;
	private final Integer      length;
	private final Integer      precision;
	private final Integer      scale;
	private final Boolean      nullable;
	private final String       defaultValue;
	private final Boolean	   insert;
	private final Boolean	   update;
	private final Expression   insertValue;
	private final Expression   updateValue;
	private final Float        sortOrder;
    private final boolean      autoMapping;
	
	public Domain(Object source, String name, String defaultColumnName, JdbcType type, Integer length, Integer precision, Integer scale,
                  Boolean nullable, String defaultValue, Boolean insert, Boolean update,
                  Expression insertValue, Expression updateValue, Float sortOrder, boolean autoMapping) {
		Args.notEmpty(name,"name");
		this.source = source;
	    this.name = name;
	    this.defaultColumnName = defaultColumnName;
	    this.type = type;
	    this.length = length;
	    this.precision = precision;
	    this.scale = scale;
	    this.nullable = nullable;
	    this.defaultValue = defaultValue;
	    this.insert = insert;
	    this.update = update;
	    this.insertValue = insertValue;
	    this.updateValue = updateValue;
	    this.sortOrder   = sortOrder;
        this.autoMapping = autoMapping;
    }
	
	@Override
	public Object getSource() {
		return source;
	}

	public String getName() {
		return name;
	}
	
	public String getDefaultColumnName() {
        return defaultColumnName;
    }

    public JdbcType getType() {
		return type;
	}

	@Nullable
	public Integer getLength() {
		return length;
	}

	@Nullable
	public Integer getPrecision() {
		return precision;
	}

	@Nullable
	public Integer getScale() {
		return scale;
	}

	@Nullable
	public Boolean getNullable() {
		return nullable;
	}

	@Nullable
	public String getDefaultValue() {
		return defaultValue;
	}
	
	@Nullable
	public Boolean getInsert() {
		return insert;
	}

	@Nullable
	public Boolean getUpdate() {
		return update;
	}

	public Expression getInsertValue() {
		return insertValue;
	}

	public Expression getUpdateValue() {
		return updateValue;
	}
	
    public Float getSortOrder() {
        return sortOrder;
    }

    public boolean isAutoMapping() {
        return autoMapping;
    }

    @Override
    public String toString() {
		return "Domain : " + name;
    }
}

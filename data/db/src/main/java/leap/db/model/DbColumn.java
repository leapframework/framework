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
package leap.db.model;

import leap.lang.Args;
import leap.lang.Strings;
import leap.lang.json.JsonStringable;
import leap.lang.json.JsonWriter;

public class DbColumn extends DbNamedObject implements JsonStringable {
	
	protected final int     typeCode;
	protected final String  typeName;
	protected final int     length;
	protected final int     precision;
	protected final int     scale;
	protected final boolean nullable;
	protected final boolean primaryKey;
	protected final boolean unique;
	protected final boolean autoIncrement;
	protected final String	defaultValue;
	protected final String  comment;
	
	public DbColumn(String name,
					int typeCode,String typeName,
					int length,int precision,int scale,
					boolean nullable,boolean primaryKey,boolean unique,
					boolean autoIncrement,String defaultValue,String comment) {
		
	    super(name);
	    
	    Args.notEmpty(typeName,"type name");
	    
	    this.typeCode      = typeCode;
	    this.typeName      = typeName;
	    this.length        = length;
	    this.precision     = precision;
	    this.scale         = scale;
	    this.nullable      = nullable;
	    this.primaryKey    = primaryKey;
	    this.unique        = unique;
	    this.autoIncrement = autoIncrement;
	    this.defaultValue  = Strings.trimToNull(defaultValue);
	    this.comment       = Strings.trimToNull(comment);
    }

	public int getTypeCode() {
		return typeCode;
	}
	
	public String getTypeName() {
		return typeName;
	}

	public int getLength() {
		return length;
	}

	public int getPrecision() {
		return precision;
	}

	public int getScale() {
		return scale;
	}

	public boolean isNullable() {
		return nullable;
	}

	public boolean isPrimaryKey() {
		return primaryKey;
	}

	public boolean isUnique() {
		return unique;
	}

	public boolean isAutoIncrement() {
		return autoIncrement;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public String getComment() {
		return comment;
	}
	
	@Override
    public String toString() {
        StringBuffer result = new StringBuffer();

        result.append("Column [name=");
        result.append(getName());
        result.append("; type=");
        result.append(getTypeName());
        result.append("]");

        return result.toString();
    }

	@Override
    public void toJson(JsonWriter writer) {
		writer.startObject();
		
		
		writer.property("name", name)
			  .property("typeCode", typeCode)
			  .property("typeName", typeName);

		if(length > 0){
			writer.property("length", length);
		}else{
			writer.property("precision", precision)
			      .property("scale", scale);
		}
		
		writer.property("nullable", nullable);
		
		if(primaryKey){
			writer.property("primaryKey", primaryKey);
		}
		
		if(unique){
			writer.property("unique", unique);
		}
		
		if(autoIncrement){
			writer.property("autoIncrement", autoIncrement);
		}
		
		writer.propertyOptional("defaultValue", defaultValue)
		      .propertyOptional("comment",comment);
		
		writer.endObject();
    }
}
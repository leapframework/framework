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

import leap.lang.Strings;
import leap.lang.json.JsonStringable;
import leap.lang.json.JsonWriter;

public class DbSchemaObjectName extends DbSchemaObject implements JsonStringable {
	
	protected final String qualifiedName;
	
	public DbSchemaObjectName(String name) {
		this(null, null, name);
	}
	
	public DbSchemaObjectName(String schema, String name) {
		this(null,schema,name);
    }

	public DbSchemaObjectName(String catalog, String schema, String name) {
		super(catalog,schema,name);
		
		this.qualifiedName = Strings.join(new String[]{catalog,schema,name},".",true);
    }
	
	public String getQualifiedName(){
		return qualifiedName;
	}
	
	public boolean equalsIgnoreCase(DbSchemaObjectName that){
		if(null == that){
			return false;
		}
		return Strings.equalsIgnoreCase(this.getQualifiedName(), that.getQualifiedName());
	}
	
	@Override
    public void toJson(JsonWriter writer) {
		writer.startObject();
		writeName(writer);
		writer.endObject();
    }

	protected void writeName(JsonWriter writer) {
		writer.propertyOptional("catalog", catalog)
		      .propertyOptional("schema", schema)
		      .property("name", name);
	}
}
/*
 * Copyright 2014 the original author or authors.
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

import leap.lang.Buildable;
import leap.lang.json.JsonObject;
import leap.lang.json.JsonParsable;
import leap.lang.json.JsonValue;

public class DbSchemaObjectNameBuilder implements Buildable<DbSchemaObjectName>,JsonParsable {

	protected String catalog;
	protected String schema;
	protected String name;

	public String getCatalog() {
		return catalog;
	}

	public DbSchemaObjectNameBuilder setCatalog(String catalog) {
		this.catalog = catalog;
		return this;
	}

	public String getSchema() {
		return schema;
	}

	public DbSchemaObjectNameBuilder setSchema(String schema) {
		this.schema = schema;
		return this;
	}

	public String getName() {
		return name;
	}

	public DbSchemaObjectNameBuilder setName(String name) {
		this.name = name;
		return this;
	}

	@Override
    public DbSchemaObjectName build() {
	    return new DbSchemaObjectName(catalog, schema, name);
    }

	@Override
    public void parseJson(JsonValue value) {
	    JsonObject o = value.asJsonObject();
	    this.catalog = o.getString("catalog");
	    this.schema  = o.getString("schema");
	    this.name    = o.getString("name");
    }
}

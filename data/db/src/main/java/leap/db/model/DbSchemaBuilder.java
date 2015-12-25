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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import leap.lang.Args;
import leap.lang.Buildable;
import leap.lang.Collections2;
import leap.lang.json.JsonArray;
import leap.lang.json.JsonObject;
import leap.lang.json.JsonParsable;
import leap.lang.json.JsonValue;

public class DbSchemaBuilder implements Buildable<DbSchema>, JsonParsable {

	protected String 	       catalog;
	protected String 		   name;
	protected List<DbTable>    tables = new ArrayList<DbTable>();
	protected List<DbSequence> sequences = new ArrayList<DbSequence>();
	
	public DbSchemaBuilder(){
		
	}
	
	public DbSchemaBuilder(String name){
		this.name = name;
	}
	
	public DbSchemaBuilder(String catalog,String name){
		this.catalog = catalog;
		this.name    = name;
	}
	
	public String getCatalog() {
		return catalog;
	}

	public DbSchemaBuilder setCatalog(String catalog) {
		this.catalog = catalog;
		return this;
	}

	public String getName() {
		return name;
	}

	public DbSchemaBuilder setName(String name) {
		this.name = name;
		return this;
	}

	public List<DbTable> getTables() {
		return tables;
	}

	public DbSchemaBuilder addTable(DbTable table){
		Args.notNull(table,"table");
		tables.add(table);
		return this;
	}
	
	public DbSchemaBuilder addTables(Collection<DbTable> tables){
		Args.notNull(tables,"tables");
		this.tables.addAll(tables);
		return this;
	}
	
	public DbSchemaBuilder addTables(DbTable... tables){
		Args.notNull(tables,"tables");
		Collections2.addAll(this.tables, tables);
		return this;
	}
	
	public List<DbSequence> getSequences() {
		return sequences;
	}

	public DbSchemaBuilder addSequence(DbSequence sequence){
		Args.notNull(sequence,"sequence");
		sequences.add(sequence);
		return this;
	}
	
	public DbSchemaBuilder addSequences(Collection<DbSequence> sequences){
		Args.notNull(sequences,"sequences");
		this.sequences.addAll(sequences);
		return this;
	}
	
	public DbSchemaBuilder addSequences(DbSequence... sequences){
		Args.notNull(sequences,"sequences");
		Collections2.addAll(this.sequences, sequences);
		return this;
	}
	
	@Override
    public DbSchema build() {
	    return new DbSchema(catalog, name, 
	    					tables.toArray(new DbTable[tables.size()]),
	    					sequences.toArray(new DbSequence[sequences.size()]));
    }

	@Override
    public void parseJson(JsonValue json) {
		JsonObject o = json.asJsonObject();
		
		this.catalog = o.getString("catalog");
		this.name    = o.getString("name");
		
		JsonArray tables = o.getArray("tables");
		if(null != tables){
			for(JsonValue v : tables){
				DbTableBuilder tb = new DbTableBuilder();
				tb.parseJson(v);
				addTable(tb.build());
			}
		}
		
		JsonArray sequences = o.getArray("sequences");
		if(null != sequences){
			for(JsonValue v : sequences){
				DbSequenceBuilder sb = new DbSequenceBuilder();
				sb.parseJson(v);
				addSequence(sb.build());
			}
		}
	}
}

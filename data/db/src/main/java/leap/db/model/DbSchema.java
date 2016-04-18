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
import java.util.List;

import leap.lang.Args;
import leap.lang.Named;
import leap.lang.Strings;
import leap.lang.json.JsonStringable;
import leap.lang.json.JsonWriter;

public class DbSchema extends DbSchemaName implements Named,JsonStringable {
	
	public static DbSchema fromJson(String json) {
		DbSchemaBuilder sb = new DbSchemaBuilder();
		sb.parseJson(json);
		return sb.build();
	}
	
	protected final DbTable[]    tables;
	protected final DbSequence[] sequences;
	
	public DbSchema(String catalog,String name,DbTable[] tables,DbSequence[] sequences){
		super(catalog,name);
		
		Args.notNull(tables,"tables");
		Args.notNull(sequences,"sequences");
		
		this.tables    = tables;
		this.sequences = sequences;
	}

	/**
	 * Returns all the {@link DbTable} objects in this schema.
	 */
	public DbTable[] getTables() {
		return tables;
	}
	
	/**
	 * Returns al the {@link DbSequence} objects in this schema.
	 */
	public DbSequence[] getSequences() {
		return sequences;
	}

	/**
	 * Returns the {@link DbTable} object matches the given name (ignore case) in this schema.
	 * 
	 * <p> 
	 * Returns <code>null</code> if no table matched.
	 */
	public DbTable findTable(String name){
		for(int i=0;i<tables.length;i++){
			DbTable table = tables[i];
			if(Strings.equalsIgnoreCase(name, table.getName())){
				return table;
			}
		}
		return null;
	}
	
	/**
	 * Returns the {@link DbTable} matches the given name (ignore case).
	 * 
	 * <p>
	 * Returns <code>null</code> if no table matched.
	 */
    public DbTable findTable(DbSchemaObjectName name) {
        for (DbTable table : tables) {
            if (table.equalsIgnoreCase(name)) {
                return table;
            }
        }
        return null;
    }
	
	/**
	 * Returns the {@link DbSequence} object matches the given name (ignore case) in this schema.
	 * 
	 * <p>
	 * Returns <code>null</code> if no sequence matched.
	 */
	public DbSequence findSequence(String name){
		for(int i=0;i<sequences.length;i++){
			DbSequence sequence = sequences[i];
			if(Strings.equalsIgnoreCase(name, sequence.getName())){
				return sequence;
			}
		}
		return null;
	}

	/**
	 * Finds all the tables matches the given table names.
	 */
	public DbTable[] findTables(String... names){
		List<DbTable> tables = new ArrayList<>();
		
		for(String name : names){
			DbTable table = findTable(name);
			if(null != table){
				tables.add(table);
			}
		}
		
		return tables.toArray(new DbTable[tables.size()]);
	}
	
	/**
	 * Finds all the tables matches the given table names.
	 */
	public DbTable[] findTables(Named...names){
		List<DbTable> tables = new ArrayList<>();
		
		for(Named name : names){
			DbTable table = findTable(name.getName());
			if(null != table){
				tables.add(table);
			}
		}
		
		return tables.toArray(new DbTable[tables.size()]);
	}
	
	@Override
    public String toString() {
		return "[" + name + "](" + tables.length + " tables, " + sequences.length + " sequences)"; 
    }
	
	@Override
	public void toJson(JsonWriter w) {
		//{ catalog : '', name : '' 
		w.startObject()
		 .propertyOptional("catalog", catalog)
		 .property("name", name);
		
		//tables : [ {},{} ]
		w.propertyJsonable("tables",tables);
		
		if(sequences.length > 0){
			w.propertyJsonable("sequences",sequences);
		}
		
		w.endObject();
	}
}
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
package leap.db.platform;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Predicate;

import leap.db.DbCommands;
import leap.db.DbExecution;
import leap.db.change.SchemaChange;
import leap.db.change.SchemaChanges;
import leap.lang.Args;
import leap.lang.collection.ListEnumerable;
import leap.lang.jdbc.ConnectionCallbackWithResult;
import leap.lang.json.JsonWriter;

public class GenericSchemaChanges extends ListEnumerable<SchemaChange> implements SchemaChanges {
	
	protected final GenericDb db;
	
	public GenericSchemaChanges(GenericDb db) {
	    super(new ArrayList<SchemaChange>());
	    this.db = db;
    }
	
	public GenericSchemaChanges add(SchemaChange change){
		l.add(change);
		return this;
	}
	
	public GenericSchemaChanges addAll(Collection<SchemaChange> changes){
		l.addAll(changes);
		return this;
	}
	
	@Override
    public SchemaChanges filter(Predicate<SchemaChange> predicate) {
		Args.notNull(predicate,"predicate");
		
		GenericSchemaChanges filtered = new GenericSchemaChanges(db);
		
		for(SchemaChange change : this){
			if(predicate.test(change)){
				filtered.add(change);
			}
		}
		
		return filtered;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends SchemaChange> T firstOrNull(Class<T> changeType) {
    	Args.notNull(changeType, "changeType");
    	
		for(SchemaChange change : this){
			if(changeType.isAssignableFrom(change.getClass())){
				return (T)change;
			}
		}
	    return null;
    }

	@Override
    public String[] getChangeScripts() {
	    return getChangeCommands().getExecutionScripts();
    }
	
	@Override
    public DbCommands getChangeCommands() {
		return getChangeCommands(null);
    }
	
	protected DbCommands getChangeCommands(Connection connection) {
		GenericDbCommands commands = new GenericDbCommands(db);
		
		GenericSchemaChangeContext context = new GenericSchemaChangeContext(db, connection);
		
		for(SchemaChange change : this){
			commands.addAll(db.getDialect().getSchemaChangeCommands(change,context));
		}
		
	    return commands;
	}

	@Override
    public DbExecution applyChanges() {
		return db.executeWithResult(new ConnectionCallbackWithResult<DbExecution>() {
			@Override
            public DbExecution execute(Connection connection) throws SQLException {
	            return GenericSchemaChanges.this.applyChanges(connection);
            }
		});
	}
	
	@Override
    public DbExecution applyChanges(Connection connection) {
	    return getChangeCommands(connection).execute(connection);
    }

	@Override
    public String toString() {
		return this.getClass().getSimpleName() + " (" + size() + " changes)";
    }

	@Override
    public void toJson(JsonWriter writer) {
		writer.startObject();
		
		for(SchemaChange change : this){
			writer.key(getChangeName(change));
			change.toJson(writer);
		}
		
		writer.endObject();
    }
	
	private static final String CHANGE_SUFFIX = "Change";
	protected String getChangeName(SchemaChange change) {
		String n = change.getClass().getSimpleName();
		if(n.endsWith(CHANGE_SUFFIX)){
			n = n.substring(0,n.length() - CHANGE_SUFFIX.length());
		}
		return n;
	}
}
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
package leap.db.change;

import java.sql.Connection;
import java.util.function.Function;
import java.util.function.Predicate;

import leap.db.DbCommand;
import leap.db.DbCommands;
import leap.db.DbExecution;
import leap.lang.Enumerable;
import leap.lang.json.JsonStringable;

public interface SchemaChanges extends Enumerable<SchemaChange>, JsonStringable {
    
    /**
     * Returns the add only changes (no update and remove).
     */
    default SchemaChanges addOnly() {
        return filter((c) -> c.isAdd());
    }
    
	/**
	 * Returns the filtered schema changes.
	 */
	SchemaChanges filter(Predicate<SchemaChange> predicate);

    /**
     * Returns the processed schema changes.
     */
    SchemaChanges process(Function<SchemaChange, SchemaChange> processor);
	
	/**
	 * Finds the first change of the given change type.
	 * 
	 * <p>
	 * Returns <code>null</code> if there are no change(s) of the given change type.
	 */
	<T extends SchemaChange> T firstOrNull(Class<T> changeType);

	/**
	 * Returns the sql scripts of all changes.
	 */
	String[] getChangeScripts();
	
	/**
	 * Returns an {@link DbCommands} contains all the {@link DbCommand} for applying this changes to the underlying db.
	 */
	DbCommands getChangeCommands();

	/**
	 * <font color="red">Be careful calling this method. It may cause data loss.</font>.
	 * 
	 * <p>
	 * Applys all changes to the underlying db.
	 */
	DbExecution applyChanges();
	
	/**
	 * <font color="red">Be careful calling this method. It may cause data loss.</font>.
	 * 
	 * <p>
	 * Applys all changes to the underlying db specified by the given connection.
	 */
	DbExecution applyChanges(Connection connection);
}

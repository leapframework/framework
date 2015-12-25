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
package leap.db.command;

import leap.db.DbCommand;
import leap.db.model.DbColumn;
import leap.db.model.DbForeignKey;
import leap.db.model.DbIndex;

public interface AlterTable extends DbCommand {
	
	default int getSortOrder() {
		return 1;
	}
	
	/**
	 * @throws IllegalStateException if a primary key aleady exists in the table.
	 */
	AlterTable addPrimaryKey(String... pkColumnNames) throws IllegalStateException;
	
	/**
	 * @throws IllegalStateException if no primary key exists in the table.
	 */
	AlterTable dropPrimaryKey() throws IllegalStateException;
	
	/**
	 * @throws IllegalStateException if the given column aleady exists in table.
	 */
	AlterTable addColumn(DbColumn column) throws IllegalStateException;
	
	/**
	 * @throws IllegalStateException if the given column not exists in table.
	 */
	AlterTable dropColumn(String columnName) throws IllegalStateException;
	
	/**
	 * @throws IllegalStateException if the given column not exists in table.
	 */
	AlterTable dropColumn(DbColumn column) throws IllegalStateException;
	
	/**
	 * @throws IllegalStateException if the given foreign key aleady exists in table.
	 */
	AlterTable addForeignKey(DbForeignKey fk) throws IllegalStateException;

	/**
	 * @throws IllegalStateException if the given foreign key not exists in table.
	 */
	AlterTable dropForeignKey(String fkName) throws IllegalStateException;
	
	/**
	 * @throws IllegalStateException if the given foreign key not exists in table.
	 */
	AlterTable dropForeignKey(DbForeignKey fk) throws IllegalStateException;
	
	/**
	 * @throws IllegalStateException if the given index aleady exists in table.
	 */
	AlterTable addIndex(DbIndex ix) throws IllegalStateException;

	/**
	 * @throws IllegalStateException if the given index not exists in table.
	 */
	AlterTable dropIndex(String ixName) throws IllegalStateException;
	
	/**
	 * @throws IllegalStateException if the given index not exists in table.
	 */
	AlterTable dropIndex(DbIndex ix) throws IllegalStateException;	
	
	/**
	 * change the comment of table.
	 */
	AlterTable changeComment(String comment);
	
	/**
	 * Returns an array contains all the {@link DbColumn} to add.
	 * 
	 * <p>
	 * Returns an empty array if no columns to add.
	 */
	DbColumn[] getColumnsToAdd();
	
	/**
	 * Returns an array contains all the column names to drop.
	 * 
	 * <p>
	 * Returns an empty array if no columns to drop.
	 */
	String[] getColumnsToDrop();
	
	/**
	 * Returns an array contains all the {@link DbForeignKey} to add.
	 * 
	 * <p>
	 * Returns an empty array if no foreign keys to add.
	 */
	DbForeignKey[] getForeignKeysToAdd();
	
	/**
	 * Returns an array contains all the foreign key names to drop.
	 * 
	 * <p>
	 * Returns an empty array if no foreign keys to drop.
	 */
	String[] getForeignKeysToDrop();
	
	/**
	 * Returns an array contains all the {@link DbIndex} to add.
	 * 
	 * <p>
	 * Returns an empty array if no indexes to add.
	 */
	DbIndex[] getIndexesToAdd();
	
	/**
	 * Returns an array contains all the index names to drop.
	 * 
	 * <p>
	 * Returns an empty array if no indexes to drop.
	 */
	String[] getIndexesToDrop();

	/**
	 * Returns the changed table comment.
	 * 
	 * <p>
	 * Returns <code>null</code> if table's comment not changed.
	 */
	String getCommentToChange();

	/**
	 * Returns <code>true</code> if table's comment changed.
	 */
	boolean isCommentChanged();
	
	/**
	 * Returns <code>true</code> if this command has one of columns, foreign keys or indexes to add.
	 */
	boolean hasObjectsToAdd();
	
	/**
	 * Returns <code>true</code> if this command has one of columns, foreign keys or indexes to drop.
	 */
	boolean hasObjectsToDrop();
}

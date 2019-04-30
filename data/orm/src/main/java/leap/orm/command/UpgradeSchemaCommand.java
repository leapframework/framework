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
package leap.orm.command;


import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.List;

public interface UpgradeSchemaCommand extends DmoCommand {
	
	/**
	 * <font color="red">Be careful calling this method. It may cause data loss.</font>
	 * 
	 * <p>
	 * Enables or Disables dropping table.
	 * 
	 * <p>
	 * Default is disabled.
	 */
	UpgradeSchemaCommand setDropTableEnabled(boolean dropTableEnabled);
	
	/**
	 * Returns <code>true</code> if {@link #isDropTableObjectsEnabled()} is <code>true</code> or enable dropping table.
	 */
	boolean isDropTableEnabled();
	
	/**
	 * <font color="red">Be careful calling this method. It may cause data loss.</font>
	 * 
	 * <p>
	 * Enables or Disables dropping any objects in table, such as column,foreign key, index, etc.
	 * 
	 * <p>
	 * Default is disabled.
	 */
	UpgradeSchemaCommand setDropTableObjectsEnabled(boolean dropTableObjectsEnabled);
	
	/**
	 * Returns <code>true</code> if enable dropping any schema objects . 
	 */
	boolean isDropTableObjectsEnabled();
	
	/**
	 * <font color="red">Be careful calling this method. It may cause data loss.</font>
	 * 
	 * <p>
	 * Enables or Disables dropping column.
	 * 
	 * <p>
	 * Default is disabled.
	 */
	UpgradeSchemaCommand setDropColumnEnabled(boolean dropColumnEnabled);
	
	/**
	 * Returns <code>true</code> if {@link #isDropTableObjectsEnabled()} is <code>true</code> or enable dropping column.
	 */
	boolean isDropColumnEnabled();
	
	/**
	 * Enables or Disables dropping primary key.
	 * 
	 * <p>
	 * Default is disabled.
	 */
	UpgradeSchemaCommand setDropPrimaryKeyEnabled(boolean dropPrimaryKeyEnabled);
	
	/**
	 * Returns <code>true</code> if {@link #isDropTableObjectsEnabled()} is <code>true</code> or enable dropping primary key.
	 */
	boolean isDropPrimaryKeyEnabled();
	
	/**
	 * Enables or Disables dropping foreign key.
	 * 
	 * <p>
	 * Default is disabled.
	 */
	UpgradeSchemaCommand setDropForeignKeyEnabled(boolean dropForeignKeyEnabled);
	
	/**
	 * Returns <code>true</code> if {@link #isDropTableObjectsEnabled()} is <code>true</code> or enable dropping foreign key.
	 */
	boolean isDropForeignKeyEnabled();
	
	/**
	 * Enables or Disables dropping index.
	 * 
	 * <p>
	 * Default is disabled.
	 */
	UpgradeSchemaCommand setDropIndexEnabled(boolean dropIndexEnabled);
	
	/**
	 * Returns <code>true</code> if {@link #isDropTableObjectsEnabled()} is <code>true</code> or enable dropping index.
	 */
	boolean isDropIndexEnabled();
	
	/**
	 * Enables or Disables altering column.
	 * 
	 * <p>
	 * Default is disabled.
	 */
	UpgradeSchemaCommand setAlterColumnEnabled(boolean alterColumnEnabled);
	
	/**
	 * Returns <code>true</code> if altering column's definition is enabled.
	 */
	boolean isAlterColumnEnabled();

    /**
     * Returns the upgrade scripts.
     */
    List<String> getUpgradeScripts();

    /**
     * Prints the upgrade scripts.
     */
    void printUpgradeScripts(PrintWriter out);

    /**
     * Prints the upgrade scripts.
     */
    void printUpgradeScripts(PrintStream out);

}
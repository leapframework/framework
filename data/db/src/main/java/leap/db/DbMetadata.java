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
package leap.db;

import java.sql.DatabaseMetaData;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;

import leap.db.model.DbSchema;
import leap.db.model.DbSchemaName;
import leap.db.model.DbSequence;
import leap.db.model.DbTable;
import leap.db.model.DbSchemaObjectName;

/**
 * a wrapper interface of {@link DatabaseMetaData}
 */
public interface DbMetadata {
	
	/**
	 * @see DatabaseMetaData#getDatabaseProductName()
	 */
	String getProductName();
	
	/**
	 * @see DatabaseMetaData#getDatabaseProductVersion()
	 */
	String getProductVersion();
	
	/**
	 * @see DatabaseMetaData#getDatabaseMajorVersion()
	 */
	int getProductMajorVersion();
	
	/**
	 * @see DatabaseMetaData#getDatabaseMinorVersion()
	 */
	int	getProductMinorVersion();
	
	/**
	 * @see DatabaseMetaData#getIdentifierQuoteString()
	 */
	String getIdentifierQuoteString();
	
	/**
	 * @see DatabaseMetaData#getMaxTableNameLength()
	 */
	int getMaxTableNameLength();
	
	/**
	 * @see DatabaseMetaData#getMaxColumnNameLength()
	 */
	int getMaxColumnNameLength();
	
	/**
	 * @see DatabaseMetaData#getSQLKeywords()
	 */
	String[] getSQLKeywords();
	
	/**
	 * @see DatabaseMetaData#supportsMixedCaseIdentifiers()
	 */
	boolean supportsMixedCaseIdentifiers();
	
	/**
	 * @see DatabaseMetaData#supportsAlterTableWithAddColumn()
	 */
	boolean supportsAlterTableWithAddColumn();
	
	/**
	 * @see DatabaseMetaData#supportsAlterTableWithDropColumn()
	 */
	boolean supportsAlterTableWithDropColumn();
	
	/**
	 * Returns <code>true</code> if current jdbc driver supports JDBC 3.0 getParameterType.
	 * 
	 * @see PreparedStatement#getParameterMetaData()
	 * @see ParameterMetaData#getParameterType(int)
	 */
	boolean driverSupportsGetParameterType();
	
	/**
	 * Returns the cached default schema name of the underlying datasource.
	 */
	String getDefaultSchemaName();
	
	/**
	 * Returns the cached {@link DbSchemaName} objects from the underlying datasource. 
	 * 
	 * <p>
	 * only 'User' schema will be returned, all 'System' (built-in) schemas are ignored.
	 */
	DbSchemaName[] getExtraSchemaNames();
	
	/**
	 * Returns the cached {@link DbSchema} object from the underlying datasource.
	 */
	DbSchema getSchema();
	
	/**
	 * Returns the cached {@link DbSchema} object from the underlying datasource.
	 */
	DbSchema getSchema(String schemaName);
	
	/**
	 * Returns the cached {@link DbSchema} object from the underlying datasource.
	 */
	DbSchema getSchema(String catalog,String schemaName);
	
	/**
	 * Returns the cached {@link DbTable} in the default schema which name equals to the given name (ignore case).
	 * 
	 * <p>
	 * Returns <code>null</code> if table not exists in the default schema.
	 */
	DbTable tryGetTable(String tableName);
	
	/**
	 * Returns the cached {@link DbTable} in the given schema which name equals to the given name (ignore case).
	 * 
	 * <p>
	 * Returns <code>null</code if table not exists in the given schema.
	 */
	DbTable tryGetTable(String schema,String tableName);
	
	/**
	 * Returns the cached {@link DbTable} in the given schema which name equals to the given name (ignore case).
	 * 
	 * <p>
	 * Returns <code>null</code if table not exists in the given schema.
	 */
	DbTable tryGetTable(DbSchemaObjectName tableName);
	
	/**
	 * Returns the cached {@link DbSequence} in the default schema which name equals to the given name (ignore case).
	 * 
	 * <p>
	 * Returns <code>null</code> if sequence not exists.
	 */
	DbSequence tryGetSequence(String sequenceName);
	
	/**
	 * Returns the cached {@link DbSequence} in the default schema which name equals to the given name (ignore case).
	 * 
	 * <p>
	 * Returns <code>null</code> if sequence not exists.
	 */
	DbSequence tryGetSequence(String schema,String sequenceName);
	
	/**
	 * Returns the cached {@link DbSequence} in the default schema which name equals to the given name (ignore case).
	 * 
	 * <p>
	 * Returns <code>null</code> if sequence not exists.
	 */
	DbSequence tryGetSequence(DbSchemaObjectName sequenceName);
	
	/**
	 * refresh cached schemas and other metadatas and returns this {@link DbMetadata} object.
	 */
	DbMetadata refresh();
}
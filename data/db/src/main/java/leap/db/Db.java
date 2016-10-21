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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;

import javax.sql.DataSource;

import leap.core.jdbc.JdbcExecutor;
import leap.core.jdbc.PreparedStatementHandler;
import leap.core.jdbc.ResultSetReader;
import leap.core.jdbc.RawScalarReader;
import leap.db.command.*;
import leap.db.model.*;
import leap.lang.Arrays2;
import leap.lang.Named;
import leap.lang.exception.NestedSQLException;

public interface Db extends Named , JdbcExecutor {
	
	/**
	 * Returns the name of this {@link Db} instance.
	 * 
	 * <p>
	 * Example : "default", "blog", etc.
	 */
	String getName();
	
	/**
	 * Returns the platform type of this {@link Db}.
	 */
	String getType();
	
	/**
	 * Returns a description string of this db.
	 */
	String getDescription();
	
	/**
	 * Returns the underlying db platform.
	 */
	DbPlatform getPlatform();
	
	/**
	 * Returns the cached {@link DbDialect} instance of this {@link Db} instance.
	 */
	DbDialect getDialect();
	
	/**
	 * Returns the cached {@link DbMetadata} instance of this {@link Db} object.
	 */
	DbMetadata getMetadata();
	
	/**
	 * Returns the cached {@link DbComparator} instance of this {@link Db} object.
	 */
	DbComparator getComparator();
	
	/**
	 * Returns the cached {@link DataSource} instance of this {@link Db} object.
	 */
	DataSource getDataSource();
	
	/**
	 * Returns <code>true</code> if this db type is mysql.
	 */
	default boolean isMySql() {
		return getType().equalsIgnoreCase(DbPlatforms.MYSQL);
	}
	
	/**
	 * Returns <code>true</code> if this db type is mariadb.
	 */
	default boolean isMariaDB() {
		return getType().equalsIgnoreCase(DbPlatforms.MARIADB);
	}
	
	/**
	 * Returns <code>true</code> if this db type is oracle.
	 */
	default boolean isOracle() {
		return getType().equalsIgnoreCase(DbPlatforms.ORACLE);
	}
	
	/**
	 * Returns <code>true</code> if this db type is h2.
	 */
	default boolean isH2() {
		return getType().equalsIgnoreCase(DbPlatforms.H2);
	}
	
	/**
	 * Returns <code>true</code> if this db type is derby.
	 */
	default boolean isDerby() {
		return getType().equalsIgnoreCase(DbPlatforms.DERBY);
	}
	
	/**
	 * Returns <code>true</code> if this db type is db2.
	 */
	default boolean isDB2() {
		return getType().equalsIgnoreCase(DbPlatforms.DB2);
	}
	
	/**
	 * Returns <code>true</code> if this db type is postgresql.
	 */
	default boolean isPostgreSQL() {
		return getType().equalsIgnoreCase(DbPlatforms.POSTGRESQL);
	}
	
	/**
	 * Returns <code>true</code> if this db type is sql server.
	 */
	default boolean isSqlServer() {
		return getType().equalsIgnoreCase(DbPlatforms.SQLSERVER);
	}
	
	/**
	 * Checks is the given table exists in the cached default schema.
	 * 
	 * <p>
	 * Returns <code>true</code> if exists.
	 * 
	 * <p>
	 * Returns <code>false</code> if not exists.
	 */
	boolean checkTableExists(String tableName) throws NestedSQLException;
	
	/**
	 * Checks is the given table exists in the given schema defined in {@link DbSchemaObjectName#getSchema()}.
	 * 
	 * <p>
	 * Returns <code>true</code> if exists.
	 * 
	 * <p>
	 * Returns <code>false</code> if not exists.
	 */
	boolean checkTableExists(DbSchemaObjectName tableName) throws NestedSQLException;
	
	/**
	 * Checks is the given sequence exists in the cached default schema.
	 * 
	 * <p>
	 * Returns <code>true</code> if exists.
	 * 
	 * <p>
	 * Returns <code>false</code> if not exists.
	 */
	boolean checkSequenceExists(String sequenceName) throws NestedSQLException;
	
	/**
	 * Checks is the given sequence exists in the given schema defined in {@link DbSchemaObjectName#getSchema()}
	 * 
	 * <p>
	 * Returns <code>true</code> if exists.
	 * 
	 * <p>
	 * Returns <code>false</code> if not exists.
	 */
	boolean checkSequenceExists(DbSchemaObjectName sequenceName) throws NestedSQLException;

    /**
     * Creates a new {@link leap.db.command.CreateSchema} command for creating the given schema later.
     *
     * <p>
     * <strong>Note :</strong>
     *
     * <p>
     * <strong>
     * You must invoke the <code>execute()</code> method in the returned command to perform this operation.
     * </strong>
     */
    CreateSchema cmdCreateSchema(DbSchema schema);
	
	/**
	 * Creates a new {@link CreateTable} command for creating the given table later.
	 * 
	 * <p>
	 * <strong>Note :</strong>
	 * 
	 * <p>
	 * <strong>
	 * You must invoke the <code>execute()</code> method in the returned command to perform this operation.
	 * </strong>
	 */
	CreateTable cmdCreateTable(DbTable table);
	
	/**
	 * Creates a new {@link AlterTable} command for modifying the given table later.
	 * 
	 * <p>
	 * <strong>Note :</strong>
	 * 
	 * <p>
	 * <strong>
	 * You must invoke the <code>execute()</code> method in the returned command to perform this operation.
	 * </strong>
	 */
	AlterTable cmdAlterTable(DbSchemaObjectName tableName);
	
	/**
	 * Creates a new {@link DropTable} command for dropping the given table later.
	 * 
	 * <p>
	 * <strong>Note :</strong>
	 * 
	 * <p>
	 * <strong>
	 * You must invoke the <code>execute()</code> method in the returned command to perform this operation.
	 * </strong>
	 */
	DropTable cmdDropTable(DbSchemaObjectName tableName);
	
	/**
	 * Creates a new {@link CreateColumn} command for adding a new column into the given table later.
	 * 
	 * <p>
	 * <strong>Note :</strong>
	 * 
	 * <p>
	 * <strong>
	 * You must invoke the <code>execute()</code> method in the returned command to perform this operation.
	 * </strong>
	 */
	CreateColumn cmdCreateColumn(DbSchemaObjectName tableName,DbColumn column);
	
	/**
	 * Creates a new {@link DropColumn} command fro dropping an exists column from the given table later.
	 * 
	 * <p>
	 * <strong>Note :</strong>
	 * 
	 * <p>
	 * <strong>
	 * You must invoke the <code>execute()</code> method in the returned command to perform this operation.
	 * </strong>
	 */
	DropColumn cmdDropColumn(DbSchemaObjectName tableName,String columnName);
	
	/**
	 * Creates a new {@link CreatePrimaryKey} command for creating primary key in the given table later.
	 * 
	 * <p>
	 * <strong>Note :</strong>
	 * 
	 * <p>
	 * <strong>
	 * You must invoke the <code>execute()</code> method in the returned command to perform this operation.
	 * </strong>
	 */
	CreatePrimaryKey cmdCreatePrimaryKey(DbSchemaObjectName tableName,DbPrimaryKey pk);
	
	/**
	 * Creates a new {@link DropPrimaryKey} command for dropping a exists primary key from the given table later.
	 * 
	 * <p>
	 * <strong>Note :</strong>
	 * 
	 * <p>
	 * <strong>
	 * You must invoke the <code>execute()</code> method in the returned command to perform this operation.
	 * </strong>
	 */
	DropPrimaryKey cmdDropPrimaryKey(DbSchemaObjectName tableName);
	
	/**
	 * Creates a new {@link CreateForeignKey} command for creating a new foreign key later.
	 * 
	 * <p>
	 * <strong>Note :</strong>
	 * 
	 * <p>
	 * <strong>
	 * You must invoke the <code>execute()</code> method in the returned command to perform this operation.
	 * </strong>
	 */
	CreateForeignKey cmdCreateForeignKey(DbSchemaObjectName tableName,DbForeignKey fk);
	
	/**
	 * Creates a new {@link DropForeignKey} command for dropping an exists foreign key later.
	 * 
	 * <p>
	 * <strong>Note :</strong>
	 * 
	 * <p>
	 * <strong>
	 * You must invoke the <code>execute()</code> method in the returned command to perform this operation.
	 * </strong>
	 */
	DropForeignKey cmdDropForeignKey(DbSchemaObjectName tableName,String fkName);
	
	/**
	 * Creates a new {@link CreateIndex} command for creating a new index later.
	 * 
	 * <p>
	 * <strong>Note :</strong>
	 * 
	 * <p>
	 * <strong>
	 * You must invoke the <code>execute()</code> method in the returned command to perform this operation.
	 * </strong>
	 */
	CreateIndex cmdCreateIndex(DbSchemaObjectName tableName,DbIndex index);
	
	/**
	 * Creates a new {@link DropIndex} command for dropping an exists index later.
	 * 
	 * <p>
	 * <strong>Note :</strong>
	 * 
	 * <p>
	 * <strong>
	 * You must invoke the <code>execute()</code> method in the returned command to perform this operation.
	 * </strong>
	 */
	DropIndex cmdDropIndex(DbSchemaObjectName tableName,String ixName);
	
	/**
	 * Creates a new {@link CreateSequence} command for creating a new sequence later.
	 * 
	 * <p>
	 * <strong>Note :</strong>
	 * 
	 * <p>
	 * <strong>
	 * You must invoke the <code>execute()</code> method in the returned command to perform this operation.
	 * </strong>
	 */
	CreateSequence cmdCreateSequence(DbSequence sequence);
	
	/**
	 * Creates a new {@link DropSequence} command to drop an exists sequence later.
	 * 
	 * <p>
	 * <strong>Note :</strong>
	 * 
	 * <p>
	 * <strong>
	 * You must invoke the <code>execute()</code> method in the returned command to perform this operation.
	 * </strong>
	 */
	DropSequence cmdDropSequence(DbSchemaObjectName sequenceName);
	
	/**
	 * Creates a new {@link DropSchema} command to drop an exists schema object includes all tables, sequences and views etc.
	 * 
	 * <p>
	 * <strong>Note :</strong>
	 * 
	 * <p>
	 * <strong>
	 * You must invoke the <code>execute()</code> method in the returned command to perform this operation.
	 * </strong>
	 */
	DropSchema cmdDropSchema(String schemaName);
	
	/**
	 * Creates a new {@link RenameColumn} command to rename an exists column.
	 * 
	 * <p>
	 * <strong>Note :</strong>
	 * 
	 * <p>
	 * <strong>
	 * You must invoke the <code>execute()</code> method in the returned command to perform this operation.
	 * </strong>Øß
	 */
	RenameColumn cmdRenameColumn(DbSchemaObjectName tableName,String columnName,String renameTo);
	
	/**
	 * Creates a new {@link DbExecution} instance for executing sql statements later.
	 */
	DbExecution createExecution();
	
	/**
	 * Executes an update use the given sql statement.
	 * 
	 * @see PreparedStatement#executeUpdate()
	 */
	int executeUpdate(Connection connection, String sql, Object[] args) throws NestedSQLException;
	
	/**
	 * Executes an update use the given sql statement.
	 * 
	 * @see PreparedStatement#executeUpdate()
	 */
	int executeUpdate(Connection connection, String sql, Object[] args, int[] types) throws NestedSQLException;
	
	/**
	 * Execute an update use the given sql statement.
	 * 
	 * @see PreparedStatement#executeUpdate()
	 */
	int executeUpdate(Connection connection, String sql, Object[] args, int[] types, PreparedStatementHandler<Db> handler) throws NestedSQLException;
	
	/**
	 * Executes an query and returns the result readed by the given {@link ResultSetReader}.
	 * 
	 * @see Statement#executeQuery(String)
	 */
	<T> T executeQuery(Connection connection, String sql, Object[] args, ResultSetReader<T> reader) throws NestedSQLException;
	
	/**
	 * Executes an query and returns the result readed by the given {@link ResultSetReader}.
	 * 
	 * @see Statement#executeQuery(String)
	 */
	<T> T executeQuery(Connection connection, String sql, Object[] args, int[] types, ResultSetReader<T> reader) throws NestedSQLException;
	
	/**
	 * Executes a query and returns the scalar value for the given type.
	 */
	default <T> T queryForScalar(Class<T> type, Connection connection, String sql) throws NestedSQLException {
		return queryForScalar(type, connection, sql, Arrays2.EMPTY_OBJECT_ARRAY,Arrays2.EMPTY_INT_ARRAY);
	}
	
	/**
	 * Executes a query and returns the scalar value for the given type.
	 */
	default <T> T queryForScalar(Class<T> type, Connection connection, String sql,Object[] args) throws NestedSQLException {
		return queryForScalar(type, connection, sql, args,Arrays2.EMPTY_INT_ARRAY);
	}
	
	/**
	 * Executes a query and returns the scalar value for the given type. 
	 */
	default <T> T queryForScalar(Class<T> type, Connection connection, String sql,Object[] args, int[] types) throws NestedSQLException {
		return executeQuery(connection, sql, args, types, new RawScalarReader<T>(type,false));
	}
}
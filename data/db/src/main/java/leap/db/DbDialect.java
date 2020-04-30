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

import leap.core.jdbc.PreparedStatementHandler;
import leap.db.change.SchemaChange;
import leap.db.change.SchemaChangeContext;
import leap.db.model.*;
import leap.db.support.FunctionSupport;
import leap.db.support.JsonColumnSupport;
import leap.lang.convert.Converts;
import leap.lang.jdbc.JdbcTypes;

import java.sql.*;
import java.util.List;
import java.util.function.Consumer;

/**
 * a dialect interface of the underlying db platform.
 */
public interface DbDialect {

    /**
     * Returns the property or <code>null</code>.
     */
    String getProperty(String name);

    /**
     * Returns the property as the given type.
     */
    <T> T getProperty(String name, Class<T> type);

    /**
     * Returns the native type def mapping to the given type name.
     */
    String getSpecialType(String name);

    /**
     * Returns the property or <code>null</code>.
     */
    default String getProperty(String name, String defaults) {
        String v = getProperty(name);
        return null == v ? defaults : v;
    }

    /**
     * Returns the property as the given type.
     */
    default <T> T getProperty(String name, Class<T> type, T defaults) {
        T v = getProperty(name, type);
        return null == v ? defaults : v;
    }

    /**
     * returns the default schema name of the given {@link Connection}.
     */
    String getDefaultSchemaName(Connection connection, DatabaseMetaData dm) throws SQLException;

    /**
     * Returns the default 'onUpdate' {@link DbCascadeAction} when creating a new foreign key.
     */
    DbCascadeAction getForeignKeyDefaultOnUpdate();

    /**
     * Returns the default 'onDelete' {@link DbCascadeAction} when creating a new foreign key
     */
    DbCascadeAction getForeignKeyDefaultOnDelete();

    /**
     * returns <code>true</code> if the given word (ignore case) is the reserved sql keyword of this db platform.
     */
    boolean isKeyword(String word);

    /**
     * returns <code>true</code> if the given schema name (ignore case) is built-in schema of this db platform.
     */
    boolean isSystemSchema(String schemaName);

    /**
     * returns <code>true</code> if this db platform automatic generated index for foreign key.
     */
    boolean isAutoGenerateIndexForForeignKey();

    /**
     * returns <code>true</code> if this db platform automatic generated index for primary key.
     */
    boolean isAutoGenerateIndexForPrimaryKey();

    /**
     * "delete table_alias from table_name table_alias"
     */
    default boolean useTableAliasAfterDelete() {
        return false;
    }

    /**
     * "update table_alias set ... from table_name table_alias"
     */
    default boolean useTableAliasAfterUpdate() {
        return false;
    }

    String getStatementDelimiter();

    /**
     * returns <code>true</code> if this db platform supoorts the given on delete {@link DbCascadeAction}.
     */
    boolean supportsOnDeleteAction(DbCascadeAction action);

    /**
     * Returns <code>true</code> if this db platform supports auto increment column (identity).
     */
    boolean supportsAutoIncrement();

    /**
     * Returns <code>true</code> if this db platform supports sequence.
     */
    boolean supportsSequence();

    /**
     * Returns <code>true</code> if this db platform supports {@link #getSelectCurrentSequenceValueSql(String)}.
     */
    default boolean supportsCurrentSequenceValue() {
        return supportsSequence();
    }

    /**
     * Returns <code>true</code> if this db platform supports column comment.
     */
    boolean supportsColumnComment();

    /**
     * Returns <code>true<code> if this db platform supports column renaming.
     */
    boolean supportsRenameColumn();

    /**
     * read the native (physical) default value string for the given type code.
     *
     * <p>
     * <p>
     * i.e. MySQL converts illegal date/time/timestamp values to "0000-00-00 00:00:00",
     * <p>
     * we should return <code>null</code> value instead of the illegal string.
     */
    String readDefaultValue(int typeCode, String nativeDefaultValue);

    /**
     * returns the quoted identifier if the identifier is keyword.
     *
     * <p>
     * returns the identifier itself if not a keyword.
     */
    String quoteIdentifier(String identifier);

    /**
     * returns the quoted identifier if the identifier is keyword and quoteKeywordOnly set to true.
     *
     * <p>
     * returns the identifier itself if not a keyword and quoteKeywordOnly set to true.
     *
     * <p>
     * returns the quoted identifier if the quoteKeywordOnly set to false.
     */
    String quoteIdentifier(String identifier, boolean quoteKeywordOnly);

    /**
     * returns the full qualified table name. i.e. catalog.schema.table.
     *
     * <p>
     * the given catalog and schema can be null or empty.
     *
     * <p>
     * the given table name must not be null or empty.
     */
    String qualifySchemaObjectName(String catalog, String schema, String table);

    /**
     * returns the full qualified table name.
     *
     * @see #qualifySchemaObjectName(String, String, String)
     */
    String qualifySchemaObjectName(DbSchemaObjectName table);

    /**
     * Escapes special characters in the given SQL literal string
     */
    String escape(String string);

    /**
     * Converts the given value to a string value used for display.
     *
     * @param value the value to be translated.
     */
    String toDisplayString(int typeCode, Object value);

    /**
     * Converts the given value to a string value as column's default used in sql.
     */
    String toSqlDefaultValue(int typeCode, Object value);

    /**
     * Returns the generated primary key name of the given table.
     */
    String generatePrimaryKeyName(DbSchemaObjectName tableName, String... pkColumnNames);

    /**
     * Returns the generated alternative key name of the given table.
     */
    String generateAlternativeKeyName(DbSchemaObjectName tableName, String... akColumnNames);

    /**
     * Returns the sql for querying table's count.
     */
    default String getCountTableSql(DbSchemaObjectName tableName) {
        return "select count(*) from " + qualifySchemaObjectName(tableName);
    }

    /**
     * Returns a new {@link List} contains the sql statements to truncate a table.
     */
    List<String> getTruncateTableSqls(DbSchemaObjectName tableName);

    /**
     * Returns a new {@link List} contains the sql statements to create a table (include primary key and columns).
     */
    List<String> getCreateTableSqls(DbTable table);

    /**
     * Returns a new {@link List} contains the sql statemetns to drop a table.
     */
    List<String> getDropTableSqls(DbSchemaObjectName tableName);

    /**
     * Returns a new {@link List} contains the sql statements to drop a view.
     */
    List<String> getDropViewSqls(DbSchemaObjectName viewName);

    /**
     * Returns a new {@link List} contains the sql statemetns to add a new column to the exists table.
     */
    List<String> getCreateColumnSqls(DbSchemaObjectName tableName, DbColumn column);

    /**
     * Returns a new {@link List} contains the sql statements to rename a exists column.
     *
     * @throws IllegalStateException if this dialect not supports column renaming.
     * @see #supportsRenameColumn()
     */
    List<String> getRenameColumnSqls(DbSchemaObjectName tableName, String columnName, String renameTo) throws IllegalStateException;

    /**
     * Returns a new {@link List} contains the sql statements to alter an exists column to unique.
     *
     * @throws IllegalStateException if this dialect not supports column renaming.
     * @see #supportsRenameColumn()
     */
    List<String> getAlterColumnUniqueSqls(DbSchemaObjectName tableName, String columnName) throws IllegalStateException;

    /**
     * Returns a new {@link List} contains the sql statements to drop a exists column in the exists table.
     */
    List<String> getDropColumnSqls(DbSchemaObjectName tableName, String columnName);

    /**
     * Returns a new {@link List} contains the sql statements to create a primary key in the exists table.
     */
    List<String> getCreatePrimaryKeySqls(DbSchemaObjectName tableName, DbPrimaryKey pk);

    /**
     * Returns a new {@link List} contains the sql statements to drop the primary key of the exists table.
     */
    List<String> getDropPrimaryKeySqls(DbSchemaObjectName tableName);

    /**
     * Returns a new {@link List} contains the sql statements to add a new foreign key to the exists table.
     */
    List<String> getCreateForeignKeySqls(DbSchemaObjectName tableName, DbForeignKey fk);

    /**
     * Returns a new {@link List} contains the sql statemetns to drop a exists foreign key in the exists table.
     */
    List<String> getDropForeignKeySqls(DbSchemaObjectName tableName, String fkName);

    /**
     * Returns a new {@link List} contains the sql statements to add a new index to the exists table.
     */
    List<String> getCreateIndexSqls(DbSchemaObjectName tableName, DbIndex ix);

    /**
     * Returns a new {@link List} contains the sql statements to drop a exists index in the exists table.
     */
    List<String> getDropIndexSqls(DbSchemaObjectName tableName, String ixName);

    /**
     * Returns a new {@link List} contains the sql statements to update the table's comment in db.
     */
    List<String> getCommentOnTableSqls(DbSchemaObjectName tableName, String comment);

    /**
     * Returns a new {@link List} contains the sql statements to create a new sequence.
     *
     * @throws IllegalStateException if this dialect not supports sequence.
     * @see #supportsSequence()
     */
    List<String> getCreateSequenceSqls(DbSequence sequence) throws IllegalStateException;

    /**
     * Returns a new {@link List} contains the sql statements to drop a exists sequence.
     *
     * @throws IllegalStateException if this dialect not supports sequence.
     */
    List<String> getDropSequenceSqls(DbSchemaObjectName sequenceName) throws IllegalStateException;

    /**
     * Returns a new {@link List} contains the sql statements to drop a exists database schema.
     */
    List<String> getDropSchemaSqls(DbSchema schema);

    /**
     * Returns a string use in the insertation sql to set the next value of a sequence.
     *
     * @throws IllegalStateException if this dialect not supports sequence.
     */
    String getNextSequenceValueSqlString(String sequenceName) throws IllegalStateException;

    /**
     * Returns a sql statement to select next value of the given sequence.
     */
    String getSelectNextSequenceValueSql(String sequenceName) throws IllegalStateException;

    /**
     * Returns a sql statement to select current value of the given sequence.
     *
     * @throws IllegalStateException if this dialect not supports sequence.
     */
    String getSelectCurrentSequenceValueSql(String sequenceName) throws IllegalStateException;

    /**
     * Returns a new {@link List} contains the db commands for applying changes to the underlying db.
     */
    default List<DbCommand> getSchemaChangeCommands(SchemaChange change) {
        return getSchemaChangeCommands(change, SchemaChangeContext.DEFAULT);
    }

    /**
     * Returns a new {@link List} contains the db commands for applying changes to the underlying db.
     */
    List<DbCommand> getSchemaChangeCommands(SchemaChange change, SchemaChangeContext context);

    /**
     * Returns a new {@link PreparedStatementHandler} to get the auto increment identity column's generated value.
     *
     * @throws IllegalStateException if this dialect not supports auto increment column.
     */
    PreparedStatementHandler<Db> getAutoIncrementIdHandler(Consumer<Object> generatedIdCallback) throws IllegalStateException;

    /**
     * Returns a new {@link PreparedStatementHandler} to get the inserted sequence value.
     *
     * <p>
     * An inserted sequence value means the insertion sql use a sequence function to generated the value.
     *
     * <p>
     * Example:
     * <pre>
     * 	insert into table1(id_) values(nextval('table1_seq'));
     * </pre>
     *
     * @throws IllegalStateException if this dialect not supports sequence.
     */
    PreparedStatementHandler<Db> getInsertedSequenceValueHandler(String sequenceName, Consumer<Object> generatedIdCallback) throws IllegalStateException;

    /**
     * Creates a new {@link PreparedStatement}.
     *
     * @see Connection#prepareStatement(String)
     */
    PreparedStatement createPreparedStatement(Connection connection, String sql) throws SQLException;

    /**
     * Creates a new {@link PreparedStatement}
     *
     * @see Connection#prepareStatement(String, int)
     */
    PreparedStatement createPreparedStatement(Connection connection, String sql, int autoGeneratedKeys) throws SQLException;

    /**
     * Returns a {@link String} as the page query sql for the underlying db.
     *
     * <p>
     * The sql query args defined in {@link DbLimitQuery#getArgs()} will be modified if necessary.
     */
    String getLimitQuerySql(DbLimitQuery query);

    /**
     * Adds the given order by expression to a sql without order by <strong>simply</strong>.
     *
     * <p>
     * <strong>Note:</strong>
     * <p>
     * This method does not promise to handle complex sql.
     *
     * @param sql     sql string without order by
     * @param orderBy order by expression contains <code>'order by'</code> characters.
     */
    String addOrderBy(String sql, String orderBy);

    /**
     * Sets the given parameter value for the given {@link PreparedStatement}.
     *
     * <p>
     * index starts from 1
     */
    int setParameter(PreparedStatement ps, int index, Object value) throws SQLException;

    /**
     * Sets the given parameter value for the given {@link PreparedStatement}.
     *
     * @param ps
     * @param index starts from 1
     * @param type  JDBC type of the given parameter, may be {@link JdbcTypes#UNKNOWN_TYPE_CODE}.
     */
    int setParameter(PreparedStatement ps, int index, Object value, int type) throws SQLException;

    /**
     * Returns the value of the column in the given {@link ResultSet} of the given index.
     *
     * <p>
     * The given index starts from 1.
     */
    Object getColumnValue(ResultSet rs, int index) throws SQLException;

    /**
     * Returns the value of the column in the given {@link ResultSet} of the given column name.
     *
     * <p>
     * The given index starts from 1.
     */
    Object getColumnValue(ResultSet rs, String name) throws SQLException;

    /**
     * Returns the value of the column in the given {@link ResultSet} of the given index.
     *
     * <p>
     * The given type code may be {@link JdbcTypes#UNKNOWN_TYPE_CODE}, the underlying implementation must handle it.
     *
     * <p>
     * The given index starts from 1.
     */
    Object getColumnValue(ResultSet rs, int index, int type) throws SQLException;

    /**
     * Returns the value of the column in the given {@link ResultSet} of the given name.
     *
     * <p>
     * The given type code may be {@link JdbcTypes#UNKNOWN_TYPE_CODE}, the underlying implementation must handle it.
     *
     * <p>
     * The given index starts from 1.
     */
    Object getColumnValue(ResultSet rs, String name, int type) throws SQLException;

    /**
     * Returns the value fo the column in the given {@link ResultSet} of the given index.
     *
     * <p>
     * The returned value will converts to the given target type.
     */
    <T> T getColumnValue(ResultSet rs, int index, Class<T> targetType) throws SQLException;

    /**
     * Returns the value fo the column in the given {@link ResultSet} of the given column name.
     *
     * <p>
     * The returned value will converts to the given target type.
     */
    <T> T getColumnValue(ResultSet rs, String name, Class<T> targetType) throws SQLException;

    /**
     * Splits the sql scripts to multiple sql statements.
     */
    List<String> splitSqlStatements(String sqlScript);

    /**
     * Returns <code>true</code> if the given state indicates the connection is disconnect.
     */
    boolean isDisconnectSQLState(String state);

    /**
     * Wraps the query sql with for update.
     */
    default void wrapSelectForUpdate(StringBuilder sql) {
        sql.append(" for update");
    }

    /**
     * Required. Returns the {@link FunctionSupport}.
     */
    FunctionSupport getFunctions();

    /**
     * Returns <code>true</code> if the db supports json column.
     */
    default boolean supportsJsonColumn() {
        return null != getJsonColumnSupport();
    }

    /**
     * Returns the {@link JsonColumnSupport} or <code>null</code> if the db does not supports json column.
     */
    default JsonColumnSupport getJsonColumnSupport() {
        return null;
    }

    /**
     * Read the value of json column.
     */
    default String getJsonColumnValue(ResultSet rs, int column, int type) throws SQLException {
        Object value = getColumnValue(rs, column, type);
        return null == value ? null : Converts.toString(value);
    }
}
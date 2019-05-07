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

import leap.core.jdbc.PreparedStatementHandler;
import leap.db.*;
import leap.db.change.*;
import leap.db.model.*;
import leap.lang.*;
import leap.lang.convert.Converts;
import leap.lang.jdbc.JDBC;
import leap.lang.jdbc.JdbcType;
import leap.lang.jdbc.JdbcTypeKind;
import leap.lang.jdbc.JdbcTypes;
import leap.lang.logging.Log;
import leap.lang.reflect.Reflection;
import leap.lang.value.Null;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.*;
import java.sql.Types;
import java.util.*;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public abstract class GenericDbDialect extends GenericDbDialectBase implements DbAware {

    protected Log log;

    private final Map<Class<?>, Method> schemaChangeMethods = new ConcurrentHashMap<>();

    protected final Set<String>          sqlKeyWords              = new HashSet<>();
    protected final Set<String>          pseudoColumns            = new HashSet<>(5);
    protected final Set<String>          systemSchemas            = new HashSet<>(5);
    protected final Set<String>          dummyTables              = new HashSet<>(5);
    protected final DbColumnTypes        columnTypes              = new DbColumnTypes();
    protected final Set<DbCascadeAction> supportedOnDeleteActions = new HashSet<>(5);
    protected final Set<String>          disconnectSqlStates      = new HashSet<>(10);

    protected GenericDb         db;
    protected GenericDbMetadata metadata;
    protected String            statementDelimiter = ";";

    protected GenericDbDialect() {

    }

    @Override
    public void setDb(Db db) {
        Assert.isTrue(this.db == null);
        this.db = (GenericDb) db;
        this.metadata = (GenericDbMetadata) db.getMetadata();
        this.log = this.db.getLog(this.getClass());
        this.registerMetadata(metadata);
    }

    public String getDefaultSchemaName(Connection connection, DatabaseMetaData dm) throws SQLException {
        return Strings.upperCase(dm.getUserName());
    }

    @Override
    public DbCascadeAction getForeignKeyDefaultOnUpdate() {
        return DbCascadeAction.RESTRICT;
    }

    @Override
    public DbCascadeAction getForeignKeyDefaultOnDelete() {
        return DbCascadeAction.RESTRICT;
    }

    @Override
    public boolean isKeyword(String word) {
        return null == word ? false : sqlKeyWords.contains(word.toUpperCase());
    }

    @Override
    public boolean isSystemSchema(String schemaName) {
        return null == schemaName ? false : systemSchemas.contains(schemaName.toUpperCase());
    }

    @Override
    public boolean isAutoGenerateIndexForForeignKey() {
        return true;
    }

    @Override
    public boolean isAutoGenerateIndexForPrimaryKey() {
        return true;
    }

    public boolean supportsOnDeleteAction(DbCascadeAction action) {
        return null == action ? false : supportedOnDeleteActions.contains(action);
    }

    @Override
    public boolean supportsAutoIncrement() {
        return true;
    }

    @Override
    public boolean supportsSequence() {
        return false;
    }

    @Override
    public boolean supportsColumnComment() {
        return true;
    }

    @Override
    public boolean supportsRenameColumn() {
        return false;
    }

    @Override
    public String readDefaultValue(int typeCode, String nativeDefaultValue) {
        return nativeDefaultValue;
    }

    @Override
    public String quoteIdentifier(String identifier) {
        return quoteIdentifier(identifier, true);
    }

    @Override
    public String quoteIdentifier(String identifier, boolean quoteKeywordOnly) {
        if (null == identifier) {
            return null;
        }

        if (quoteKeywordOnly) {
            if (isKeyword(identifier) || shouldQuoteIdentifier(identifier)) {
                return doQuoteIdentifier(identifier);
            } else {
                return identifier;
            }
        } else {
            return doQuoteIdentifier(identifier);
        }
    }

    protected boolean shouldQuoteIdentifier(String word) {
        return false;
    }

    @Override
    public String qualifySchemaObjectName(String catalog, String schema, String name) {
        Args.notEmpty(name, "name");

        StringBuilder sb = new StringBuilder();

        if(isQualifyFullSchemaObjectName()) {
            if (!Strings.isEmpty(catalog)) {
                sb.append(quoteIdentifier(catalog)).append('.');

                if (Strings.isEmpty(schema)) {
                    throw new IllegalStateException("schema must not be empty if the catalog is not empty");
                }

                sb.append(quoteIdentifier(schema)).append('.');
            } else if (!Strings.isEmpty(schema)) {
                sb.append(quoteIdentifier(schema)).append('.');
            }
        }

        sb.append(quoteIdentifier(name));

        return sb.toString();
    }

    @Override
    public String qualifySchemaObjectName(DbSchemaObjectName schemaObjectName) {
        Args.notNull(schemaObjectName, "schema object name");

        String name = schemaObjectName.getName();
        if (schemaObjectName.isQuoted()) {
            name = quoteSchemaObjectName(name);
        }

        return qualifySchemaObjectName(schemaObjectName.getCatalog(), schemaObjectName.getSchema(), name);
    }

    protected boolean isQualifyFullSchemaObjectName() {
        return false;
    }

    @Override
    public String escape(String string) {
        if (null == string) {
            return null;
        }
        return Strings.replace(string, "'", "''");
    }

    @Override
    public String toDisplayString(int type, Object value) {
        if (null == value) {
            return "null";
        }

        if (type != JdbcTypes.UNKNOWN_TYPE_CODE) {

            if (type == Types.CLOB) {
                return "(clob)";
            }

            if (type == Types.BLOB) {
                return "(blob)";
            }

            JdbcType jdbcType = JdbcTypes.tryForTypeCode(type);

            if (null != jdbcType) {
                JdbcTypeKind kind = jdbcType.getKind();

                if (kind.isBinary()) {
                    return "(binary)";
                }

            }
        }

        Class<?> cls = Primitives.wrap(value.getClass());

        if (Number.class.isAssignableFrom(cls)) {
            return Converts.convert(value, BigDecimal.class).toPlainString();
        }

        if (Date.class.isAssignableFrom(cls)) {
            return Dates.format((Date) value);
        }

        if (Boolean.class.equals(cls)) {
            return ((Boolean) value) ? "1" : "0";
        }

        String s = Converts.toString(value);
        s = Strings.replace(s, "\r", "\\r");
        s = Strings.replace(s, "\n", "\\n");

        return "'" + s + "'";
    }

    @Override
    public String toSqlDefaultValue(int typeCode, Object value) {
        if (Types.BOOLEAN == typeCode || Types.BIT == typeCode) {
            Boolean bool = Converts.toBoolean(value);
            return bool ? getBooleanTrueString() : getBooleanFalseString();
        }

        JdbcType type = JdbcTypes.forTypeCode(typeCode);
        if (type.getKind().isNumeric()) {
            return Converts.toString(value);
        } else {
            String s = Converts.toString(value);
            if (s.endsWith(")")) {
                return s;
            } else if (s.startsWith("'") && s.endsWith("'")) {
                return s;
            } else if (typeCode == Types.VARCHAR || typeCode == Types.CHAR) {
                return "'" + Converts.toString(value) + "'";
            } else {
                return s;
            }
        }
    }

    protected String getBooleanTrueString() {
        return "1";
    }

    protected String getBooleanFalseString() {
        return "0";
    }

    @Override
    public String generatePrimaryKeyName(DbSchemaObjectName tableName, String... pkColumnNames) {
        return "PK_" + Strings.upperCase(tableName.getName());
    }

    @Override
    public String generateAlternativeKeyName(DbSchemaObjectName tableName, String... akColumnNames) {
        String name = "AK_" + Strings.left(Strings.upperCase(tableName.getName()), 8);

        for (String c : akColumnNames) {
            name = name + "_" + c;
        }

        return name;
    }

    @Override
    public List<String> getTruncateTableSqls(DbSchemaObjectName tableName) {
        return New.arrayList("TRUNCATE TABLE " + qualifySchemaObjectName(tableName));
    }

    @Override
    public List<String> getCreateTableSqls(DbTable table) {
        StringBuilder script = new StringBuilder();

        script.append("CREATE TABLE ")
                .append(qualifySchemaObjectName(table)).append(" ( \n");

        for (int i = 0; i < table.getColumns().length; i++) {
            DbColumn column = table.getColumn(i);

            if (i > 0) {
                script.append(",\n");
            }

            script.append("    ")
                    .append(getColumnDefinitionForCreateTable(column));
        }

        //PRIMARY KEY (col[,col])
        if (table.hasPrimaryKey()) {
            script.append(",\n    ").append(getPrimaryKeyDefinition(table.getPrimaryKeyColumnNames()));
        }

        script.append("\n)");

        List<String> sqls = New.arrayList(script.toString());

        if (supportsColumnComment() && !supportsColumnCommentInDefinition()) {
            for (DbColumn c : table.getColumns()) {
                if (!Strings.isEmpty(c.getComment())) {
                    sqls.addAll(getCommentOnColumnSqls(table, c.getName(), c.getComment()));
                }
            }
        }

        if (!supportsUniqueInColumnDefinition()) {
            for (DbColumn c : table.getColumns()) {
                if (c.isUnique()) {
                    sqls.addAll(getAlterColumnUniqueSqls(table, c.getName()));
                }
            }
        }

        return sqls;
    }

    @Override
    public List<String> getDropTableSqls(DbSchemaObjectName tableName) {
        return New.arrayList("DROP TABLE " + qualifySchemaObjectName(tableName));
    }

    @Override
    public List<String> getDropViewSqls(DbSchemaObjectName viewName) {
        return New.arrayList("DROP VIEW " + qualifySchemaObjectName(viewName));
    }

    @Override
    public List<String> getCreateColumnSqls(DbSchemaObjectName tableName, DbColumn column) {
        List<String> sqls =
                New.arrayList(getAddColumnSqlPrefix(tableName, column) + getColumnDefinitionForAlterTable(column));

        if (supportsColumnComment() && !supportsColumnCommentInDefinition()) {
            if (!Strings.isEmpty(column.getComment())) {
                sqls.addAll(getCommentOnColumnSqls(tableName, column.getName(), column.getComment()));
            }
        }

        return sqls;
    }

    protected String getAddColumnSqlPrefix(DbSchemaObjectName tableName, DbColumn column) {
        return "ALTER TABLE " + qualifySchemaObjectName(tableName) + " ADD COLUMN ";
    }

    @Override
    public List<String> getRenameColumnSqls(DbSchemaObjectName tableName, String columnName, String renameTo) throws IllegalStateException {
        throw new IllegalStateException("This dialect '" + db.getDescription() + "' not supports column renaming");
    }

    @Override
    public List<String> getAlterColumnUniqueSqls(DbSchemaObjectName tableName, String columnName) throws IllegalStateException {
        return New.arrayList("ALTER TABLE " + qualifySchemaObjectName(tableName) +
                " ADD UNIQUE(" + quoteIdentifier(columnName) + ")");
    }

    @Override
    public List<String> getDropColumnSqls(DbSchemaObjectName tableName, String columnName) {
        return New.arrayList("ALTER TABLE " + qualifySchemaObjectName(tableName) + " DROP COLUMN " + quoteIdentifier(columnName));
    }

    @Override
    public List<String> getCreatePrimaryKeySqls(DbSchemaObjectName tableName, DbPrimaryKey pk) {
        return New.arrayList("ALTER TABLE " + qualifySchemaObjectName(tableName) + " ADD PRIMARY KEY " + getColumnsString(pk.getColumnNames()));
    }

    @Override
    public List<String> getDropPrimaryKeySqls(DbSchemaObjectName tableName) {
        return New.arrayList("ALTER TABLE " + qualifySchemaObjectName(tableName) + " DROP PRIMARY KEY");
    }

    @Override
    public List<String> getCreateForeignKeySqls(DbSchemaObjectName tableName, DbForeignKey fk) {
        StringBuilder script = new StringBuilder();

        script.append("ALTER TABLE ").append(qualifySchemaObjectName(tableName)).append(" ADD ");

        if (!Strings.isEmpty(fk.getName())) {
            script.append("CONSTRAINT ").append(quoteIdentifier(fk.getName()));
        }

        script.append(" ").append(getForeignKeyDefinition(fk));

        return New.arrayList(script.toString());
    }

    @Override
    public List<String> getDropForeignKeySqls(DbSchemaObjectName tableName, String fkName) {
        return New.arrayList("ALTER TABLE " + qualifySchemaObjectName(tableName) + " DROP CONSTRAINT " + fkName);
    }

    @Override
    public List<String> getCreateIndexSqls(DbSchemaObjectName tableName, DbIndex index) {
        StringBuilder script = new StringBuilder();

        script.append("CREATE ");

        if (index.isUnique()) {
            script.append("UNIQUE ");
        }

        script.append("INDEX ").append(index.getName())
                .append(" ON ").append(qualifySchemaObjectName(tableName))
                .append("(");

        String[] columns = index.getColumnNames();
        for (int i = 0; i < columns.length; i++) {
            String column = columns[i];

            script.append(quoteIdentifier(column));

            if (i < columns.length - 1) {
                script.append(",");
            }
        }

        script.append(")");

        return New.arrayList(script.toString());
    }

    @Override
    public List<String> getDropIndexSqls(DbSchemaObjectName tableName, String ixName) {
        return New.arrayList("DROP INDEX " + qualifySchemaObjectName(tableName.getCatalog(), tableName.getSchema(), ixName));
    }

    @Override
    public List<String> getCommentOnTableSqls(DbSchemaObjectName tableName, String comment) {
        return New.arrayList("COMMENT ON TABLE " + qualifySchemaObjectName(tableName) + " IS '" + escape(comment) + "'");
    }

    public List<String> getCommentOnColumnSqls(DbSchemaObjectName tableName, String columnName, String comment) {
        return New.arrayList("COMMENT ON COLUMN " + qualifySchemaObjectName(tableName) + "." + quoteIdentifier(columnName) + " IS '" + escape(comment) + "'");
    }

    @Override
    public List<String> getCreateSequenceSqls(DbSequence sequence) throws IllegalStateException {
        throw notSupportsSequence();
    }

    @Override
    public List<String> getDropSequenceSqls(DbSchemaObjectName sequenceName) throws IllegalStateException {
        if (!supportsSequence()) {
            throw notSupportsSequence();
        } else {
            return New.arrayList("DROP SEQUENCE " + qualifySchemaObjectName(sequenceName));
        }
    }

    @Override
    public List<String> getDropSchemaSqls(DbSchema schema) {
        List<String> sqls = new ArrayList<String>();

        //drop sequences
        for (DbSequence seq : schema.getSequences()) {
            sqls.addAll(getDropSequenceSqls(seq));
        }

        //drop foreign keys
        for (DbTable table : schema.getTables()) {
            for (DbForeignKey fk : table.getForeignKeys()) {
                sqls.addAll(getDropForeignKeySqls(table, fk.getName()));
            }
        }

        //drop views
        for (DbTable table : schema.getTables()) {
            if (table.isView()) {
                sqls.addAll(getDropViewSqls(table));
            }
        }

        //drop tables
        for (DbTable table : schema.getTables()) {
            if (!table.isView()) {
                sqls.addAll(getDropTableSqls(table));
            }
        }

        return sqls;
    }

    @Override
    public String getNextSequenceValueSqlString(String sequenceName) throws IllegalStateException {
        throw notSupportsSequence();
    }

    @Override
    public String getSelectNextSequenceValueSql(String sequenceName) throws IllegalStateException {
        throw notSupportsSequence();
    }

    @Override
    public String getSelectCurrentSequenceValueSql(String sequenceName) throws IllegalStateException {
        if (!supportsCurrentSequenceValue()) {
            throw new IllegalStateException("This dialect '" + db.getDescription() + "' not supports 'current sequence value'");
        } else {
            throw new IllegalStateException("This dialect '" + db.getDescription() + "' not implements 'current sequence value'");
        }
    }

    @Override
    public List<DbCommand> getSchemaChangeCommands(SchemaChange change, SchemaChangeContext context) {
        List<DbCommand> commands = new ArrayList<>();

        Method method = schemaChangeMethods.get(change.getClass());
        if (null == method) {
            method = Reflection.findMethod(this.getClass(),
                    "createSchemaChangeCommands",
                    SchemaChangeContext.class, change.getClass(), List.class);

            if (null == method) {
                throw new UnsupportedChangeException("Unsupported change '" + change.getClass().getSimpleName() + "'", change);
            }

            schemaChangeMethods.put(change.getClass(), method);
        }

        Reflection.invokeMethod(method, this, context, change, commands);

        return commands;
    }

    protected void createSchemaChangeCommands(SchemaChangeContext context, AddTableChange change, List<DbCommand> commands) {
        commands.add(db.cmdCreateTable(change.getNewTable()).setCreateForeignKey(false).setCreateIndex(false));

        for (DbForeignKey fk : change.getNewTable().getForeignKeys()) {
            commands.add(db.cmdCreateForeignKey(change.getNewTable(), fk));
        }

        for (DbIndex ix : change.getNewTable().getIndexes()) {
            commands.add(db.cmdCreateIndex(change.getNewTable(), ix));
        }
    }

    protected void createSchemaChangeCommands(SchemaChangeContext context, RemoveTableChange change, List<DbCommand> commands) {
        for (DbForeignKey fk : change.getOldTable().getForeignKeys()) {
            commands.add(db.cmdDropForeignKey(change.getOldTable(), fk.getName()));
        }

        for (DbIndex ix : change.getOldTable().getIndexes()) {
            commands.add(db.cmdDropIndex(change.getOldTable(), ix.getName()));
        }

        commands.add(db.cmdDropTable(change.getOldTable()));
    }

    protected void createSchemaChangeCommands(SchemaChangeContext context, AddColumnChange change, List<DbCommand> commands) {
        commands.add(db.cmdCreateColumn(change.getTable(), change.getNewColumn()));
    }

    protected void createSchemaChangeCommands(SchemaChangeContext context, RemoveColumnChange change, List<DbCommand> commands) {
        commands.add(db.cmdDropColumn(change.getTable(), change.getOldColumn().getName()));
    }

    protected void createSchemaChangeCommands(SchemaChangeContext context, AddPrimaryKeyChange change, List<DbCommand> commands) {
        commands.add(db.cmdCreatePrimaryKey(change.getTable(), change.getNewPrimaryKey()));
    }

    protected void createSchemaChangeCommands(SchemaChangeContext context, RemovePrimaryKeyChange change, List<DbCommand> commands) {
        commands.add(db.cmdDropPrimaryKey(change.getTable()));
    }

    protected void createSchemaChangeCommands(SchemaChangeContext context, AddForeignKeyChange change, List<DbCommand> commands) {
        commands.add(db.cmdCreateForeignKey(change.getTable(), change.getNewForeignKey()));
    }

    protected void createSchemaChangeCommands(SchemaChangeContext context, RemoveForeignKeyChange change, List<DbCommand> commands) {
        commands.add(db.cmdDropForeignKey(change.getTable(), change.getOldForeignKey().getName()));
    }

    protected void createSchemaChangeCommands(SchemaChangeContext context, AddIndexChange change, List<DbCommand> commands) {
        commands.add(db.cmdCreateIndex(change.getTable(), change.getNewIndex()));
    }

    protected void createSchemaChangeCommands(SchemaChangeContext context, RemoveIndexChange change, List<DbCommand> commands) {
        commands.add(db.cmdDropIndex(change.getTable(), change.getOldIndex().getName()));
    }

    protected void createSchemaChangeCommands(SchemaChangeContext context, AddSequenceChange change, List<DbCommand> commands) {
        commands.add(db.cmdCreateSequence(change.getNewSequence()));
    }

    protected void createSchemaChangeCommands(SchemaChangeContext context, RemoveSequenceChange change, List<DbCommand> commands) {
        commands.add(db.cmdDropSequence(change.getOldSequence()));
    }

    protected void createSchemaChangeCommands(SchemaChangeContext context, ColumnDefinitionChange change, List<DbCommand> commands) {
        List<String> safeAlterColumnSqls = tryGetSafeAlterColumnSqlsForChange(context, change);
        if (null != safeAlterColumnSqls && safeAlterColumnSqls.size() > 0) {
            commands.add(new GenericDbCommands.GenericSqlCommand(db, safeAlterColumnSqls));
        } else {
            commands.add(db.cmdDropColumn(change.getTable(), change.getOldColumn().getName()));
            commands.add(db.cmdCreateColumn(change.getTable(), change.getNewColumn()));
        }
    }

    protected void createSchemaChangeCommands(SchemaChangeContext context, ForeignKeyDefinitionChange change, List<DbCommand> commands) {
        //Recreate foreign key
        commands.add(db.cmdDropForeignKey(change.getTable(), change.getOldForeignKey().getName()));
        commands.add(db.cmdCreateForeignKey(change.getTable(), change.getNewForeignKey()));
    }

    protected void createSchemaChangeCommands(SchemaChangeContext context, IndexDefinitionChange change, List<DbCommand> commands) {
        //Recreate index
        commands.add(db.cmdDropIndex(change.getTable(), change.getOldIndex().getName()));
        commands.add(db.cmdCreateIndex(change.getTable(), change.getNewIndex()));
    }

    protected List<String> tryGetSafeAlterColumnSqlsForChange(SchemaChangeContext context, ColumnDefinitionChange change) {
        if (!canSafeAlterColumnForChange(context, change)) {
            return null;
        } else {
            return createSafeAlterColumnSqlsForChange(context, change);
        }
    }

    protected List<String> createSafeAlterColumnSqlsForChange(SchemaChangeContext context, ColumnDefinitionChange change) {
        throw new IllegalStateException("Safe alter column not implemented by this dialect '" + db.getDescription() + "'");
    }

    protected boolean canSafeAlterColumnForChange(SchemaChangeContext context, ColumnDefinitionChange change) {
        if (change.isTypeChanged()) {
            return false;
        }

        DbColumn o = change.getOldColumn();
        DbColumn n = change.getNewColumn();

        if (change.isSizeChanged()) {
            if (n.getLength() < o.getLength()) {
                return false;
            }

            if (n.getPrecision() < o.getPrecision() || n.getScale() < o.getScale()) {
                return false;
            }
        }

        //null -> not null may be failed
        if (change.isNullableChanged()) {
            if (!n.isNullable() && o.isNullable() && !context.isEmptyTable(change.getTable())) {
                return false;
            }
        }

        //non unique -> unique may be failed.
        if (change.isUniqueChanged()) {
            if (n.isUnique() && !o.isUnique() && !context.isEmptyTable(change.getTable())) {
                return false;
            }
        }

        return true;
    }

    @Override
    public PreparedStatementHandler<Db> getAutoIncrementIdHandler(Consumer<Object> generatedIdCallback) {
        if (!supportsAutoIncrement()) {
            throw notSupportsSequence();
        }
        return new GenericDbPreparedStatementHandlers.AutoIncrementIdHandler(generatedIdCallback);
    }

    @Override
    public PreparedStatementHandler<Db> getInsertedSequenceValueHandler(String sequenceName, Consumer<Object> generatedIdCallback) throws IllegalStateException {
        if (!supportsSequence()) {
            throw notSupportsSequence();
        }
        return new GenericDbPreparedStatementHandlers.InsertedSequenceValueHandler(sequenceName, generatedIdCallback);
    }

    protected IllegalStateException notSupportsSequence() {
        if (!supportsSequence()) {
            return new IllegalStateException("This dialect '" + db.getDescription() + "' not supports sequence");
        } else {
            return new IllegalStateException("This dialect '" + db.getDescription() + "' not implements sequence");
        }
    }

    @Override
    public PreparedStatement createPreparedStatement(Connection connection, String sql) throws SQLException {
        return connection.prepareStatement(sql);
    }

    @Override
    public PreparedStatement createPreparedStatement(Connection connection, String sql, int autoGeneratedKeys) throws SQLException {
        return connection.prepareStatement(sql, autoGeneratedKeys);
    }

    @Override
    public String getLimitQuerySql(DbLimitQuery query) {
        throw new UnsupportedOperationException("This dialect '" + db.getDescription() + "' not implements page query");
    }

    @Override
    public String addOrderBy(String sql, String orderBy) {
        int havingIndex = Strings.lastIndexOfIgnoreCase(sql, " having ");

        if (havingIndex > 0) {
            return sql.substring(0, havingIndex) + " " + orderBy + sql.substring(havingIndex);
        } else {
            return sql + " " + orderBy;
        }
    }

    @Override
    public int setParameter(PreparedStatement ps, int index, Object value) throws SQLException {
        return setParameter(ps, index, value, JdbcTypes.UNKNOWN_TYPE_CODE);
    }

    @Override
    public int setParameter(PreparedStatement ps, int index, Object value, int type) throws SQLException {
        if (null == value || Null.is(value)) {
            return setNullParameter(ps, index, type);
        } else {
            setNonNullParameter(ps, index, value, type);
            return type;
        }
    }

    @Override
    public Object getColumnValue(ResultSet rs, int index) throws SQLException {
        return getColumnValue(rs, index, JdbcTypes.UNKNOWN_TYPE_CODE);
    }

    @Override
    public Object getColumnValue(ResultSet rs, String name) throws SQLException {
        return getColumnValue(rs, name, JdbcTypes.UNKNOWN_TYPE_CODE);
    }

    @Override
    public Object getColumnValue(ResultSet rs, int index, int type) throws SQLException {
        if (type == JdbcTypes.UNKNOWN_TYPE_CODE) {
            return getColumnValueTypeUnknown(rs, index);
        } else {
            return getColumnValueTypeKnown(rs, index, type);
        }
    }

    @Override
    public Object getColumnValue(ResultSet rs, String name, int type) throws SQLException {
        if (type == JdbcTypes.UNKNOWN_TYPE_CODE) {
            return getColumnValueTypeUnknown(rs, name);
        } else {
            return getColumnValueTypeKnown(rs, name, type);
        }
    }

    @Override
    public <T> T getColumnValue(ResultSet rs, int index, Class<T> targetType) throws SQLException {
        Object value = getColumnValueTypeUnknown(rs, index);
        if (null == value) {
            return null;
        } else {
            return Converts.convert(value, targetType);
        }
    }

    @Override
    public <T> T getColumnValue(ResultSet rs, String name, Class<T> targetType) throws SQLException {
        Object value = getColumnValueTypeUnknown(rs, name);
        if (null == value) {
            return null;
        } else {
            return Converts.convert(value, targetType);
        }
    }

    @Override
    public List<String> splitSqlStatements(String sqlScript) {
        if (Strings.isEmpty(sqlScript)) {
            return Collections.emptyList();
        }

        List<String> sqls = new ArrayList<>();

        String         defaultDelimiter = getStatementDelimiter();
        BufferedReader reader           = new BufferedReader(new StringReader(sqlScript));

        try {
            boolean eof       = false;
            boolean inBlock   = false;
            String  delimiter = defaultDelimiter;

            StringBuffer sql = new StringBuffer();
            while (!eof) {
                String line = reader.readLine();
                if (null == line) {
                    eof = true;

                    //eof
                    if (sql.length() > 0) {
                        Collections2.addIfNotEmpty(sqls, sql.toString(), true);
                    }

                    break;
                }

                //delimiter
                String newDelimiter = parseDelimiter(reader, delimiter, line);
                if (null != newDelimiter) {
                    delimiter = newDelimiter;
                    continue;
                }

                //line comment
                if (Strings.startsWithIgnoreCase(line, getLineCommentString())) {
                    continue;
                }

                //block comment
                if (Strings.startsWithIgnoreCase(line, getBlockCommentStart())) {
                    for (; ; ) {
                        line = reader.readLine();
                        if (null == line) {
                            eof = true;

                            if (sql.length() > 0) {
                                Collections2.addIfNotEmpty(sqls, sql.toString(), true);
                            }

                            break;
                        }

                        if (Strings.endsWithIgnoreCase(line, getBlockCommentEnd())) {
                            break;
                        }
                    }

                    if (eof) {
                        break;
                    } else {
                        continue;
                    }
                }

                //in block.
                if (inBlock) {
                    if (line.endsWith("}}")) {
                        sql.append(Strings.removeEnd(line, "}}"));
                        sqls.add(sql.toString().trim());
                        sql.delete(0, sql.length());
                        inBlock = false;
                    } else {
                        sql.append(line).append('\n');
                    }
                    continue;
                }

                //not in block.
                if (line.startsWith("{{")) {
                    inBlock = true;
                    sql.append(Strings.removeStart(line, "{{")).append('\n');
                    continue;
                }

                //found a statement.
                if (line.endsWith(delimiter)) {
                    sql.append(line.substring(0, line.length() - delimiter.length()));
                    Collections2.addIfNotEmpty(sqls, sql.toString(), true);
                    sql.delete(0, sql.length());
                    continue;
                }

                sql.append(line).append('\n');
            }
        } catch (IOException e) {
            throw Exceptions.uncheck(e);
        }

        return sqls;
    }

    protected String parseDelimiter(BufferedReader reader, String delimiter, String line) {
        return null;
    }

    @Override
    public boolean isDisconnectSQLState(String state) {
        return null != state && disconnectSqlStates.contains(state);
    }

    public String getStatementDelimiter() {
        return statementDelimiter;
    }

    public String getLineCommentString() {
        return "--";
    }

    public String getBlockCommentStart() {
        return "/*";
    }

    public String getBlockCommentEnd() {
        return "*/";
    }

    protected Object getColumnValueTypeUnknown(ResultSet rs, int index) throws SQLException {
        return rs.getObject(index);
    }

    protected Object getColumnValueTypeUnknown(ResultSet rs, String name) throws SQLException {
        return rs.getObject(name);
    }

    protected Object getColumnValueTypeKnown(ResultSet rs, int index, int type) throws SQLException {
        /*
            from jdk :

            The method recommended for retrieving BINARY and VARBINARY values is ResultSet.getBytes.
            If a column of type JDBC LONGVARBINARY stores a byte array that is many megabytes long, however,
            the method getBinaryStream is recommended.
         */
        if (type == Types.BINARY || type == Types.VARBINARY) {
            return rs.getBytes(index);
        }

        if (type == Types.LONGVARBINARY) {
            return rs.getBinaryStream(index);
        }

        return rs.getObject(index);
    }

    protected Object getColumnValueTypeKnown(ResultSet rs, String name, int type) throws SQLException {
        if (type == Types.BINARY || type == Types.VARBINARY) {
            return rs.getBytes(name);
        }

        if (type == Types.LONGVARBINARY) {
            return rs.getBinaryStream(name);
        }

        return rs.getObject(name);
    }

    public int setNullParameter(PreparedStatement ps, int index, int type) throws SQLException {
        if (type == JdbcTypes.UNKNOWN_TYPE_CODE) {
            return setNullParameterTypeUnknow(ps, index);
        } else {
            ps.setNull(index, type);
            return type;
        }
    }

    protected boolean testDriverSupportsGetParameterType() {
        return db.executeWithResult(connection -> {
            PreparedStatement ps = null;
            try {
                ps = connection.prepareStatement(getTestDriverSupportsGetParameterTypeSQL());
                ps.getParameterMetaData().getParameterType(1);
                return true;
            } catch (SQLException e) {
                log.debug("JDBC 3.0 getParameterType call not supported, message : {}", e.getMessage());
                return false;
            } finally {
                JDBC.closeStatementOnly(ps);
            }
        });
    }

    protected int setNullParameterTypeUnknow(PreparedStatement ps, int index) throws SQLException {
        if (useSetObjectForNull()) {
            ps.setObject(index, null);
            return JdbcTypes.UNKNOWN_TYPE_CODE;
        } else if (metadata.driverSupportsGetParameterType()) {
            int type = ps.getParameterMetaData().getParameterType(index);
            ps.setNull(index, type);
            return type;
        } else {
            return nativeSetNullParameterTypeUnknow(ps, index);
            //			// JDBC driver not compliant with JDBC 3.0
            //			// -> proceed with database-specific checks
            //			try {
            //				DatabaseMetaData dbmd = ps.getConnection().getMetaData();
            //				String databaseProductName = dbmd.getDatabaseProductName();
            //				String jdbcDriverName = dbmd.getDriverName();
            //				if (databaseProductName.startsWith("Informix") ||
            //						jdbcDriverName.startsWith("Microsoft SQL Server")) {
            //					useSetObject = true;
            //				}
            //				else if (databaseProductName.startsWith("DB2") ||
            //						jdbcDriverName.startsWith("jConnect") ||
            //						jdbcDriverName.startsWith("SQLServer")||
            //						jdbcDriverName.startsWith("Apache Derby")) {
            //					sqlType = Types.VARCHAR;
            //				}
            //			}
            //			catch (Throwable ex2) {
            //				logger.debug("Could not check database or driver name", ex2);
            //			}
        }
    }

    protected boolean useSetObjectForNull() {
        return false;
    }

    protected int nativeSetNullParameterTypeUnknow(PreparedStatement ps, int index) throws SQLException {
        ps.setNull(index, Types.NULL);
        return Types.NULL;
    }

    @SuppressWarnings("rawtypes")
    protected void setNonNullParameter(PreparedStatement ps, int index, Object value, int type) throws SQLException {
        Class<?> valueType = Primitives.wrap(value.getClass());

        if (valueType.isEnum()) {
            value = Enums.getValue((Enum) value);
        }

        if (type == Types.VARCHAR || type == Types.LONGVARCHAR || (type == Types.CLOB && CharSequence.class.isAssignableFrom(valueType))) {
            ps.setString(index, value.toString());
        } else if (type == Types.DECIMAL || type == Types.NUMERIC) {
            if (value instanceof BigDecimal) {
                ps.setBigDecimal(index, (BigDecimal) value);
            } else {
                setObject(ps, index, value, type);
            }
        } else if (type == Types.DATE) {
            if (value instanceof java.util.Date) {
                if (value instanceof java.sql.Date) {
                    ps.setDate(index, (java.sql.Date) value);
                } else {
                    ps.setDate(index, new java.sql.Date(((java.util.Date) value).getTime()));
                }
            } else if (value instanceof Calendar) {
                Calendar cal = (Calendar) value;
                ps.setDate(index, new java.sql.Date(cal.getTime().getTime()), cal);
            } else {
                setObject(ps, index, value, type);
            }
        } else if (type == Types.TIME) {
            if (value instanceof java.util.Date) {
                if (value instanceof java.sql.Time) {
                    ps.setTime(index, (java.sql.Time) value);
                } else {
                    ps.setTime(index, new java.sql.Time(((java.util.Date) value).getTime()));
                }
            } else if (value instanceof Calendar) {
                Calendar cal = (Calendar) value;
                ps.setTime(index, new java.sql.Time(cal.getTime().getTime()), cal);
            } else {
                setObject(ps, index, value, type);
            }
        } else if (type == Types.TIMESTAMP) {
            if (value instanceof java.util.Date) {
                if (value instanceof java.sql.Timestamp) {
                    ps.setTimestamp(index, (java.sql.Timestamp) value);
                } else {
                    ps.setTimestamp(index, new java.sql.Timestamp(((java.util.Date) value).getTime()));
                }
            } else if (value instanceof Calendar) {
                Calendar cal = (Calendar) value;
                ps.setTimestamp(index, new java.sql.Timestamp(cal.getTime().getTime()), cal);
            } else {
                setObject(ps, index, value, type);
            }
        } else if (type == JdbcTypes.UNKNOWN_TYPE_CODE) {
            if (CharSequence.class.isAssignableFrom(valueType) || Character.class.equals(valueType)) {
                ps.setString(index, value.toString());
            } else if (isDateValue(value.getClass())) {
                ps.setTimestamp(index, new java.sql.Timestamp(((java.util.Date) value).getTime()));
            } else if (value instanceof Calendar) {
                Calendar cal = (Calendar) value;
                ps.setTimestamp(index, new java.sql.Timestamp(cal.getTime().getTime()), cal);
            } else {
                // Fall back to generic setObject call without SQL type specified.
                setObject(ps, index, value);
            }
        } else {
            // Fall back to generic setObject call with SQL type specified.
            setObject(ps, index, value, type);
        }
    }

    protected void setObject(PreparedStatement ps, int index, Object value) throws SQLException {
        ps.setObject(index, value);
    }

    protected void setObject(PreparedStatement ps, int index, Object value, int type) throws SQLException {
        ps.setObject(index, value, type);
    }

    protected String getColumnDefinitionForCreateTable(DbColumn column) {
        StringBuilder definition = new StringBuilder();

        definition.append(quoteIdentifier(column.getName()));

        defColumnAfterName(column, definition);

        return definition.toString();
    }

    protected void defColumnAfterName(DbColumn column, StringBuilder definition) {
        defColumnType(column, definition);
        defColumnNullAndDefault(column, definition);
        defColumnUnique(column, definition);
        defColumnAutoIncrement(column, definition);
        defColumnComment(column, definition);
    }

    protected void defColumnType(DbColumn column, StringBuilder definition) {
        definition.append(' ')
                .append(column.isAutoIncrement() ? getAutoIncrementColumnTypeDefinition(column) : getColumnTypeDefinition(column));
    }

    protected void defColumnNullAndDefault(DbColumn column, StringBuilder definition) {
        if (isDefaultBeforeNullInColumnDefinition()) {
            //default
            String defaults = getColumnDefaultDefinition(column);
            if (!Strings.isEmpty(defaults)) {
                definition.append(' ').append(defaults);
            }

            //null
            String nullDefinition = getColumnNullableDefinition(column);
            if (!Strings.isEmpty(nullDefinition)) {
                definition.append(" ").append(nullDefinition);
            }
        } else {
            //null
            String nullDefinition = getColumnNullableDefinition(column);
            if (!Strings.isEmpty(nullDefinition)) {
                definition.append(" ").append(nullDefinition);
            }

            //default
            String defaults = getColumnDefaultDefinition(column);
            if (!Strings.isEmpty(defaults)) {
                definition.append(' ').append(defaults);
            }
        }
    }

    protected void defColumnUnique(DbColumn column, StringBuilder definition) {
        if (column.isUnique() && supportsUniqueInColumnDefinition()) {
            definition.append(' ').append(getColumnUniqueDefinition(column));
        }
    }

    protected void defColumnAutoIncrement(DbColumn column, StringBuilder definition) {
        if (column.isAutoIncrement()) {
            if (supportsAutoIncrement()) {
                definition.append(' ').append(getAutoIncrementColumnDefinitionEnd(column));
            } else {
                log.warn("Unsupported auto increment column in " + db.getPlatform().getName());
            }
        }
    }

    protected void defColumnComment(DbColumn column, StringBuilder definition) {
        if (supportsColumnCommentInDefinition() && !Strings.isEmpty(column.getComment())) {
            definition.append(' ').append(getColumnCommentDefinition(column));
        }
    }

    protected String getColumnDefinitionForAlterTable(DbColumn column) {
        return getColumnDefinitionForCreateTable(column);
    }

    protected String getColumnTypeDefinition(DbColumn column) {
        if (column.isDatetime()) {
            String datetimeDef = getColumnDatetimeDef(column);
            if (!Strings.isEmpty(datetimeDef)) {
                return datetimeDef;
            }
        }
        return getColumnTypeDefinition(column, getColumnType(column));
    }

    protected String getColumnDatetimeDef(DbColumn column) {
        return null;
    }

    protected String getColumnTypeDefinition(DbColumn column, DbColumnType type) {
        String dataType = type.getTypeDef();

        dataType = Strings.replaceOnce(dataType, "$l", column.getLength() < 0 ? "" : String.valueOf(column.getLength()));
        dataType = Strings.replaceOnce(dataType, "$p", column.getPrecision() < 0 ? "" : String.valueOf(column.getPrecision()));
        dataType = Strings.replaceOnce(dataType, "$s", column.getScale() < 0 ? "" : String.valueOf(column.getScale()));

        return dataType;
    }

    protected DbColumnType getColumnType(DbColumn column) {
        JdbcType jdbcType = JdbcTypes.forTypeCode(column.getTypeCode());

        DbColumnType columnType = columnTypes.get(jdbcType.getCode(), column.getLength());

        if (null == columnType) {
            throw new DbException(Strings.format(
                    "Unsupported column type '{0}' defined in column '{1}'", jdbcType.getName(), column.getName()));
        }

        if (!columnType.matchesLength(column.getLength())) {
            throw new DbException("Length must in range " + columnType.getRangeString() + " in column '" + column.getName() + "'");
        }

        return columnType;
    }

    protected String getColumnDefaultDefinition(DbColumn column) {
        if (Strings.isEmpty(column.getDefaultValue())) {
            return "";
        } else {
            return "DEFAULT " + toSqlDefaultValue(column.getTypeCode(), column.getDefaultValue());
        }
    }

    protected String getColumnNullableDefinition(DbColumn column) {
        if (column.isNullable()) {
            return "NULL";
        } else {
            return "NOT NULL";
        }
    }

    protected String getColumnUniqueDefinition(DbColumn column) {
        if (column.isUnique()) {
            return "UNIQUE";
        } else {
            return "";
        }
    }

    protected String getColumnCommentDefinition(DbColumn column) {
        if (Strings.isEmpty(column.getComment())) {
            return "";
        } else {
            return "COMMENT '" + escape(column.getComment()) + "'";
        }
    }

    protected String getAutoIncrementColumnTypeDefinition(DbColumn column) {
        return getColumnTypeDefinition(column);
    }

    protected String getAutoIncrementColumnDefinitionEnd(DbColumn column) {
        throw new IllegalStateException("This dialect '" + db.getDescription() + "' not implements auto increment column's definition");
    }

    protected boolean isDefaultBeforeNullInColumnDefinition() {
        return true;
    }

    protected boolean supportsUniqueInColumnDefinition() {
        return true;
    }

    protected boolean supportsColumnCommentInDefinition() {
        return supportsColumnComment();
    }

    protected String getPrimaryKeyDefinition(String[] pkcolumns) {
        return "PRIMARY KEY " + getColumnsString(pkcolumns);
    }

    protected String getColumnsString(String... columns) {
        StringBuilder sb = new StringBuilder();

        sb.append("(");

        for (int i = 0; i < columns.length; i++) {
            String column = columns[i];

            if (i > 0) {
                sb.append(", ");
            }

            sb.append(quoteIdentifier(column));
        }

        sb.append(')');

        return sb.toString();
    }

    protected String getForeignKeyDefinition(DbForeignKey fk) {
        StringBuilder definition = new StringBuilder();

        definition.append("FOREIGN KEY (");

        DbForeignKeyColumn[] columns = fk.getColumns();
        for (int i = 0; i < columns.length; i++) {
            DbForeignKeyColumn column = columns[i];

            definition.append(quoteIdentifier(column.getLocalColumnName()));

            if (i < columns.length - 1) {
                definition.append(",");
            }
        }

        definition.append(") REFERENCES ")
                .append(qualifySchemaObjectName(fk.getForeignTable()))
                .append(" (");

        for (int i = 0; i < columns.length; i++) {
            DbForeignKeyColumn column = columns[i];

            definition.append(quoteIdentifier(column.getForeignColumnName()));

            if (i < columns.length - 1) {
                definition.append(",");
            }
        }

        definition.append(")");

        DbCascadeAction onDeleteAction = fk.getOnDelete();
        if (null != onDeleteAction && !onDeleteAction.equals(DbCascadeAction.NONE) && supportsOnDeleteAction(onDeleteAction)) {
            definition.append(" on delete ");

            if (DbCascadeAction.CASCADE.equals(onDeleteAction)) {
                definition.append("cascade");
            } else if (DbCascadeAction.SET_NULL.equals(onDeleteAction)) {
                definition.append("set null");
            } else if (DbCascadeAction.SET_DEFAULT.equals(onDeleteAction)) {
                definition.append("set default");
            } else if (DbCascadeAction.RESTRICT.equals(onDeleteAction)) {
                definition.append("restrict");
            }
        }

        return definition.toString();
    }

    protected String quoteSchemaObjectName(String name) {
        return getIdentifierQuoteString() + name + getIdentifierQuoteString();
    }

    protected String doQuoteIdentifier(String identifier) {
        return getOpenQuoteString() + caseQuotedIdentifier(identifier) + getCloseQuoteString();
    }

    protected String caseQuotedIdentifier(String identifier) {
        return identifier;
    }

    protected String getIdentifierQuoteString() {
        return metadata.getIdentifierQuoteString();
    }

    protected void registerMetadata(DbMetadata metadata) {
        this.registerSQLKeyWords();
        this.registerSystemSchemas();
        this.registerColumnTypes();
        this.registerSupportedOnDeleteActions();
        this.registerDisconnectSqlStates();
    }

    /**
     * the registered keywords must be upper case
     */
    protected void registerSQLKeyWords() {
        this.sqlKeyWords.addAll(Arrays.asList(metadata.getSQLKeywords()));
        this.sqlKeyWords.addAll(Arrays.asList(SQL92_RESERVED_WORDS));
        this.sqlKeyWords.addAll(Arrays.asList(SQL99_RESERVED_WORDS));
        this.sqlKeyWords.addAll(Arrays.asList(SQL2003_RESERVED_WORDS));
    }

    /**
     * the registered schema name must be upper case.
     */
    protected void registerSystemSchemas() {

    }

    protected void registerDisconnectSqlStates() {
        //see http://pubs.opengroup.org/onlinepubs/9695959099/toc.pdf
    	/*
    	’01002’ — Disconnect error
    	
		’08000’ Connection exception
		’08001’ — Client unable to establish connection 5.7.6
		’08002’ — Connection name in use. 5.7.3,6
		’08003’ — Connection does not exist. 5.2.6, 5.7.3,5,7,8
		’08004’ — Server rejected the connection. 5.7.6
		’08006’ — Connection failure. 5.7.5,8
		’08007’ — Transaction resolution unknown. 5.6.2, 5.7.5    	
    	*/
        disconnectSqlStates.add("01002");
        disconnectSqlStates.add("08000");
        disconnectSqlStates.add("08001");
        disconnectSqlStates.add("08002");
        disconnectSqlStates.add("08003");
        disconnectSqlStates.add("08004");
        disconnectSqlStates.add("08006");
        disconnectSqlStates.add("08007");
    }

    protected void registerSupportedOnDeleteActions() {
        setSupportedOnDeleteActions(DbCascadeAction.values());
    }

    protected void setSupportedOnDeleteActions(DbCascadeAction... actions) {
        supportedOnDeleteActions.clear();
        Collections2.addAll(supportedOnDeleteActions, actions);
    }

    protected static boolean isDateValue(Class<?> inValueType) {
        return (java.util.Date.class.isAssignableFrom(inValueType) &&
                !(java.sql.Date.class.isAssignableFrom(inValueType) ||
                        java.sql.Time.class.isAssignableFrom(inValueType) ||
                        java.sql.Timestamp.class.isAssignableFrom(inValueType)));
    }

    protected abstract String getTestDriverSupportsGetParameterTypeSQL();

    protected abstract String getOpenQuoteString();

    protected abstract String getCloseQuoteString();

    protected abstract void registerColumnTypes();
}

/*
 * Copyright 2017 the original author or authors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *       http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package leap.db.platform.sqlserver;

import leap.db.Db;
import leap.db.DbLimitQuery;
import leap.db.change.ColumnDefinitionChange;
import leap.db.change.SchemaChangeContext;
import leap.db.model.DbColumn;
import leap.db.model.DbColumnBuilder;
import leap.db.model.DbSchemaObjectName;
import leap.db.platform.GenericDbDialect;
import leap.lang.New;
import leap.lang.Strings;
import leap.lang.value.Limit;

import java.io.ByteArrayInputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 12 means 2012
 */
public class SqlServer12Dialect extends GenericDbDialect {

    @Override
    protected String getConfigName(Db db) {
        return "sqlserver";
    }

    @Override
    protected String getTestDriverSupportsGetParameterTypeSQL() {
        return "select 1";
    }

    @Override
    protected String getOpenQuoteString() {
        return "\"";
    }

    @Override
    protected String getCloseQuoteString() {
        return "\"";
    }

    @Override
    protected String getAutoIncrementColumnDefinitionEnd(DbColumn column) {
        return "IDENTITY";
    }

    @Override
    public boolean supportsColumnComment() {
        return false;
    }

    @Override
    protected boolean supportsUniqueInColumnDefinition() {
        return false;
    }

    @Override
    public boolean useTableAliasAfterDelete() {
        return true;
    }

    @Override
    public boolean useTableAliasAfterUpdate() {
        return true;
    }

    @Override
    protected boolean testDriverSupportsGetParameterType() {
        return true;
    }

    @Override
    protected String getAddColumnSqlPrefix(DbSchemaObjectName tableName, DbColumn column) {
        return "ALTER TABLE " + qualifySchemaObjectName(tableName) + " ADD ";
    }

    @Override
    protected List<String> createSafeAlterColumnSqlsForChange(SchemaChangeContext context,
                                                              ColumnDefinitionChange change) {
        List<String> sqls = new ArrayList<String>();

        if(change.isUniqueChanged()){
            sqls.addAll(getAlterColumnUniqueSqls(change.getTable(), change.getOldColumn().getName()));

            if(change.getPropertyChanges().size() > 1){
                DbColumn c = new DbColumnBuilder(change.getNewColumn()).setUnique(false).build();
                sqls.add(getAlterColumnSql(change.getTable(), c));
            }
        }else{
            sqls.add(getAlterColumnSql(change.getTable(), change.getNewColumn()));
        }

        return sqls;
    }

    @Override
    public List<String> getAlterColumnUniqueSqls(DbSchemaObjectName tableName, String columnName) throws IllegalStateException {
        return New.arrayList("ALTER TABLE " + qualifySchemaObjectName(tableName) +
                             " ADD CONSTRAINT " + generateAlternativeKeyName(tableName, columnName) +
                             " UNIQUE(" + quoteIdentifier(columnName) + ")");
    }

    protected String getAlterColumnSql(DbSchemaObjectName tableName,DbColumn column){
        return "ALTER TABLE " + qualifySchemaObjectName(tableName) +
                " MODIFY COLUMN " + getColumnDefinitionForAlterTable(column);
    }

    @Override
    public String getLimitQuerySql(DbLimitQuery query) {
        //sql server 2012
        //https://msdn.microsoft.com/en-us/library/ms188385(v=SQL.110).aspx

        //OFFSET { integer_constant | offset_row_count_expression } { ROW | ROWS }
        //FETCH { FIRST | NEXT } { integer_constant | fetch_row_count_expression } { ROW | ROWS } ONLY

        Limit limit = query.getLimit();

        int offset = limit.getStart() - 1;
        int rows   = limit.getEnd()   - offset;

        String sql = query.getSql(db);

        if(Strings.isEmpty(query.getOrderBy())) {
            sql += " order by 1";
        }

        sql += " offset ? rows fetch next ? rows only";

        query.getArgs().add(offset);
        query.getArgs().add(rows);

        return sql;
    }

    @Override
    protected void registerColumnTypes() {
        //https://docs.microsoft.com/en-us/sql/connect/jdbc/using-basic-data-types

        columnTypes.add(Types.BOOLEAN,       "bit");
        columnTypes.add(Types.BIT,           "bit");

        columnTypes.add(Types.TINYINT,       "tinyint");
        columnTypes.add(Types.SMALLINT,      "smallint");
        columnTypes.add(Types.INTEGER,       "int");
        columnTypes.add(Types.BIGINT,        "bigint"  );

        //JDBC's real type mapping to java's float, JDBC's float type mapping to java's double
        columnTypes.add(Types.REAL,          "real");
        columnTypes.add(Types.FLOAT,         "float");
        columnTypes.add(Types.DOUBLE,        "float");

        columnTypes.add(Types.DECIMAL,       "decimal($p,$s)");
        columnTypes.add(Types.NUMERIC,       "decimal($p,$s)");

        columnTypes.add(Types.CHAR,          "char($l)");
        columnTypes.add(Types.VARCHAR,       "varchar($l)");
        columnTypes.add(Types.LONGVARCHAR,   "text");

        columnTypes.add(Types.BINARY,        "binary");
        columnTypes.add(Types.VARBINARY,     "varbinary($l)");
        columnTypes.add(Types.LONGVARBINARY, "image");

        columnTypes.add(Types.DATE,          "date");
        columnTypes.add(Types.TIME,          "time");
        columnTypes.add(Types.TIMESTAMP,     "datetime");

        //https://docs.microsoft.com/en-us/sql/connect/jdbc/using-advanced-data-types#blob-and-clob-and-nclob-data-types
        columnTypes.add(Types.BLOB,          "varbinary(max");
        columnTypes.add(Types.CLOB,          "varchar(max)");
    }
}

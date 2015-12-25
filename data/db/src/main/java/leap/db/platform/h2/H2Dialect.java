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
package leap.db.platform.h2;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import leap.db.DbLimitQuery;
import leap.db.change.ColumnDefinitionChange;
import leap.db.change.SchemaChangeContext;
import leap.db.model.DbColumn;
import leap.db.model.DbColumnBuilder;
import leap.db.model.DbSchemaObjectName;
import leap.db.model.DbSequence;
import leap.db.platform.GenericDbDialect;
import leap.lang.Collections2;
import leap.lang.New;
import leap.lang.Strings;
import leap.lang.value.Limit;

public class H2Dialect extends GenericDbDialect {
	
	private static final String DEFAULT_SCHEMA = "PUBLIC";
	
	private static final String[] SYSTEM_SCHEMAS = new String[]{"INFORMATION_SCHEMA"};   

	protected H2Dialect() {
		
	}
	
	@Override
    public String getDefaultSchemaName(Connection connection, DatabaseMetaData dm) throws SQLException {
		return DEFAULT_SCHEMA;
    }
	
	@Override
    public boolean supportsSequence() {
	    return true;
    }
	
	@Override
    public boolean supportsRenameColumn() {
		return true;
	}

	@Override
    protected String getAutoIncrementColumnDefinitionEnd(DbColumn column) {
	    return "IDENTITY";
    }
	
	@Override
    protected String getTestDriverSupportsGetParameterTypeSQL() {
	    return "select 1 from dual where 1 = ?";
    }
	
	@Override
    public List<String> getRenameColumnSqls(DbSchemaObjectName tableName, String columnName, String renameTo) throws IllegalStateException {
		//ALTER TABLE TEST ALTER COLUMN NAME RENAME TO TEXT;
		return New.arrayList("ALTER TABLE " + qualifySchemaObjectName(tableName) + 
							 " ALTER COLUMN " + quoteIdentifier(columnName) + " RENAME TO " + quoteIdentifier(renameTo));
	}

	@Override
    public List<String> getCreateSequenceSqls(DbSequence sequence) throws IllegalStateException {
		//CREATE SEQUENCE [IF NOT EXISTS] newSequenceName [START WITH long] [INCREMENT BY long] [CACHE long]
		StringBuilder sb = new StringBuilder();
		
		sb.append("CREATE SEQUENCE ").append(qualifySchemaObjectName(sequence));
		
		if(null != sequence.getStart()){
			sb.append(" START WITH ").append(sequence.getStart());
		}
		
		if(null != sequence.getIncrement()){
			sb.append(" INCREMENT BY ").append(sequence.getIncrement());
		}
		
		if(null != sequence.getCache()){
			sb.append(" CACHE ").append(sequence.getCache());
		}
		
	    return New.arrayList(sb.toString());
    }

	@Override
    public List<String> getDropSequenceSqls(DbSchemaObjectName sequenceName) throws IllegalStateException {
	    return New.arrayList("DROP SEQUENCE IF EXISTS " + qualifySchemaObjectName(sequenceName));
    }
	
	@Override
    public String getNextSequenceValueSqlString(String sequenceName) throws IllegalStateException {
		return "nextval('" + quoteIdentifier(sequenceName) + "')";
	}
	
	@Override
    public String getSelectNextSequenceValueSql(String sequenceName) throws IllegalStateException {
		return "select " + getNextSequenceValueSqlString(sequenceName) + " from dual";
    }

	@Override
    public String getSelectCurrentSequenceValueSql(String sequenceName) throws IllegalStateException {
	    return "select currval('" + quoteIdentifier(sequenceName) + "') from dual";
    }
	
	@Override
    public String getLimitQuerySql(DbLimitQuery query) {
		Limit limit = query.getLimit();
		
		int offset = limit.getStart() - 1;
		int rows   = limit.getEnd()   - offset;
		
        String sql = query.getSql(db) + " limit ?,?";
        query.getArgs().add(offset);
        query.getArgs().add(rows);
		
		return sql;
    }

	@Override
    protected void registerSystemSchemas() {
		Collections2.addAll(systemSchemas, SYSTEM_SCHEMAS);
    }

	@Override
    protected void registerColumnTypes() {
        columnTypes.add(Types.BOOLEAN,       "boolean");
        columnTypes.add(Types.BIT,           "boolean");
        
        columnTypes.add(Types.SMALLINT,      "tinyint", 0, 0, Types.TINYINT);
        columnTypes.add(Types.TINYINT,       "tinyint");
        columnTypes.add(Types.SMALLINT,      "smallint");
        columnTypes.add(Types.INTEGER,       "integer");
        columnTypes.add(Types.BIGINT,        "bigint"  );

        //JDBC's real type mapping to java's float, JDBC's float type mapping to java's double
        columnTypes.add(Types.REAL,          "float");
        columnTypes.add(Types.FLOAT,         "double");
        columnTypes.add(Types.DOUBLE,        "double");
        
        columnTypes.add(Types.DECIMAL,       "decimal($p,$s)");
        columnTypes.add(Types.NUMERIC,       "decimal($p,$s)");
        
        columnTypes.add(Types.CHAR,          "char($l)");
        columnTypes.add(Types.VARCHAR,       "varchar($l)");        
        columnTypes.add(Types.LONGVARCHAR,   "longvarchar");

        columnTypes.add(Types.BINARY,        "binary");
        columnTypes.add(Types.VARBINARY,     "binary($l)");
        columnTypes.add(Types.LONGVARBINARY, "longvarbinary");
        
        columnTypes.add(Types.DATE,          "date");
        columnTypes.add(Types.TIME,          "time");
        columnTypes.add(Types.TIMESTAMP,     "timestamp");

        columnTypes.add(Types.BLOB,          "blob");
        columnTypes.add(Types.CLOB,          "clob");
    }
	
	@Override
    protected String getOpenQuoteString() {
	    return "`";
    }

	@Override
    protected String getCloseQuoteString() {
	    return "`";
    }
	
	@Override
    public List<String> getCreateColumnSqls(DbSchemaObjectName tableName, DbColumn column) {
		List<String> sqls = new ArrayList<>();
		
		sqls.add("ALTER TABLE " + qualifySchemaObjectName(tableName) + " ADD COLUMN " + getColumnDefinitionForAlterTable(column));
		
		if(column.isUnique()){
			sqls.add(getAddUniqueColumnSql(tableName, column.getName()));
		}
	
		return sqls;
	}

	@Override
    protected String getColumnDefinitionForAlterTable(DbColumn column) {
        StringBuilder definition = new StringBuilder();
        
        definition.append(quoteIdentifier(column.getName()))
                  .append(' ')
                  .append(column.isAutoIncrement() ? getAutoIncrementColumnTypeDefinition(column) : getColumnTypeDefinition(column));

        //default
        if(!Strings.isEmpty(column.getDefaultValue())){
        	definition.append(' ').append(getColumnDefaultDefinition(column));
        }
            
        //null
    	String nullDefinition = getColumnNullableDefinition(column);
    	if(!Strings.isEmpty(nullDefinition)){
    		definition.append(" ").append(nullDefinition);
    	}
    	
    	//comment
    	if(!Strings.isEmpty(column.getComment())){
    		definition.append(" COMMENT '").append(escape(column.getComment())).append("'");
    	}
    	
        return definition.toString();
	}
	
	@Override
    protected List<String> createSafeAlterColumnSqlsForChange(SchemaChangeContext context, ColumnDefinitionChange change) {
		List<String> sqls = new ArrayList<String>();
	
		if(change.isUniqueChanged()){
			sqls.add(getAddUniqueColumnSql(change.getTable(), change.getOldColumn().getName()));
			
			if(change.getPropertyChanges().size() > 1){
				DbColumn c = new DbColumnBuilder(change.getNewColumn()).setUnique(false).build();
				sqls.add(getAlterColumnSql(change.getTable(), c));
			}
		}else{
			sqls.add(getAlterColumnSql(change.getTable(), change.getNewColumn()));
		}

		return sqls;
	}
	
	protected String getAddUniqueColumnSql(DbSchemaObjectName tableName,String columnName) {
		return "ALTER TABLE " + qualifySchemaObjectName(tableName) + 
				 " ADD UNIQUE(" + quoteIdentifier(columnName) + ")";
	}

	protected String getAlterColumnSql(DbSchemaObjectName tableName,DbColumn column){
		return "ALTER TABLE " + qualifySchemaObjectName(tableName) + 
		 	 	 " ALTER COLUMN " + getColumnDefinitionForAlterTable(column);
		
	}
}
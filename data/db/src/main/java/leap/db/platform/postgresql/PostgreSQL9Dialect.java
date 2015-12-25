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
package leap.db.platform.postgresql;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import leap.db.DbLimitQuery;
import leap.db.change.ColumnDefinitionChange;
import leap.db.change.ColumnPropertyChange;
import leap.db.change.SchemaChangeContext;
import leap.db.model.DbColumn;
import leap.db.model.DbColumnType;
import leap.db.model.DbSchemaObjectName;
import leap.db.model.DbSequence;
import leap.db.model.DbTable;
import leap.db.platform.GenericDbDialect;
import leap.lang.Collections2;
import leap.lang.New;
import leap.lang.Strings;
import leap.lang.value.Limit;

public class PostgreSQL9Dialect extends GenericDbDialect {
	
	private static final String   DEFAULT_SCHEMA = "public";
	private static final String[] SYSTEM_SCHEMAS = new String[]{"INFORMATION_SCHEMA","PG_CATALOG"};
	
	//http://www.postgresql.org/docs/current/static/sql-keywords-appendix.html
	private static final String[] SQL_KEY_WORDS = new String[]{"USER"};

	public PostgreSQL9Dialect() {
	
	}
	
	@Override
    protected void registerSQLKeyWords() {
        super.registerSQLKeyWords();
        sqlKeyWords.addAll(Arrays.asList(SQL_KEY_WORDS));
    }

    @Override
    protected String caseQuotedIdentifier(String identifier) {
        //TODO : review
        return identifier.toLowerCase();
    }
    
    @Override
    protected String getBooleanTrueString() {
        return "'1'";
    }

    @Override
    protected String getBooleanFalseString() {
        return "'0'";
    }

    @Override
    public boolean supportsAutoIncrement() {
	    return false;
    }

	@Override
    public boolean supportsSequence() {
	    return true;
    }
	
	@Override
    protected boolean supportsColumnCommentInDefinition() {
		return false;
    }

	@Override
    protected String getTestDriverSupportsGetParameterTypeSQL() {
	    return "select 1 where 1 = ?";
    }
	
	@Override
    public String getLimitQuerySql(DbLimitQuery query) {
		Limit limit = query.getLimit();
		
		int offset = limit.getStart() - 1;
		int rows   = limit.getEnd()   - offset;
		
		//http://www.postgresql.org/docs/8.3/static/queries-limit.html
		//[ LIMIT { number | ALL } ] [ OFFSET number ]
        String sql = query.getSql(db) + " limit ? offset ?";
        query.getArgs().add(rows);
        query.getArgs().add(offset);
		
		return sql;
    }
	
	@Override
    public List<String> getCreateSequenceSqls(DbSequence sequence) throws IllegalStateException {
		/*
		CREATE [ TEMPORARY | TEMP ] SEQUENCE name [ INCREMENT [ BY ] increment ]
		    [ MINVALUE minvalue | NO MINVALUE ] [ MAXVALUE maxvalue | NO MAXVALUE ]
		    [ START [ WITH ] start ] [ CACHE cache ] [ [ NO ] CYCLE ]
		    [ OWNED BY { table.column | NONE } ]
		 */
		StringBuilder sb = new StringBuilder();
		
		sb.append("CREATE SEQUENCE ").append(qualifySchemaObjectName(sequence));
		
		if(null != sequence.getIncrement()){
			sb.append(" INCREMENT ").append(sequence.getIncrement());
		}
		
		if(null != sequence.getMinValue()){
			sb.append(" MINVALUE ").append(sequence.getMinValue());
		}
		
		if(null != sequence.getMaxValue()){
			sb.append(" MAXVALUE ").append(sequence.getMaxValue());
		}
		
		if(null != sequence.getStart()){
			sb.append(" START ").append(sequence.getStart());
		}
		
		if(null != sequence.getCache()){
			sb.append(" CACHE ").append(sequence.getCache());
		}
		
		if(null != sequence.getCycle()){
			if(!sequence.getCycle()){
				sb.append(" NO");
			}
			sb.append(" CYCLE");
		}
		
	    return New.arrayList(sb.toString());
    }
	
	@Override
    public String getNextSequenceValueSqlString(String sequenceName) throws IllegalStateException {
		return "nextval('" + quoteIdentifier(sequenceName) + "')";
    }
	
	@Override
    public String getSelectNextSequenceValueSql(String sequenceName) throws IllegalStateException {
		return "select " + getNextSequenceValueSqlString(sequenceName);
    }
	
	@Override
    public String getSelectCurrentSequenceValueSql(String sequenceName) throws IllegalStateException {
		return "select currval('" + quoteIdentifier(sequenceName) + "')";
	}

	@Override
    public List<String> getDropSequenceSqls(DbSchemaObjectName sequenceName) throws IllegalStateException {
		return New.arrayList("DROP SEQUENCE IF EXISTS " + qualifySchemaObjectName(sequenceName));
    }

	@Override
    protected List<String> createSafeAlterColumnSqlsForChange(SchemaChangeContext context, ColumnDefinitionChange change) {
		List<String> sqls = new ArrayList<String>();
		
		DbTable  t = change.getTable();
		DbColumn o = change.getOldColumn();
		DbColumn n = change.getNewColumn();
		
		/*
	    ALTER [ COLUMN ] column [ SET DATA ] TYPE data_type [ COLLATE collation ] [ USING expression ]
	    ALTER [ COLUMN ] column SET DEFAULT expression
	    ALTER [ COLUMN ] column DROP DEFAULT
	    ALTER [ COLUMN ] column { SET | DROP } NOT NULL
	    ALTER [ COLUMN ] column SET STATISTICS integer
	    ALTER [ COLUMN ] column SET ( attribute_option = value [, ... ] )
	    ALTER [ COLUMN ] column RESET ( attribute_option [, ... ] )
	    ALTER [ COLUMN ] column SET STORAGE { PLAIN | EXTERNAL | EXTENDED | MAIN }
		 */
		
		for(ColumnPropertyChange pc : change.getPropertyChanges()){
			if(pc.isUnique()){
				sqls.add(getAddUniqueColumnSql(t, o.getName()));
				continue;
			}
			
			if(pc.isDefault()){
				if(Strings.isEmpty(n.getDefaultValue())){
					sqls.add("ALTER TABLE " + qualifySchemaObjectName(t) + 
							 " ALTER COLUMN " + quoteIdentifier(o.getName()) + 
							 " DROP DEFAULT");
				}else{
					sqls.add("ALTER TABLE " + qualifySchemaObjectName(t) + 
							 " ALTER COLUMN " + quoteIdentifier(o.getName()) + 
							 " SET DEFAULT " + toSqlDefaultValue(n.getTypeCode(), n.getDefaultValue()));
				}
				continue;
			}
			
			if(pc.isNullable()){
				sqls.add("ALTER TABLE " + qualifySchemaObjectName(t) + 
						 " ALTER COLUMN " + quoteIdentifier(o.getName()) + 
						 (n.isNullable() ? " SET NULL" : " SET NOT NULL"));
				continue;
			}
			
			if(pc.isSize()) {
				DbColumnType type = getColumnType(n);
				String typeDefinition = getColumnTypeDefinition(n,type);
				
				if(typeDefinition.startsWith("varchar")) {
					sqls.add("ALTER TABLE " + qualifySchemaObjectName(t) + 
							 " ALTER COLUMN " + quoteIdentifier(o.getName()) + 
							 " SET DATA TYPE " + typeDefinition);
				}
				
				continue;
			}
			
			if(pc.isComment()){
				//ignore 
				continue;
			}
		}

		return sqls;
	}
	
	protected String getAddUniqueColumnSql(DbSchemaObjectName tableName,String columnName) {
		return "ALTER TABLE " + qualifySchemaObjectName(tableName) + " ADD UNIQUE(" + quoteIdentifier(columnName) + ")";
	}
	
	@Override
    public String getDefaultSchemaName(Connection connection, DatabaseMetaData dm) throws SQLException {
	    return DEFAULT_SCHEMA;
    }

    public boolean isSystemSchema(String schemaName) {
	    if(Strings.startsWithIgnoreCase(schemaName,"pg_toast_temp_")){
	    	return true;
	    }
	    return super.isSystemSchema(schemaName);
    }

	protected void registerSystemSchemas() {
	    Collections2.addAll(systemSchemas, SYSTEM_SCHEMAS);
    }

	@Override
	protected void registerColumnTypes() {
		//see : http://www.postgresql.org/docs/9.0/static/datatype.html
		
        columnTypes.add(Types.BOOLEAN,       "boolean");
        columnTypes.add(Types.BIT,           "bit");
        
        columnTypes.add(Types.TINYINT,       "smallint");
        columnTypes.add(Types.SMALLINT,      "smallint");
        columnTypes.add(Types.INTEGER,       "integer");
        columnTypes.add(Types.BIGINT,        "bigint");

        //JDBC's real type mapping to java's float, JDBC's float type mapping to java's double
        columnTypes.add(Types.REAL,          "real");
        columnTypes.add(Types.FLOAT,         "double precision");
        columnTypes.add(Types.DOUBLE,        "double precision");
        
        columnTypes.add(Types.DECIMAL,       "numeric($p,$s)");
        columnTypes.add(Types.NUMERIC,       "numeric($p,$s)");
        
        columnTypes.add(Types.CHAR,          "char($l)");
        columnTypes.add(Types.VARCHAR,       "varchar($l)");      
        columnTypes.add(Types.LONGVARCHAR,   "text");

        columnTypes.add(Types.BINARY,        "bytea");
        columnTypes.add(Types.VARBINARY,     "bytea");
        columnTypes.add(Types.LONGVARBINARY, "bytea");
        
        columnTypes.add(Types.DATE,          "date");
        columnTypes.add(Types.TIME,          "time");
        columnTypes.add(Types.TIMESTAMP,     "timestamp");

        columnTypes.add(Types.BLOB,          "oid");
        columnTypes.add(Types.CLOB,          "text");		 
	}
	
	@Override
	protected String getOpenQuoteString() {
		return "\"";
	}

	@Override
	protected String getCloseQuoteString() {
		return "\"";
	}
}

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
package leap.db.platform.derby;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import leap.core.validation.Valid;
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
import leap.db.platform.GenericDbMetadataReader;
import leap.lang.Collections2;
import leap.lang.New;
import leap.lang.Strings;
import leap.lang.jdbc.SimpleClob;
import leap.lang.value.Limit;

public class Derby10Dialect extends GenericDbDialect {
	
	//select schemaname from sys.sysschemas;
    private static final String[] SYSTEM_SCHEMAS = 
    		new String[]{"NULLID","SQLJ","SYS","SYSCAT","SYSCS_DIAG","SYSCS_UITL","SYSFUN","SYSIBM","SYSPROC","SYSSTAT"};

    private static final String INTERNAL_UNIQUE_INDEX_PREFIX = GenericDbMetadataReader.INTERNAL_NAME_PREFIX + "UNIQUE_IX_";
    
	public Derby10Dialect() {
	    super();
    }

	@Override
	protected Object nativeToJava(Object v, int type) throws SQLException {
		if(v instanceof Clob) {
			//Can't read the clob if result set closed, so we read it immediately.
			return new SimpleClob(clobToString((Clob) v));
		}
		return super.nativeToJava(v, type);
	}

	@Override
    public String getDefaultSchemaName(Connection connection, DatabaseMetaData dm) throws SQLException {
		try(PreparedStatement ps = connection.prepareStatement("select current schema from sysibm.sysdummy1")) {
			try(ResultSet rs = ps.executeQuery()){
				rs.next();
				return rs.getString(1);
			}
		}
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
    protected String getTestDriverSupportsGetParameterTypeSQL() {
	    return "select 1 from sysibm.sysdummy1 where 1 = ?";
    }
	
    protected String getColumnNullableDefinition(DbColumn column){
        if(column.isNullable()){
            return "";
        }else{
            return "NOT NULL";
        }
    }
    
	@Override
    protected String getAutoIncrementColumnDefinitionEnd(DbColumn column) {
		return "GENERATED ALWAYS AS IDENTITY";
    }
	
	@Override
    public String getLimitQuerySql(DbLimitQuery query) {
		Limit limit = query.getLimit();
		
		int offset = limit.getStart() - 1;
		int rows   = limit.getEnd()   - offset;
		
		//http://db.apache.org/derby/docs/10.10/ref/rrefsqljoffsetfetch.html#rrefsqljoffsetfetch
		//OFFSET { integerLiteral | ? } { ROW | ROWS }
		//FETCH { FIRST | NEXT } [ integerLiteral | ? ] { ROW | ROWS } ONLY
        String sql = query.getSql(db) + " offset ? rows fetch first ? rows only";
        query.getArgs().add(offset);
        query.getArgs().add(rows);
		
		return sql;
    }
	
	@Override
    public boolean supportsColumnComment() {
	    return false;
    }

	@Override
    public boolean supportsSequence() {
	    return true;
    }
	
	@Override
    public boolean supportsCurrentSequenceValue() {
		return false;
    }

	@Override
    public List<String> getCreateSequenceSqls(DbSequence sequence) throws IllegalStateException {
		/*
		    CREATE SEQUENCE sequenceName
			{
			  AS dataType 
			  | START WITH signedInteger 
			  | INCREMENT BY signedInteger 
			  | MAXVALUE signedInteger | NO MAXVALUE 
			  | MINVALUE signedInteger | NO MINVALUE 
			  | CYCLE | NO CYCLE 
			}
	    */
		StringBuilder sb = new StringBuilder();
		
		sb.append("CREATE SEQUENCE ").append(qualifySchemaObjectName(sequence));
		
		if(null != sequence.getStart()){
			sb.append(" START WITH ").append(sequence.getStart());
		}
		
		if(null != sequence.getIncrement()){
			sb.append(" INCREMENT BY ").append(sequence.getIncrement());
		}
		
		if(null != sequence.getMinValue()){
			sb.append(" MINVALUE ").append(sequence.getMinValue());
		}
		
		if(null != sequence.getMaxValue()){
			sb.append(" MAXVALUE ").append(sequence.getMaxValue());
		}
		
		if(Boolean.TRUE.equals(sequence.getCycle())){
			sb.append(" CYCLE");
		}else{
			sb.append(" NO CYCLE");
		}
		
	    return New.arrayList(sb.toString());
    }
	
	@Override
    public List<String> getDropSequenceSqls(DbSchemaObjectName sequenceName) throws IllegalStateException {
		return New.arrayList("DROP SEQUENCE " + qualifySchemaObjectName(sequenceName) + " RESTRICT");
    }

	@Override
    public String getNextSequenceValueSqlString(String sequenceName) throws IllegalStateException {
		return "next value for " + sequenceName;
    }
	
	@Override
    protected List<String> createSafeAlterColumnSqlsForChange(SchemaChangeContext context, ColumnDefinitionChange change) {
		List<String> sqls = new ArrayList<String>();
		
		DbTable  t = change.getTable();
		DbColumn o = change.getOldColumn();
		DbColumn n = change.getNewColumn();
		
		for(ColumnPropertyChange pc : change.getPropertyChanges()){
			if(pc.isUnique()){
				//sqls.add("ALTER TABLE " + qualifySchemaObjectName(t) + " ADD UNIQUE(" + quoteIdentifier(o.getName()) + ")");
				sqls.add("CREATE UNIQUE INDEX " + INTERNAL_UNIQUE_INDEX_PREFIX + o.getName() + 
						 " ON " + qualifySchemaObjectName(t) + "(" + quoteIdentifier(o.getName() + ")"));
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
						 (n.isNullable() ? " NULL" : " NOT NULL"));
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
	
	@Override
    protected void registerSystemSchemas() {
		Collections2.addAll(systemSchemas, SYSTEM_SCHEMAS);
    }

	@Override
    protected void registerColumnTypes() {
		//http://db.apache.org/derby/docs/10.10/ref/crefsqlj31068.html
        columnTypes.add(Types.BOOLEAN,       "boolean");
        columnTypes.add(Types.BIT,           "smallint");
        
        columnTypes.add(Types.SMALLINT,      "smallint");
        columnTypes.add(Types.TINYINT,       "smallint");
        columnTypes.add(Types.SMALLINT,      "smallint");
        columnTypes.add(Types.INTEGER,       "integer");
        columnTypes.add(Types.BIGINT,        "bigint"  );

        //JDBC's real type mapping to java's float, JDBC's float type mapping to java's double
        columnTypes.add(Types.REAL,          "real");
        columnTypes.add(Types.FLOAT,         "real");
        columnTypes.add(Types.DOUBLE,        "double");
        
        columnTypes.add(Types.DECIMAL,       "decimal($p,$s)");
        columnTypes.add(Types.NUMERIC,       "decimal($p,$s)");
        
        columnTypes.add(Types.CHAR,          "char($l)", 1, 254);
        columnTypes.add(Types.CHAR,          "char(254)",0, 254);
        columnTypes.add(Types.VARCHAR,       "varchar($l)",0,32672);  
        columnTypes.add(Types.VARCHAR,		 "clob");
        columnTypes.add(Types.LONGVARCHAR,   "varchar($l)",0,32672);
        columnTypes.add(Types.LONGVARCHAR,   "clob");

        columnTypes.add(Types.BINARY,        "char($l) for bit data",1,254);
        //columnTypes.add(Types.BINARY,        "char(254) for bit data");
        columnTypes.add(Types.BINARY,        "blob");
        columnTypes.add(Types.VARBINARY,     "varchar($l) for bit data",1,32672);
        columnTypes.add(Types.VARBINARY,     "blob");
        columnTypes.add(Types.LONGVARBINARY, "varchar($l) for bit data",1,32672);
        columnTypes.add(Types.LONGVARBINARY, "blob");
        
        columnTypes.add(Types.DATE,          "date");
        columnTypes.add(Types.TIME,          "time");
        columnTypes.add(Types.TIMESTAMP,     "timestamp");

        columnTypes.add(Types.BLOB,          "blob");
        columnTypes.add(Types.CLOB,          "clob");
    }
}
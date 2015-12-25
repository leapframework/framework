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
package leap.db.platform.db2;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

import leap.db.model.DbSequence;
import leap.db.platform.GenericDbDialect;
import leap.lang.Collections2;
import leap.lang.New;

public class DB29Dialect extends GenericDbDialect {
	
    private static final String[] SYSTEM_SCHEMAS = 
    		new String[]{"NULLID","SQLJ","SYSCAT","SYSFUN","SYSIBM","SYSIBMADM",
    					 "SYSIBMINTERNAL","SYSIBMTS","SYSPROC","SYSPUBLIC","SYSSTAT","SYSTOOLS"};

	protected DB29Dialect() {
		
	}
	
	@Override
    public String getDefaultSchemaName(Connection connection, DatabaseMetaData dm) throws SQLException {
		try(PreparedStatement ps = connection.prepareStatement("select current_schema from sysibm.sysdummy1")) {
			try(ResultSet rs = ps.executeQuery()){
				rs.next();
				return rs.getString(1);
			}
		}
    }
	
	@Override
    public boolean supportsSequence() {
	    return true;
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
	
	@Override
    public List<String> getCreateSequenceSqls(DbSequence sequence) throws IllegalStateException {
		/*
		   CREATE SEQUENCE ORG_SEQ
		     START WITH 1
		     INCREMENT BY 1
		     NO MAXVALUE
		     NO CYCLE
		     CACHE 24
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
		
		if(null != sequence.getCache()){
			sb.append(" CACHE ").append(sequence.getCache());
		}else {
			sb.append(" NO CACHE");
		}
		
	    return New.arrayList(sb.toString());
    }

	@Override
    protected void registerSystemSchemas() {
		Collections2.addAll(systemSchemas, SYSTEM_SCHEMAS);
    }

	@Override
    protected void registerColumnTypes() {
		//http://www-01.ibm.com/support/knowledgecenter/SSEPGG_9.7.0/com.ibm.db2.luw.apdv.java.doc/src/tpc/imjcc_rjvjdata.html
		//http://www-01.ibm.com/support/knowledgecenter/SSEPGG_9.7.0/com.ibm.db2.luw.sql.ref.doc/doc/r0008470.html?cp=SSEPGG_9.7.0%2F2-10-2-3-0-1
        columnTypes.add(Types.BOOLEAN,       "smallint");
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
        
        columnTypes.add(Types.CHAR,          "char($l)");
        columnTypes.add(Types.CHAR,          "char(254)",0,0);
        columnTypes.add(Types.VARCHAR,       "varchar($l)",0,32672);  
        columnTypes.add(Types.VARCHAR,		 "clob");
        columnTypes.add(Types.LONGVARCHAR,   "varchar($l)",0,32672);
        columnTypes.add(Types.LONGVARCHAR,   "clob");

        columnTypes.add(Types.BINARY,        "char($l) for bit data");
        columnTypes.add(Types.BINARY,        "char(254) for bit data",0,0);
        columnTypes.add(Types.VARBINARY,     "varchar($l) for bit data",0,32672);
        columnTypes.add(Types.VARBINARY,     "blob");
        columnTypes.add(Types.LONGVARBINARY, "varchar($l) for bit data",0,32672);
        columnTypes.add(Types.LONGVARBINARY, "blob");
        
        columnTypes.add(Types.DATE,          "date");
        columnTypes.add(Types.TIME,          "time");
        columnTypes.add(Types.TIMESTAMP,     "timestamp");

        columnTypes.add(Types.BLOB,          "blob");
        columnTypes.add(Types.CLOB,          "clob");
    }
}
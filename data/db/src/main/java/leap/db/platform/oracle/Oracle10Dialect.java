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
package leap.db.platform.oracle;

import java.sql.Types;
import java.util.List;

import leap.db.model.DbSchemaObjectName;
import leap.db.model.DbSequence;
import leap.db.platform.GenericDbDialect;
import leap.lang.New;

public class Oracle10Dialect extends GenericDbDialect {

	@Override
    protected String getTestDriverSupportsGetParameterTypeSQL() {
	    return "select 1 from dual where 1 = ?";
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
    protected void registerColumnTypes() {
		//http://docs.oracle.com/cd/B19306_01/server.102/b14200/sql_elements001.htm
		//http://docs.oracle.com/cd/B19306_01/java.102/b14355/datacc.htm#g1028145
		
        columnTypes.add(Types.BOOLEAN,       "number(1,0)");
        columnTypes.add(Types.BIT,           "number(1,0)");
        
        columnTypes.add(Types.TINYINT,       "number(3,0)");
        columnTypes.add(Types.SMALLINT,      "number(5,0)");
        columnTypes.add(Types.INTEGER,       "integer");
        columnTypes.add(Types.BIGINT,        "number(19,0)");

        //JDBC's real type mapping to java's float, JDBC's float type mapping to java's double
        columnTypes.add(Types.REAL,          "float");  			
        columnTypes.add(Types.FLOAT,         "double precision");	
        columnTypes.add(Types.DOUBLE,        "double precision");
        
        columnTypes.add(Types.DECIMAL,       "number($p,$s)");
        columnTypes.add(Types.NUMERIC,       "number($p,$s)");
        
        columnTypes.add(Types.CHAR,          "char($l)",0,2000);
        columnTypes.add(Types.VARCHAR,       "varchar2($l)",0,4000);      
        columnTypes.add(Types.VARCHAR,       "long");
        columnTypes.add(Types.LONGVARCHAR,   "long");

        columnTypes.add(Types.BINARY,        "raw($l)",0,2000);
        columnTypes.add(Types.BINARY,        "long raw");
        columnTypes.add(Types.VARBINARY,     "raw($l)",0,2000);
        columnTypes.add(Types.VARBINARY,     "long raw");
        columnTypes.add(Types.LONGVARBINARY, "long raw");
        
        columnTypes.add(Types.DATE,          "date");
        columnTypes.add(Types.TIME,          "date");
        columnTypes.add(Types.TIMESTAMP,     "timestamp");

        columnTypes.add(Types.BLOB,          "blob");
        columnTypes.add(Types.CLOB,          "clob");
    }

	@Override
    public boolean supportsSequence() {
	    return true;
    }

	@Override
    public List<String> getCreateSequenceSqls(DbSequence sequence) throws IllegalStateException {
		/*
			CREATE SEQUENCE [ schema. ]sequence
			   [ { INCREMENT BY | START WITH } integer
			   | { MAXVALUE integer | NOMAXVALUE }
			   | { MINVALUE integer | NOMINVALUE }
			   | { CYCLE | NOCYCLE }
			   | { CACHE integer | NOCACHE }
			   | { ORDER | NOORDER }
			   ]
			     [ { INCREMENT BY | START WITH } integer
			     | { MAXVALUE integer | NOMAXVALUE }
			     | { MINVALUE integer | NOMINVALUE }
			     | { CYCLE | NOCYCLE }
			     | { CACHE integer | NOCACHE }
			     | { ORDER | NOORDER }
			     ]... ;
		 */
		StringBuilder sb = new StringBuilder();
		
		sb.append("CREATE SEQUENCE ").append(qualifySchemaObjectName(sequence));
		
		if(null != sequence.getStart()){
			sb.append(" START WITH ").append(sequence.getStart());
		}
		
		if(null != sequence.getIncrement()){
			sb.append(" INCREMENT BY").append(sequence.getIncrement());
		}
		
		if(null != sequence.getMinValue()){
			sb.append(" MINVALUE ").append(sequence.getMinValue());
		}
		
		if(null != sequence.getMaxValue()){
			sb.append(" MAXVALUE ").append(sequence.getMaxValue());
		}
		
		if(null != sequence.getCache()){
			sb.append(" CACHE ").append(sequence.getCache());
		}
		
		if(null != sequence.getCycle()){
			if(!sequence.getCycle()){
				sb.append(" NOCYCLE");
			}
			sb.append(" CYCLE");
		}
		
	    return New.arrayList(sb.toString());
    }
	
	@Override
    public String getNextSequenceValueSqlString(String sequenceName) throws IllegalStateException {
		return quoteIdentifier(sequenceName) + ".nextval";
    }
	
	@Override
    public String getSelectNextSequenceValueSql(String sequenceName) throws IllegalStateException {
		return "select " + getNextSequenceValueSqlString(sequenceName) + " from dual";
    }
	
	@Override
    public String getSelectCurrentSequenceValueSql(String sequenceName) throws IllegalStateException {
		return "select " + quoteIdentifier(sequenceName) + ".currval from dual";
	}

	@Override
    public List<String> getDropSequenceSqls(DbSchemaObjectName sequenceName) throws IllegalStateException {
		return New.arrayList("DROP SEQUENCE " + qualifySchemaObjectName(sequenceName));
    }
}
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
package leap.db.platform.mysql;

import java.io.BufferedReader;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import leap.db.DbLimitQuery;
import leap.db.change.ColumnDefinitionChange;
import leap.db.change.SchemaChangeContext;
import leap.db.model.DbColumn;
import leap.db.model.DbColumnBuilder;
import leap.db.model.DbSchemaObjectName;
import leap.db.platform.GenericDbDialect;
import leap.lang.Collections2;
import leap.lang.New;
import leap.lang.Strings;
import leap.lang.value.Limit;

public class MySql5Dialect extends GenericDbDialect {
	
    /**
     * the reserved sql keywords not defined in {@link SqlStandards#SQL92_RESERVED_WORDS}. <p/>
     * 
     * <p>
     * see：http://www.mysql.com/doc/en/Reserved_words.html.
     */
    private static final String[] NONSQL92_RESERVED_WORDS = new String[] { 
        "ANALYZE","AUTO_INCREMENT","BDB","BERKELEYDB","BIGINT","BINARY","BLOB","BTREE",
        "CHANGE","COLUMNS","DATABASE","DATABASES","DAY_HOUR","DAY_MINUTE","DAY_SECOND",
        "DELAYED","DISTINCTROW","DIV","ENCLOSED","ERRORS","ESCAPED","EXPLAIN","FIELDS",
        "FORCE","FULLTEXT","FUNCTION","GEOMETRY","HASH","HELP","HIGH_PRIORITY",
        "HOUR_MINUTE","HOUR_SECOND","IF","IGNORE","INDEX","INFILE","INNODB","KEYS","KEY","KILL",
        "LIMIT","LINES","LOAD","LOCALTIME","LOCALTIMESTAMP","LOCK","LONG","LONGBLOB",
        "LONGTEXT","LOW_PRIORITY","MASTER_SERVER_ID","MEDIUMBLOB","MEDIUMINT",
        "MEDIUMTEXT","MIDDLEINT","MINUTE_SECOND","MOD","MRG_MYISAM","OPTIMIZE",
        "OPTIONALLY","OUTFILE","PURGE","REGEXP","RENAME","REPLACE","REQUIRE","RETURNS",
        "RLIKE","RTREE","SHOW","SONAME","SPATIAL","SQL_BIG_RESULT","SQL_CALC_FOUND_ROWS",
        "SQL_SMALL_RESULT","SSL","STARTING","STRAIGHT_JOIN","STRIPED","TABLES",
        "TERMINATED","TINYBLOB","TINYINT","TINYTEXT","TYPES","UNLOCK","UNSIGNED","USE",
        "USER_RESOURCES","VARBINARY","VARCHARACTER","WARNINGS","XOR","YEAR_MONTH",
        "ZEROFILL","INT","INT1","INT2","INT3","INT4","INT8","USAGE"};
    
    private static final String[] SYSTEM_SCHEMAS = new String[]{"INFORMATION_SCHEMA","PERFORMANCE_SCHEMA"};

    protected MySql5Dialect(){
    	
    }
    
	@Override
    protected String getAutoIncrementColumnDefinitionEnd(DbColumn column) {
	    return "AUTO_INCREMENT";
    }

	@Override
    protected String getTestDriverSupportsGetParameterTypeSQL() {
	    return "select 1 from dual where 1 = ?";
    }

	@Override
    public String getDefaultSchemaName(Connection connection, DatabaseMetaData dm) throws SQLException {
		return connection.getCatalog();
    }

    @Override
    protected void setNonNullParameter(PreparedStatement ps, int index, Object value, int type) throws SQLException {
        super.setNonNullParameter(ps, index, value, type);
    }

    @Override
    protected Object getColumnValueTypeKnown(ResultSet rs, int index, int type) throws SQLException {
        //https://dev.mysql.com/doc/refman/5.7/en/binary-varbinary.html
        /*
            The BINARY and VARBINARY types are similar to CHAR and VARCHAR,
            except that they contain binary strings rather than nonbinary strings.
            That is, they contain byte strings rather than character strings.
            This means that they have no character set, and sorting and comparison are
             based on the numeric values of the bytes in the values
         */
        if(type == Types.BINARY || type == Types.VARBINARY) {
            return rs.getString(index);
        }

        return super.getColumnValueTypeKnown(rs, index, type);
    }

    @Override
    protected Object getColumnValueTypeKnown(ResultSet rs, String name, int type) throws SQLException {
        if(type == Types.BINARY || type == Types.VARBINARY) {
            return rs.getString(name);
        }

        return super.getColumnValueTypeKnown(rs, name, type);
    }

    @Override
    public String readDefaultValue(int typeCode, String nativeDefaultValue) {
        // MySQL converts illegal date/time/timestamp values to "0000-00-00 00:00:00", but this
        // is an illegal ISO value, so we replace it with NULL
		if ((typeCode == Types.TIMESTAMP) && "0000-00-00 00:00:00".equals(nativeDefaultValue)) {
			return null;
		}else if(Strings.equals("b'0'",nativeDefaultValue)){
    		return "0";
    	}else if(Strings.equals("b'1'", nativeDefaultValue)){
    		return "1";
    	}		
		return super.readDefaultValue(typeCode, nativeDefaultValue);
    }
	
	@Override
    public String getLimitQuerySql(DbLimitQuery query) {
		//[LIMIT {[offset,] row_count | row_count OFFSET offset}]
		//The offset of the initial row is 0 (not 1):
		
		Limit limit = query.getLimit();
		
		int offset = limit.getStart() - 1;
		int rows   = limit.getEnd()   - offset;
		
        String sql = query.getSql(db) + " limit ?,?";
        query.getArgs().add(offset);
        query.getArgs().add(rows);
		
		return sql;
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
		 	 	 " MODIFY COLUMN " + getColumnDefinitionForAlterTable(column);
	}
	
	@Override
    public List<String> getDropForeignKeySqls(DbSchemaObjectName tableName, String fkName) {
	    return New.arrayList("ALTER TABLE " + qualifySchemaObjectName(tableName) + " DROP FOREIGN KEY " + fkName);
    }

    @Override
    public List<String> getDropIndexSqls(DbSchemaObjectName tableName, String ixName) {
        return New.arrayList("DROP INDEX " + ixName + " ON " + qualifySchemaObjectName(tableName));
    }

    @Override
    protected void registerSQLKeyWords() {
	    super.registerSQLKeyWords();
	    this.sqlKeyWords.addAll(Arrays.asList(NONSQL92_RESERVED_WORDS));
    }

	@Override
    protected void registerSystemSchemas() {
		Collections2.addAll(systemSchemas, SYSTEM_SCHEMAS);
    }

	@Override
    protected void registerColumnTypes() {
		//see http://dev.mysql.com/doc/refman/5.6/en/storage-requirements.html
		
        columnTypes.add(Types.BOOLEAN,       "bit",0,0,Types.BIT);
        columnTypes.add(Types.BIT,           "bit");
        
        columnTypes.add(Types.TINYINT,       "tinyint");
        columnTypes.add(Types.SMALLINT,      "smallint");
        columnTypes.add(Types.INTEGER,       "integer");
        columnTypes.add(Types.BIGINT,        "bigint"  );

        //JDBC's real type mapping to java's float, JDBC's float type mapping to java's double
        columnTypes.add(Types.REAL,          "float");  			
        columnTypes.add(Types.FLOAT,         "double precision");	
        columnTypes.add(Types.DOUBLE,        "double precision");
        
        columnTypes.add(Types.DECIMAL,       "decimal($p,$s)");
        columnTypes.add(Types.NUMERIC,       "decimal($p,$s)");
        
        columnTypes.add(Types.CHAR,          "char($l)",0,255);
        columnTypes.add(Types.VARCHAR,       "varchar($l)",0,65535);
        columnTypes.add(Types.VARCHAR,       "longtext");
        columnTypes.add(Types.LONGVARCHAR,   "longtext");

        columnTypes.add(Types.BINARY,        "binary($l)",1,255);
        columnTypes.add(Types.BINARY,        "longblob");
        columnTypes.add(Types.VARBINARY,     "varbinary($l)",1,65535);
        columnTypes.add(Types.VARBINARY,     "longblob");
        columnTypes.add(Types.LONGVARBINARY, "longblob");
        
        columnTypes.add(Types.DATE,          "date");
        columnTypes.add(Types.TIME,          "time");
        columnTypes.add(Types.TIMESTAMP,     "datetime");

        columnTypes.add(Types.BLOB,          "blob");
        columnTypes.add(Types.CLOB,          "text");
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
    protected String parseDelimiter(BufferedReader reader, String delimiter, String line) {

        if(Strings.startsWithIgnoreCase(line, "delimiter ")) {
            String newDelimiter = Strings.removeStartIgnoreCase(line, "delimiter").trim();

            while(newDelimiter.length() > 0 && newDelimiter.endsWith(delimiter)) {
                newDelimiter = Strings.removeEnd(newDelimiter, delimiter);
            }

            if(newDelimiter.length() > 0) {
                return newDelimiter;
            }
        }

        return null;
    }

    @Override
    public String quoteIdentifier(String identifier, boolean quoteKeywordOnly) {
	    if(Strings.equalsIgnoreCase(identifier,"dual")){
	        return identifier;
        }
	    return super.quoteIdentifier(identifier,quoteKeywordOnly);
    }

}
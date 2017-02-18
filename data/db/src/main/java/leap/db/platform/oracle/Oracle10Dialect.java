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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import leap.db.DbCommand;
import leap.db.DbLimitQuery;
import leap.db.change.ColumnDefinitionChange;
import leap.db.change.RemoveTableChange;
import leap.db.change.SchemaChangeContext;
import leap.db.model.*;
import leap.db.platform.GenericDbDialect;
import leap.lang.New;
import leap.lang.Strings;
import leap.lang.jdbc.JdbcTypes;
import leap.lang.value.Limit;

public class Oracle10Dialect extends GenericDbDialect {

	@Override
    protected String getTestDriverSupportsGetParameterTypeSQL() {
	    return "select 1 from dual where 1 = ?";
    }

    @Override
    protected boolean testDriverSupportsGetParameterType() {
        return false;
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
        columnTypes.add(Types.INTEGER,       "number(10,0)");
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

        columnTypes.add(Types.BINARY,        "blob");
        columnTypes.add(Types.BINARY,        "blob");
        columnTypes.add(Types.VARBINARY,     "blob");
        columnTypes.add(Types.LONGVARBINARY, "blob");
        
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

    @Override
    protected List<String> createSafeAlterColumnSqlsForChange(SchemaChangeContext context,
                                                              ColumnDefinitionChange change) {
	    // TODO create safe alter column sqls for change
        List<String> sqls = new ArrayList<>();

        if(change.isUniqueChanged()){
            sqls.add(getAddUniqueColumnSql(change.getTable(), change.getOldColumn().getName()));

            if(change.getPropertyChanges().size() > 1){
                DbColumn c = new DbColumnBuilder(change.getNewColumn()).setUnique(false).build();
                sqls.add(getAlterColumnSql(change.getTable(), c, change.getOldColumn()));
            }
        }else{
            sqls.add(getAlterColumnSql(change.getTable(), change.getNewColumn(), change.getOldColumn()));
        }
        
        DbColumn newColumn = change.getNewColumn();
        if(!supportsColumnCommentInDefinition() && !Strings.isEmpty(newColumn.getComment())){
            sqls.addAll(getCommentOnColumnSqls(change.getTable(),newColumn.getName(),newColumn.getComment()));
        }
        
        return sqls;
    }
    protected String getAddUniqueColumnSql(DbSchemaObjectName tableName,String columnName) {
        return "ALTER TABLE " + qualifySchemaObjectName(tableName) +
                " ADD UNIQUE(" + quoteIdentifier(columnName) + ")";
    }

    protected String getAlterColumnSql(DbSchemaObjectName tableName,DbColumn newColumn, DbColumn oldColumn){
        return "ALTER TABLE " + qualifySchemaObjectName(tableName) +
                " MODIFY (" + getColumnDefinitionForAlterTable(newColumn,oldColumn) +")";
    }

    protected String getColumnDefinitionForAlterTable(DbColumn newColumn, DbColumn oldColumn){
        StringBuilder definition = new StringBuilder();

        definition.append(quoteIdentifier(newColumn.getName()))
                .append(' ')
                .append(newColumn.isAutoIncrement() ? getAutoIncrementColumnTypeDefinition(newColumn) : getColumnTypeDefinition(newColumn));

        if(isDefaultBeforeNullInColumnDefinition()){
            //default
            if(!Strings.isEmpty(newColumn.getDefaultValue())){
                definition.append(' ').append(getColumnDefaultDefinition(newColumn));
            }

            //null
            String nullDefinition = getColumnNullableDefinition(newColumn,oldColumn);
            if(!Strings.isEmpty(nullDefinition)){
                definition.append(" ").append(nullDefinition);
            }
        }else{
            //null
            String nullDefinition = getColumnNullableDefinition(newColumn,oldColumn);
            if(!Strings.isEmpty(nullDefinition)){
                definition.append(" ").append(nullDefinition);
            }

            //default
            if(!Strings.isEmpty(newColumn.getDefaultValue())){
                definition.append(' ').append(getColumnDefaultDefinition(newColumn));
            }
        }
        
        if(supportsColumnCommentInDefinition() && !Strings.isEmpty(newColumn.getComment())){
            definition.append(' ').append(getColumnCommentDefinition(newColumn));
        }

        if(newColumn.isUnique() && supportsUniqueInColumnDefinition()){
            definition.append(' ').append(getColumnUniqueDefinition(newColumn));
        }

        if(newColumn.isAutoIncrement()){
            if(supportsAutoIncrement()){
                definition.append(' ').append(getAutoIncrementColumnDefinitionEnd(newColumn));
            }else{
                log.warn("Unsupported auto increment column in " + db.getPlatform().getName());
            }
        }

        return definition.toString();
    }

    protected String getColumnNullableDefinition(DbColumn newColumn, DbColumn oldColumn) {
        if(newColumn.isNullable() == oldColumn.isNullable()){
            return "";
        }else{
            return getColumnNullableDefinition(newColumn);
        }
    }

    @Override
    public List<String> getCreateColumnSqls(DbSchemaObjectName tableName, DbColumn column) {
        List<String> sqls = New.arrayList();
        StringBuilder sql = new StringBuilder("ALTER TABLE ");
        sql.append(qualifySchemaObjectName(tableName));
        sql.append(" ADD (");
        sql.append(getColumnDefinitionForAlterTable(column));
        sql.append(")");
        
        sqls.add(sql.toString());
        
        if(Strings.isNotEmpty(column.getComment())){
            sqls.addAll(getCommentOnColumnSqls(tableName,column.getName(),column.getComment()));
        }
        return sqls;
    }
    @Override
    protected void createSchemaChangeCommands(SchemaChangeContext context, RemoveTableChange change, List<DbCommand> commands){
        for(DbForeignKey fk : change.getOldTable().getForeignKeys()) {
            commands.add(db.cmdDropForeignKey(change.getOldTable(), fk.getName()));
        }
        commands.add(db.cmdDropTable(change.getOldTable()));
    }
    
    @Override
    protected boolean supportsColumnCommentInDefinition() {
        // Oracle not support column comment in definition
        return false;
    }

    @Override
    public boolean supportsAutoIncrement() {
        // Oracle not support auto increment column
        return false;
    }

    @Override
    public DbCascadeAction getForeignKeyDefaultOnUpdate() {
        return DbCascadeAction.CASCADE;
    }

    @Override
    public String getLimitQuerySql(DbLimitQuery query) {

        Limit limit = query.getLimit();

        int start = limit.getStart();
        int end   = limit.getEnd();
        
        
        StringBuilder limitSql = new StringBuilder("SELECT * FROM ( ");
        limitSql.append("SELECT A.*,ROWNUM ORACLE_ROWNUM FROM ( ");
        String sql = query.getSql(db);
        limitSql.append(sql);
        
        limitSql.append(" ) A WHERE ROWNUM <= ?");
        limitSql.append(" ) WHERE ORACLE_ROWNUM >= ?");
        
        
        query.getArgs().add(end);
        query.getArgs().add(start);
        return limitSql.toString();
    }

    @Override
    protected Object getColumnValueTypeKnown(ResultSet rs, int index, int type) throws SQLException {
        if(type == Types.TIMESTAMP){
            return rs.getTimestamp(index);
        }
        if(type == Types.NUMERIC){
            int scale = rs.getMetaData().getScale(index);
            int precision = rs.getMetaData().getPrecision(index);
            
            if(scale == 0){
                Object o = rs.getObject(index);
                if(precision == 10){
                    if(o != null){
                        return rs.getInt(index);
                    }
                    return o;
                }else if (precision > 10){
                    if(o != null){
                        return rs.getLong(index);
                    }
                    return o;
                }
            }
        }
        return super.getColumnValueTypeKnown(rs, index, type);
    }
}
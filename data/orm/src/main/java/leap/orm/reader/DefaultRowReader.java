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
package leap.orm.reader;

import leap.core.exception.TooManyRecordsException;
import leap.core.value.Record;
import leap.core.value.SimpleRecord;
import leap.db.DbDialect;
import leap.lang.Strings;
import leap.lang.beans.BeanProperty;
import leap.lang.beans.BeanType;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.orm.naming.NamingStrategy;
import leap.orm.sql.Sql;
import leap.orm.sql.SqlCommand;
import leap.orm.sql.SqlExecutionContext;
import leap.orm.sql.ast.AstNode;
import leap.orm.sql.ast.SqlSelect;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DefaultRowReader implements RowReader {
	
	private static final Log log = LogFactory.get(DefaultRowReader.class);

	@Override
    public <T> T readFirst(SqlExecutionContext context, ResultSet rs, Class<T> resultClass,SqlCommand command) throws SQLException {
		if(rs.next()){
			ResultColumn[] columns = createResultColumns(context,command, rs);
			return readCurrentRow(context, rs, columns, resultClass);
		}
		
		log.debug("No result(s) returned");
		return null;
    }

	@Override
    public <T> T readSingle(SqlExecutionContext context, ResultSet rs, Class<T> resultClass, SqlCommand command) throws SQLException, TooManyRecordsException {
		if(rs.next()){
			ResultColumn[] columns = createResultColumns(context,command, rs);
			
			T result = readCurrentRow(context, rs, columns, resultClass);
			
			if(rs.next()){
				throw new TooManyRecordsException("Found two or more results in the returned result set");
			}
			
			return result;
		}
		
		log.debug("No result(s) returned");
		return null;
    }

    @Override
	@SuppressWarnings("unchecked")
    public <T> List<T> readList(SqlExecutionContext context, ResultSet rs, Class<T> elementType, Class<? extends T> resultClass, SqlCommand command) throws SQLException {
		List<T> list = new ArrayList<T>();
		
		if(rs.next()){
			ResultColumn[] columns = createResultColumns(context, command, rs);

			if(Record.class.equals(resultClass)) {
				do{
					list.add((T)readRecord(context, rs, columns));
				}while(rs.next());
			}else if(Map.class.equals(resultClass)){
				do{
					list.add((T)readMap(context, rs, columns));
				}while(rs.next());
			}else{
				BeanType bt = BeanType.of(resultClass);
				do{
					list.add(readBean(context, rs, columns,bt));
				}while(rs.next());
			}
		}
		
		log.debug("Read {} rows from result(s)",list.size());
		
	    return list;
    }
	
	@SuppressWarnings("unchecked")
    protected <T> T readCurrentRow(SqlExecutionContext context, ResultSet rs,ResultColumn[] columns,Class<T> resultClass) throws SQLException {
		
		if(Record.class.equals(resultClass)) {
			return (T)readRecord(context, rs, columns);
		}
		
		if(Map.class.equals(resultClass)){
			return (T)readMap(context, rs, columns);
		}
		
		return readBean(context, rs, columns, resultClass);
	}
	
	protected Map<String, Object> readMap(SqlExecutionContext context, ResultSet rs,ResultColumn[] columns) throws SQLException {
		Map<String,Object> map = new LinkedHashMap<String, Object>(columns.length);
		
		readMap(context, rs, columns, map);
		
		return map;
	}
	
	protected Record readRecord(SqlExecutionContext context, ResultSet rs,ResultColumn[] columns) throws SQLException {
		Record r = new SimpleRecord(columns.length);
		
		readMap(context, rs, columns, r);
		
		return r;
	}
	
	protected <T> T readBean(SqlExecutionContext context, ResultSet rs,ResultColumn[] columns,Class<T> beanClass) throws SQLException {
		return readBean(context,rs,columns,BeanType.of(beanClass));
	}
	
	protected <T> T readBean(SqlExecutionContext context, ResultSet rs,ResultColumn[] columns,BeanType beanType) throws SQLException {
		T bean = beanType.newInstance();
		
		DbDialect dialect = context.dialect();
        NamingStrategy ns = context.getOrmContext().getNamingStrategy();
		
		for(int i=0;i<columns.length;i++){
			ResultColumn column = columns[i];
			BeanProperty     bp = null;

            String name = ns.columnToFieldName(column.fieldName);

			bp = beanType.tryGetProperty(name, true);
			
			if(null != bp){
				bp.setValue(bean, dialect.getColumnValue(rs, i+1, column.columnType));
			}
		}
		
		return bean;
	}
	
	protected void readMap(SqlExecutionContext context,ResultSet rs,ResultColumn[] columns,Map<String,Object> map) throws SQLException {
		DbDialect dialect = context.dialect();
		
		for(int i=0;i<columns.length;i++){
			ResultColumn column = columns[i];
			Object value = dialect.getColumnValue(rs, i+1, column.columnType);
			//map.put(getKey(column, column.fieldName), value);
			map.put(column.fieldName, value);
		}
	}

	private String getKey(ResultColumn cm, String key) {
		if(cm.isAlias){
			return key;
		}
		if(Strings.isBlank(cm.columnLabel) || Strings.equals(cm.columnLabel, cm.columnName)) {
			key = Strings.lowerCamel(key, '_');
		}
		return key;
	}


	protected static ResultColumn[] createResultColumns(SqlExecutionContext context, SqlCommand command, ResultSet rs) throws SQLException {
		ResultSetMetaData rsm = rs.getMetaData();
		NamingStrategy    ns  = context.getOrmContext().getNamingStrategy();
		
		ResultColumn[] columns = new ResultColumn[rsm.getColumnCount()];
		for(int i=1;i<=columns.length;i++){
			ResultColumn c = new ResultColumn();
			c.columnName  = rsm.getColumnName(i);
			c.columnLabel = rsm.getColumnLabel(i);
			c.columnType  = rsm.getColumnType(i);
			boolean isAlias = false;

//            if(command.getClause() instanceof DynamicSqlClause){
//				DynamicSqlClause sqlClause = (DynamicSqlClause)command.getClause();
				Sql sql = context.sql();// sqlClause.getSql();
				if(null != sql && sql.isSelect() && sql.nodes()[0] instanceof SqlSelect){
					SqlSelect selectCmd = (SqlSelect)sql.nodes()[0];
					if(selectCmd.isSelectItemAlias(c.columnLabel)){
						c.fieldName = selectCmd.getSelectItemAlias(c.columnLabel);
						isAlias = true;
					}
				}
//			}
			
			if(!isAlias){
				c.fieldName = ns.columnToFieldName(c.columnLabel);
			}
			c.isAlias = isAlias;
			columns[i-1] = c;
		}
		return columns;
	}
	
	protected static final class ResultColumn {
		protected String columnName;
		protected String columnLabel;
		protected int    columnType;
		protected String fieldName;
		protected boolean isAlias = false;
		
		@Override
        public String toString() {
			return "Column[name=" + columnName + ",label=" + columnLabel + ",field=" + fieldName + "']";
        }
	}
}

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
import leap.orm.mapping.FieldMapping;
import leap.orm.naming.NamingStrategy;
import leap.orm.sql.Sql;
import leap.orm.sql.SqlCommand;
import leap.orm.sql.SqlExecutionContext;
import leap.orm.sql.ast.AstNode;
import leap.orm.sql.ast.SqlObjectName;
import leap.orm.sql.ast.SqlSelect;
import leap.orm.sql.ast.SqlSelectList;

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
		return doReadList(context, rs, elementType, resultClass, command, false);
    }

	@Override
	public <T> List<T> readNativeList(SqlExecutionContext context, ResultSet rs, Class<T> elementType, Class<? extends T> resultClass, SqlCommand command) throws SQLException {
		return doReadList(context, rs, elementType, resultClass, command, true);
	}

	protected  <T> List<T> doReadList(SqlExecutionContext context, ResultSet rs, Class<T> elementType, Class<? extends T> resultClass, SqlCommand command, boolean readNative) throws SQLException {
		List<T> list = new ArrayList<T>();

		if(rs.next()){
			ResultColumn[] columns = createResultColumns(context, command, rs, readNative);

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
			
			if(null != bp && bp.isWritable()){
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
		return createResultColumns(context, command, rs, false);
	}

	protected static ResultColumn[] createResultColumns(SqlExecutionContext context, SqlCommand command, ResultSet rs, boolean readNative) throws SQLException {
		ResultSetMetaData rsm = rs.getMetaData();
		NamingStrategy    ns  = context.getOrmContext().getNamingStrategy();

		SqlSelect select = null;
		Sql sql = context.sql();
		if(null != sql && sql.isSelect() && sql.nodes()[0] instanceof SqlSelect){
			select = (SqlSelect)sql.nodes()[0];
		}
		
		ResultColumn[] columns = new ResultColumn[rsm.getColumnCount()];
		for(int i=1;i<=columns.length;i++){
			ResultColumn c = new ResultColumn();
			c.columnName  = rsm.getColumnName(i);
			c.columnLabel = rsm.getColumnLabel(i);
			c.columnType  = rsm.getColumnType(i);

			if(null != select && select.isSelectItemAlias(c.columnLabel)) {
				c.isAlias   = true;
				c.fieldName = select.getSelectItemAlias(c.columnLabel);
			}else {
				c.fieldName = readNative ?
								Strings.firstNotEmpty(c.columnLabel, c.columnName) :
								ns.columnToFieldName(c.columnLabel);
			}
			columns[i-1] = c;
		}

		if(null != select) {
		    for(AstNode node : select.getSelectList().getNodes()){
		        if(node instanceof SqlObjectName) {
		            SqlObjectName name = (SqlObjectName)node;
                    FieldMapping fm = name.getFieldMapping();
                    if(null != fm) {
                        ResultColumn c = findColumn(columns, fm.getColumnName());
                        if(null != c && !c.isAlias) {
                            c.fieldName = fm.getFieldName();
                        }
                    }
                }
            }
		}

		return columns;
	}

	protected static ResultColumn findColumn(ResultColumn[] columns, String name) {
	    for(ResultColumn c : columns) {
	        if(c.columnName.equalsIgnoreCase(name)) {
	            return c;
            }
        }
        return null;
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

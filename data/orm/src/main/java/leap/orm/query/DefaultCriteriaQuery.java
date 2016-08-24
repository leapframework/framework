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
package leap.orm.query;

import leap.core.exception.TooManyRecordsException;
import leap.core.jdbc.ResultSetReader;
import leap.core.jdbc.SimpleScalarReader;
import leap.core.jdbc.SimpleScalarsReader;
import leap.core.value.Scalar;
import leap.core.value.Scalars;
import leap.lang.Args;
import leap.lang.Strings;
import leap.lang.beans.DynaBean;
import leap.lang.params.ArrayParams;
import leap.lang.params.MapArrayParams;
import leap.lang.params.Params;
import leap.lang.params.ParamsMap;
import leap.lang.value.Limit;
import leap.orm.dao.Dao;
import leap.orm.linq.Condition;
import leap.orm.mapping.EntityMapping;
import leap.orm.mapping.FieldMapping;
import leap.orm.reader.ResultSetReaders;
import leap.orm.sql.SqlClause;
import leap.orm.sql.SqlStatement;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;

public class DefaultCriteriaQuery<T> extends AbstractQuery<T> implements CriteriaQuery<T>,QueryContext {
	
	protected SqlBuilder  			  builder;
	protected Predicate<FieldMapping> selectFilter;
	protected String	  			  where;
	protected String	   			  groupBy;
	protected String	   			  having;
	protected ArrayParams  			  whereParameters;

	public DefaultCriteriaQuery(Dao dao, EntityMapping em, Class<T> targetType) {
	    super(dao, targetType, em);
	    Args.notNull(em,"entity mapping");
	    this.builder = new SqlBuilder(em.getEntityName());
    }
	
	@Override
    public CriteriaQuery<T> params(Map<String, Object> params) {
	    return (CriteriaQuery<T>)super.params(params);
    }
	
	@Override
    public CriteriaQuery<T> params(Params params) {
	    return (CriteriaQuery<T>)super.params(params);
    }
	
	@Override
    public CriteriaQuery<T> params(DynaBean bean) {
	    return (CriteriaQuery<T>)super.params(bean);
    }
	
	@Override
    public CriteriaQuery<T> param(String name, Object value) {
	    return (CriteriaQuery<T>)super.param(name, value);
    }
	
	@Override
    public CriteriaQuery<T> limit(int startRows, int endRows) {
	    return (CriteriaQuery<T>)super.limit(startRows, endRows);
    }

	@Override
    public EntityMapping getEntityMapping() {
	    return em;
    }

	@Override
    public CriteriaQuery<T> alias(String alias) {
		Args.notEmpty(alias,"alias");
		builder.alias = alias;
	    return this;
    }
	
	@Override
    public CriteriaWhere<T> where() {
	    return new DefaultCriteriaWhere<T>(getOrmContext(), this);
    }
	
	@Override
    public CriteriaQuery<T> where(Condition<T> condition) {
		Args.notNull(condition,"condition");
		
		Params params = new ParamsMap();
		where(context.getConditionParser().parse(condition, params));
		if(!params.isEmpty()){
			params(params);
		}
		
		return this;
    }

	@Override
    public CriteriaQuery<T> where(String expression) {
		Args.notEmpty(expression = Strings.trim(expression),"where expression");
		where = expression;
	    return this;
    }
	
	@Override
    public CriteriaQuery<T> where(String expression, Object... args) {
		Args.notEmpty(expression = Strings.trim(expression),"where expression");
		where = expression;
		
		if(null != args && args.length > 0){
			this.whereParameters = new ArrayParams(args);
		}
		
	    return this;
    }

	@Override
    public CriteriaQuery<T> select(String... fields) {
		if(null == fields || fields.length == 0){
			builder.selects = null;
		}else{
			builder.selects = columns(fields);
		}
	    return this;
    }
	
	@Override
    public CriteriaQuery<T> select(Predicate<FieldMapping> filter) {
		this.selectFilter = filter;
	    return this;
    }

	@Override
    public CriteriaQuery<T> groupBy(String expression) {
	    this.groupBy = expression;
	    return this;
    }

	@Override
    public CriteriaQuery<T> having(String expression) {
		this.having = expression;
	    return this;
    }
	
	@Override
    public CriteriaQuery<T> limit(Integer size) {
	    return (CriteriaQuery<T>)super.limit(size);
    }

	@Override
    public CriteriaQuery<T> limit(Limit limit) {
	    return (CriteriaQuery<T>)super.limit(limit);
    }

	@Override
    public CriteriaQuery<T> orderBy(String expression) {
	    return (CriteriaQuery<T>)super.orderBy(expression);
    }

	@Override
    public CriteriaQuery<T> orderByIdAsc() {
		orderById("asc");
		return this;
    }

	@Override
    public CriteriaQuery<T> orderByIdDesc() {
		orderById("desc");
	    return this;
    }
	
	protected void orderById(String order) {
		StringBuilder s = new StringBuilder();

		for(int i=0;i<em.getKeyColumnNames().length;i++){
			if(i > 0){
				s.append(",");
			}
			
			s.append(builder.alias).append('.')
			 .append(em.getKeyColumnNames()[i])
			 .append(' ')
			 .append(order);
		}
		
		orderBy(s.toString());
	}

	@Override
    public long count() {
		String sql = builder.buildCountSql();
		SqlStatement statement = createQueryStatement(this,sql);
	    return statement.executeQuery(ResultSetReaders.forScalarValue(Long.class, false));
    }
	
	@Override
    public int delete() {
		String sql = builder.buildDeleteSql();
		SqlStatement statement = createUpdateStatement(this,sql);
	    return statement.executeUpdate();
    }

	@Override
    public int update(Map<String, Object> fields) {
		Args.notEmpty(fields,"update fields");
		String sql = builder.buildUpdateSql(fields, paramsMap());
		SqlStatement statement = createUpdateStatement(this,sql);
	    return statement.executeUpdate();
    }

	@Override
    protected QueryResult<T> executeQuery(QueryContext qc) {
		String sql = builder.buildSelectSql();
		SqlStatement statement = createQueryStatement(qc,sql);
		
		ResultSetReader<List<T>> reader = ResultSetReaders.forListEntity(dao.getOrmContext(), em, targetType, targetType);
		
		return new DefaultQueryResult<T>(sql,statement.executeQuery(reader));
    }
	
	@Override
    protected Scalar executeQueryForScalar(QueryContext context) throws TooManyRecordsException {
	    return buildQueryStatement(context).executeQuery(SimpleScalarReader.DEFAULT_INSTANCE);
    }

	@Override
    protected Scalars executeQueryForScalars(QueryContext context) throws TooManyRecordsException {
	    return buildQueryStatement(context).executeQuery(SimpleScalarsReader.DEFAULT_INSTANCE);
    }
	
	protected SqlStatement buildQueryStatement(QueryContext qc) {
		return createQueryStatement(qc,builder.buildSelectSql());
	}

	protected SqlStatement createQueryStatement(QueryContext qc, String sql) {
		SqlClause clause = context.getQueryFactory().createQueryClause(dao, sql);
		
		if(null != whereParameters){
			return clause.createQueryStatement(qc, whereParameters);
		}else{
			return clause.createQueryStatement(qc, params());
		}
	}
	
	protected SqlStatement createUpdateStatement(QueryContext qc, String sql) {
		SqlClause clause = context.getQueryFactory().createQueryClause(dao, sql);
		
		Object updateParams = null;
		if(null == whereParameters) {
			updateParams = params();
		}else {
			updateParams = new MapArrayParams(paramsMap(), whereParameters.array());
		}
		
		return clause.createUpdateStatement(qc, updateParams);
	}
	
	protected String[] columns(String[] fields){
		String[] columns = new String[fields.length];
		
		for(int i=0;i<fields.length;i++){
			columns[i] = column(fields[i]);
		}
		
		return columns;
	}
	
	protected String column(String field){
		FieldMapping fm = em.tryGetFieldMapping(field);
		return null == fm ? field : fm.getColumnName();
	}
	
	protected class SqlBuilder {
		protected String   table;
		protected String   alias = "t";
		protected String[] selects;
		
		private StringBuilder sql;
		
		protected SqlBuilder(String table){
			this.table = table;
		}
		
		public String buildDeleteSql() {
			sql = new StringBuilder();
			
			delete().from().where();
			
			return sql.toString();
		}
		
		public String buildSelectSql() {
			sql = new StringBuilder();
			
			select().columns().from().where().groupBy().orderBy();
			
			return sql.toString();
		}
		
		public String buildCountSql() {
			sql = new StringBuilder();
			
			select().count().from().where().groupBy();
			
			return sql.toString();
		}
		
		public String buildUpdateSql(Map<String, Object> fields, Map<String,Object> params) {
			sql = new StringBuilder();
			
			updateSetColumns(fields, params).where();
			
			return sql.toString();
		}
		
		protected SqlBuilder delete() {
			sql.append("delete");
			
			//MySQL:
			/*
				If you declare an alias for a table, you must use the alias when referring to the table:
				DELETE t1 FROM test AS t1, test2 WHERE ...
			 */
			if(context.getDb().isMySql() || context.getDb().isMariaDB()) {
				sql.append(" ").append(alias);
			}
			
			return this;
		}
		
		protected SqlBuilder select() {
			sql.append("select");
			return this;
		}
		
		protected SqlBuilder count() {
			sql.append(" count(*)");
			return this;
		}
		
		protected SqlBuilder updateSetColumns(Map<String, Object> columns, Map<String,Object> params) {
			sql.append("update ").append(table).append(" set ");
			
            int index = 0;
            for(Entry<String, Object> entry : columns.entrySet()){
            	String column = entry.getKey();
            	Object value  = entry.getValue();
            	String param  = "new_" + column;
            	
                if(index > 0){
                    sql.append(",");
                }
                
                sql.append(column).append("=").append(':').append(param);
                
                params.put(param, value);
                
                index++;
            }
			return this;
		}
		
		protected SqlBuilder columns() {
			sql.append(' ');
			
            if(null == selects || selects.length == 0){
            	if(null == selectFilter) {
            		sql.append(alias).append(".*");	
            	}else{
            		int index = 0;
            		for(FieldMapping fm : em.getFieldMappings()) {
            			if(selectFilter.test(fm)) {
                			if(index > 0) {
                				sql.append(',');
                			}
                			
                			sql.append(alias).append('.').append(fm.getColumnName());
                			index++;
            			}
            		}
            	}
                
            }else{
                int index = 0;
                for(String column : selects){
                    if(index > 0){
                        sql.append(",");
                    }
                    
                    sql.append(alias).append(".").append(column);
                    
                    index++;
                }
            } 
            
            return this;
		}
		
		protected SqlBuilder from() {
	        sql.append(" from ").append(table).append(" ").append(alias);
	        return this;
		}
		
		protected SqlBuilder where() {
	        if(!Strings.isEmpty(where)){
	        	where = where.trim();
	        	if(!Strings.startsWithIgnoreCase(where,"where")){
	        		sql.append(" where ").append(where);
	        	}else{
	        		sql.append(" ").append(where);
	        	}
	        }
	        return this;
		}
		
		protected SqlBuilder groupBy() {
	        if(!Strings.isEmpty(groupBy)){
	        	sql.append(" group by ").append(groupBy);
	        }
	        
	        if(!Strings.isEmpty(having)){
	        	sql.append(" having ").append(having);
	        }
	        
	        return this;
		}
		
		protected SqlBuilder orderBy() {
	        if(!Strings.isEmpty(orderBy)){
	        	sql.append(" order by ").append(orderBy);
	        }
	        return this;
		}
	}
}
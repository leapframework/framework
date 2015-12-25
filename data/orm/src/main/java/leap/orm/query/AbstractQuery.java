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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import leap.core.exception.EmptyRecordsException;
import leap.core.exception.TooManyColumnsException;
import leap.core.exception.TooManyRecordsException;
import leap.core.jdbc.JdbcExecutor;
import leap.core.value.Scalar;
import leap.core.value.Scalars;
import leap.lang.Args;
import leap.lang.beans.DynaBean;
import leap.lang.params.Params;
import leap.lang.value.Limit;
import leap.lang.value.Page;
import leap.orm.OrmContext;
import leap.orm.OrmMetadata;
import leap.orm.dao.Dao;
import leap.orm.mapping.EntityMapping;

public abstract class AbstractQuery<T> implements Query<T>,QueryContext {
	
	protected final OrmContext			context;
	protected final OrmMetadata         metadata;
	protected final Dao		            dao;
	protected final EntityMapping       em;
	protected final Class<T>            targetType;
	protected final Map<String, Object> params = new HashMap<String, Object>();
	
	protected Limit  limit;
	protected String orderBy;
	protected String groupBy;
	protected String having;
	
	protected AbstractQuery(Dao dao, Class<T> targetType){
		this(dao,targetType,null);
	}
	
	protected AbstractQuery(Dao dao, Class<T> targetType, EntityMapping entityMapping){
		this.dao        = dao;
		this.context	= dao.getOrmContext();
		this.metadata   = context.getMetadata();
		this.targetType = targetType;
		this.em 		= entityMapping;
	}
	
	@Override
    public EntityMapping getPrimaryEntityMapping() {
	    return em;
    }

	protected boolean isEntityType(){
		return null != em;
	}

	@Override
    public Query<T> param(String name, Object value) {
		params.put(name, value);
	    return this;
    }

	@Override
    public Query<T> params(Map<String, Object> params) {
		if(null != params){
			this.params.putAll(params);
		}
	    return this;
    }
	
    @Override
    public Query<T> params(Params params) {
    	if(null != params){
    		this.params.putAll(params.map());
    	}
	    return this;
    }

	@Override
    public Query<T> params(DynaBean bean) {
		if(null != bean){
			this.params.putAll(bean.getProperties());
		}
	    return this;
    }
	
	@Override
    public Query<T> orderBy(String expression) {
		this.orderBy = expression;
	    return this;
    }

	@Override
    public Query<T> limit(Integer size) {
		this.limit = Page.limit(size);
	    return this;
    }

	@Override
    public Query<T> limit(int startRows, int endRows) {
		this.limit = new Limit(startRows, endRows);
	    return this;
    }

	@Override
    public Query<T> limit(Limit limit) {
		this.limit = limit;
	    return this;
    }

	@Override
    public T first() throws EmptyRecordsException {
	    return limit(1).result().first();
    }

	@Override
    public T firstOrNull() {
	    return limit(1).result().firstOrNull();
    }

	@Override
    public T single() throws EmptyRecordsException, TooManyRecordsException {
	    return limit(2).result().single();
    }

	@Override
    public T singleOrNull() throws TooManyRecordsException {
	    return limit(2).result().singleOrNull();
    }

	@Override
    public List<T> list() {
	    return result().list();
    }
	
	@Override
    public OrmContext getOrmContext() {
	    return dao.getOrmContext();
    }

	@Override
    public JdbcExecutor getJdbcExecutor() {
	    return dao;
    }

	@Override
    public Limit getLimit() {
	    return limit;
    }

	@Override
    public String getOrderBy() {
	    return orderBy;
    }

	@Override
    public QueryResult<T> result() {
	    return executeQuery(this);
    }
	
	@Override
    public QueryResult<T> result(Limit limit) {
	    return null == limit ? executeQuery(this) : executeQuery(new LimitQueryContext(limit));
    }

	@Override
    public PageResult<T> pageResult(Page page) {
		Args.notNull(page,"page");
	    return new DefaultPageResult<T>(this, page);
    }
	
	@Override
    public Scalar scalar() throws EmptyRecordsException, TooManyRecordsException, TooManyColumnsException {
		Scalar scalar = scalarOrNull();
		
		if(null == scalar) {
			throw new EmptyRecordsException("No records, cannot return scalar value");
		}
		
		return scalar;
    }

	@Override
    public Scalar scalarOrNull() throws TooManyRecordsException {
	    return executeQueryForScalar(this);
    }

	@Override
    public Scalars scalars() {
	    return executeQueryForScalars(this);
    }

	protected abstract QueryResult<T> executeQuery(QueryContext context);
	
	protected abstract Scalar executeQueryForScalar(QueryContext context) throws TooManyRecordsException;
	
	protected abstract Scalars executeQueryForScalars(QueryContext context) throws TooManyRecordsException;
	
	protected class LimitQueryContext implements QueryContext {
		
		private final Limit limit;
		
		public LimitQueryContext(Limit limit) {
			this.limit = limit;
		}
		
		@Override
        public EntityMapping getPrimaryEntityMapping() {
	        return em;
        }

		@Override
        public OrmContext getOrmContext() {
	        return context;
        }

		@Override
        public JdbcExecutor getJdbcExecutor() {
	        return dao;
        }

		@Override
        public Limit getLimit() {
	        return limit;
        }

		@Override
        public String getOrderBy() {
	        return orderBy;
        }
	}
}
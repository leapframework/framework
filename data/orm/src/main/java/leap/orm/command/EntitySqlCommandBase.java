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
package leap.orm.command;

import java.util.HashMap;
import java.util.Map;

import leap.core.jdbc.ResultSetReader;
import leap.lang.Args;
import leap.lang.value.Limit;
import leap.lang.value.Page;
import leap.orm.dao.Dao;
import leap.orm.mapping.EntityMapping;
import leap.orm.query.QueryContext;
import leap.orm.sql.SqlCommand;

public abstract class EntitySqlCommandBase extends AbstractDaoCommand implements QueryContext {
	
	protected final EntityMapping em;
	protected final SqlCommand    sqlCommand;
	
	protected Page                page;
	protected StringBuilder       orderBy;
	protected Map<String, Object> params;

	public EntitySqlCommandBase(Dao dao,EntityMapping em,String commandName) {
		super(dao);
		
		this.em = em;
		this.sqlCommand = metadata.getSqlCommand(em.getEntityName(), commandName);
	}
	
	@Override
    public EntityMapping getPrimaryEntityMapping() {
	    return em;
    }

	@Override
    public Limit getLimit() {
	    return page;
    }

	@Override
    public String getOrderBy() {
	    return null == orderBy ? null : orderBy.toString();
    }

	@Override
	public String getGroupBy() {
		return null;
	}

	protected void param(String name,Object value){
		params().put(name, value);
	}
	
	protected void id(Object id){
		Args.notNull(id,"id");
		params().putAll(context.getParameterStrategy().createIdParameters(context, em, id).map());
	}
	
	protected Map<String, Object> params(){
		if(null == params){
			params = new HashMap<String, Object>();
		}
		return params;
	}

	protected <T> T executeQuery(ResultSetReader<T> reader){
		return sqlCommand.executeQuery(this, params, reader);
	}
	
	protected int executeUpdate(){
		return sqlCommand.executeUpdate(this, params);
	}
}

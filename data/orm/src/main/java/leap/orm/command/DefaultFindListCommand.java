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

import java.util.ArrayList;
import java.util.List;

import leap.core.exception.RecordNotFoundException;
import leap.core.exception.TooManyRecordsException;
import leap.core.jdbc.ResultSetReader;
import leap.db.DbDialect;
import leap.db.model.DbTable;
import leap.lang.params.MapParams;
import leap.lang.params.Params;
import leap.lang.value.Limit;
import leap.orm.dao.Dao;
import leap.orm.mapping.EntityMapping;
import leap.orm.mapping.FieldMapping;
import leap.orm.query.QueryContext;
import leap.orm.reader.ResultSetReaders;
import leap.orm.sql.SqlCommand;

public class DefaultFindListCommand<T> extends AbstractEntityDaoCommand implements FindListCommand<T>,QueryContext {
	
	protected final Class<T>           elementType;
	protected final Class<? extends T> resultClass;
	protected final Object[]     	   ids;
	protected final boolean            checkNotFound;
	
	protected SqlCommand sqlCommand;
	protected Object   	 idParameters;

	public DefaultFindListCommand(Dao dao, EntityMapping em, Object[] ids, Class<T> elementType, Class<? extends T> resultClass, boolean checkNotFound) {
		super(dao,em);
		
		this.elementType  = elementType;
		this.resultClass  = resultClass;
		this.ids          = ids;
		this.checkNotFound = checkNotFound;
		
		if(em.isCompositeKey()) {
			createCompositeIdCommand();
		}else{
			this.sqlCommand   = metadata.getSqlCommand(em.getEntityName(), SqlCommand.FIND_LIST_COMMAND_NAME);
			this.idParameters = createSingleIdParameters();
		}
	}
	
	@Override
    public Limit getLimit() {
	    return null;
    }

	@Override
    public String getOrderBy() {
	    return null;
    }

	@Override
	public String getGroupBy() {
		return null;
	}

	@Override
    public List<T> execute() throws TooManyRecordsException,RecordNotFoundException {
		ResultSetReader<List<T>> reader = ResultSetReaders.forListEntity(context, this, em, elementType, resultClass);
	    List<T> list = sqlCommand.executeQuery(this, idParameters, reader);
	    
	    if(list.size() > ids.length) {
	        throw new TooManyRecordsException("Returns " + list.size() + " records, exceeds the given id array's size " + ids.length);
	    }
	    
	    if(checkNotFound && list.size() < ids.length) {
	        throw new RecordNotFoundException("Returns " + list.size() + " records, less then the given id array's size " + ids.length);
	    }
	    
	    return list;
    }
	
	/*
	 
	 select * from t where (k1 = :k1_0 and k2 = :k2_0) or (k1 = :k1_1 and k2 = :k2_1) ...
	 
	 */
	protected void createCompositeIdCommand() {
		MapParams p = new MapParams();
		
		List<Params> ps = new ArrayList<Params>();
		for(Object id : ids) {
			if(id != null) {
				ps.add(context.getParameterStrategy().createIdParameters(context, em, id));
			}
		}
		
		String sql = createCompositeFindListSql(p, ps);
		
		this.sqlCommand   = context.getSqlFactory().createSqlCommand(context, sql);
		this.idParameters = p;
	}
	
	protected String createCompositeFindListSql(Params p, List<Params> ids){
		DbDialect dialect = context.getDb().getDialect();
		DbTable   table   = em.getTable();
		
		StringBuilder sql = new StringBuilder();
		
		sql.append("select * from ").append(dialect.qualifySchemaObjectName(table)).append(" where ");
		
		for(int i=0;i<ids.size();i++) {
			Params id = ids.get(i);
			
			if(i > 0) {
				sql.append(" or ");
			}
			
			sql.append('(');
			int index = 0;
			for(FieldMapping key : em.getKeyFieldMappings()){
				if(index > 0){
					sql.append(" and ");
				}
				
				String pname = key.getFieldName() + "_" + i;
				
				sql.append(dialect.quoteIdentifier(key.getColumnName()))
				   .append(" = :")
				   .append(pname);
				
				if(id.isIndexed()) {
					p.set(pname, id.get(index));
				}else{
					p.set(pname, id.get(key.getFieldName()));
				}
				
				index++;
			}
			sql.append(')');
		}
		
		return sql.toString();
	}
	
	protected Object createSingleIdParameters() {
		MapParams p = new MapParams();
		p.set(em.getKeyFieldNames()[0], ids);
		return p;
	}
}
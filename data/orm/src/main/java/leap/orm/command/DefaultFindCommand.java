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

import leap.core.exception.RecordNotFoundException;
import leap.core.exception.TooManyRecordsException;
import leap.core.jdbc.ResultSetReader;
import leap.lang.params.Params;
import leap.lang.value.Limit;
import leap.orm.dao.Dao;
import leap.orm.mapping.EntityMapping;
import leap.orm.query.QueryContext;
import leap.orm.reader.ResultSetReaders;
import leap.orm.sql.SqlCommand;

public class DefaultFindCommand<T> extends AbstractEntityDaoCommand implements FindCommand<T>,QueryContext {
	
    protected final Class<T>   resultClass;
    protected final SqlCommand sqlCommand;
    protected final Object     id;
    protected final Params     idParameters;
    protected final boolean    checkNotFound;

	public DefaultFindCommand(Dao dao, EntityMapping em, Object id, Class<T> resultClass, boolean checkNotFound) {
		super(dao,em);
		
		this.resultClass  = resultClass;
		this.sqlCommand   = metadata.getSqlCommand(em.getEntityName(), SqlCommand.FIND_COMMAND_NAME);
		this.id           = id;
		this.idParameters = context.getParameterStrategy().createIdParameters(context, em, id);
		this.checkNotFound = checkNotFound;
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
    public T execute() throws TooManyRecordsException {
		ResultSetReader<T> reader = ResultSetReaders.forSingleEntity(context, em, resultClass);
		
	    T result = sqlCommand.executeQuery(this, idParameters, reader);
	    
	    if(null == result && checkNotFound) {
	        throw new RecordNotFoundException("Record not found for the id '" + id + "'");
	    }
	    
	    return result;
    }
}
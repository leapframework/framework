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

import java.util.List;

import leap.core.exception.TooManyRecordsException;
import leap.core.jdbc.ResultSetReader;
import leap.lang.value.Limit;
import leap.orm.dao.Dao;
import leap.orm.mapping.EntityMapping;
import leap.orm.query.QueryContext;
import leap.orm.reader.ResultSetReaders;
import leap.orm.sql.SqlCommand;

public class DefaultFindAllCommand<T> extends AbstractEntityDaoCommand implements FindAllCommand<T>,QueryContext {
	
	protected final Class<T>           elementType;
	protected final Class<? extends T> resultClass;
	protected final SqlCommand         sqlCommand;

	public DefaultFindAllCommand(Dao dao, EntityMapping em, Class<T> elementType, Class<? extends T> resultClass) {
		super(dao,em);
		
		this.elementType = elementType;
		this.resultClass = resultClass;
		this.sqlCommand  = metadata.getSqlCommand(em.getEntityName(), SqlCommand.FIND_ALL_COMMAND_NAME);
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
    public List<T> execute() throws TooManyRecordsException {
		ResultSetReader<List<T>> reader = ResultSetReaders.forListEntity(context, this, em, elementType, resultClass);
	    return sqlCommand.executeQuery(this, null, reader);
    }
}
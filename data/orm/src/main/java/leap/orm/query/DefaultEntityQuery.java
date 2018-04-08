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
import leap.orm.dao.Dao;
import leap.orm.mapping.EntityMapping;
import leap.orm.reader.ResultSetReaders;
import leap.orm.sql.SqlClause;
import leap.orm.sql.SqlCommand;

import java.util.List;

public class DefaultEntityQuery<T> extends AbstractQuery<T> implements EntityQuery<T> {

	protected final SqlCommand         command;
	protected final Class<? extends T> resultClass;
	
	public DefaultEntityQuery(Dao dao, EntityMapping em, SqlCommand command, Class<T> targetType) {
		this(dao,em,command,targetType,targetType);
	}
	
	public DefaultEntityQuery(Dao dao, EntityMapping em, SqlCommand command, Class<T> targetType, Class<? extends T> resultClass) {
	    super(dao, targetType, em);
	    this.command     = command;
	    this.resultClass = resultClass;
    }
	
	@Override
    public EntityMapping getEntityMapping() {
	    return em;
    }
	
	@Override
    public long count() {
	    return command.executeCount(this, params());
    }

	@Override
    protected QueryResult<T> executeQuery(QueryContext qc) {
		ResultSetReader<List<T>> reader = ResultSetReaders.forListEntity(dao.getOrmContext(), qc, em, targetType, resultClass);
		
	    return new DefaultQueryResult<T>(command.toString(),command.executeQuery(qc, params(), reader));
    }

	@Override
    protected Scalar executeQueryForScalar(QueryContext context) throws TooManyRecordsException {
	    return command.executeQuery(context, params(), SimpleScalarReader.DEFAULT_INSTANCE);
    }

	@Override
    protected Scalars executeQueryForScalars(QueryContext context) throws TooManyRecordsException {
	    return command.executeQuery(context, params(), SimpleScalarsReader.DEFAULT_INSTANCE);
    }

	@Override
	public SqlClause getSqlClause() {
		return command.getSqlClause();
	}
}
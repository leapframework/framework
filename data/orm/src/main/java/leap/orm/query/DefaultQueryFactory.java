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

import leap.core.annotation.Inject;
import leap.core.annotation.M;
import leap.orm.dao.Dao;
import leap.orm.mapping.EntityMapping;
import leap.orm.sql.SqlClause;
import leap.orm.sql.SqlCommand;
import leap.orm.sql.SqlFactory;
import leap.orm.sql.SqlLanguage;

public class DefaultQueryFactory implements QueryFactory {
	
    protected @Inject @M SqlLanguage queryLanguage;
    protected @Inject @M SqlFactory  sqlFactory;
	
	@Override
    public <T> Query<T> createQuery(Dao dao, Class<T> resultClass, String sql) {
		return createQuery(dao, resultClass, sqlFactory.createSqlCommand(dao.getOrmContext(), sql));
    }

	@Override
    public <T> CriteriaQuery<T> createCriteriaQuery(Dao dao, EntityMapping em, Class<T> targetType) {
	    return new DefaultCriteriaQuery<T>(dao, em, targetType);
    }

	@Override
	public <T> Query<T> createQuery(Dao dao, Class<T> targetType, SqlCommand command) {
		return new DefaultCommandQuery<T>(dao,command,targetType);
	}
	
	@Override
    public <T> EntityQuery<T> createEntityQuery(Dao dao, EntityMapping em, Class<T> resultClass, String sql) {
	    return createEntityQuery(dao, em, resultClass, sqlFactory.createSqlCommand(dao.getOrmContext(), sql));
    }

	@Override
	public <T> EntityQuery<T> createEntityQuery(Dao dao, EntityMapping em, Class<T> targetType, SqlCommand command) {
		return new DefaultEntityQuery<T>(dao, em, command, targetType);
	}
	
	@Override
    public SqlClause createQueryClause(Dao dao, String sql) {
	    return queryLanguage.parseClause(dao.getOrmContext(), sql);
    }
}

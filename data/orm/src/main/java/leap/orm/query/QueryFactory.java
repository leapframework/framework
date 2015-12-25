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

import leap.orm.dao.Dao;
import leap.orm.mapping.EntityMapping;
import leap.orm.sql.SqlClause;
import leap.orm.sql.SqlCommand;

public interface QueryFactory {
	
	<T> Query<T> createQuery(Dao dao,Class<T> resultClass, String sql);
	
	<T> Query<T> createQuery(Dao dao,Class<T> resultClass, SqlCommand command);
	
	<T> CriteriaQuery<T> createCriteriaQuery(Dao dao,EntityMapping em, Class<T> resultClass);
	
	<T> EntityQuery<T> createEntityQuery(Dao dao, EntityMapping em, Class<T> resultClass, String sql);
	
	<T> EntityQuery<T> createEntityQuery(Dao dao,EntityMapping em, Class<T> resultClass, SqlCommand command);
	
	SqlClause createQueryClause(Dao dao, String sql);
}
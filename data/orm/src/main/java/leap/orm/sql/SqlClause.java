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
package leap.orm.sql;

import leap.orm.query.QueryContext;

public interface SqlClause {
	
    /**
     * Creates a sql update statement.
     */
	SqlStatement createUpdateStatement(SqlContext context,Object params);

    /**
     * Creates a sql query statement.
     */
	SqlStatement createQueryStatement(QueryContext context,Object params);

    /**
     * Creates a 'select count(*)' sql statement.
     */
	SqlStatement createCountStatement(QueryContext context,Object params);

    /**
     * Creates a batch sql statement.
     */
	BatchSqlStatement createBatchStatement(SqlContext context, Object[] params);
	
}
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

import leap.core.jdbc.JdbcExecutor;
import leap.db.Db;
import leap.orm.OrmContext;
import leap.orm.OrmMetadata;
import leap.orm.dao.Dao;
import leap.orm.sql.SqlContext;

public abstract class AbstractDaoCommand implements SqlContext,DaoCommand {

	protected final Dao         dao;
	protected final OrmContext  context;
	protected final OrmMetadata metadata;
	protected final Db			db;

	protected AbstractDaoCommand(Dao dao){
		this.dao      = dao;
		this.context  = dao.getOrmContext();
		this.metadata = context.getMetadata();
		this.db		  = context.getDb();
	}

	@Override
    public OrmContext getOrmContext() {
	    return dao.getOrmContext();
    }

	@Override
    public JdbcExecutor getJdbcExecutor() {
	    return dao;
    }
}
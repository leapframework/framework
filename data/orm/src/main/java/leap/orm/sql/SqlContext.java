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

import leap.core.jdbc.JdbcExecutor;
import leap.db.Db;
import leap.db.DbDialect;
import leap.orm.OrmContext;
import leap.orm.mapping.EntityMapping;

public interface SqlContext extends SqlLanguage.Options {

    /**
     * Returns current query sql or null.
     */
    Sql getQuerySql();

    /**
     * Sets current query sql.
     */
    void setQuerySql(Sql sql);
    
    /**
     * Returns the {@link DbDialect}.
     */
    default DbDialect dialect() {
        return getOrmContext().getDb().getDialect();
    }

    /**
     * Returns the {@link Db}.
     */
    default Db db() {
        return getOrmContext().getDb();
    }

    /**
     * Returns true if current sql execution is {@link SqlCommand#FIND_COMMAND_NAME} .
     */
    default boolean isFind() {
        return false;
    }

    /**
     * Returns the {@link OrmContext}.
     */
	OrmContext getOrmContext();
	
	/**
	 * Returns the {@link JdbcExecutor}.
	 */
	JdbcExecutor getJdbcExecutor();
	
	/**
	 * Returns the primary {@link EntityMapping} in this sql or <code>null</code>.
	 */
	EntityMapping getPrimaryEntityMapping();

}
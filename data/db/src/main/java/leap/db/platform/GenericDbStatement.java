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
package leap.db.platform;

import leap.db.DbError;
import leap.db.DbStatement;
import leap.lang.Args;

public class GenericDbStatement implements DbStatement {
	
	protected final String sql;
	
	protected boolean executed;
	protected DbError error;
	protected int     affected = -1;
	
	protected GenericDbStatement(String sql){
		Args.notEmpty(sql,"sql");
		this.sql = sql;
	}

	@Override
    public String sql() {
	    return sql;
    }

	@Override
    public int affected() {
	    return affected;
    }

	@Override
    public DbError error() {
	    return error;
    }

	@Override
    public boolean isExecuted() {
	    return executed;
    }

	@Override
    public boolean isError() {
	    return null != error;
    }
	
	protected void success(int affected){
		this.affected = affected;
		this.executed = true;
	}
	
	protected void error(DbError error){
		this.error    = error;
		this.executed = true;
	}
}
/*
 * Copyright 2014 the original author or authors.
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class DefaultPreparedBatchSqlStatementBuilder implements PreparedBatchSqlStatementBuilder {
	
	private final Sql 				 sql;
	private final AtomicInteger	     pi     = new AtomicInteger(-1);
	private final StringBuilder	     buf    = new StringBuilder();
	private final List<SqlParameter> params = new ArrayList<SqlParameter>();
	
	public DefaultPreparedBatchSqlStatementBuilder(Sql sql) {
		this.sql = sql;
	}
	
	@Override
    public Sql sql() {
	    return sql;
    }

	@Override
	public Appendable append(CharSequence csq) throws IOException {
		buf.append(csq);
		return this;
	}

	@Override
	public Appendable append(CharSequence csq, int start, int end) throws IOException {
		buf.append(csq,start,end);
		return this;
	}

	@Override
	public Appendable append(char c) throws IOException {
		buf.append(c);
		return this;
	}
	
	@Override
    public int getIndexedParametersCount() {
	    return pi.get() + 1;
    }

	@Override
    public int increaseAndGetParameterIndex() {
	    return pi.incrementAndGet();
    }

	@Override
	public PreparedBatchSqlStatementBuilder addBatchParameter(SqlParameter p) {
		params.add(p);
		return this;
	}

	@Override
    public PreparedBatchSqlStatement build() {
	    return new DefaultPreparedSqlStatement(buf.toString(), params.toArray(new SqlParameter[params.size()]));
    }

	protected static final class DefaultPreparedSqlStatement implements PreparedBatchSqlStatement {
		private final String 		 sql;
		private final SqlParameter[] params;
		
		public DefaultPreparedSqlStatement(String sql, SqlParameter[] params) {
			this.sql    = sql;
			this.params = params;
		}

		@Override
        public SqlParameter[] getBatchParameters() {
	        return params;
        }

		@Override
        public BatchSqlStatement createBatchSqlStatement(SqlContext context, Object[][] args) {
	        return new DefaultSqlStatement(context, sql, args, null);
        }
	}
}

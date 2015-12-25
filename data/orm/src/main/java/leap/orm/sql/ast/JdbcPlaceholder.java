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
package leap.orm.sql.ast;

import java.io.IOException;

import leap.lang.jdbc.JDBC;
import leap.lang.params.Params;
import leap.orm.sql.JdbcSqlParameter;
import leap.orm.sql.PreparedBatchSqlStatementBuilder;
import leap.orm.sql.SqlContext;
import leap.orm.sql.SqlStatementBuilder;

public class JdbcPlaceholder extends ParamBase {
	
	@Override
    protected void prepareBatchStatement_(SqlContext context, PreparedBatchSqlStatementBuilder stm) throws IOException {
		stm.append(JDBC.PARAMETER_PLACEHOLDER_CHAR);
		stm.addBatchParameter(new JdbcSqlParameter(stm.increaseAndGetParameterIndex()));
    }

	@Override
    protected void toString_(Appendable buf) throws IOException {
		buf.append('?');
    }

	@Override
    protected Object getParameterValue(SqlStatementBuilder stm, Params params) {
		throw new IllegalArgumentException("Cannot get value of jdbc placeholder '?' from non indexed parameters '" + params + "'");
    }

}
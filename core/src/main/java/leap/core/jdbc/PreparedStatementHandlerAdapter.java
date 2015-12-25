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
package leap.core.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class PreparedStatementHandlerAdapter<T> implements PreparedStatementHandler<T> {

	@Override
	public PreparedStatement preparedStatement(T context, Connection connection, String sql) throws SQLException {
		return null;
	}

	@Override
	public boolean setParameters(T context, Connection connection, PreparedStatement ps, Object[] args, int[] types) throws SQLException {
		return false;
	}

	@Override
	public void preExecuteUpdate(T context, Connection connection, PreparedStatement ps) throws SQLException {

	}

	@Override
	public void postExecuteUpdate(T context, Connection connection, PreparedStatement ps, int updatedResult) throws SQLException {

	}

	@Override
	public void preExecuteQuery(T context, Connection connection, PreparedStatement ps) throws SQLException {

	}

	@Override
	public void postExecuteQuery(T context, Connection connection, PreparedStatement ps, ResultSet rs) throws SQLException {

	}
}
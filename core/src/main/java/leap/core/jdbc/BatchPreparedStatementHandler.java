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
package leap.core.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface BatchPreparedStatementHandler<T> {

	/**
	 * Returns a {@link PreparedStatement} object if this handler can handle the creation of {@link PreparedStatement}.
	 * 
	 * <p>
	 * Returns <code>null</code> if this handler not handle the creation of {@link PreparedStatement}.
	 */
	PreparedStatement preparedStatement(T context,Connection connection, String sql, Object[] batchArgs, int[] types) throws SQLException;
	
	/**
	 * Sets the sql parameters of the given {@link PreparedStatement} if this handler can handle the setting of parameters.
	 * 
	 * <p>
	 * Returns <code>true</code> if this handler handle the setting of sql parameters.
	 * 
	 * <p>
	 * Returns <code>false</code> if this handle not handle the setting of sql parameters.
	 */
	boolean setBatchParameters(T context, Connection connection, PreparedStatement ps, Object[] args, int[] types, int batchIndex) throws SQLException;
	
	void preExecuteBatchUpdate(T context, Connection connection, PreparedStatement ps) throws SQLException;
	
	void ostExecuteBatchUpdate(T context,Connection connection, PreparedStatement ps, int[] updatedResult) throws SQLException;
	
}

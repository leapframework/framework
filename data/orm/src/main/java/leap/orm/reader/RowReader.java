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
package leap.orm.reader;

import leap.core.exception.TooManyRecordsException;
import leap.orm.sql.SqlCommand;
import leap.orm.sql.SqlExecutionContext;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public interface RowReader {
	
	<T> T readFirst(SqlExecutionContext context, ResultSet rs, Class<T> resultClass,SqlCommand command) throws SQLException;

	<T> T readSingle(SqlExecutionContext context, ResultSet rs, Class<T> resultClass, SqlCommand command) throws SQLException, TooManyRecordsException;
	
	<T> List<T> readList(SqlExecutionContext context, ResultSet rs, Class<T> elementType, Class<? extends T> resultClass, SqlCommand command) throws SQLException;

}
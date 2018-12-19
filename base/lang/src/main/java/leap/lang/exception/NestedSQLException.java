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
package leap.lang.exception;

import java.sql.SQLException;

public class NestedSQLException extends RuntimeException {

	private static final long serialVersionUID = 3382331577872459024L;

	private final String databaseName;

    public NestedSQLException(SQLException cause) {
        this(cause, null);
    }

    public NestedSQLException(SQLException cause, String databaseName) {
		super(cause);
		this.databaseName = databaseName;
	}

    public NestedSQLException(String message, SQLException cause) {
        this(message, cause, null);
    }

	public NestedSQLException(String message, SQLException cause, String databaseName) {
		super(message, cause);
		this.databaseName = databaseName;
	}

	public SQLException getSQLException(){
		return (SQLException)getCause();
	}

    public String getDatabaseName() {
        return databaseName;
    }
}
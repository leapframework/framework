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
package leap.lang.jdbc;

import java.sql.Connection;

public enum TransactionIsolation {
	NONE(Connection.TRANSACTION_NONE), 
	READ_COMMITTED(Connection.TRANSACTION_READ_COMMITTED), 
	READ_UNCOMMITTED(Connection.TRANSACTION_READ_UNCOMMITTED), 
	REPEATABLE_READ(Connection.TRANSACTION_REPEATABLE_READ), 
	SERIALIZABLE(Connection.TRANSACTION_SERIALIZABLE);

	private final int value;

	private TransactionIsolation(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

}
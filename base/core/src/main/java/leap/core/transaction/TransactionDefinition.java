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
package leap.core.transaction;

import java.sql.Connection;

public interface TransactionDefinition {

    /**
     * @see <a href="http://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/transaction/annotation/Propagation.html">Spring's Propagation</a>
     */
	enum Propagation {

		/**
		 * Support a current transaction; create a new one if none exists.
		 */
		REQUIRED(0),
		
		/**
		 * Create a new transaction, suspending the current transaction if one exists.
		 */
		REQUIRES_NEW(3);
		
		private final int value;
		
		Propagation(int value) {
			this.value = value;
        }

		public int getValue() {
			return value;
		}
	}

    /**
     * @see <a href="http://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/transaction/annotation/Isolation.html">Spring's Isolation</a>
     */
	enum Isolation {
		
		/**
		 * Use the default isolation level of the underlying datasource.
		 * @see java.sql.Connection
		 */
		DEFAULT(-1),
		
		/**
		 * @see java.sql.Connection#TRANSACTION_READ_UNCOMMITTED
		 */
		READ_UNCOMMITTED(Connection.TRANSACTION_READ_UNCOMMITTED),
		
		/**
		 * @see java.sql.Connection#TRANSACTION_READ_COMMITTED
		 */
		READ_COMMITTED(Connection.TRANSACTION_READ_COMMITTED),
		
		/**
		 * @see java.sql.Connection#TRANSACTION_REPEATABLE_READ
		 */
		REPEATABLE_READ(Connection.TRANSACTION_REPEATABLE_READ),
		
		/**
		 * @see java.sql.Connection#TRANSACTION_SERIALIZABLE
		 */
		SERIALIZABLE(Connection.TRANSACTION_SERIALIZABLE);
		
		private final int value;
		
		Isolation(int value) {
	        this.value = value;
        }

		public int getValue() {
			return value;
		}
	}

	/**
	 * Return the propagation behavior.
	 */
	Propagation getPropagation();

	/**
	 * Return the isolation level.
	 */
	Isolation getIsolation();
}

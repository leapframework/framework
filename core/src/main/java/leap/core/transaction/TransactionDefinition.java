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

//from spring framework
public interface TransactionDefinition {
	
	public enum PropagationBehaviour {
		/**
		 * Support a current transaction; create a new one if none exists.
		 * Analogous to the EJB transaction attribute of the same name.
		 * <p>This is typically the default setting of a transaction definition,
		 * and typically defines a transaction synchronization scope.
		 */
		REQUIRED(0),
		
		/**
		 * Create a new transaction, suspending the current transaction if one exists.
		 * Analogous to the EJB transaction attribute of the same name.
		 * <p><b>NOTE:</b> Actual transaction suspension will not work out-of-the-box
		 * on all transaction managers. 
		 */
		REQUIRES_NEW(3);
		
		private final int value;
		
		private PropagationBehaviour(int value) {
			this.value = value;
        }

		public int getValue() {
			return value;
		}
	}
	
	public enum IsolationLevel{
		
		/**
		 * Use the default isolation level of the underlying datastore.
		 * All other levels correspond to the JDBC isolation levels.
		 * @see java.sql.Connection
		 */
		DEFAULT(-1),
		
		/**
		 * Indicates that dirty reads, non-repeatable reads and phantom reads
		 * can occur.
		 * <p>This level allows a row changed by one transaction to be read by another
		 * transaction before any changes in that row have been committed (a "dirty read").
		 * If any of the changes are rolled back, the second transaction will have
		 * retrieved an invalid row.
		 * @see java.sql.Connection#TRANSACTION_READ_UNCOMMITTED
		 */
		READ_UNCOMMITTED(Connection.TRANSACTION_READ_UNCOMMITTED),
		
		/**
		 * Indicates that dirty reads are prevented; non-repeatable reads and
		 * phantom reads can occur.
		 * <p>This level only prohibits a transaction from reading a row
		 * with uncommitted changes in it.
		 * @see java.sql.Connection#TRANSACTION_READ_COMMITTED
		 */
		READ_COMMITTED(Connection.TRANSACTION_READ_COMMITTED),
		
		/**
		 * Indicates that dirty reads and non-repeatable reads are prevented;
		 * phantom reads can occur.
		 * <p>This level prohibits a transaction from reading a row with uncommitted changes
		 * in it, and it also prohibits the situation where one transaction reads a row,
		 * a second transaction alters the row, and the first transaction re-reads the row,
		 * getting different values the second time (a "non-repeatable read").
		 * @see java.sql.Connection#TRANSACTION_REPEATABLE_READ
		 */
		REPEATABLE_READ(Connection.TRANSACTION_REPEATABLE_READ),
		
		/**
		 * Indicates that dirty reads, non-repeatable reads and phantom reads
		 * are prevented.
		 * <p>This level includes the prohibitions in {@link #ISOLATION_REPEATABLE_READ}
		 * and further prohibits the situation where one transaction reads all rows that
		 * satisfy a {@code WHERE} condition, a second transaction inserts a row
		 * that satisfies that {@code WHERE} condition, and the first transaction
		 * re-reads for the same condition, retrieving the additional "phantom" row
		 * in the second read.
		 * @see java.sql.Connection#TRANSACTION_SERIALIZABLE
		 */
		SERIALIZABLE(Connection.TRANSACTION_SERIALIZABLE);
		
		private final int value;
		
		private IsolationLevel(int value) {
	        this.value = value;
        }

		public int getValue() {
			return value;
		}
	}

	/**
	 * Use the default timeout of the underlying transaction system,
	 * or none if timeouts are not supported.
	 */
	int TIMEOUT_DEFAULT = -1;

	/**
	 * Return the propagation behavior.
	 * <p>Must return one of the {@code PROPAGATION_XXX} constants
	 * defined on {@link TransactionDefinition this interface}.
	 * @return the propagation behavior
	 * @see #PROPAGATION_REQUIRED
	 * @see org.springframework.transaction.support.TransactionSynchronizationManager#isActualTransactionActive()
	 */
	PropagationBehaviour getPropagationBehavior();

	/**
	 * Return the isolation level.
	 * <p>Must return one of the {@code ISOLATION_XXX} constants
	 * defined on {@link TransactionDefinition this interface}.
	 * <p>Only makes sense in combination with {@link #PROPAGATION_REQUIRED}
	 * or {@link #PROPAGATION_REQUIRES_NEW}.
	 * <p>Note that a transaction manager that does not support custom isolation levels
	 * will throw an exception when given any other level than {@link #ISOLATION_DEFAULT}.
	 * @return the isolation level
	 */
	IsolationLevel getIsolationLevel();
}

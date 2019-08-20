/*
 * Copyright 2015 the original author or authors.
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
package leap.db.cp;

import leap.lang.Strings;
import leap.lang.jdbc.TransactionIsolation;


class PoolConfig {

	private final boolean 			   testOnBorrow;
	private final String  			   validationQuery;
	private final int                  validationTimeout;
	private final boolean              defaultAutoCommit;
	private final TransactionIsolation defaultTransactionIsolation;
	private final String               defaultCatalog;
	private final boolean              defaultReadOnly;
	private final boolean              rollbackPendingTransaction;
	private final boolean              throwPendingTransactionException;
	private final long                 maxWait;
	private final int                  maxActive;
	private final int                  maxIdle;
	private final int                  minIdle;
	private final int                  healthCheckIntervalMs;
	private final boolean              healthCheck;
	private final long                 idleTimeoutMs;
	private final int                  statementTimeout;
	private final long                 connectionLeakTimeoutMs;
	private final boolean			   initializationFailRetry;
	private final int			   	   initializationFailRetryIntervalMs;

	PoolConfig(PoolProperties props) {
		this.testOnBorrow      			       = props.isTestOnBorrow();
		this.validationQuery   			       = Strings.trimToNull(props.getValidationQuery());
		this.validationTimeout 			       = props.getValidationTimeout();
		this.defaultAutoCommit 			       = props.isDefaultAutoCommit();
		this.defaultTransactionIsolation       = props.getDefaultTransactionIsolation();
		this.defaultCatalog				       = Strings.trimToNull(props.getDefaultCatalog());
		this.defaultReadOnly                   = props.isDefaultReadonly();
		this.rollbackPendingTransaction        = props.isRollbackPendingTransaction();
		this.throwPendingTransactionException  = props.isThrowPendingTransactionException();
		this.maxWait           			       = props.getMaxWait();
		this.maxActive           			   = props.getMaxActive();
		this.maxIdle						   = props.getMaxIdle();
		this.minIdle						   = props.getMinIdle();
		this.healthCheckIntervalMs             = props.getHealthCheckIntervalMs();
		this.healthCheck					   = props.isHealthCheck();
		this.idleTimeoutMs					   = props.getIdleTimeoutMs();
		this.statementTimeout				   = props.getStatementTimeout();
		this.connectionLeakTimeoutMs           = props.getConnectionLeakTimeoutMs();
		this.initializationFailRetry           = props.isInitializationFailRetry();
		this.initializationFailRetryIntervalMs = 1000 * (props.getInitializationFailRetryInterval() > 0 ?
													props.getInitializationFailRetryInterval() : 1);
	}

	public boolean isTestOnBorrow() {
		return testOnBorrow;
	}

	public String getValidationQuery() {
		return validationQuery;
	}

	public boolean hasValidationQuery() {
		return null != validationQuery;
	}

	public int getValidationTimeout() {
		return validationTimeout;
	}

	public boolean isDefaultAutoCommit() {
		return defaultAutoCommit;
	}

	public boolean hasDefaultTransactionIsolation() {
		return null != defaultTransactionIsolation;
	}

	public TransactionIsolation getDefaultTransactionIsolation() {
		return defaultTransactionIsolation;
	}

	public boolean hasDefaultCatalog() {
		return null != defaultCatalog;
	}

	public String getDefaultCatalog() {
		return defaultCatalog;
	}

	public boolean isDefaultReadOnly() {
		return defaultReadOnly;
	}

	public boolean isRollbackPendingTransaction() {
		return rollbackPendingTransaction;
	}

	public boolean isThrowPendingTransactionException() {
		return throwPendingTransactionException;
	}

	public long getMaxWait() {
		return maxWait;
	}

	public int getMaxActive() {
		return maxActive;
	}

	public boolean hasMaxIdle() {
		return maxIdle >= 0;
	}

	public int getMaxIdle() {
		return maxIdle;
	}

	public boolean hasMinIdle() {
		return minIdle > 0;
	}

	public int getMinIdle() {
		return minIdle;
	}

	public long getIdleTimeoutMs() {
		return idleTimeoutMs;
	}

	public int getStatementTimeout() {
		return statementTimeout;
	}

	public boolean isDetecteConnectionLeak() {
		return connectionLeakTimeoutMs > 0;
	}

	public long getConnectionLeakTimeoutMs() {
		return connectionLeakTimeoutMs;
	}

	public int getHealthCheckIntervalMs() {
		return healthCheckIntervalMs;
	}

	public boolean isHealthCheck() {
		return healthCheck;
	}

	public boolean isInitializationFailRetry() {
		return initializationFailRetry;
	}

    public int getInitializationFailRetryIntervalMs() {
        return initializationFailRetryIntervalMs;
    }
}
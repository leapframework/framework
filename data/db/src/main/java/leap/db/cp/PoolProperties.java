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

import leap.lang.Beans;
import leap.lang.jdbc.TransactionIsolation;

import javax.sql.DataSource;
import java.util.Properties;

public class PoolProperties {

    public static final int DEFAULT_MAX_WAIT                = 30 * 1000; //30 seconds
    public static final int DEFAULT_MAX_ACTIVE              = 50;
    public static final int DEFAULT_MAX_IDLE                = 10;
    public static final int DEFAULT_MIN_IDLE                = 0;
    public static final int DEFAULT_VALIDATION_TIMEOUT      = 5;    //5 seconds
    public static final int DEFAULT_HEALTH_CHECK_INTERVAL   = 10;    //1 seconds
    public static final int DEFAULT_IDLE_TIMEOUT            = 1800; //30 minutes
    public static final int DEFAULT_STATEMENT_TIMEOUT       = -1;
    public static final int DEFAULT_CONNECTION_LEAK_TIMEOUT = 60 * 5 * 1000; //5 minutes.

    public static final int MAX_MAX_WAIT              = 10 * 60 * 1000; //10 minutes
    public static final int MIN_MAX_WAIT              = 0;
    public static final int MAX_MAX_ACTIVE            = 10000;
    public static final int MIN_MAX_ACTIVE            = 1;
    public static final int MAX_HEALTH_CHECK_INTERVAL = 10;          //10 seconds
    public static final int MIN_HEALTH_CHECK_INTERVAL = 1;          //1  second
    public static final int MIN_VALIDATION_TIMEOUT    = 1;          //1  second
    public static final int MAX_VALIDATION_TIMEOUT    = 60;          //60 seconds
    public static final int MAX_IDLE_TIMEOUT          = 24 * 60 * 60; //24 hours
    public static final int MIN_STATEMENT_TIMEOUT     = 1;              //1  second

    protected String dataSourceClassName;

    protected String  dataSourceJndiName;
    protected boolean dataSourceJndiResourceRef;

    protected String driverClassName;
    protected String jdbcUrl;
    protected String username;
    protected String password;

    protected boolean testOnBorrow;
    protected String  validationQuery;
    protected int     validationTimeout = DEFAULT_VALIDATION_TIMEOUT;
    protected String  initSQL;

    protected boolean              defaultAutoCommit           = true;
    protected TransactionIsolation defaultTransactionIsolation = null;
    protected String               defaultCatalog;
    protected boolean              defaultReadonly             = false;

    protected boolean rollbackPendingTransaction       = true;
    protected boolean throwPendingTransactionException = true;

    protected int statementTimeout = DEFAULT_STATEMENT_TIMEOUT;

    protected int idleTimeout   = DEFAULT_IDLE_TIMEOUT;
    protected int idleTimeoutMs = -1;

    protected int connectionLeakTimeout   = DEFAULT_CONNECTION_LEAK_TIMEOUT;
    protected int connectionLeakTimeoutMs = -1;

    protected int maxWait               = DEFAULT_MAX_WAIT;
    protected int maxActive             = DEFAULT_MAX_ACTIVE;
    protected int maxIdle               = -1;
    protected int minIdle               = DEFAULT_MIN_IDLE;
    protected int healthCheckInterval   = DEFAULT_HEALTH_CHECK_INTERVAL;
    protected int healthCheckIntervalMs = -1;

    protected boolean healthCheck = true;
    protected boolean initializationFailRetry;
    protected int     initializationFailRetryInterval;

    protected DataSource dataSource;

    protected Properties dataSourceProperties = new Properties();

    public String getDataSourceClassName() {
        return dataSourceClassName;
    }

    public void setDataSourceClassName(String dataSourceClassName) {
        this.dataSourceClassName = dataSourceClassName;
    }

    public String getDataSourceJndiName() {
        return dataSourceJndiName;
    }

    public void setDataSourceJndiName(String dataSourceJndiName) {
        this.dataSourceJndiName = dataSourceJndiName;
    }

    public boolean isDataSourceJndiResourceRef() {
        return dataSourceJndiResourceRef;
    }

    public void setDataSourceJndiResourceRef(boolean dataSourceJndiResourceRef) {
        this.dataSourceJndiResourceRef = dataSourceJndiResourceRef;
    }

    public String getDriverClassName() {
        return driverClassName;
    }

    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public void setJdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    /**
     * The alias property of 'jdbcUrl'.
     */
    public String getUrl() {
        return jdbcUrl;
    }

    /**
     * The alias property of 'jdbcUrl'.
     */
    public void setUrl(String url) {
        this.jdbcUrl = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isTestOnBorrow() {
        return testOnBorrow;
    }

    public void setTestOnBorrow(boolean testOnBorrow) {
        this.testOnBorrow = testOnBorrow;
    }

    public String getValidationQuery() {
        return validationQuery;
    }

    public void setValidationQuery(String validationQuery) {
        this.validationQuery = validationQuery;
    }

    public int getValidationTimeout() {
        return validationTimeout;
    }

    public void setValidationTimeout(int validationTimeout) {
        this.validationTimeout = validationTimeout;
    }

    public String getInitSQL() {
        return initSQL;
    }

    public void setInitSQL(String initSQL) {
        this.initSQL = initSQL;
    }

    public boolean isDefaultAutoCommit() {
        return defaultAutoCommit;
    }

    public void setDefaultAutoCommit(boolean defaultAutoCommit) {
        this.defaultAutoCommit = defaultAutoCommit;
    }

    public TransactionIsolation getDefaultTransactionIsolation() {
        return defaultTransactionIsolation;
    }

    public void setDefaultTransactionIsolation(TransactionIsolation defaultTransactionIsolation) {
        this.defaultTransactionIsolation = defaultTransactionIsolation;
    }

    public String getDefaultCatalog() {
        return defaultCatalog;
    }

    public void setDefaultCatalog(String defaultCatalog) {
        this.defaultCatalog = defaultCatalog;
    }

    public boolean isDefaultReadonly() {
        return defaultReadonly;
    }

    public void setDefaultReadonly(boolean defaultReadonly) {
        this.defaultReadonly = defaultReadonly;
    }

    public boolean isRollbackPendingTransaction() {
        return rollbackPendingTransaction;
    }

    public void setRollbackPendingTransaction(boolean rollbackPendingTransaction) {
        this.rollbackPendingTransaction = rollbackPendingTransaction;
    }

    public boolean isThrowPendingTransactionException() {
        return throwPendingTransactionException;
    }

    public void setThrowPendingTransactionException(boolean throwPendingTransactionException) {
        this.throwPendingTransactionException = throwPendingTransactionException;
    }

    public int getMaxWait() {
        return maxWait;
    }

    /**
     * The maximum number of milliseconds that the pool will wait
     * (when there are no available connections) for a connection to be returned before throwing an exception.
     * Default value is 30000 (30 seconds)
     */
    public void setMaxWait(int maxWait) {
        this.maxWait = maxWait;
    }

    public int getMaxActive() {
        return maxActive;
    }

    /**
     * (int) The maximum number of active connections that can be allocated from pool at the same time. The default value is 50
     */
    public void setMaxActive(int maxActive) {
        this.maxActive = maxActive;
    }

    public int getMaxIdle() {
        return maxIdle;
    }

    public void setMaxIdle(int maxIdle) {
        this.maxIdle = maxIdle;
    }

    public int getMinIdle() {
        return minIdle;
    }

    public void setMinIdle(int minIdle) {
        this.minIdle = minIdle;
    }

    public int getIdleTimeout() {
        return idleTimeout;
    }

    /**
     * Sets idle timeout in seconds.
     */
    public void setIdleTimeout(int seconds) {
        this.idleTimeout = seconds;
    }

    public int getIdleTimeoutMs() {
        return idleTimeoutMs < 0 ? idleTimeout * 1000 : idleTimeoutMs;
    }

    public void setIdleTimeoutMs(int idleTimeoutMs) {
        this.idleTimeoutMs = idleTimeoutMs;
    }

    public int getStatementTimeout() {
        return statementTimeout;
    }

    public void setStatementTimeout(int statementTimeout) {
        this.statementTimeout = statementTimeout;
    }

    public int getConnectionLeakTimeout() {
        return connectionLeakTimeout;
    }

    public void setConnectionLeakTimeout(int seconds) {
        this.connectionLeakTimeout = seconds;
    }

    public int getConnectionLeakTimeoutMs() {
        return connectionLeakTimeoutMs < 0 ? connectionLeakTimeout * 1000 : connectionLeakTimeoutMs;
    }

    public void setConnectionLeakTimeoutMs(int connectionLeakTimeoutMs) {
        this.connectionLeakTimeoutMs = connectionLeakTimeoutMs;
    }

    public int getHealthCheckInterval() {
        return healthCheckInterval;
    }

    /**
     * Sets health check interval in seconds.
     */
    public void setHealthCheckInterval(int seconds) {
        this.healthCheckInterval = seconds;
    }

    public int getHealthCheckIntervalMs() {
        return healthCheckIntervalMs < 0 ? healthCheckInterval * 1000 : healthCheckIntervalMs;
    }

    public void setHealthCheckIntervalMs(int healthCheckIntervalMs) {
        this.healthCheckIntervalMs = healthCheckIntervalMs;
    }

    public boolean isHealthCheck() {
        return healthCheck;
    }

    public void setHealthCheck(boolean healthCheck) {
        this.healthCheck = healthCheck;
    }

    public boolean isInitializationFailRetry() {
        return initializationFailRetry;
    }

    public void setInitializationFailRetry(boolean initializationFailRetry) {
        this.initializationFailRetry = initializationFailRetry;
    }

    public int getInitializationFailRetryInterval() {
        return initializationFailRetryInterval;
    }

    public void setInitializationFailRetryInterval(int initializationFailRetryInterval) {
        this.initializationFailRetryInterval = initializationFailRetryInterval;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    /**
     * The underlying {@link DataSource} for creating new {@link java.sql.Connection}.
     *
     * <p>
     * May be <code>null</code> if {@link #driverClassName} and {@link #jdbcUrl} are present.
     */
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Properties getDataSourceProperties() {
        return dataSourceProperties;
    }

    public void setDataSourceProperties(Properties dataSourceProperties) {
        this.dataSourceProperties = dataSourceProperties;
    }

    public void setDataSourceProperty(String name, String value) {
        if (null == dataSourceProperties) {
            dataSourceProperties = new Properties();
        }
        dataSourceProperties.put(name, value);
    }

    public void setProperties(PoolProperties props) {
        if (null != props) {
            Beans.copyProperties(props, this);
        }
    }

    public void validate() {
        if (maxIdle < 0 && DEFAULT_MAX_IDLE < maxActive) {
            maxIdle = DEFAULT_MAX_IDLE;
        }

        validateRange("maxWait", maxWait, MIN_MAX_WAIT, MAX_MAX_WAIT);
        validateRange("maxActive", maxActive, MIN_MAX_ACTIVE, MAX_MAX_ACTIVE);

        validateRange("healthCheckInterval", healthCheckInterval, MIN_HEALTH_CHECK_INTERVAL, MAX_HEALTH_CHECK_INTERVAL);
        validateRange("validationTimeout", validationTimeout, MIN_VALIDATION_TIMEOUT, MAX_VALIDATION_TIMEOUT);
        validateRange("idleTimeout", idleTimeout, healthCheckInterval, MAX_IDLE_TIMEOUT);

        if (maxIdle >= 0) {
            validateRange("maxIdle", maxIdle, 0, maxActive);
            validateRange("minIdle", minIdle, 0, maxIdle);
        } else {
            validateRange("minIdle", minIdle, 0, maxActive);
        }
    }

    private void validateRange(String name, int value, int min, int max) {
        if (value < min || value > max) {
            throw new IllegalArgumentException("The value of pool property '" + name + "' must be in range [" + min + "," + max + "]");
        }
    }

}

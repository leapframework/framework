/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package leap.core.transaction;

import leap.lang.exception.NestedSQLException;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;

import java.sql.Connection;
import java.sql.SQLException;

public class LocalTransaction implements Transaction, TransactionStatus {
    private static final Log log = LogFactory.get(LocalTransaction.class);

    private final LocalTransactionProvider tp;
    private final TransactionDefinition    td;
    private final int                      isolation;

    private Connection connection;
    private boolean    rollbackOnly;

    private int referenceCount = 0;
    private boolean originalAutoCommit;
    private int     originalIsolationLevel;

    protected LocalTransaction(LocalTransactionProvider tp, TransactionDefinition td) {
        this.tp        = tp;
        this.td        = td;
        this.isolation = td.getIsolation().getValue();
    }

    @Override
    public boolean isNewTransaction() {
        return referenceCount <= 1;
    }

    @Override
    public void setRollbackOnly() {
        log.debug("Transaction set to rollback-only");
        rollbackOnly = true;
    }

    @Override
    public boolean isRollbackOnly() {
        return rollbackOnly;
    }

    @Override
    public boolean isCompleted() {
        return connection == null;
    }

    public boolean hasConnection() {
        return null != connection;
    }

    public Connection getConnection() {
        return connection;
    }

    protected void setConnection(Connection connection) {
        try {
            this.originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            tp.returnConnectionToDataSource(connection);
            throw new NestedSQLException("Error setting connection's 'autoCommit'" + e.getMessage(), e);
        }

        if (isolation != TransactionDefinition.Isolation.DEFAULT.getValue()) {
            try {
                this.originalIsolationLevel = connection.getTransactionIsolation();
                if (this.isolation != this.originalIsolationLevel) {
                    connection.setTransactionIsolation(this.isolation);
                }
            } catch (SQLException e) {
                tp.returnConnectionToDataSource(connection);
                throw new NestedSQLException("Error setting connection's transaction isolation" + e.getMessage(), e);
            }
        }

        this.connection = connection;
    }

    @Override
    public void execute(TransactionCallback callback) {
        begin();

        try {
            callback.doInTransaction(this);
        } catch (Throwable e) {
            setRollbackOnly();
            log.warn("Error executing transaction, auto rollback", e);

            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            } else {
                throw new TransactionException("Error executing transaction, " + e.getMessage(), e);
            }
        } finally {
            complete();
        }
    }

    @Override
    public <T> T execute(TransactionCallbackWithResult<T> callback) {
        begin();

        try {
            return callback.doInTransaction(this);
        } catch (Throwable e) {
            setRollbackOnly();
            log.warn("Error executing transaction, auto rollback", e);

            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            } else {
                throw new TransactionException("Error executing transaction, " + e.getMessage(), e);
            }
        } finally {
            complete();
        }
    }

    @Override
    public void remove() {
        LocalTransaction trans = tp.removeActiveTransaction();
        if(trans != this) {
            throw new IllegalStateException("The removed active transaction must be the self");
        }
    }

    protected void begin() {
        increase();

        if (log.isDebugEnabled()) {
            if (isNewTransaction()) {
                log.debug("Begin a new transaction, referencedCount={}", referenceCount);
            } else {
                log.debug("Join a exists transaction, referencedCount={}", referenceCount);
            }
        }
    }

    protected void complete() {
        decrease();

        if (referenceCount == 0) {
            try {
                //Connection may be null if no database access in transaction.
                if (null != connection) {
                    if (rollbackOnly) {
                        try {
                            log.debug("Rollback transaction, referencedCount={}", referenceCount);
                            connection.rollback();
                            connection.setAutoCommit(originalAutoCommit); //Notice: must do the rollback before setAutoCommit
                        } catch (SQLException e) {
                            log.warn("Error rollback transaction, " + e.getMessage(), e);
                        }
                    } else {
                        try {
                            log.debug("Commit transaction, referencedCount={}", referenceCount);
                            connection.commit();
                            connection.setAutoCommit(originalAutoCommit);

                            if (isolation != TransactionDefinition.Isolation.DEFAULT.getValue() &&
                                    isolation != originalIsolationLevel) {
                                connection.setTransactionIsolation(isolation);
                            }
                        } catch (SQLException e) {
                            throw new TransactionException("Error commit transaction, " + e.getMessage(), e);
                        }
                    }
                }
            } finally {
                try {
                    tp.removeActiveTransaction();
                } finally {
                    tp.closeConnection(connection);
                }
            }
        } else {
            log.debug("Exit transaction(no rollback or commit), referencedCount={}, rollbackOnly={}", referenceCount, rollbackOnly);
        }
    }

    /**
     * Increase the reference count by one because the connection has been requested
     */
    protected void increase() {
        this.referenceCount++;
    }

    /**
     * Decrease the reference count by one because the connection has been released
     */
    protected void decrease() {
        this.referenceCount--;
    }
}

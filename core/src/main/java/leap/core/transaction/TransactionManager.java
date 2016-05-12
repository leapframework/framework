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

import leap.lang.jdbc.ConnectionCallback;
import leap.lang.jdbc.ConnectionCallbackWithResult;

import javax.sql.DataSource;

public interface TransactionManager {

    /**
     * Begins the transactions for all datasource(s) with default definition.
     */
    Transactions beginTransactionsAll();

    /**
     * Begins the transactions for all datasource(s) with the given definition.
     */
    Transactions beginTransactionsAll(TransactionDefinition td);

    /**
     * Begins the transactions for the given datasource(s) with default definition.
     */
    Transactions beginTransactionsWith(String[] dataSourceNames);

    /**
     * Begins the transactions for the given datasource(s) with the given definition.
     */
    Transactions beginTransactionsWith(String[] dataSourceNames, TransactionDefinition td);

    /**
     * Returns the {@link TransactionProvider} of the given data source.
     */
    TransactionProvider getProvider(DataSource ds);

    /**
     * Executes the callback with default datasource.
     *
     * <p/>
     * If an active transaction is exist of the default datasource, the callback will be executed in the transaction.
     */
    void execute(ConnectionCallback callback);

    /**
     * Executes the callback with default datasource.
     *
     * <p/>
     * If an active transaction is exists of the default datasource, the callback will be executed in the transaction.
     */
    <T> T executeWithResult(ConnectionCallbackWithResult<T> callback);

    /**
     * Executes the callback in a currently active transaction or a new one if no active transaction.
     */
    void doTransaction(TransactionCallback callback);

    /**
     * Executes the callback in a currently active transaction or a new one if no active transaction.
     */
    <T> T doTransaction(TransactionCallbackWithResult<T> callback);

    /**
     * Executes the callback in a currently active transaction or a new one if no active transaction.
     *
     * <p>
     * If <code>requiresNew</code> is <code>true</code>, a new transaction will be created.
     */
    void doTransaction(TransactionCallback callback, boolean requiresNew);

    /**
     * Executes the callback in a currently active transaction or a new one if no active transaction.
     *
     * <p>
     * If <code>requiresNew</code> is <code>true</code>, a new transaction will be created.
     */
    <T> T doTransaction(TransactionCallbackWithResult<T> callback, boolean requiresNew);

    /**
     * Executes the callback with the given datasource.
     *
     * <p/>
     * If an active transaction is exist of the given datasource, the callback will be executed in the transaction.
     */
    void execute(DataSource ds, ConnectionCallback callback);

    /**
     * Executes the callback with the given datasource.
     *
     * <p/>
     * If an active transaction is exists of the given datasource, the callback will be execute in the transaction.
     */
    <T> T executeWithResult(DataSource ds, ConnectionCallbackWithResult<T> callback);

    /**
     * Executes the callback in a currently active transaction or a new one if no active transaction.
     */
    void doTransaction(DataSource ds, TransactionCallback callback);

    /**
     * Executes the callback in a currently active transaction or a new one if no active transaction.
     */
    <T> T doTransaction(DataSource ds, TransactionCallbackWithResult<T> callback);

    /**
     * Executes the callback in a currently active transaction or a new one if no active transaction.
     *
     * <p>
     * If <code>requiresNew</code> is <code>true</code>, a new transaction will be created.
     */
    void doTransaction(DataSource ds, TransactionCallback callback, boolean requiresNew);

    /**
     * Executes the callback in a currently active transaction or a new one if no active transaction.
     *
     * <p>
     * If <code>requiresNew</code> is <code>true</code>, a new transaction will be created.
     */
    <T> T doTransaction(DataSource ds, TransactionCallbackWithResult<T> callback, boolean requiresNew);

}

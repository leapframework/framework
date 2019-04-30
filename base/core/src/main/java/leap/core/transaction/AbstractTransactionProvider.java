/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package leap.core.transaction;

import leap.lang.Exceptions;
import leap.lang.jdbc.ConnectionCallback;
import leap.lang.jdbc.ConnectionCallbackWithResult;

import java.sql.Connection;
import java.sql.SQLException;

public abstract class AbstractTransactionProvider implements TransactionProvider {

    @Override
    public void execute(ConnectionCallback callback) {
        Connection connection = null;
        try{
            connection = getConnection();
            callback.execute(connection);
        }catch(SQLException e){
            Exceptions.wrapAndThrow(e);
        }finally{
            closeConnection(connection);
        }
    }

    @Override
    public <T> T executeWithResult(ConnectionCallbackWithResult<T> callback) {
        Connection connection = null;
        try{
            connection = getConnection();
            return callback.execute(connection);
        }catch(SQLException e){
            Exceptions.wrapAndThrow(e);
            return null;
        }finally{
            closeConnection(connection);
        }
    }

    @Override
    public void doTransaction(TransactionCallback callback) {
        getTransaction(false).execute(callback);
    }

    @Override
    public <T> T doTransaction(TransactionCallbackWithResult<T> callback) {
        return getTransaction(false).executeWithResult(callback);
    }

    @Override
    public void doTransaction(TransactionCallback callback,boolean requiresNew) {
        getTransaction(requiresNew).execute(callback);
    }

    @Override
    public <T> T doTransaction(TransactionCallbackWithResult<T> callback, boolean requiresNew) {
        return getTransaction(requiresNew).executeWithResult(callback);
    }

    @Override
    public void doTransaction(TransactionCallback callback, TransactionDefinition td) {
        getTransaction(td).execute(callback);
    }

    @Override
    public <T> T doTransaction(TransactionCallbackWithResult<T> callback, TransactionDefinition td) {
        return getTransaction(td).executeWithResult(callback);
    }

    /**
     * Returns a connection from data source.
     */
    protected abstract Connection getConnection();

    /**
     * Release the connection to data source.
     */
    protected abstract void closeConnection(Connection connection);

    /**
     * Return a currently active transaction or create a new one.
     */
    protected abstract AbstractTransaction getTransaction(boolean requiresNew);

    /**
     * Return a currently active transaction or create a new one.
     */
    protected abstract AbstractTransaction getTransaction(TransactionDefinition td);
}
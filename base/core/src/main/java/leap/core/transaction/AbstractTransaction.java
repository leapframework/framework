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

import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;

public abstract class AbstractTransaction implements Transaction,TransactionStatus {

    protected static final Log log = LogFactory.get(AbstractTransaction.class);

    public void execute(TransactionCallback callback) {
        executeWithResult(new TransactionCallbackWithResult<Void>() {
            @Override
            public Void doInTransaction(TransactionStatus status) throws Throwable {
                callback.doInTransaction(status);
                return null;
            }
        });
    }

    public <T> T executeWithResult(TransactionCallbackWithResult<T> callback) {
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

    protected AbstractTransaction begin() {
        return this;
    }

}

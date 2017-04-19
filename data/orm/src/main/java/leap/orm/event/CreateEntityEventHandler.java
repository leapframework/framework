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

package leap.orm.event;

import leap.core.transaction.TransactionStatus;

public interface CreateEntityEventHandler {

    /**
     * Returns <code>true</code> if the handler should be executed in a transaction.
     *
     * <p/>
     * That means the {@link #preCreateEntityWithTransaction(CreateEntityEvent, TransactionStatus)}
     *
     * and {@link #postCreateEntityWithTransaction(CreateEntityEvent, TransactionStatus)} will be invoked.
     */
    boolean isTransactional();

    void preCreateEntity(CreateEntityEvent e);

    void preCreateEntityWithTransaction(CreateEntityEvent e, TransactionStatus status);

    void postCreateEntity(CreateEntityEvent e);

    void postCreateEntityWithTransaction(CreateEntityEvent e, TransactionStatus status);

}
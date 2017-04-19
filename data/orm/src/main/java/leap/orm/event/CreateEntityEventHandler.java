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
import leap.orm.value.EntityWrapper;

/**
 * The handler interface for handling events of creating an entity (save an entity record to db).
 */
public interface CreateEntityEventHandler {

    /**
     * Returns <code>true</code> if the handler should be executed in a transaction.
     *
     * <p/>
     * That means the {@link #preCreateEntityWithTransaction(EntityWrapper, TransactionStatus)}
     *
     * and {@link #postCreateEntityWithTransaction(EntityWrapper, TransactionStatus)} will be invoked.
     */
    boolean isTransactional();

    /**
     * Executed before saving the entity record to db outside transaction.
     *
     * <p/>
     * The entity fields can be changed and all the changes will be saved to db.
     */
    void preCreateEntity(EntityWrapper entity);

    /**
     * Executed before saving the entity record to db inside transaction.
     *
     * <p/>
     * The entity fields can be changed and all the changes will be saved to db.
     */
    void preCreateEntityWithTransaction(EntityWrapper entity, TransactionStatus status);

    /**
     * Executed after saving the the entity record to db inside transaction.
     */
    void postCreateEntityWithTransaction(EntityWrapper entity, TransactionStatus status);

    /**
     * Executed after saving the the entity record to db outside transaction.
     */
    void postCreateEntity(EntityWrapper entity);

}
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
import leap.orm.OrmContext;
import leap.orm.mapping.EntityMapping;

public abstract class EntityEventBase implements EntityEvent {

    private final OrmContext    context;
    private final EntityMapping mapping;

    private TransactionStatus transactionStatus;

    public EntityEventBase(OrmContext context, EntityMapping em) {
        this.context = context;
        this.mapping = em;
    }

    @Override
    public OrmContext getContext() {
        return context;
    }

    @Override
    public EntityMapping getMapping() {
        return mapping;
    }

    @Override
    public TransactionStatus getTransactionStatus() {
        return transactionStatus;
    }

    public void setTransactionStatus(TransactionStatus transactionStatus) {
        this.transactionStatus = transactionStatus;
    }

}

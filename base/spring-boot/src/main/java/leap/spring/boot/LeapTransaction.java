/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package leap.spring.boot;

import leap.core.transaction.AbstractTransaction;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

class LeapTransaction extends AbstractTransaction {

    private final PlatformTransactionManager manager;
    private final TransactionStatus          status;

    LeapTransaction(PlatformTransactionManager manager, TransactionStatus status) {
        this.manager = manager;
        this.status = status;
    }

    @Override
    public boolean isNewTransaction() {
        return status.isNewTransaction();
    }

    @Override
    public boolean isRollbackOnly() {
        return status.isRollbackOnly();
    }

    @Override
    public boolean isCompleted() {
        return status.isCompleted();
    }

    @Override
    public void setRollbackOnly() throws IllegalStateException {
        status.setRollbackOnly();
    }

    @Override
    public void complete() throws IllegalStateException {
        if(status.isRollbackOnly()) {
            manager.rollback(status);
        }else{
            manager.commit(status);
        }
    }

    @Override
    protected AbstractTransaction begin() {
        return super.begin();
    }
}

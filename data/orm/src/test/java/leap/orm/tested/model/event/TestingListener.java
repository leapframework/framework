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

package leap.orm.tested.model.event;

import leap.core.transaction.TransactionStatus;
import leap.orm.annotation.event.*;
import leap.orm.event.*;
import leap.orm.value.EntityWrapper;

import java.util.concurrent.atomic.AtomicInteger;

public class TestingListener implements PreCreateListener,PreUpdateListener,PreDeleteListener {

    public static final int POST_CREATE_NO_TRANS_WITH_ERROR = 1;
    public static final int POST_CREATE_IN_TRANS_WITH_ERROR = 2;
    public static final int POST_UPDATE_NO_TRANS_WITH_ERROR = 3;
    public static final int POST_UPDATE_IN_TRANS_WITH_ERROR = 4;

    public static Object lastUpdateId;
    public static Object lastDeleteId;

    @PreCreate
    public void preCreateEntity(EntityWrapper entity) {
        entity.set("col1","Test1");
    }

    @Override
    public void preCreateEntity(CreateEntityEvent e) {
        e.getEntity().set("col2", "Test2");
    }

    @PostCreate
    public void postCreateNoTransWithError(EventModel m) {
        if(m.getTestType() == POST_CREATE_NO_TRANS_WITH_ERROR) {
            throw new RuntimeException("error");
        }
    }

    @PostCreate
    public void postCreateNoTransWithError(EventModel m, TransactionStatus ts) {
        if(m.getTestType() == POST_CREATE_IN_TRANS_WITH_ERROR) {
            throw new RuntimeException("error");
        }
    }

    @PreUpdate
    public void preUpdateEntity(EventModel m) {
        m.setCol1("UpdateCol1");
    }

    @Override
    public void preUpdateEntity(UpdateEntityEvent e) {
        lastUpdateId = e.getId();
        e.getEntity().set("col2", "UpdateCol2");
    }

    @PostUpdate
    public void postUpdateNoTransWithError(EventModel m) {
        if(m.getTestType() == POST_UPDATE_NO_TRANS_WITH_ERROR) {
            throw new RuntimeException("error");
        }
    }

    @PostUpdate
    public void postUpdateNoTransWithError(EventModel m, TransactionStatus ts) {
        if(m.getTestType() == POST_UPDATE_IN_TRANS_WITH_ERROR) {
            throw new RuntimeException("error");
        }
    }

    public static final ThreadLocal<AtomicInteger> PRE_DELETE_CONTEXT  = ThreadLocal.withInitial(() -> new AtomicInteger(0));
    public static final ThreadLocal<AtomicInteger> POST_DELETE_CONTEXT = ThreadLocal.withInitial(() -> new AtomicInteger(0));

    @PreDelete
    public void preDelete(Object id) {
        PRE_DELETE_CONTEXT.get().incrementAndGet();
    }

    @Override
    public void preDeleteEntity(DeleteEntityEvent e) {
        PRE_DELETE_CONTEXT.get().incrementAndGet();
    }

    @PostDelete
    public void postDeleteNoTransWithError(Object id) {
        if(POST_DELETE_CONTEXT.get().get() == 1) {
            POST_DELETE_CONTEXT.get().set(0);
            throw new RuntimeException("error");
        }
    }

    @PostDelete
    public void postDeleteNoTransWithError(DeleteEntityEvent d, TransactionStatus ts) {
        if(POST_DELETE_CONTEXT.get().get() == 2) {
            POST_DELETE_CONTEXT.get().set(0);
            throw new RuntimeException("error");
        }
    }

}
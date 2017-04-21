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
import leap.orm.annotation.event.PostCreate;
import leap.orm.annotation.event.PostUpdate;
import leap.orm.annotation.event.PreCreate;
import leap.orm.annotation.event.PreUpdate;
import leap.orm.event.CreateEntityEvent;
import leap.orm.event.PreCreateListener;
import leap.orm.event.PreUpdateListener;
import leap.orm.event.UpdateEntityEvent;
import leap.orm.value.EntityWrapper;

public class TestingListener implements PreCreateListener,PreUpdateListener {

    public static final int POST_CREATE_NO_TRANS_WITH_ERROR = 1;
    public static final int POST_CREATE_IN_TRANS_WITH_ERROR = 2;
    public static final int POST_UPDATE_NO_TRANS_WITH_ERROR = 3;
    public static final int POST_UPDATE_IN_TRANS_WITH_ERROR = 4;

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

}
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

import leap.orm.OrmContext;
import leap.orm.annotation.event.PreUpdate;
import leap.orm.mapping.EntityMapping;

public class DefaultEntityEventHandler implements EntityEventHandler {

    @Override
    public boolean isHandleCreateEvent(OrmContext context, EntityMapping em) {
        return em.getListeners().hasCreateListeners();
    }

    @Override
    public boolean isCreateEventTransactional(OrmContext context, EntityMapping em) {
        return em.getListeners().hasTransCreateListeners();
    }

    @Override
    public void preCreateEntityNoTrans(OrmContext context, EntityMapping em, CreateEntityEvent e) {
        for(PreCreateListener listener : em.getListeners().getNoTransPreCreateListeners()) {
            listener.preCreateEntity(e);
        }
    }

    @Override
    public void preCreateEntityInTrans(OrmContext context, EntityMapping em, CreateEntityEvent e) {
        for(PreCreateListener listener : em.getListeners().getInTransPreCreateListeners()) {
            listener.preCreateEntity(e);
        }
    }

    @Override
    public void postCreateEntityInTrans(OrmContext context, EntityMapping em, CreateEntityEvent e) {
        for(PostCreateListener listener : em.getListeners().getInTransPostCreateListeners()) {
            listener.postCreateEntity(e);
        }
    }

    @Override
    public void postCreateEntityNoTrans(OrmContext context, EntityMapping em, CreateEntityEvent e) {
        for(PostCreateListener listener : em.getListeners().getNoTransPostCreateListeners()) {
            listener.postCreateEntity(e);
        }
    }

    @Override
    public boolean isHandleUpdateEvent(OrmContext context, EntityMapping em) {
        return em.getListeners().hasUpdateListeners();
    }

    @Override
    public boolean isUpdateEventTransactional(OrmContext context, EntityMapping em) {
        return em.getListeners().hasTransUpdateListeners();
    }

    @Override
    public void preUpdateEntityNoTrans(OrmContext context, EntityMapping em, UpdateEntityEvent e) {
        for(PreUpdateListener listener : em.getListeners().getNoTransPreUpdateListeners()) {
            listener.preUpdateEntity(e);
        }
    }

    @Override
    public void preUpdateEntityInTrans(OrmContext context, EntityMapping em, UpdateEntityEvent e) {
        for(PreUpdateListener listener : em.getListeners().getInTransPreUpdateListeners()) {
            listener.preUpdateEntity(e);
        }
    }

    @Override
    public void postUpdateEntityInTrans(OrmContext context, EntityMapping em, UpdateEntityEvent e) {
        for(PostUpdateListener listener : em.getListeners().getInTransPostUpdateListeners()) {
            listener.postUpdateEntity(e);
        }
    }

    @Override
    public void postUpdateEntityNoTrans(OrmContext context, EntityMapping em, UpdateEntityEvent e) {
        for(PostUpdateListener listener : em.getListeners().getNoTransPostUpdateListeners()) {
            listener.postUpdateEntity(e);
        }
    }
}
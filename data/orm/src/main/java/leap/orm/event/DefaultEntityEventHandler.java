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

import leap.lang.Arrays2;
import leap.orm.OrmContext;
import leap.orm.mapping.EntityMapping;

public class DefaultEntityEventHandler implements EntityEventHandler {

    @Override
    public boolean isHandleCreateEvent(OrmContext context, EntityMapping em) {
        return getListeners(em).hasCreateListeners();
    }

    @Override
    public boolean isCreateEventTransactional(OrmContext context, EntityMapping em) {
        return getListeners(em).hasTransCreateListeners();
    }

    @Override
    public boolean isHandleUpdateEvent(OrmContext context, EntityMapping em) {
        return getListeners(em).hasUpdateListeners();
    }

    @Override
    public boolean isUpdateEventTransactional(OrmContext context, EntityMapping em) {
        return getListeners(em).hasTransUpdateListeners();
    }

    @Override
    public boolean isHandleDeleteEvent(OrmContext context, EntityMapping em) {
        return getListeners(em).hasDeleteListeners();
    }

    @Override
    public boolean isDeleteEventTransactional(OrmContext context, EntityMapping em) {
        return getListeners(em).hasTransDeleteListeners();
    }

    @Override
    public boolean isHandleLoadEvent(OrmContext context, EntityMapping em) {
        return getListeners(em).hasLoadListeners();
    }

    @Override
    public void preCreateEntityNoTrans(OrmContext context, EntityMapping em, CreateEntityEvent e) {
        for(PreCreateListener listener : getListeners(em).getNoTransPreCreateListeners()) {
            listener.preCreateEntity(e);
        }
        clearContext(em);
    }

    @Override
    public void preCreateEntityInTrans(OrmContext context, EntityMapping em, CreateEntityEvent e) {
        for(PreCreateListener listener : getListeners(em).getInTransPreCreateListeners()) {
            listener.preCreateEntity(e);
        }
        clearContext(em);
    }

    @Override
    public void postCreateEntityInTrans(OrmContext context, EntityMapping em, CreateEntityEvent e) {
        for(PostCreateListener listener : getListeners(em).getInTransPostCreateListeners()) {
            listener.postCreateEntity(e);
        }
        clearContext(em);
    }

    @Override
    public void postCreateEntityNoTrans(OrmContext context, EntityMapping em, CreateEntityEvent e) {
        for(PostCreateListener listener : getListeners(em).getNoTransPostCreateListeners()) {
            listener.postCreateEntity(e);
        }
        clearContext(em);
    }

    @Override
    public void preUpdateEntityNoTrans(OrmContext context, EntityMapping em, UpdateEntityEvent e) {
        for(PreUpdateListener listener : getListeners(em).getNoTransPreUpdateListeners()) {
            listener.preUpdateEntity(e);
        }
        clearContext(em);
    }

    @Override
    public void preUpdateEntityInTrans(OrmContext context, EntityMapping em, UpdateEntityEvent e) {
        for(PreUpdateListener listener : getListeners(em).getInTransPreUpdateListeners()) {
            listener.preUpdateEntity(e);
        }
        clearContext(em);
    }

    @Override
    public void postUpdateEntityInTrans(OrmContext context, EntityMapping em, UpdateEntityEvent e) {
        for(PostUpdateListener listener : getListeners(em).getInTransPostUpdateListeners()) {
            listener.postUpdateEntity(e);
        }
        clearContext(em);
    }

    @Override
    public void postUpdateEntityNoTrans(OrmContext context, EntityMapping em, UpdateEntityEvent e) {
        for(PostUpdateListener listener : getListeners(em).getNoTransPostUpdateListeners()) {
            listener.postUpdateEntity(e);
        }
        clearContext(em);
    }

    @Override
    public void preDeleteEntityNoTrans(OrmContext context, EntityMapping em, DeleteEntityEvent e) {
        for(PreDeleteListener listener : getListeners(em).getNoTransPreDeleteListeners()) {
            listener.preDeleteEntity(e);
        }
        clearContext(em);
    }

    @Override
    public void preDeleteEntityInTrans(OrmContext context, EntityMapping em, DeleteEntityEvent e) {
        for(PreDeleteListener listener : getListeners(em).getInTransPreDeleteListeners()) {
            listener.preDeleteEntity(e);
        }
        clearContext(em);
    }

    @Override
    public void postDeleteEntityInTrans(OrmContext context, EntityMapping em, DeleteEntityEvent e) {
        for(PostDeleteListener listener : getListeners(em).getInTransPostDeleteListeners()) {
            listener.postDeleteEntity(e);
        }
        clearContext(em);
    }

    @Override
    public void postDeleteEntityNoTrans(OrmContext context, EntityMapping em, DeleteEntityEvent e) {
        for(PostDeleteListener listener : getListeners(em).getNoTransPostDeleteListeners()) {
            listener.postDeleteEntity(e);
        }
        clearContext(em);
    }

    @Override
    public void postLoadEntityNoTrans(OrmContext context, EntityMapping em, LoadEntityEvent e) {
        for(PostLoadListener listener : getListeners(em).getPostLoadListeners()) {
            listener.postLoadEntity(e);
        }
        clearContext(em);
    }

    protected EntityListeners getListeners(EntityMapping em) {
        EntityListeners contextListeners = EntityMapping.getContextListeners();
        if(null != contextListeners) {
            return new CompositeListeners(contextListeners, em.getListeners());
        }else {
            return em.getListeners();
        }
    }

    protected void clearContext(EntityMapping em) {
        em.setContextListeners(null);
    }

    protected static final class CompositeListeners implements EntityListeners {
        private final EntityListeners context;
        private final EntityListeners fixed;

        public CompositeListeners(EntityListeners context, EntityListeners fixed) {
            this.context = context;
            this.fixed = fixed;
        }

        @Override
        public boolean hasCreateListeners() {
            return context.hasCreateListeners() || fixed.hasCreateListeners();
        }

        @Override
        public boolean hasTransCreateListeners() {
            return context.hasTransCreateListeners() || fixed.hasTransCreateListeners();
        }

        @Override
        public boolean hasUpdateListeners() {
            return context.hasUpdateListeners() || fixed.hasUpdateListeners();
        }

        @Override
        public boolean hasTransUpdateListeners() {
            return context.hasTransUpdateListeners() || fixed.hasTransUpdateListeners();
        }

        @Override
        public boolean hasDeleteListeners() {
            return context.hasDeleteListeners() || fixed.hasDeleteListeners();
        }

        @Override
        public boolean hasTransDeleteListeners() {
            return context.hasTransDeleteListeners() || fixed.hasTransDeleteListeners();
        }

        @Override
        public boolean hasLoadListeners() {
            return context.hasLoadListeners() || fixed.hasLoadListeners();
        }

        @Override
        public PreCreateListener[] getNoTransPreCreateListeners() {
            return Arrays2.concat(context.getNoTransPreCreateListeners(), fixed.getNoTransPreCreateListeners());
        }

        @Override
        public PreCreateListener[] getInTransPreCreateListeners() {
            return Arrays2.concat(context.getInTransPreCreateListeners(), fixed.getInTransPreCreateListeners());
        }

        @Override
        public PostCreateListener[] getNoTransPostCreateListeners() {
            return Arrays2.concat(context.getNoTransPostCreateListeners(), fixed.getNoTransPostCreateListeners());
        }

        @Override
        public PostCreateListener[] getInTransPostCreateListeners() {
            return Arrays2.concat(context.getInTransPostCreateListeners(), fixed.getInTransPostCreateListeners());
        }

        @Override
        public PreUpdateListener[] getNoTransPreUpdateListeners() {
            return Arrays2.concat(context.getNoTransPreUpdateListeners(), fixed.getNoTransPreUpdateListeners());
        }

        @Override
        public PreUpdateListener[] getInTransPreUpdateListeners() {
            return Arrays2.concat(context.getInTransPreUpdateListeners(), fixed.getInTransPreUpdateListeners());
        }

        @Override
        public PostUpdateListener[] getNoTransPostUpdateListeners() {
            return Arrays2.concat(context.getNoTransPostUpdateListeners(), fixed.getNoTransPostUpdateListeners());
        }

        @Override
        public PostUpdateListener[] getInTransPostUpdateListeners() {
            return Arrays2.concat(context.getInTransPostUpdateListeners(), fixed.getInTransPostUpdateListeners());
        }

        @Override
        public PreDeleteListener[] getNoTransPreDeleteListeners() {
            return Arrays2.concat(context.getNoTransPreDeleteListeners(), fixed.getNoTransPreDeleteListeners());
        }

        @Override
        public PreDeleteListener[] getInTransPreDeleteListeners() {
            return Arrays2.concat(context.getInTransPreDeleteListeners(), fixed.getInTransPreDeleteListeners());
        }

        @Override
        public PostDeleteListener[] getNoTransPostDeleteListeners() {
            return Arrays2.concat(context.getNoTransPostDeleteListeners(), fixed.getNoTransPostDeleteListeners());
        }

        @Override
        public PostDeleteListener[] getInTransPostDeleteListeners() {
            return Arrays2.concat(context.getInTransPostDeleteListeners(), fixed.getInTransPostDeleteListeners());
        }

        @Override
        public PostLoadListener[] getPostLoadListeners() {
            return Arrays2.concat(context.getPostLoadListeners(), fixed.getPostLoadListeners());
        }
    }
}
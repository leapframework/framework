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

import leap.lang.Collections2;
import leap.orm.OrmContext;
import leap.orm.mapping.EntityMapping;

import java.util.ArrayList;
import java.util.List;

public class DefaultEntityEventHandler implements EntityEventHandler {

    private static final EntityListeners EMPTY_LISTENERS = new EntityListenersBuilder().build();

    @Override
    public boolean isHandleCreateEvent(OrmContext context, EntityMapping em) {
        return getListeners(context, em).hasCreateListeners();
    }

    @Override
    public boolean isCreateEventTransactional(OrmContext context, EntityMapping em) {
        return getListeners(context, em).hasTransCreateListeners();
    }

    @Override
    public boolean isHandleUpdateEvent(OrmContext context, EntityMapping em) {
        return getListeners(context, em).hasUpdateListeners();
    }

    @Override
    public boolean isUpdateEventTransactional(OrmContext context, EntityMapping em) {
        return getListeners(context, em).hasTransUpdateListeners();
    }

    @Override
    public boolean isHandleDeleteEvent(OrmContext context, EntityMapping em) {
        return getListeners(context, em).hasDeleteListeners();
    }

    @Override
    public boolean isDeleteEventTransactional(OrmContext context, EntityMapping em) {
        return getListeners(context, em).hasTransDeleteListeners();
    }

    @Override
    public boolean isHandleLoadEvent(OrmContext context, EntityMapping em) {
        return getListeners(context, em).hasLoadListeners();
    }

    @Override
    public void preCreateEntityNoTrans(OrmContext context, EntityMapping em, CreateEntityEvent e) {
        for (PreCreateListener listener : getListeners(context, em).getNoTransPreCreateListeners()) {
            listener.preCreateEntity(e);
        }
        clearContext(em);
    }

    @Override
    public void preCreateEntityInTrans(OrmContext context, EntityMapping em, CreateEntityEvent e) {
        for (PreCreateListener listener : getListeners(context, em).getInTransPreCreateListeners()) {
            listener.preCreateEntity(e);
        }
        clearContext(em);
    }

    @Override
    public void postCreateEntityInTrans(OrmContext context, EntityMapping em, CreateEntityEvent e) {
        for (PostCreateListener listener : getListeners(context, em).getInTransPostCreateListeners()) {
            listener.postCreateEntity(e);
        }
        clearContext(em);
    }

    @Override
    public void postCreateEntityNoTrans(OrmContext context, EntityMapping em, CreateEntityEvent e) {
        for (PostCreateListener listener : getListeners(context, em).getNoTransPostCreateListeners()) {
            listener.postCreateEntity(e);
        }
        clearContext(em);
    }

    @Override
    public void preUpdateEntityNoTrans(OrmContext context, EntityMapping em, UpdateEntityEvent e) {
        for (PreUpdateListener listener : getListeners(context, em).getNoTransPreUpdateListeners()) {
            listener.preUpdateEntity(e);
        }
        clearContext(em);
    }

    @Override
    public void preUpdateEntityInTrans(OrmContext context, EntityMapping em, UpdateEntityEvent e) {
        for (PreUpdateListener listener : getListeners(context, em).getInTransPreUpdateListeners()) {
            listener.preUpdateEntity(e);
        }
        clearContext(em);
    }

    @Override
    public void postUpdateEntityInTrans(OrmContext context, EntityMapping em, UpdateEntityEvent e) {
        for (PostUpdateListener listener : getListeners(context, em).getInTransPostUpdateListeners()) {
            listener.postUpdateEntity(e);
        }
        clearContext(em);
    }

    @Override
    public void postUpdateEntityNoTrans(OrmContext context, EntityMapping em, UpdateEntityEvent e) {
        for (PostUpdateListener listener : getListeners(context, em).getNoTransPostUpdateListeners()) {
            listener.postUpdateEntity(e);
        }
        clearContext(em);
    }

    @Override
    public void preDeleteEntityNoTrans(OrmContext context, EntityMapping em, DeleteEntityEvent e) {
        for (PreDeleteListener listener : getListeners(context, em).getNoTransPreDeleteListeners()) {
            listener.preDeleteEntity(e);
        }
        clearContext(em);
    }

    @Override
    public void preDeleteEntityInTrans(OrmContext context, EntityMapping em, DeleteEntityEvent e) {
        for (PreDeleteListener listener : getListeners(context, em).getInTransPreDeleteListeners()) {
            listener.preDeleteEntity(e);
        }
        clearContext(em);
    }

    @Override
    public void postDeleteEntityInTrans(OrmContext context, EntityMapping em, DeleteEntityEvent e) {
        for (PostDeleteListener listener : getListeners(context, em).getInTransPostDeleteListeners()) {
            listener.postDeleteEntity(e);
        }
        clearContext(em);
    }

    @Override
    public void postDeleteEntityNoTrans(OrmContext context, EntityMapping em, DeleteEntityEvent e) {
        for (PostDeleteListener listener : getListeners(context, em).getNoTransPostDeleteListeners()) {
            listener.postDeleteEntity(e);
        }
        clearContext(em);
    }

    @Override
    public void postLoadEntityNoTrans(OrmContext context, EntityMapping em, LoadEntityEvent e) {
        for (PostLoadListener listener : getListeners(context, em).getPostLoadListeners()) {
            listener.postLoadEntity(e);
        }
        clearContext(em);
    }

    protected EntityListeners getListeners(OrmContext context, EntityMapping em) {
        boolean withEvents = context.getDao().isWithEvents();
        if (!withEvents) {
            return EMPTY_LISTENERS;
        }

        List<EntityListeners> list = EntityMapping.getContextListeners();
        if (null != list && !list.isEmpty()) {
            List<EntityListeners> all = new ArrayList<>(list);
            all.add(em.getListeners());
            return new CompositeListeners(all);
        } else {
            return em.getListeners();
        }
    }

    protected void clearContext(EntityMapping em) {
        em.clearContextListeners();
    }

    protected static final class CompositeListeners implements EntityListeners {
        private final List<EntityListeners> all;

        public CompositeListeners(List<EntityListeners> all) {
            this.all = all;
        }

        @Override
        public boolean hasCreateListeners() {
            return all.stream().anyMatch(listeners -> listeners.hasCreateListeners());
        }

        @Override
        public boolean hasTransCreateListeners() {
            return all.stream().anyMatch(listeners -> listeners.hasTransCreateListeners());
        }

        @Override
        public boolean hasUpdateListeners() {
            return all.stream().anyMatch(listeners -> listeners.hasUpdateListeners());
        }

        @Override
        public boolean hasTransUpdateListeners() {
            return all.stream().anyMatch(listeners -> listeners.hasTransUpdateListeners());
        }

        @Override
        public boolean hasDeleteListeners() {
            return all.stream().anyMatch(listeners -> listeners.hasDeleteListeners());
        }

        @Override
        public boolean hasTransDeleteListeners() {
            return all.stream().anyMatch(listeners -> listeners.hasTransDeleteListeners());
        }

        @Override
        public boolean hasLoadListeners() {
            return all.stream().anyMatch(listeners -> listeners.hasLoadListeners());
        }

        @Override
        public PreCreateListener[] getNoTransPreCreateListeners() {
            final List<PreCreateListener> result = new ArrayList<>();
            for (EntityListeners listeners : all) {
                Collections2.addAll(result, listeners.getNoTransPreCreateListeners());
            }
            return result.toArray(new PreCreateListener[result.size()]);
        }

        @Override
        public PreCreateListener[] getInTransPreCreateListeners() {
            final List<PreCreateListener> result = new ArrayList<>();
            for (EntityListeners listeners : all) {
                Collections2.addAll(result, listeners.getInTransPreCreateListeners());
            }
            return result.toArray(new PreCreateListener[result.size()]);
        }

        @Override
        public PostCreateListener[] getNoTransPostCreateListeners() {
            final List<PostCreateListener> result = new ArrayList<>();
            for (EntityListeners listeners : all) {
                Collections2.addAll(result, listeners.getNoTransPostCreateListeners());
            }
            return result.toArray(new PostCreateListener[result.size()]);
        }

        @Override
        public PostCreateListener[] getInTransPostCreateListeners() {
            final List<PostCreateListener> result = new ArrayList<>();
            for (EntityListeners listeners : all) {
                Collections2.addAll(result, listeners.getInTransPostCreateListeners());
            }
            return result.toArray(new PostCreateListener[result.size()]);
        }

        @Override
        public PreUpdateListener[] getNoTransPreUpdateListeners() {
            final List<PreUpdateListener> result = new ArrayList<>();
            for (EntityListeners listeners : all) {
                Collections2.addAll(result, listeners.getNoTransPreUpdateListeners());
            }
            return result.toArray(new PreUpdateListener[result.size()]);
        }

        @Override
        public PreUpdateListener[] getInTransPreUpdateListeners() {
            final List<PreUpdateListener> result = new ArrayList<>();
            for (EntityListeners listeners : all) {
                Collections2.addAll(result, listeners.getInTransPreUpdateListeners());
            }
            return result.toArray(new PreUpdateListener[result.size()]);
        }

        @Override
        public PostUpdateListener[] getNoTransPostUpdateListeners() {
            final List<PostUpdateListener> result = new ArrayList<>();
            for (EntityListeners listeners : all) {
                Collections2.addAll(result, listeners.getNoTransPostUpdateListeners());
            }
            return result.toArray(new PostUpdateListener[result.size()]);
        }

        @Override
        public PostUpdateListener[] getInTransPostUpdateListeners() {
            final List<PostUpdateListener> result = new ArrayList<>();
            for (EntityListeners listeners : all) {
                Collections2.addAll(result, listeners.getInTransPostUpdateListeners());
            }
            return result.toArray(new PostUpdateListener[result.size()]);
        }

        @Override
        public PreDeleteListener[] getNoTransPreDeleteListeners() {
            final List<PreDeleteListener> result = new ArrayList<>();
            for (EntityListeners listeners : all) {
                Collections2.addAll(result, listeners.getNoTransPreDeleteListeners());
            }
            return result.toArray(new PreDeleteListener[result.size()]);
        }

        @Override
        public PreDeleteListener[] getInTransPreDeleteListeners() {
            final List<PreDeleteListener> result = new ArrayList<>();
            for (EntityListeners listeners : all) {
                Collections2.addAll(result, listeners.getInTransPreDeleteListeners());
            }
            return result.toArray(new PreDeleteListener[result.size()]);
        }

        @Override
        public PostDeleteListener[] getNoTransPostDeleteListeners() {
            final List<PostDeleteListener> result = new ArrayList<>();
            for (EntityListeners listeners : all) {
                Collections2.addAll(result, listeners.getNoTransPostDeleteListeners());
            }
            return result.toArray(new PostDeleteListener[result.size()]);
        }

        @Override
        public PostDeleteListener[] getInTransPostDeleteListeners() {
            final List<PostDeleteListener> result = new ArrayList<>();
            for (EntityListeners listeners : all) {
                Collections2.addAll(result, listeners.getInTransPostDeleteListeners());
            }
            return result.toArray(new PostDeleteListener[result.size()]);
        }

        @Override
        public PostLoadListener[] getPostLoadListeners() {
            final List<PostLoadListener> result = new ArrayList<>();
            for (EntityListeners listeners : all) {
                Collections2.addAll(result, listeners.getPostLoadListeners());
            }
            return result.toArray(new PostLoadListener[result.size()]);
        }
    }
}
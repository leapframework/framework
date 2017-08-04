/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package leap.orm.change;

import leap.core.BeanFactory;
import leap.core.ioc.PostCreateBean;
import leap.core.schedule.Scheduler;
import leap.core.schedule.SchedulerManager;
import leap.lang.Disposable;
import leap.lang.Try;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.orm.dao.Dao;
import leap.orm.mapping.EntityMapping;
import leap.orm.mapping.FieldMapping;
import leap.orm.query.CriteriaQuery;
import leap.orm.value.EntityWrapper;

import java.sql.Timestamp;
import java.sql.Types;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class DefaultChangeManager implements ChangeManager, PostCreateBean, Disposable {

    private static final Log log = LogFactory.get(DefaultChangeManager.class);

    protected Scheduler scheduler;
    protected final Set<ChangeObserver> startedObservers = new CopyOnWriteArraySet<>();

    @Override
    public void postCreate(BeanFactory factory) throws Throwable {
        scheduler = factory.getBean(SchedulerManager.class).newFixedThreadPoolScheduler("change-manager");
    }

    @Override
    public <T> ChangeObserver createEntityChangeObserver(Dao dao, Class<T> entityClass, String field) {
        EntityMapping em = dao.getOrmContext().getMetadata().getEntityMapping(entityClass);
        FieldMapping  fm = em.getFieldMapping(field);

        int typeCode = fm.getColumn().getTypeCode();
        if(typeCode == Types.BIGINT || typeCode == Types.TIMESTAMP) {
            return new ChangeObserverImpl(dao, em, entityClass, fm);
        }else{
            throw new IllegalStateException("The field '" + field + "' must be BIGINT or TIMESTAMP");
        }

    }

    @Override
    public void dispose() throws Throwable {
        startedObservers.forEach(ChangeObserver::stop);
    }

    protected class ChangeObserverImpl implements ChangeObserver, Runnable {

        protected final Dao           dao;
        protected final EntityMapping em;
        protected final Class<?>      resultClass;
        protected final FieldMapping  fm;

        protected ChangeListener listener;
        protected boolean         started;
        protected boolean         inited;
        protected TimeUnit        timeUnit;
        protected int             period;
        protected int             limit = 100;
        protected ScheduledFuture scheduled;
        protected Object          maxValue;
        protected CriteriaQuery   changesQuery;

        public ChangeObserverImpl(Dao dao, EntityMapping em, Class<?> resultClass, FieldMapping fm) {
            this.dao = dao;
            this.em = em;
            this.resultClass = resultClass;
            this.fm = fm;
            this.timeUnit = TimeUnit.SECONDS;
            this.period = 1;
            if(fm.getColumn().getTypeCode() == Types.BIGINT) {
                maxValue = 0L;
            }else if(fm.getColumn().getTypeCode() == Types.TIMESTAMP) {
                maxValue = new Timestamp(0L);
            }
        }

        @Override
        public ChangeObserver setListener(ChangeListener listener) {
            checkStarted();
            this.listener = listener;
            return this;
        }

        @Override
        public ChangeObserver setPeriod(TimeUnit timeUnit, int period) {
            checkStarted();
            this.timeUnit = timeUnit;
            this.period   = period;
            return this;
        }

        @Override
        public ChangeObserver limit(int maxChanges) {
            checkStarted();
            this.limit = maxChanges;
            return this;
        }

        @Override
        public void start() {
            checkStarted();

            if(null == listener) {
                throw new IllegalStateException("Empty Listeners");
            }

            scheduled = scheduler.scheduleAtFixedRate(this, period, timeUnit);
            started = true;
        }

        @Override
        public void stop() {
            if(null != scheduled) {
                Try.catchAll(() -> scheduled.cancel(true));
                scheduled = null;
            }
        }

        @Override
        public void run() {
            if(!inited) {
                init();
                inited = true;
            }

            if(null == changesQuery) {
                changesQuery = dao.createCriteriaQuery(em, resultClass)
                                  .where(fm.getFieldName() + " > :maxValue")
                                  .limit(limit)
                                  .orderBy(fm.getFieldName() + " asc");
            }

            List changes = changesQuery.param("maxValue", maxValue).list();
            if(!changes.isEmpty()) {

                log.debug("Found {} changes", changes.size());

                Object lastNotified = null;
                try {
                    for (int i = 0; i < changes.size(); i++) {
                        Object entity = changes.get(i);
                        listener.onEntityChanged(dao, entity);
                        lastNotified = entity;
                    }
                }catch(Throwable e) {
                    log.error("Error notify changes to listener '{}', {}", listener, e.getMessage(), e);
                }

                if(null != lastNotified) {
                    Object newMaxValue = EntityWrapper.wrap(em, lastNotified).get(fm.getFieldName());
                    if(null != newMaxValue) {
                        log.debug("Set maxValue from {} to {}", maxValue, newMaxValue);
                        this.maxValue = newMaxValue;
                    }
                }
            }
        }

        protected void checkStarted() {
            if(started) {
                throw new IllegalStateException("Observer already started");
            }
        }

        protected void init() {
            log.info("Finding the max value of field '{}' at entity '{}'...", fm.getFieldName(), em.getEntityName());

            Object maxValue = dao.createCriteriaQuery(em)
                    .select(fm.getFieldName())
                    .limit(1).orderBy(fm.getFieldName() + " desc").firstOrNull();

            log.info("Max value : {}", maxValue);

            if(null != maxValue) {
                this.maxValue = maxValue;
            }
        }

    }
}

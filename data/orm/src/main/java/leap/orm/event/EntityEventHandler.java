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
import leap.orm.mapping.EntityMapping;

/**
 * The handler interface for handling events of entity (create, update, delete and load).
 */
public interface EntityEventHandler {

    /**
     * Returns true if handles the 'Create' event of the given entity.
     */
    boolean isHandleCreateEvent(OrmContext context, EntityMapping em);

    /**
     * Returns true if the create event should be handled in a transaction.
     */
    boolean isCreateEventTransactional(OrmContext context, EntityMapping em);

    /**
     * Executed before saving the entity record to db outside transaction.
     *
     * <p/>
     * The entity fields can be changed and all the changes will be saved to db.
     */
    void preCreateEntityNoTrans(OrmContext context, EntityMapping em, CreateEntityEvent e);

    /**
     * Executed before saving the entity record to db inside transaction.
     *
     * <p/>
     * The entity fields can be changed and all the changes will be saved to db.
     */
    void preCreateEntityInTrans(OrmContext context, EntityMapping em, CreateEntityEvent e);

    /**
     * Executed after saving the the entity record to db inside transaction.
     */
    void postCreateEntityInTrans(OrmContext context, EntityMapping em, CreateEntityEvent e);

    /**
     * Executed after saving the the entity record to db outside transaction.
     */
    void postCreateEntityNoTrans(OrmContext context, EntityMapping em, CreateEntityEvent e);

}
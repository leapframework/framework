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
import leap.orm.mapping.Mappings;
import leap.orm.value.EntityWrapper;

public class EntityEventWithWrapperImpl extends EntityEventBase implements CreateEntityEvent, UpdateEntityEvent {

    protected static final Object NULL_ID = new Object();

    protected final Type          type;
    protected final EntityWrapper entity;

    protected Object id;

    public EntityEventWithWrapperImpl(OrmContext context, EntityWrapper entity, Type type) {
        this(context, entity, null, type);
    }

    public EntityEventWithWrapperImpl(OrmContext context, EntityWrapper entity, Object id, Type type) {
        super(context, entity.getEntityMapping());
        this.entity = entity;
        this.id = id;
        this.type = type;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public Object getId() {
        if (id == NULL_ID) {
            return null;
        }
        if (id == null) {
            id = Mappings.getId(entity.getEntityMapping(), entity);
            if (null == id) {
                id = NULL_ID;
            }
        }
        return id;
    }

    @Override
    public EntityWrapper getEntity() {
        return entity;
    }

}
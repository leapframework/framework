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

package leap.orm.event.reflect;

import leap.core.transaction.TransactionStatus;
import leap.lang.Types;
import leap.lang.beans.BeanProperty;
import leap.lang.beans.BeanType;
import leap.lang.reflect.ReflectMethod;
import leap.lang.reflect.ReflectParameter;
import leap.lang.reflect.Reflection;
import leap.orm.annotation.Entity;
import leap.orm.event.EntityEvent;
import leap.orm.event.EntityEventWithWrapper;
import leap.orm.model.Model;
import leap.orm.value.EntityWrapper;

import java.util.function.Consumer;

public abstract class ReflectEntityListenerBase<E extends EntityEvent> {

    protected final Object        inst;
    protected final ReflectMethod method;
    protected final Consumer<E>   func;
    protected final boolean       transactional;

    protected ReflectEntityListenerBase(Object inst, ReflectMethod m) {
        this.inst   = inst;
        this.method = m;

        Class<?> eventType  = Types.getActualTypeArgument(this.getClass().getGenericSuperclass());
        boolean  withEntity = EntityEventWithWrapper.class.isAssignableFrom(eventType);

        if(m.getParameters().length == 1) {
            this.transactional = false;

            ReflectParameter p = m.getParameters()[0];

            if(p.getType().equals(eventType)) {
                func = (e) -> m.invoke(inst, e);
                return;
            }

            if(withEntity) {
                if(p.getType().equals(EntityWrapper.class)) {
                    func = (e) -> m.invoke(inst, ((EntityEventWithWrapper)e).getEntity());
                    return;
                }

                if(isEntityType(p)) {
                    BeanType bt = BeanType.of(p.getType());
                    func = (e) -> invoke((EntityEventWithWrapper)e, bt, (bean) -> m.invoke(inst, bean));
                    return;
                }
            }

        } else if(m.getParameters().length == 2) {
            this.transactional = true;

            ReflectParameter p0 = m.getParameters()[0];
            ReflectParameter p1 = m.getParameters()[1];

            if(withEntity) {
                if(p0.getType().equals(EntityWrapper.class) && p1.getType().equals(TransactionStatus.class)) {
                    func = (e) -> m.invoke(inst, ((EntityEventWithWrapper)e).getEntity(), e.getTransactionStatus());
                    return;
                }

                if(isEntityType(p0) && p1.getType().equals(TransactionStatus.class)) {
                    BeanType bt = BeanType.of(p0.getType());
                    func = (e) -> invoke((EntityEventWithWrapper)e, bt, (bean) -> m.invoke(inst, bean, e.getTransactionStatus()));
                    return;
                }
            }
        }

        throw new IllegalArgumentException("Incorrect parameters in method '" +
                                            Reflection.fullQualifyName(m.getReflectedMethod()) +
                                            "' for '" + eventType.getSimpleName() + "' event");
    }

    public boolean isTransactional() {
        return transactional;
    }

    protected boolean isEntityType(ReflectParameter p) {
        return Model.class.isAssignableFrom(p.getType()) || p.getType().isAnnotationPresent(Entity.class);
    }

    protected void invoke(EntityEventWithWrapper e, BeanType bt, Consumer<Object> func) {
        EntityWrapper entity = e.getEntity();

        boolean typeMatches = e.getEntity().raw().getClass().equals(bt.getBeanClass());

        Object bean;

        if(!typeMatches) {
            bean = bt.newInstance();
            for (String name : entity.getFieldNames()) {
                bt.trySet(bean, name, entity.get(name));
            }
        }else {
            bean = e.getEntity().raw();
        }

        func.accept(bean);

        if(!typeMatches) {
            for (BeanProperty bp : bt.getProperties()) {
                if(bp.isWritable()) {
                    entity.set(bp.getName(), bp.getValue(bean));
                }
            }
        }
    }

}

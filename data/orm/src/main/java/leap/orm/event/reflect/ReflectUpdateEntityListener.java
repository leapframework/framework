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
import leap.lang.reflect.ReflectMethod;
import leap.lang.reflect.ReflectParameter;
import leap.lang.reflect.Reflection;
import leap.orm.event.*;
import leap.orm.value.EntityWrapper;

import java.util.function.Consumer;

public class ReflectUpdateEntityListener implements PreUpdateListener,PostUpdateListener {

    private final Object                      inst;
    private final ReflectMethod               method;
    private final Consumer<UpdateEntityEvent> func;
    private final boolean                     transactional;

    public ReflectUpdateEntityListener(Object inst, ReflectMethod m) {
        this.inst = inst;
        this.method   = m;

        if(m.getParameters().length == 1) {
            this.transactional = false;

            ReflectParameter p = m.getParameters()[0];

            if(p.getType().equals(EntityWrapper.class)) {
                func = (e) -> m.invoke(inst, e.getEntity());
                return;
            }else if(p.getType().equals(UpdateEntityEvent.class)) {
                func = (e) -> m.invoke(inst, e);
                return;
            }

        } else if(m.getParameters().length == 2) {
            this.transactional = true;

            ReflectParameter p0 = m.getParameters()[0];
            ReflectParameter p1 = m.getParameters()[1];

            if(p0.getType().equals(EntityWrapper.class) && p1.getType().equals(TransactionStatus.class)) {
                func = (e) -> m.invoke(e.getEntity(), e.getTransactionStatus());
                return;
            }
        }

        throw new IllegalArgumentException("Incorrect parameters in method '" +
                                            Reflection.fullQualifyName(m.getReflectedMethod()) +
                                            "' for 'UpdateEntity' event");
    }

    public boolean isTransactional() {
        return transactional;
    }

    @Override
    public void postUpdateEntity(UpdateEntityEvent e) {
        func.accept(e);
    }

    @Override
    public void preUpdateEntity(UpdateEntityEvent e) {
        func.accept(e);
    }
}

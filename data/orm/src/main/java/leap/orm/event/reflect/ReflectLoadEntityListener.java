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

import leap.lang.reflect.ReflectMethod;
import leap.orm.event.LoadEntityEvent;
import leap.orm.event.PostLoadListener;

public class ReflectLoadEntityListener extends ReflectEntityListenerBase<LoadEntityEvent> implements PostLoadListener {

    public ReflectLoadEntityListener(Object inst, ReflectMethod m) {
        super(inst, m);
    }

    @Override
    public void postLoadEntity(LoadEntityEvent e) {
        func.accept(e);
    }

}
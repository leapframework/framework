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

import leap.orm.annotation.event.PreCreate;
import leap.orm.event.CreateEntityEvent;
import leap.orm.event.PreCreateListener;
import leap.orm.value.EntityWrapper;

public class EventListener implements PreCreateListener {

    @PreCreate
    public void setColumn1Value(EntityWrapper entity) {
        entity.set("col1","Test1");
    }

    @Override
    public void preCreateEntity(CreateEntityEvent e) {
        e.getEntity().set("col2", "Test2");
    }

}
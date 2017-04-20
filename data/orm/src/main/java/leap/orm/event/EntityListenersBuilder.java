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

import leap.lang.Buildable;
import leap.orm.annotation.event.PostCreate;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class EntityListenersBuilder implements Buildable<EntityListeners> {

    protected final Map<PreCreateListener,  Boolean> preCreateListeners = new LinkedHashMap<>();
    protected final Map<PostCreateListener, Boolean> postCreateListeners = new LinkedHashMap<>();

    public EntityListenersBuilder addPreCreateListener(PreCreateListener listener, boolean transactional) {
        preCreateListeners.put(listener, transactional);
        return this;
    }

    public EntityListenersBuilder addPostCreateListener(PostCreateListener listener, boolean transactional) {
        postCreateListeners.put(listener, transactional);
        return this;
    }

    @Override
    public EntityListeners build() {

        List<PreCreateListener> noTransPreCreateListeners   = new ArrayList<>();
        List<PreCreateListener> inTransPreCreateListeners   = new ArrayList<>();
        List<PostCreateListener> noTransPostCreateListeners = new ArrayList<>();
        List<PostCreateListener> inTransPostCreateListeners = new ArrayList<>();

        preCreateListeners.forEach((listener, trans) -> {
            if(trans) {
                inTransPreCreateListeners.add(listener);
            } else {
                noTransPreCreateListeners.add(listener);
            }
        });

        postCreateListeners.forEach((listener, trans) -> {
            if(trans) {
                inTransPostCreateListeners.add(listener);
            } else {
                noTransPostCreateListeners.add(listener);
            }
        });

        return new EntityListeners(noTransPreCreateListeners.toArray(new PreCreateListener[0]),
                                   inTransPreCreateListeners.toArray(new PreCreateListener[0]),
                                   noTransPostCreateListeners.toArray(new PostCreateListener[0]),
                                   inTransPostCreateListeners.toArray(new PostCreateListener[0]));
    }

}

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

public class EntityListeners {

    protected final PreCreateListener[]  noTransPreCreateListeners;
    protected final PreCreateListener[]  inTransPreCreateListeners;
    protected final PostCreateListener[] noTransPostCreateListeners;
    protected final PostCreateListener[] inTransPostCreateListeners;

    private final boolean hasCreateListeners;

    public EntityListeners(PreCreateListener[] noTransPreCreateListeners, PreCreateListener[] inTransPreCreateListeners,
                           PostCreateListener[] noTransPostCreateListeners, PostCreateListener[] inTransPostCreateListeners) {

        this.noTransPreCreateListeners  = noTransPreCreateListeners;
        this.inTransPreCreateListeners  = inTransPreCreateListeners;
        this.noTransPostCreateListeners = noTransPostCreateListeners;
        this.inTransPostCreateListeners = inTransPostCreateListeners;

        this.hasCreateListeners = noTransPreCreateListeners.length > 0 || inTransPreCreateListeners.length > 0 ||
                                  noTransPostCreateListeners.length > 0 || inTransPostCreateListeners.length > 0;
    }

    public boolean hasCreateListeners() {
        return hasCreateListeners;
    }

    public boolean hasTransCreateListeners() {
        return inTransPreCreateListeners.length > 0 || inTransPostCreateListeners.length > 0;
    }

    public PreCreateListener[] getNoTransPreCreateListeners() {
        return noTransPreCreateListeners;
    }

    public PreCreateListener[] getInTransPreCreateListeners() {
        return inTransPreCreateListeners;
    }

    public PostCreateListener[] getNoTransPostCreateListeners() {
        return noTransPostCreateListeners;
    }

    public PostCreateListener[] getInTransPostCreateListeners() {
        return inTransPostCreateListeners;
    }

}
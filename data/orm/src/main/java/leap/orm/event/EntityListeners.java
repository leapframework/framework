/*
 *  Copyright 2019 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package leap.orm.event;

public interface EntityListeners {
    PreCreateListener[]  EMPTY_PRE_CREATE_LISTENERS  = new PreCreateListener[0];
    PostCreateListener[] EMPTY_POST_CREATE_LISTENERS = new PostCreateListener[0];
    PreUpdateListener[]  EMPTY_PRE_UPDATE_LISTENERS  = new PreUpdateListener[0];
    PostUpdateListener[] EMPTY_POST_UPDATE_LISTENERS = new PostUpdateListener[0];
    PreDeleteListener[]  EMPTY_PRE_DELETE_LISTENERS  = new PreDeleteListener[0];
    PostDeleteListener[] EMPTY_POST_DELETE_LISTENERS = new PostDeleteListener[0];
    PostLoadListener[]   EMPTY_POST_LOAD_LISTENERS   = new PostLoadListener[0];

    default boolean hasCreateListeners() {
        return getNoTransPreCreateListeners().length > 0 || getNoTransPostCreateListeners().length > 0 ||
                hasTransCreateListeners();
    }

    default boolean hasTransCreateListeners() {
        return getInTransPreCreateListeners().length > 0 || getInTransPostCreateListeners().length > 0;
    }

    default boolean hasUpdateListeners() {
        return getNoTransPreUpdateListeners().length > 0 || getNoTransPostUpdateListeners().length > 0 ||
                hasTransUpdateListeners();
    }

    default boolean hasTransUpdateListeners() {
        return getInTransPreUpdateListeners().length > 0 || getInTransPostUpdateListeners().length > 0;
    }

    default boolean hasDeleteListeners() {
        return getNoTransPreDeleteListeners().length > 0 || getNoTransPostDeleteListeners().length > 0 ||
                hasTransDeleteListeners();
    }

    default boolean hasTransDeleteListeners() {
        return getInTransPreDeleteListeners().length > 0 || getInTransPostDeleteListeners().length > 0;
    }

    default boolean hasLoadListeners() {
        return getPostLoadListeners().length > 0;
    }

    default PreCreateListener[] getNoTransPreCreateListeners() {
        return EMPTY_PRE_CREATE_LISTENERS;
    }

    default PreCreateListener[] getInTransPreCreateListeners() {
        return EMPTY_PRE_CREATE_LISTENERS;
    }

    default PostCreateListener[] getNoTransPostCreateListeners() {
        return EMPTY_POST_CREATE_LISTENERS;
    }

    default PostCreateListener[] getInTransPostCreateListeners() {
        return EMPTY_POST_CREATE_LISTENERS;
    }

    default PreUpdateListener[] getNoTransPreUpdateListeners() {
        return EMPTY_PRE_UPDATE_LISTENERS;
    }

    default PreUpdateListener[] getInTransPreUpdateListeners() {
        return EMPTY_PRE_UPDATE_LISTENERS;
    }

    default PostUpdateListener[] getNoTransPostUpdateListeners() {
        return EMPTY_POST_UPDATE_LISTENERS;
    }

    default PostUpdateListener[] getInTransPostUpdateListeners() {
        return EMPTY_POST_UPDATE_LISTENERS;
    }

    default PreDeleteListener[] getNoTransPreDeleteListeners() {
        return EMPTY_PRE_DELETE_LISTENERS;
    }

    default PreDeleteListener[] getInTransPreDeleteListeners() {
        return EMPTY_PRE_DELETE_LISTENERS;
    }

    default PostDeleteListener[] getNoTransPostDeleteListeners() {
        return EMPTY_POST_DELETE_LISTENERS;
    }

    default PostDeleteListener[] getInTransPostDeleteListeners() {
        return EMPTY_POST_DELETE_LISTENERS;
    }

    default PostLoadListener[] getPostLoadListeners() {
        return EMPTY_POST_LOAD_LISTENERS;
    }
}
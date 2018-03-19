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

    protected final PreUpdateListener[]  noTransPreUpdateListeners;
    protected final PreUpdateListener[]  inTransPreUpdateListeners;
    protected final PostUpdateListener[] noTransPostUpdateListeners;
    protected final PostUpdateListener[] inTransPostUpdateListeners;

    protected final PreDeleteListener[]  noTransPreDeleteListeners;
    protected final PreDeleteListener[]  inTransPreDeleteListeners;
    protected final PostDeleteListener[] noTransPostDeleteListeners;
    protected final PostDeleteListener[] inTransPostDeleteListeners;

    protected final PostLoadListener[] postLoadListeners;

    private final boolean hasCreateListeners;
    private final boolean hasUpdateListeners;
    private final boolean hasDeleteListeners;
    private final boolean hasLoadListeners;

    public EntityListeners(PreCreateListener[] noTransPreCreateListeners, PreCreateListener[] inTransPreCreateListeners,
                           PostCreateListener[] noTransPostCreateListeners, PostCreateListener[] inTransPostCreateListeners,
                           PreUpdateListener[] noTransPreUpdateListeners, PreUpdateListener[] inTransPreUpdateListeners,
                           PostUpdateListener[] noTransPostUpdateListeners, PostUpdateListener[] inTransPostUpdateListeners,
                           PreDeleteListener[] noTransPreDeleteListeners, PreDeleteListener[] inTransPreDeleteListeners,
                           PostDeleteListener[] noTransPostDeleteListeners, PostDeleteListener[] inTransPostDeleteListeners,
                           PostLoadListener[] postLoadListeners) {

        this.noTransPreCreateListeners  = noTransPreCreateListeners;
        this.inTransPreCreateListeners  = inTransPreCreateListeners;
        this.noTransPostCreateListeners = noTransPostCreateListeners;
        this.inTransPostCreateListeners = inTransPostCreateListeners;

        this.noTransPreUpdateListeners  = noTransPreUpdateListeners;
        this.inTransPreUpdateListeners  = inTransPreUpdateListeners;
        this.noTransPostUpdateListeners = noTransPostUpdateListeners;
        this.inTransPostUpdateListeners = inTransPostUpdateListeners;

        this.noTransPreDeleteListeners  = noTransPreDeleteListeners;
        this.inTransPreDeleteListeners  = inTransPreDeleteListeners;
        this.noTransPostDeleteListeners = noTransPostDeleteListeners;
        this.inTransPostDeleteListeners = inTransPostDeleteListeners;

        this.postLoadListeners = postLoadListeners;

        this.hasCreateListeners = noTransPreCreateListeners.length > 0 || inTransPreCreateListeners.length > 0 ||
                                  noTransPostCreateListeners.length > 0 || inTransPostCreateListeners.length > 0;

        this.hasUpdateListeners = noTransPreUpdateListeners.length > 0 || inTransPreUpdateListeners.length > 0 ||
                                  noTransPostUpdateListeners.length > 0 || inTransPostUpdateListeners.length > 0;

        this.hasDeleteListeners = noTransPreDeleteListeners.length > 0 || inTransPreDeleteListeners.length > 0 ||
                                  noTransPostDeleteListeners.length > 0 || inTransPostDeleteListeners.length > 0;

        this.hasLoadListeners = postLoadListeners.length > 0;
    }

    public boolean hasCreateListeners() {
        return hasCreateListeners;
    }

    public boolean hasTransCreateListeners() {
        return inTransPreCreateListeners.length > 0 || inTransPostCreateListeners.length > 0;
    }

    public boolean hasUpdateListeners() {
        return hasUpdateListeners;
    }

    public boolean hasTransUpdateListeners() {
        return inTransPreUpdateListeners.length > 0 || inTransPostUpdateListeners.length > 0;
    }

    public boolean hasDeleteListeners() {
        return hasDeleteListeners;
    }

    public boolean hasTransDeleteListeners() {
        return inTransPreDeleteListeners.length > 0 || inTransPostDeleteListeners.length > 0;
    }

    public boolean hasLoadListeners() {
        return hasLoadListeners;
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

    public PreUpdateListener[] getNoTransPreUpdateListeners() {
        return noTransPreUpdateListeners;
    }

    public PreUpdateListener[] getInTransPreUpdateListeners() {
        return inTransPreUpdateListeners;
    }

    public PostUpdateListener[] getNoTransPostUpdateListeners() {
        return noTransPostUpdateListeners;
    }

    public PostUpdateListener[] getInTransPostUpdateListeners() {
        return inTransPostUpdateListeners;
    }

    public PreDeleteListener[] getNoTransPreDeleteListeners() {
        return noTransPreDeleteListeners;
    }

    public PreDeleteListener[] getInTransPreDeleteListeners() {
        return inTransPreDeleteListeners;
    }

    public PostDeleteListener[] getNoTransPostDeleteListeners() {
        return noTransPostDeleteListeners;
    }

    public PostDeleteListener[] getInTransPostDeleteListeners() {
        return inTransPostDeleteListeners;
    }

    public PostLoadListener[] getPostLoadListeners() {
        return postLoadListeners;
    }
}
/*
 *  Copyright 2018 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package leap.web.api.orm;

import leap.web.api.mvc.params.DeleteOptions;

import java.util.Map;

public interface ModelDeleteInterceptor {

    @Deprecated
    default boolean processDeleteOneOptions(ModelExecutorContext context, Object id, DeleteOptions options) {
        return false;
    }

    default boolean processDeleteOneOptions(ModelExecutionContext context, Object id, DeleteOptions options) {
        return false;
    }

    default boolean preDeleteOne(ModelExecutionContext context, Object id) {
        return false;
    }

    default boolean preDeleteOneByKey(ModelExecutionContext context, Map<String, Object> key) {
        return false;
    }

    default DeleteOneResult handleDeleteOne(ModelExecutionContext context, Object id, DeleteOptions options) {
        return null;
    }

    default Object processDeleteOneResult(ModelExecutionContext context, Object id, boolean success) {
        return null;
    }

    default void completeDeleteOne(ModelExecutionContext context, DeleteOneResult result, Throwable e) {

    }
}

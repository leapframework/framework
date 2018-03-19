/*
 * Copyright 2018 the original author or authors.
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

package leap.web.api.orm;

import leap.web.api.mvc.params.DeleteOptions;

public interface ModelDeleteHandler {

    /**
     * Process the delete options.
     */
    default void processDeleteOptions(ModelExecutorContext context, Object id, DeleteOptions options) {

    }

    /**
     * Returns <code>null</code> if the execution has not been handled.
     *
     * <p/>
     * Returns {@link DeleteOneResult} if the delete execution has been handled.
     */
    default DeleteOneResult handleDeleteExecution(ModelExecutorContext context, Object id, DeleteOptions options) {
        return null;
    }

    /**
     * Called after the delete execution has been executed.
     */
    default DeleteOneResult postDeleteRecore(ModelExecutorContext context, Object id, DeleteOptions options, DeleteOneResult result) {
        return null;
    }

}
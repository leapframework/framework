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

package leap.web.api.orm;

import leap.orm.event.EntityListeners;
import leap.web.api.mvc.params.DeleteOptions;

import java.util.Map;

public interface ModelDeleteExecutor {

    interface DeleteHandler {

        /**
         * Returns the affected rows.
         */
        int deleteOne(ModelExecutionContext context, Object id, DeleteOptions options);

    }

    ModelDeleteExecutor withHandler(DeleteHandler handler);

    ModelDeleteExecutor withListeners(EntityListeners listeners);

    DeleteOneResult deleteOne(Object id, DeleteOptions options);

    /**
     * Delete a record by the unique key.
     */
    DeleteOneResult deleteOneByKey(Map<String, Object> key, DeleteOptions options);

}
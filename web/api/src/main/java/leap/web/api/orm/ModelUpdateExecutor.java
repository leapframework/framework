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
import leap.web.api.mvc.params.Partial;

import java.util.Map;

public interface ModelUpdateExecutor {

    interface UpdateHandler {

        /**
         * Returns the affected rows.
         */
        int partialUpdate(ModelExecutionContext context, Object id, Map<String, Object> properties);

    }

    ModelUpdateExecutor withHandler(UpdateHandler handler);

    ModelUpdateExecutor withListeners(EntityListeners listeners);

    UpdateOneResult replaceUpdateOne(Object id, Map<String, Object> record);

    UpdateOneResult partialUpdateOne(Object id, Partial partial);

    UpdateOneResult partialUpdateOne(Object id, Map<String,Object> properties);

}
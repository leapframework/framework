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

import java.util.Map;

public interface ModelUpdateHandler {

    /**
     * Process the replace record.
     */
    default void processReplaceRecord(ModelExecutorContext context, Object id, Map<String, Object> record) {

    }

    /**
     * Process the update properties.
     */
    default void processUpdateProperties(ModelExecutorContext context, Object id, Map<String, Object> properties) {

    }

    /**
     * Called before updating properties
     */
    default void preUpdateProperties(ModelExecutorContext context, Object id, Map<String, Object> properties) {

    }

    /**
     * Called after updating properties.
     */
    default void postUpdateProperties(ModelExecutorContext context, Object id, int affected) {

    }
}

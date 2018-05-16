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

import java.util.Map;

public interface ModelUpdateInterceptor {

    /**
     * Process the update properties.
     */
    default boolean processUpdateProperties(ModelExecutorContext context, Object id, Map<String, Object> properties) {
        return false;
    }

    /**
     * Handles not exists property.
     */
    default boolean handleUpdatePropertyNotFound(ModelExecutorContext context, String name, Object value) {
        return false;
    }

    /**
     * Handles not updatable property
     */
    default boolean handleUpdatePropertyReadonly(ModelExecutorContext context, String name, Object value) {
        return false;
    }

    /**
     * Called before updating properties
     */
    default boolean preUpdateProperties(ModelExecutorContext context, Object id, Map<String, Object> properties) {
        return false;
    }

}

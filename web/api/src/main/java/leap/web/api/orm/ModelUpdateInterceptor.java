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
import java.util.Set;

public interface ModelUpdateInterceptor {

    /**
     * Process the update properties.
     */
    default boolean processUpdateProperties(ModelExecutionContext context, Object id, Map<String, Object> properties) {
        return false;
    }

    default ModelDynamic resolveUpdateDynamic(ModelExecutionContext context, Object id, Map<String, Object> properties) {
        return null;
    }

    /**
     * Process the update properties.
     */
    default boolean processUpdatePropertiesByKey(ModelExecutionContext context, Map<String, Object> filters, Map<String, Object> properties) {
        return false;
    }

    /**
     * Handles not exists property.
     */
    default boolean handleUpdatePropertyNotFound(ModelExecutionContext context, String name, Object value, Set<String> removes) {
        return false;
    }

    /**
     * Handles not updatable property
     */
    default boolean handleUpdatePropertyReadonly(ModelExecutionContext context, String name, Object value, Set<String> removes) {
        return false;
    }

    default boolean preUpdate(ModelExecutionContext context, Object id, Map<String, Object> properties) {
        return false;
    }

    default Object postUpdateProperties(ModelExecutionContext context, Object id, int affected) {
        return null;
    }

    default boolean preUpdateByKey(ModelExecutionContext context, Map<String, Object> filters, Map<String, Object> properties) {
        return false;
    }

    default Object postUpdatePropertiesByKey(ModelExecutionContext context, Map<String, Object> filters, int affected) {
        return null;
    }

    default UpdateOneResult handleUpdateOne(ModelExecutionContext context, Object id, Map<String, Object> properties) {
        return null;
    }

    default void completeUpdate(ModelExecutionContext context, UpdateOneResult result, Throwable e) {

    }
}

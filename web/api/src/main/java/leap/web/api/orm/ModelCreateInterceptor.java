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

import leap.core.value.Record;

import java.util.Map;
import java.util.Set;

public interface ModelCreateInterceptor {

    /**
     * Returns <code>null</code> if not processed.
     *
     * <p/>
     * Returns an object will replace the passed in params.
     */
    default Object processCreationParams(ModelExecutionContext context, Object params) {
        return null;
    }

    /**
     * Process the record for creation.
     */
    default boolean processCreationRecord(ModelExecutionContext context, Map<String,Object> record) {
        return false;
    }

    /**
     * Returns the {@link ModelDynamic} if exists.
     */
    default ModelDynamic resolveModelDynamic(ModelExecutionContext context, Map<String,Object> record) {
        return null;
    }

    /**
     * Handles not exists property.
     */
    default boolean handleCreationPropertyNotFound(ModelExecutionContext context, String name, Object value, Set<String> removes) {
        return false;
    }

    /**
     * Handles not creatable property
     */
    default boolean handleCreationPropertyReadonly(ModelExecutionContext context, String name, Object value, Set<String> removes) {
        return false;
    }

    /**
     * Called before creating record.
     */
    default boolean preCreateRecord(ModelExecutionContext context, Map<String, Object> record) {
        return false;
    }

    /**
     * Called after record has been created.
     */
    default Object postCreateRecord(ModelExecutionContext context, Object id, Record record) {
        return null;
    }
}
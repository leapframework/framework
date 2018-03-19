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

public interface ModelCreateHandler {

    /**
     * Returns <code>null</code> if not processed.
     *
     * <p/>
     * Returns an object will replace the passed in params.
     */
    default Object processCreationParams(ModelExecutorContext context, Object params) {
        return null;
    }

    /**
     * Process the record for creation.
     */
    default void processCreationRecord(ModelExecutorContext context, Map<String,Object> record) {

    }

    /**
     * Called before creating record.
     */
    default void preCreateRecord(ModelExecutorContext context, Map<String, Object> record) {

    }

    /**
     * Called after record has been created.
     */
    default void postCreateRecord(ModelExecutorContext context, Object id) {

    }

}

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
package leap.web.api.restd;

import leap.lang.Named;
import leap.lang.enums.Bool;
import leap.web.api.meta.model.MApiOperationBuilder;

import java.util.Map;

public interface RestdOperationDef extends Named {

    /**
     * Required. Returns the type of operation.
     */
    String getType();

    /**
     * A path starts with '/' but suffix without '/'
     */
    String getPath();

    /**
     * Optional. Returns the script if necessary.
     */
    String getScript();

    /**
     * Optional. Returns the path of script.
     */
    String getScriptPath();

    /**
     * todo: doc
     */
    Boolean getPrior();

    /**
     * Optional. Returns the meta operation.
     */
    MApiOperationBuilder getMetaOperation();

    /**
     * Optional. Returns the arguments for creating operation if necessary.
     */
    Map<String, Object> getArguments();

    /**
     * Returns the argument or null.
     */
    <T> T getArgument(String name);

    /**
     * todo: doc
     */
    default boolean isExplicitPrior() {
        return Boolean.TRUE.equals(getPrior());
    }

    /**
     * todo: doc
     */
    default boolean isExplicitNotPrior() {
        return Boolean.FALSE.equals(getPrior());
    }
}
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

package leap.web.api.config.model;

import leap.lang.*;

import java.util.Set;

public interface ParamConfig extends Named,Titled,Described,Keyed {

    /**
     * Builds a key.
     */
    static String key(String className, String name) {
        return "class=" + className + (Strings.isEmpty(name) ? "" : ",name=" + name.toLowerCase());
    }

    /**
     * Required. The unique key of param.
     */
    default String getKey() {
        return key(getClassName(), getName());
    }

    /**
     * Is overrides the old with same key.
     */
    boolean isOverride();

    /**
     * Required.
     */
    String getClassName();

    /**
     * The name of parameter, optional if this is a wrapped param.
     */
    String getName();

    /**
     * Optional.
     */
    String getTitle();

    /**
     * Optional.
     */
    String getSummary();

    /**
     * Optional.
     */
    String getDescription();

    /**
     * Optional.
     */
    Set<ParamConfig> getWrappedParams();

    /**
     * Returns the wrapped parameter or null.
     */
    ParamConfig getWrappedParam(String name);

}

/*
 *  Copyright 2018 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package leap.lang.beans;

import leap.lang.Objects2;

import java.util.LinkedHashMap;
import java.util.Map;

public interface DynaProps {

    /**
     * Returns all the dyna properties.
     */
    Map<String, Object> getDynaProperties();

    /**
     * Sets all the dyna properties.
     */
    void setDynaProperties(Map<String, Object> props);

    /**
     * Returns <code>true</code> if the dyna property exists.
     */
    default boolean hasDynaProperty(String name) {
        Map props = getDynaProperties();
        return null == props ? false : props.containsKey(name);
    }

    /**
     * Returns <code>true</code> if the dyan property's value is empty (empty string, empty collection, etc).
     */
    default boolean isEmptyDynaProperty(String name) {
        return Objects2.isEmpty(getDynaProperty(name));
    }

    /**
     * Returns the value of dyan property, returns <code>null</code> if not exists.
     */
    default Object getDynaProperty(String name) {
        Map map = getDynaProperties();
        return null == map ? null : map.get(name);
    }

    /**
     * Sets the dyna property.
     */
    default void setDynaProperty(String name, Object value) {
        if(null == getDynaProperties()) {
            setDynaProperties(new LinkedHashMap<>());
        }
        getDynaProperties().put(name, value);
    }
}

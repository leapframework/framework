/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package leap.core.config;

import leap.lang.accessor.AttributeAccessor;
import leap.lang.resource.Resource;

import java.util.Map;

public interface AppConfigContextBase extends AttributeAccessor {

    /**
     * Returns true if the config property is override by default.
     */
    boolean isDefaultOverride();

    /**
     * Sets the default override.
     */
    void setDefaultOverride(boolean b);

    /**
     * Resets the default override to original value.
     */
    void resetDefaultOverride();

    /**
     * Returns current profile's name.
     */
    String getProfile();

    /**
     * Returns true if the property exists.
     */
    boolean hasProperty(String name);

    /**
     * Sets the property.
     */
    void putProperty(Object source, String name, String value);

    /**
     * Puts all the properties.
     */
    void putProperties(Object source, Map<String,String> props);

    /**
     * Imports a config resource.
     */
    void importResource(Resource resource, boolean override);

}
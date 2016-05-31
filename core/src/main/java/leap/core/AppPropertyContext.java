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

package leap.core;

import java.util.Map;

/**
 * The context for {@link AppPropertyReader}.
 */
public interface AppPropertyContext extends AppConfigContextBase {

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
     * Adds a loader's config.
     */
    void addLoader(AppPropertyLoaderConfig loader);

}
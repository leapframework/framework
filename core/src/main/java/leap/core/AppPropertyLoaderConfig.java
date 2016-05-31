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

import leap.lang.Ordered;

import java.util.Map;

public interface AppPropertyLoaderConfig extends Ordered {

    /**
     * Loads the config, return true if enabled.
     */
    boolean load(Map<String,String> properties);

    /**
     * The class name of object.
     */
    String getClassName();

    /**
     * The property values of object.
     */
    Map<String,String> getProperties();

}

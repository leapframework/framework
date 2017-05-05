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

import leap.lang.Described;
import leap.lang.Named;
import leap.lang.Titled;

import java.util.Set;

/**
 * The configuration of an api model.
 */
public interface ModelConfig {

    /**
     * Optional. The name of model.
     */
    String getName();

    /**
     * Optional. The class name of model.
     */
    String getClassName();

    /**
     * Optional.
     */
    Set<Property> getProperties();

    /**
     * Returns the property or null if not exists.
     */
    Property getProperty(String name);

    /**
     * The configuration a model's property.
     */
    interface Property extends Named,Titled,Described {

        /**
         * Required. The name of property.
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
    }

}
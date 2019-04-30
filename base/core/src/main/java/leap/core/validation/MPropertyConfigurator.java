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

package leap.core.validation;

import leap.core.validation.annotations.NotNull;
import leap.core.validation.annotations.Required;
import leap.lang.beans.BeanProperty;
import leap.lang.meta.AbstractMTypeFactory;
import leap.lang.meta.MPropertyBuilder;

public class MPropertyConfigurator {

    static {
        AbstractMTypeFactory.addPropertyConfigurator(MPropertyConfigurator::configureProperty);
    }

    protected static void configureProperty(BeanProperty bp, MPropertyBuilder mp) {
        NotNull notNull = bp.getAnnotation(NotNull.class);
        if(null != notNull) {
            mp.setRequired(true);
        }

        Required required = bp.getAnnotation(Required.class);
        if(null != required) {
            mp.setRequired(true);
        }
    }

}
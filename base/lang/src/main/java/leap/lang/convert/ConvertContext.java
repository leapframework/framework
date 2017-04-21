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

package leap.lang.convert;

import leap.lang.beans.BeanProperty;

import java.lang.reflect.Type;
import java.util.Map;

public interface ConvertContext {

    ConvertContext EMPTY = new ConvertContext() {};

    interface ConcreteTypes {

        /**
         * Creates a new instance, returns null if not supported.
         */
        default Object newInstance(ConvertContext context, Class<?> type, Type genericType, Map<String,Object> map) {
            return null;
        }

    }

    default ConcreteTypes getConcreteTypes() {
        return null;
    }

}

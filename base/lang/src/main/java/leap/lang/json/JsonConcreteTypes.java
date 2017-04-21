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

package leap.lang.json;

import leap.lang.Classes;
import leap.lang.Strings;
import leap.lang.convert.ConvertContext;
import leap.lang.reflect.Reflection;

import java.lang.reflect.Type;
import java.util.Map;

class JsonConcreteTypes implements ConvertContext.ConcreteTypes {

    static final JsonConcreteTypes INSTANCE = new JsonConcreteTypes();

    @Override
    public Object newInstance(ConvertContext context, Class<?> type, Type genericType, Map<String, Object> map) {
        JsonType jsonType = type.getAnnotation(JsonType.class);
        if(null == jsonType) {
            return null;
        }

        String propertyName  = Strings.firstNotEmpty(jsonType.property(), jsonType.meta().getDefaultPropertyName());
        Object propertyValue = map.get(propertyName);
        if(null == propertyValue) {
            return null;
        }

        String name = (String)propertyValue;

        if(jsonType.meta() == JsonType.MetaType.CLASS_NAME) {
            return Reflection.newInstance(Classes.forName(name));
        }else {
            for(JsonType.SubType subType : jsonType.types()) {
                if(subType.name().equals(name)) {
                    return Reflection.newInstance(subType.type());
                }
            }
        }

        return null;
    }

}

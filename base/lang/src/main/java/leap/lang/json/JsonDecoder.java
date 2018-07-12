/*
 * Copyright 2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package leap.lang.json;

import leap.lang.Classes;
import leap.lang.Types;
import leap.lang.beans.BeanProperty;
import leap.lang.beans.BeanType;

import java.io.Reader;
import java.lang.reflect.Type;
import java.util.*;

class JsonDecoder {
    
    public JsonDecoder(){

    }

    public Object decode(String string){
        return new JsonParser(JsonParser.MODE_PERMISSIVE).parse(string);
    }
    
    public Object decode(Reader reader){
        return new JsonParser(JsonParser.MODE_PERMISSIVE).parse(reader);
    }

    static Set<String> checkMissingProperties(Class<?> type, Map map) {
        Set<String> set = new LinkedHashSet<>();

        doCheckMissingProperties(set, "", type, null, map);

        return set;
    }

    static void doCheckMissingProperties(Set<String> set, String prefix, Class<?> type, Type genericType, Map map) {
        if(null == map || map.isEmpty()) {
            return;
        }

        if(Map.class.isAssignableFrom(type)) {
            if(null == genericType) {
                return;
            }

            Class<?> valueType = Types.getActualTypeArguments(genericType)[1];
            if(valueType.equals(Object.class)) {
                return;
            }

            if(Classes.isSimpleValueType(valueType)) {
                return;
            }

            map.forEach((k, v) -> {
                if(v instanceof Map) {
                    String name = k.toString();

                    doCheckMissingProperties(set, prefix + name + ".", valueType, null, (Map)v);
                }
            });
        }else {
            BeanType bt = BeanType.of(type);

            map.forEach((k, v) -> {
                if(null == v) {
                    return;
                }

                String name = k.toString();

                BeanProperty bp = bt.tryGetProperty(name);
                if(bp == null) {
                    set.add(prefix + name);
                }else if(v instanceof Map) {
                    doCheckMissingProperties(set, prefix + name + ".", bp.getType(), bp.getGenericType(), (Map)v);
                }else if(v instanceof List) {
                    List list = (List)v;
                    if(list.isEmpty()) {
                        return;
                    }

                    Class<?> elementType = bp.getElementType();
                    for(int i=0;i< list.size();i++) {
                        Object item = list.get(i);
                        if(item instanceof Map) {
                            String itemName = name + "[" + i + "]";
                            doCheckMissingProperties(set, prefix + itemName + ".", elementType, null, (Map)item);
                        }
                    }
                }
            });
        }

    }
}
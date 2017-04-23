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

package leap.web.api.mvc.params;

import leap.lang.Types;
import leap.lang.json.JSON;

import java.lang.reflect.Type;
import java.util.Map;

public class PartialImpl<T> implements Partial {

    private final Class<T> clzz;
    private final Map m;
    private T t;

    public PartialImpl(Map m,Type genericType) throws IllegalAccessException, InstantiationException {
        this.m = m;
        if(genericType != null){
            clzz = (Class<T>)Types.getActualTypeArgument(genericType);
        }else{
            clzz = null;
        }
    }

    @Override
    public boolean isEmpty() {
        return null == m || m.isEmpty();
    }

    @Override
    public Map<String, Object> getProperties() {
        return (Map<String,Object>)m;
    }

    @Override
    public T getObject() {
        if(t != null){
            return t;
        }else if (clzz != null){
            if(m != null){
                t = JSON.decode(JSON.encode(m),clzz);
            }
            return t;
        }else {
            return (T)m;
        }
    }
}
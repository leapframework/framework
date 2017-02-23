/*
 *
 *  * Copyright 2013 the original author or authors.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  
 */

package leap.lang.json;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;

/**
 * Created by kael on 2017/2/23.
 */
public abstract class Target<T> {
    
    public final Class<T> targetType;
    public Type[] argTypes;
    
    public Target(Class<T> targetType) {
        this.targetType = targetType;
    }

    public static <K,V> Target<Map<K,V>> ofMap(Class<K> keyType, Class<V> valueType){
        Target<Map<K,V>> target = new Target(Map.class) {};
        ParameterizedTypeImpl[] types = new ParameterizedTypeImpl[]{
            new ParameterizedTypeImpl(null,target.targetType,keyType),
            new ParameterizedTypeImpl(null,target.targetType,valueType),
        };
        target.argTypes = types;
        return target;
    }
    
    public static class ParameterizedTypeImpl implements ParameterizedType{
        private final Type[] actualTypeArguments;
        private final Type   ownerType;
        private final Type   rawType;

        public ParameterizedTypeImpl(Type[] actualTypeArguments, Type ownerType, Type rawType) {
            this.actualTypeArguments = actualTypeArguments;
            this.ownerType = ownerType;
            this.rawType = rawType;
        }

        @Override
        public Type[] getActualTypeArguments() {
            return actualTypeArguments;
        }

        @Override
        public Type getRawType() {
            return rawType;
        }

        @Override
        public Type getOwnerType() {
            return ownerType;
        }
    } 
    
}

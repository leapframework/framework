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

package leap.core.ioc;

import leap.core.AppConfig;
import leap.core.annotation.Inject;
import leap.lang.Out;
import leap.lang.Types;
import leap.lang.reflect.ReflectValued;

import java.lang.annotation.Annotation;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author kael.
 */
public class ClassesFieldInjector implements BeanInjector {
    
    protected @Inject AppConfig config;
    
    @Override
    public boolean resolveInjectValue(BeanDefinition bd, Object bean, ReflectValued v, Annotation a,
                                      Out<Object> value) {
        if(!(a instanceof Inject)){
            return false;
        }
        if(v.getType().isArray() && Class.class.isAssignableFrom(v.getType().getComponentType())){
            java.lang.reflect.Type type = v.getGenericType();
            if(type instanceof GenericArrayType){
                java.lang.reflect.Type componentType = ((GenericArrayType) type).getGenericComponentType();
                Type elementType = Types.getTypeArgument(componentType);
                if(elementType == Object.class){
                    return false;
                }
                Class<?>[] values = Stream.of(config.getResources().searchClasses())
                        .filter(cls -> Types.isAssignable(cls,elementType) && cls != elementType)
                        .toArray(length -> new Class<?>[length]);
                value.ok(values);
                return true;
            }
            return false;
        } 
        if(List.class.isAssignableFrom(v.getType()) && Class.class.isAssignableFrom(Types.getActualTypeArgument(v.getGenericType()))){
            java.lang.reflect.Type elementType = v.getGenericType();
            if(elementType instanceof ParameterizedType){
                java.lang.reflect.Type actualClass = ((ParameterizedType) elementType).getActualTypeArguments()[0];
                java.lang.reflect.Type classType =  Types.getTypeArgument(actualClass);
                if(elementType == Object.class){
                    return false;
                }
                List<Class<?>> values = Stream.of(config.getResources().searchClasses())
                        .filter(cls -> Types.isAssignable(cls,classType) && cls != classType)
                        .collect(Collectors.toList());
                value.ok(values);
                return true;
            }
            return false;
        }
        return false;
    }
}

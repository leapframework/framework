/*
 * Copyright 2013 the original author or authors.
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
package leap.orm.model;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import leap.lang.Classes;
import leap.lang.Strings;

public class ModelFields {

	private static final Map<Class<?>, AtomicInteger> ATTR_COUNTERS = new ConcurrentHashMap<Class<?>, AtomicInteger>();
	
	static void postInitialize(){
		ATTR_COUNTERS.clear();
	}
	
	public static ModelField define(Class<?> type){
		Field field = getCurrentField();
		String name = Strings.lowerCamel(field.getName().toLowerCase(),'_');
		return new ModelField(name, type, field);
	}
	
	private static Field getCurrentField(){
		Class<?>      modelClass  = getCurrentModelClass();
		AtomicInteger attrCounter = ATTR_COUNTERS.get(modelClass);
		
		if(null == attrCounter){
			attrCounter = new AtomicInteger();
			ATTR_COUNTERS.put(modelClass,attrCounter);
		}
		
		int index=-1;
		for(Field field : modelClass.getDeclaredFields()){
			if(Modifier.isStatic(field.getModifiers()) && field.getType().equals(ModelField.class)){
				index++;
				
				if(attrCounter.get() == index){
					attrCounter.incrementAndGet();
					return field;
				}
			}
		}
		
		throw new IllegalStateException("Failed to get attribute's field, something wrong?");
	}
	
    private static Class<?> getCurrentModelClass() {
        StackTraceElement[] traces = Thread.currentThread().getStackTrace();
        
        boolean foundAttributes = false; 
        
        for(int i=0;i<traces.length;i++){
            StackTraceElement trace = traces[i];
            
            if(trace.getClassName().equals(ModelFields.class.getName())){
            	foundAttributes = true;
            	continue;
            }else if(foundAttributes){
                return Classes.tryForName(trace.getClassName());
            }
        }
        throw new IllegalStateException("Failed to determine Model class, something wrong?");
    }
	
	protected ModelFields(){
		
	}
}

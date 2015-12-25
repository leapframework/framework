/*
 * Copyright 2012 the original author or authors.
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
package leap.lang.convert;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.Collection;

import leap.lang.Out;
import leap.lang.Strings;
import leap.lang.Types;

@SuppressWarnings({"unchecked","rawtypes"})
public abstract class AbstractCollectionConverter<T extends Collection> extends AbstractConverter<T>{

	@Override
    public boolean convertFrom(Object value, Class<?> targetType, Type genericType, Out<Object> out) throws Throwable {

		Class<?> elementType = null == genericType ? Object.class : Types.getActualTypeArgument(genericType);
		
		if(value instanceof Iterable<?>){
			out.set(toCollection(targetType, elementType, (Iterable<?>)value));
			return true;
		}else if(value.getClass().isArray()){
			out.set(toCollection(targetType, elementType, (Object[])value));
			return true;
		}else if(value instanceof CharSequence){
			out.set(toCollection(targetType, elementType, value.toString()));
			return true;
		}
		
		return false;
    }
	
	@Override
    public String convertToString(T collection) throws Throwable {
        StringBuilder buf = new StringBuilder(100);
        
        int i = 0;
        
        for (Object value : collection) {
            
        	if (i > 0) {
                buf.append(',');
            }
            
            buf.append(Converts.toString(value));
            
            i++;
        }
        
        return buf.toString();
    }

    public T toCollection(Class<?> targetType,Class<?> elementType,Iterable iterable) throws Throwable {
		
		T collection = newInstance(targetType);
		
		for(Object e : iterable){
			collection.add(Converts.convert(e,elementType));
		}
		
		return collection;
	}
	
    public T toCollection(Class<?> targetType,Class<?> elementType,Object[] array) throws Throwable {
		
		T collection = newInstance(targetType);
		
		for(Object e : array){
			collection.add(Converts.convert(e,elementType));
		}
		
		return collection;
	}
    
    public T toCollectionFromArray(Class<?> targetType,Class<?> elementType,Object array) throws Throwable {
    	T collection = newInstance(targetType);
    	
    	for(int i=0;i<Array.getLength(array);i++){
    		collection.add(Converts.convert(Array.get(array, i),elementType));
    	}
    	
    	return collection;
    }
	
    public T toCollection(Class<?> targetType,Class<?> elementType,String stringValue) throws Throwable {
		
		String[] stringArray = Strings.split(stringValue);
		
		T collection = newInstance(targetType);
		
		for(String string : stringArray){
			collection.add(Converts.convert(string,elementType));
		}
		
		return collection;
		
	}
	
	protected abstract T newInstance(Class<?> targetType);
}

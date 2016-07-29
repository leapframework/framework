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
package leap.lang;

import leap.lang.reflect.ReflectEnum;

/**
 * <code>null</code> safe {@link Enum} utility.
 */
public class Enums {

	public static Object getValue(Enum<?> enumObject) {
		ReflectEnum reflectEnum = ReflectEnum.of(enumObject.getClass());
		
		if(reflectEnum.isValued()){
			return reflectEnum.getValue(enumObject);
		}else{
			return enumObject.toString();
		}
	}
	
	public static <E extends Enum<?>> E valueOf(Class<E> enumType,Object value) throws IllegalStateException{
        if(null == value){
            return null;
        }
        
    	String stringValue = value.toString();
        
        ReflectEnum reflectEnum = ReflectEnum.of(enumType);
        
        if(reflectEnum.isValued()){
        	
            for(E e : enumType.getEnumConstants()){
                if(reflectEnum.getValue(e).toString().equals(stringValue)){
                    return e;
                }
            }
        }else{
            if(stringValue.equals("")){
                return null;
            }
            
            for(E e : enumType.getEnumConstants()){
                if(e.toString().equals(stringValue)){
                    return e;
                }
            }
        }
        
        throw new IllegalStateException("Invalid enum value '" + value + "' of type '" + enumType.getName() + "'");
	}
	
	public static <E extends Enum<?>> E nameOf(Class<E> enumType,String name) throws IllegalStateException{
		return nameOf(enumType,name,true);
	}
	
	public static <E extends Enum<?>> E nameOf(Class<E> enumType,String name, boolean ignorecase) throws IllegalStateException{
        if(Strings.isEmpty(name)){
            return null;
        }
        
        for(E e : enumType.getEnumConstants()){
            if(ignorecase ? e.name().equalsIgnoreCase(name) : e.name().equals(name)){
                return e;
            }
        }
        
        throw new IllegalStateException("Invalid enum name '" + name + "' of type '" + enumType.getName() + "'");
	}

    public static String[] getValues(Class<?> enumType) {
        ReflectEnum reflectEnum = ReflectEnum.of(enumType);

        String[] values = new String[enumType.getEnumConstants().length];

        for(int i=0;i<values.length;i++) {
            Object e = enumType.getEnumConstants()[i];
            String v = reflectEnum.isValued() ? String.valueOf(reflectEnum.getValue(e)) : e.toString();

            values[i] = v;
        }

        return values;
    }
	
	protected Enums(){
		
	}
}
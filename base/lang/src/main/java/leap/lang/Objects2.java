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
package leap.lang;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 * null safe utils for {@link Object}
 */
public class Objects2 {
	
    public static final int    HASH_SEED   = 17;
    public static final int    HASH_OFFSET = 37;
    public static final String NULL        = "null";
	
	@SuppressWarnings("rawtypes")
    public static boolean isEmpty(Object value){
		if(null == value){
			return true;
		}
		
		if(value instanceof CharSequence){
			return ((CharSequence) value).length() == 0;
		}
		
		if(value instanceof Object[]){
			return ((Object[]) value).length == 0;
		}
		
		if(value instanceof Collection){
			return ((Collection) value).size() == 0;
		}
		
		if(value instanceof Map){
			return ((Map) value).isEmpty();
		}
		
		if(value instanceof Emptiable){
			return ((Emptiable) value).isEmpty();
		}
		
		if(value.getClass().isArray()){
			return Array.getLength(value) == 0;
		}
		
		return false;
	}
	
	public static String toStringOrEmpty(Object o){
		return Objects.toString(o, Strings.EMPTY);
	}

	public static <T> T firstNotNull(T first, T second) {
		return null != first ? first : second;
	}
	
	/**
	 * Convert the given array (which may be a primitive array) to an
	 * object array (if necessary of primitive wrapper objects).
	 * <p>A {@code null} source value will be converted to an
	 * empty Object array.
	 * @param array the (potentially primitive) array
	 * @return the corresponding object array (never {@code null})
	 * @throws IllegalArgumentException if the parameter is not an array
	 */
	public static Object[] toObjectArray(Object array) {
		if (array instanceof Object[]) {
			return (Object[]) array;
		}
		if (array == null) {
			return new Object[0];
		}
		if (!array.getClass().isArray()) {
			throw new IllegalArgumentException("Source is not an array: " + array);
		}
		int length = Array.getLength(array);
		if (length == 0) {
			return new Object[0];
		}
		Class<?> wrapperType = Array.get(array, 0).getClass();
		Object[] newArray = (Object[]) Array.newInstance(wrapperType, length);
		for (int i = 0; i < length; i++) {
			newArray[i] = Array.get(array, i);
		}
		return newArray;
	}	
	
	/**
	 * Return a hex String form of an object's identity hash code.
	 * @param obj the object
	 * @return the object's identity code in hex notation
	 */
	//from spring framework
	public static String getIdentityHexString(Object obj) {
		return Integer.toHexString(System.identityHashCode(obj));
	}	
	
    public static int hashCode(final int seed, final int hashcode) {
        return seed * HASH_OFFSET + hashcode;
    }

    public static int hashCode(final int seed, final Object obj) {
        return hashCode(seed, obj != null ? obj.hashCode() : 0);
    }
    
    public static int hashCode(final int seed, final boolean b) {
        return hashCode(seed, b ? 1 : 0);
    }
	
	protected Objects2(){
		
	}
}

/*
 * Copyright 2014 the original author or authors.
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
package leap.core.value;

import java.util.List;

import leap.lang.Emptiable;
import leap.lang.convert.ConvertException;
import leap.lang.exception.EmptyElementsException;
import leap.lang.exception.TooManyElementsException;

/**
 * Represents scalar values.
 */
public interface Scalars extends Emptiable {
	
	/**
	 * Returns the size of returned scalar values.
	 */
	int size();
	
	/**
	 * Returns the first scalar value as an {@link Object}.
	 * 
	 * @throws EmptyElementsException if no values.
	 */
	Object first() throws EmptyElementsException;
	
	/**
	 * Returns the first scalar value or <code>null</code> if no any values.
	 */
	Object firstOrNull();
	
	/**
	 * Returns the first scalar value as an {@link Object}.
	 * 
	 * @throws EmptyElementsException if not values.
	 * @throws TooManyElementsException if there are two or more values exists.
	 */
	Object single() throws EmptyElementsException,TooManyElementsException;
	
	/**
	 * Returns the first scalar value or <code>null</code> if no any values.
	 * 
	 * @throws TooManyElementsException if there are two or more values exists.
	 */
	Object singleOrNull() throws TooManyElementsException;

	/**
	 * Returns all the scalar values as an immutable {@link List}.
	 */
	List<Object> list();
	
	/**
	 * Returns all the scalar values as an immutable {@link List}.
	 * 
	 * @throws ConvertException if cannot converts the values to the element type.
	 */
	<T> List<T> list(Class<T> elementType) throws ConvertException;
	
    /**
     * Returns an array object contains all the scalar values.
     * 
     * @throws ConvertException if cannot converts the values to the component type.
     */
    Object arrayObject(Class<?> componentType) throws ConvertException;
    
	/**
	 * Returns all the scalar valeus as an object array.
	 */
	default Object[] array() {
		List<Object> list = list();
		return list.toArray(new Object[list.size()]);
	}
	
	/**
	 * Returns all the scalar values as an array.
	 * 
	 * @throws ConvertException if cannot converts the values to the component type.
	 */
    @SuppressWarnings("unchecked")
    default <T> T[] arrray(Class<T> componentType) throws ConvertException {
    	return (T[])arrayObject(componentType);
    }
    
    /**
     * Returns all the scalar values as an int[] array.
     * 
     * @throws ConvertException if cannot converts the values to int.
     */
    default int[] intArray() throws ConvertException {
    	return (int[])arrayObject(int.class);
    }
    
    /**
     * Returns all the scalar values as an long[] array.
     * 
     * @throws ConvertException if cannot converts the values to long.
     */
    default long[] longArray() throws ConvertException {
    	return (long[])arrayObject(long.class);
    }
}

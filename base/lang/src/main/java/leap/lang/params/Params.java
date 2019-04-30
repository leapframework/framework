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
package leap.lang.params;

import leap.lang.Emptiable;
import leap.lang.accessor.NamedGetter;
import leap.lang.beans.DynaBean;

import java.util.Collection;
import java.util.Map;

/**
 * Case insensitive interface to get and set parameters.
 * 
 * <p>
 * Note : the implementation must guarantee case insensitive.
 */
public interface Params extends Emptiable, NamedGetter {
	
	/**
	 * Returns an readonly empty params.
	 */
	static EmptyParams empty() {
		return EmptyParams.INSTANCE;
	}
	
	/**
	 * Returns a {@link BeanParams} contains all the properties in the given bean..
	 */
	static BeanParams bean(Object bean) {
		return new BeanParams(bean);
	}
	
	/**
	 * Returns an {@link ArrayParams} contains all the values in the given array.
	 */
	static ArrayParams array(Object... values) {
		return new ArrayParams(values);
	}
	
	/**
	 * Returns an {@link ArrayParams} contains all the values in the given array.
	 */
	static ArrayParams of(Object[] array) {
		return new ArrayParams(array);
	}
	
	/**
	 * Returns an {@link ArrayParams} contains all the elements in the given collection.
	 */
	static ArrayParams of(@SuppressWarnings("rawtypes") Collection c) {
		return new ArrayParams(null == c ? null : c.toArray());
	}
	
	/**
	 * Returns a {@link MapParams} contains the given parameter name and value.
	 */
	static MapParams of(String name,Object value) {
		return (MapParams)new MapParams().set(name, value);
	}
	
	/**
	 * Returns a {@link MapParams} contains the given parameters.
	 */
	static MapParams of(String name1,Object value1,String name2,Object value2) {
		return (MapParams)new MapParams().set(name1, value1).set(name2, value2);
	}
	
	/**
	 * Returns a {@link MapParams} contains the given parameters.
	 */
	static MapParams of(String name1,Object value1,String name2,Object value2,String name3,Object value3) {
		return (MapParams)new MapParams().set(name1, value1).set(name2, value2).set(name3, value2);
	}
	
	/**
	 * Returns a {@link MapParams} contains all the key values in the given map.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
    static MapParams of(Map map) {
		return new MapParams(map);
	}
	
	/**
	 * Returns a {@link MapParams} contains all the properties in the given bean.
	 */
	static MapParams of(DynaBean bean) {
		return null == bean ? new MapParams() : new MapParams(bean.getProperties());
	}
	
	/**
	 * Returns a {@link Map} object contains all the parameters.
	 */
	Map<String,Object> map();

	/**
	 * Update parameter's value in the underlying data object, such as {@link Map} or java bean. 
	 */
	Params set(String name,Object value);
	
	/**
	 * Set all parameters from the given map.
	 */
	Params setAll(Map<String,? extends Object> m);

    /**
     * Returns <code>true</code> if this params object is a named params.
     *
     * <p>
     * A named params can use {@link #get(String)} to get parameter's value.
     */
    default boolean isNamed() {
        return true;
    }
	
	/**
	 * Returns <code>true</code> if this params object is readonly.
	 */
	default boolean isReadonly() {
		return false;
	}
	
	/**
	 * Returns <code>true</code> if this params object is an indexed params.
	 * 
	 * <p>
	 * An indexed params can used {@link #get(int)} to get parameter's value.
	 */
	default boolean isIndexed() {
        return false;
    }

	/**
	 * Returns true if this params is an array.
	 */
	default boolean isArray() {
		return false;
	}

	/**
	 * Returns the max index if this params is indexed.
	 * 
	 * <p>
	 * Returns -1 if this params is not indexed.
	 */
	default int maxIndex() {
        return -1;
    }
	
	/**
	 * Index starts from 0.
	 * 
	 * @throws IllegalStateException if not an indexed parameters.
	 */
	default Object get(int i) throws IllegalStateException,IndexOutOfBoundsException {
        throw new IllegalStateException("Not an indexed parameters");
    }
}
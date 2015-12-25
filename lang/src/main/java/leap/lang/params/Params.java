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

import java.util.Collection;
import java.util.Map;

import leap.lang.Emptiable;
import leap.lang.beans.DynaBean;

/**
 * Case insensitive interface to get and set parameters.
 * 
 * <p>
 * Note : the implementation must guarantee case insensitive.
 */
public interface Params extends Emptiable {
	
	/**
	 * Returns an readonly empty params.
	 */
	public static EmptyParams empty() {
		return EmptyParams.INSTANCE;
	}
	
	/**
	 * Returns a {@link BeanParams} contains all the properties in the given bean..
	 */
	public static BeanParams bean(Object bean) {
		return new BeanParams(bean);
	}
	
	/**
	 * Returns an {@link ArrayParams} contains all the values in the given array.
	 */
	public static ArrayParams array(Object... values) {
		return new ArrayParams(values);
	}
	
	/**
	 * Returns an {@link ArrayParams} contains all the values in the given array.
	 */
	public static ArrayParams of(Object[] array) {
		return new ArrayParams(array);
	}
	
	/**
	 * Returns an {@link ArrayParams} contains all the elements in the given collection.
	 */
	public static ArrayParams of(@SuppressWarnings("rawtypes") Collection c) {
		return new ArrayParams(null == c ? null : c.toArray());
	}
	
	/**
	 * Returns a {@link MapParams} contains the given parameter name and value.
	 */
	public static MapParams of(String name,Object value) {
		return (MapParams)new MapParams().set(name, value);
	}
	
	/**
	 * Returns a {@link MapParams} contains the given parameters.
	 */
	public static MapParams of(String name1,Object value1,String name2,Object value2) {
		return (MapParams)new MapParams().set(name1, value1).set(name2, value2);
	}
	
	/**
	 * Returns a {@link MapParams} contains the given parameters.
	 */
	public static MapParams of(String name1,Object value1,String name2,Object value2,String name3,Object value3) {
		return (MapParams)new MapParams().set(name1, value1).set(name2, value2).set(name3, value2);
	}
	
	/**
	 * Returns a {@link MapParams} contains all the key values in the given map.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
    public static MapParams of(Map map) {
		return new MapParams(map);
	}
	
	/**
	 * Returns a {@link MapParams} contains all the properties in the given bean.
	 */
	public static MapParams of(DynaBean bean) {
		return null == bean ? new MapParams() : new MapParams(bean.getProperties());
	}
	
	/**
	 * Returns a {@link Map} object contains all the parameters.
	 */
	Map<String,Object> map();

	/**
	 * Returns <code>true</code> if this parameters contains the given parameter name .
	 * 
	 * <p>
	 * Returns <code>false</code> if the parameter not exists.
	 */
	boolean contains(String name);
	
	/**
	 * Returns the parameter's value of the given name.
	 * 
	 * <p>
	 * Returns <code>null</code> if the parameter not exists or the parameter's value is <code>null</code>
	 */
	Object get(String name);
	
	/**
	 * Update parameter's value in the underlying data object, such as {@link Map} or java bean. 
	 */
	Params set(String name,Object value);
	
	/**
	 * Set all parameters from the given map.
	 */
	Params setAll(Map<String,? extends Object> m);
	
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
	boolean isIndexed();
	
	/**
	 * Returns <code>true</code> if this params object is a named params.
	 * 
	 * <p>
	 * A named params can use {@link #get(String)} to get parameter's value.
	 */
	boolean isNamed();
	
	/**
	 * Returns the max index if this params is indexed.
	 * 
	 * <p>
	 * Returns -1 if this params is not indexed.
	 */
	int maxIndex();
	
	/**
	 * Index starts from 0.
	 * 
	 * @throws IllegalStateException if not an indexed parameters.
	 */
	Object get(int i) throws IllegalStateException,IndexOutOfBoundsException;
}
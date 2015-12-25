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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import leap.lang.annotation.Nullable;

public class New {
	
	/**
	 * Returns a new array of the given length with the specified component type.
	 * 
	 * @param type the component type
	 * @param length the length of the new array
	 */
	@SuppressWarnings("unchecked")
    public static <T> T[] array(Class<T> type, int length) {
		return (T[]) Array.newInstance(type, length);
	}
	
	/**
	 * Create a new empty {@link ArrayList}.
	 */
	public static <E> ArrayList<E> arrayList() {
		return new ArrayList<E>();
	}
	
    /**
     * Create a new {@link ArrayList} and wrapped by {@link Collections#unmodifiableList(java.util.List)}.
     */
    public static <E> List<E> unmodifiableArrayList(Collection<E> c){
    	return Collections.unmodifiableList(null == c ? new ArrayList<E>() : new ArrayList<E>(c));
    }

    /**
     * Create a new {@link ArrayList}.
     */
	@SuppressWarnings("unchecked")
    public static <E> ArrayList<E> arrayList(E... elements) {
        ArrayList<E> list = new ArrayList<E>(elements.length);
        if(null != elements){
            for(E e : elements){
            	list.add(e);
            }
        }
        return list;
    }
    
    /**
     * Create a new {@link ArrayList}.
     */
    public static <E> ArrayList<E> arrayList(@Nullable Iterable<? extends E> elements) {
        ArrayList<E> list = new ArrayList<E>();
        if(null != elements){
            for(E e : elements){
            	list.add(e);
            }
        }
        return list;
    }
    
    /**
     * Create a new {@link ArrayList}.
     */
    public static <E> ArrayList<E> arrayList(@Nullable Iterator<? extends E> elements) {
        ArrayList<E> list = new ArrayList<E>();
        if(null != elements){
            while(elements.hasNext()){
            	list.add(elements.next());
            }
        }
        return list;
    } 
    
    /**
     * Create a new {@link HashSet}
     */
    @SuppressWarnings("unchecked")
    public static <E> HashSet<E> hashSet(@Nullable E... elements){
    	HashSet<E> set = new HashSet<E>();
        if(null != elements){
            for(E e : elements){
            	set.add(e);
            }
        }
        return set;
    }
    
    /**
     * Create a new {@link HashSet}.
     */
    public static <E> HashSet<E> hashSet(@Nullable Iterable<? extends E> elements) {
    	HashSet<E> set = new HashSet<E>();
        if(null != elements){
            for(E e : elements){
            	set.add(e);
            }
        }
        return set;
    }
    
    /**
     * Create a new {@link HashSet}.
     */
    public static <E> HashSet<E> hashSet(@Nullable Iterator<? extends E> elements) {
    	HashSet<E> set = new HashSet<E>();
        if(null != elements){
            while(elements.hasNext()){
            	set.add(elements.next());
            }
        }
        return set;
    }
    
    /**
     * Create a new {@link LinkedHashSet}.
     */
    @SuppressWarnings("unchecked")
    public static <E> LinkedHashSet<E> linkedHashSet(@Nullable E... elements) {
    	LinkedHashSet<E> set = new LinkedHashSet<E>();
        if(null != elements){
            for(E e : elements){
            	set.add(e);
            }
        }
        return set;
    } 
    
    /**
     * Create a new {@link LinkedHashSet}.
     */
    public static <E> LinkedHashSet<E> linkedHashSet(@Nullable Iterable<? extends E> elements) {
    	LinkedHashSet<E> set = new LinkedHashSet<E>();
        if(null != elements){
            for(E e : elements){
            	set.add(e);
            }
        }
        return set;
    }
    
    /**
     * Create a new {@link LinkedHashSet}.
     */
    public static <E> LinkedHashSet<E> linkedHashSet(@Nullable Iterator<? extends E> elements) {
    	LinkedHashSet<E> set = new LinkedHashSet<E>();
        if(null != elements){
            while(elements.hasNext()){
            	set.add(elements.next());
            }
        }
        return set;
    }
    
    /**
     * Create a new {@link HashMap}
     */
    public static <K,V> HashMap<K,V> hashMap(){
    	return new HashMap<K, V>();
    }
    
    /**
     * Create a new {@link HashMap} and wrapped by {@link Collections#unmodifiableMap(java.util.Map)}.
     */
    public static <K,V> Map<K,V> unmodifiableHashMap(Map<K, V> m){
    	return Collections.unmodifiableMap(new HashMap<K, V>(m));
    }
    
    /**
     * Create a new {@link HashMap}
     */
    public static <K,V> HashMap<K,V> hashMap(K k1,V v1){
    	HashMap<K, V> map = new HashMap<K, V>();
    	
    	map.put(k1, v1);
    	
    	return map;
    }
    
    /**
     * Create a new {@link HashMap}
     */
    public static <K,V> HashMap<K,V> hashMap(K k1,V v1,K k2,V v2){
    	HashMap<K, V> map = new HashMap<K, V>();
    	
    	map.put(k1, v1);
    	map.put(k2, v2);
    	
    	return map;
    }
    
    /**
     * Create a new {@link LinkedHashMap}
     */
    public static <K,V> LinkedHashMap<K,V> linkedHashMap(){
    	return new LinkedHashMap<K, V>();
    }
    
    /**
     * Create a new {@link LinkedHashMap} and wrapped by {@link Collections#unmodifiableMap(java.util.Map)}.
     */
    public static <K,V> Map<K,V> unmodifiableLinkedHashMap(Map<K, V> m){
    	return Collections.unmodifiableMap(new LinkedHashMap<K, V>(m));
    }
    
    /**
     * Create a new {@link LinkedHashMap}
     */
    public static <K,V> LinkedHashMap<K,V> linkedHashMap(K k1,V v1){
    	LinkedHashMap<K, V> map = new LinkedHashMap<K, V>();
    	
    	map.put(k1, v1);
    	
    	return map;
    }
    
    /**
     * Create a new {@link LinkedHashMap}
     */
    public static <K,V> LinkedHashMap<K,V> linkedHashMap(K k1,V v1,K k2,V v2){
    	LinkedHashMap<K, V> map = new LinkedHashMap<K, V>();
    	
    	map.put(k1, v1);
    	map.put(k2, v2);
    	
    	return map;
    }
	
	protected New(){
		
	}
}
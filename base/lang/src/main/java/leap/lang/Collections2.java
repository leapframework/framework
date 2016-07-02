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

import java.util.Collection;

/**
 * null safe utils for {@link Collection}
 */
public class Collections2 {
	
	public static boolean isEmpty(Collection<?> c){
		return null == c || c.isEmpty();
	}
	
	public static boolean isNotEmpty(Collection<?> c){
		return null != c && !c.isEmpty();
	}
	
	public static boolean contains(Collection<?> collection, Object object) {
		if(null == collection){
			return false;
		}
		try {
			return collection.contains(object);
		} catch (ClassCastException e) {
			return false;
		} catch (NullPointerException e) {
			return false;
		}
	}
	
	public static <E> void addAll(Collection<? super E> collection,E[] elements){
		if(null == collection || null == elements){
			return;
		}
		
		for(int i=0;i<elements.length;i++){
			collection.add(elements[i]);
		}
	}
	
	public static <E> void addAll(Collection<? super E> collection,Iterable<E> elements){
		if(null == collection || null == elements){
			return;
		}
		
		for(E element : elements){
			collection.add(element);
		}
	}
	
	public static <E> boolean addIfNotNull(Collection<? super E> collection,E element){
		if(null == collection || null == element){
			return false;
		}
		collection.add(element);
		return true;
	}
	
	public static boolean addIfNotEmpty(Collection<String> stringCollection,String stringElement, boolean trim){
		if(null == stringCollection || null == stringElement){
			return false;
		}
		
		if(trim){
			stringElement = stringElement.trim();
		}
		
		if(Strings.EMPTY.equals(stringElement)){
			return false;
		}
		
		stringCollection.add(stringElement);
		
		return true;
	}
	
	public static String[] toStringArray(Collection<String> c){
		if(null == c || c.isEmpty()){
			return Arrays2.EMPTY_STRING_ARRAY;
		}
		return c.toArray(new String[c.size()]);
	}
	
	protected Collections2(){
		
	}
}
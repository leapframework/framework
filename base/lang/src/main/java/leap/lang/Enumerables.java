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
import java.util.List;

import leap.lang.collection.ArrayIterable;
import leap.lang.collection.ArrayObjectIterable;
import leap.lang.collection.CollectionEnumerable;
import leap.lang.collection.EmptyIterable;
import leap.lang.collection.IterableEnumerable;
import leap.lang.collection.ListEnumerable;

/**
 * null safe utils for {@link Enumerable} objects.
 */
public class Enumerables {
	
	@SuppressWarnings("rawtypes")
    public static final Enumerable EMPTY = new EmptyIterable();

	@SuppressWarnings("unchecked")
    public static final <E> Enumerable<E> empty(){
		return (Enumerable<E>)EMPTY;
	}
	
	@SuppressWarnings("unchecked")
    public static final <E> Enumerable<E> of(Object object) throws IllegalArgumentException {
		if(null == object){
			return empty();
		}
		
		if(object instanceof Enumerable){
			return (Enumerable<E>)object;
		}
		
		if(object instanceof List){
			return new ListEnumerable<E>((List<E>)object);
		}
		
		if(object instanceof Collection){
			return new CollectionEnumerable<>((Collection<E>)object);
		}
		
		Class<?> clazz = object.getClass();
		if(clazz.isArray()){
			if(clazz.getComponentType().isPrimitive()){
				return new ArrayObjectIterable<E>(object);
			}else{
				return ofArray((E[])object);	
			}
		}
		
		if(object instanceof Iterable){
			return new IterableEnumerable<E>((Iterable<E>)object);
		}		
		
		throw new IllegalArgumentException("not a supported enumerable type '" + object.getClass().getName() + "'");
	}
	
	@SuppressWarnings("unchecked")
    public static final <E> Enumerable<E> ofArray(E... array){
		return null == array || array.length == 0 ? (Enumerable<E>)EMPTY : new ArrayIterable<E>(array);
	}
	
	protected Enumerables(){
		
	}
}

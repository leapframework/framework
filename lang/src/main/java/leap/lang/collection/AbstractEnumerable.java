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
package leap.lang.collection;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import leap.lang.Enumerable;
import leap.lang.Iterables;
import leap.lang.New;
import leap.lang.exception.EmptyElementsException;
import leap.lang.exception.TooManyElementsException;

public abstract class AbstractEnumerable<E> implements Enumerable<E>{

	@Override
    public E first() throws EmptyElementsException {
	    if(size() == 0){
	    	throw new EmptyElementsException();
	    }
	    return get(0);
    }

	@Override
    public E firstOrNull() {
	    if(size() == 0){
	    	return null;
	    }
	    return get(0);
    }
	
	@Override
    public E firstOrNull(Predicate<? super E> predicate) {
	    return Iterables.firstOrNull(this, predicate);
    }

	@Override
    public E single() throws EmptyElementsException, TooManyElementsException {
		int size = size();
		
	    if(size == 0){
	    	throw new EmptyElementsException();
	    }
	    if(size > 1){
	    	throw new TooManyElementsException();
	    }
	    return get(0);
    }

	@Override
    public E singleOrNull() throws TooManyElementsException {
		int size = size();
		
	    if(size == 0){
	    	return null;
	    }
	    if(size > 1){
	    	throw new TooManyElementsException();
	    }
	    return get(0);
    }

    @Override
    public Object[] toArray() {
	    return Iterables.toArray(this);
    }
    
	@Override
    public E[] toArray(Class<E> type) {
	    return Iterables.toArray(this, type);
    }

	@Override
    public List<E> toList() {
	    return New.arrayList(this);
    }

	@Override
    public Set<E> toSet() {
	    return New.linkedHashSet(this);
    }
}

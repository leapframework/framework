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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import leap.lang.Arrays2;
import leap.lang.Enumerable;
import leap.lang.exception.EmptyElementsException;
import leap.lang.exception.TooManyElementsException;

@SuppressWarnings({"unchecked","rawtypes"})
public final class EmptyIterable<E> implements Iterable<E>,Enumerable<E>{
	
    private static EmptyIterable INSTANCE = new EmptyIterable();
	
    public static final <T> EmptyIterable<T> instance(){
		return (EmptyIterable<T>)INSTANCE;
	}
	
	private EmptyIterator<E> iterator = new EmptyIterator<E>();

	public Iterator<E> iterator() {
	    return iterator;
    }

	@Override
    public boolean isEmpty() {
	    return true;
    }

	@Override
    public int size() {
	    return 0;
    }

	@Override
    public E get(int index) throws IndexOutOfBoundsException {
	    throw new IndexOutOfBoundsException("invalid index '" + index + "' for empty collection");
    }

	@Override
    public boolean contains(Object element) {
	    return false;
    }

	@Override
    public E first() throws EmptyElementsException {
	    throw new EmptyElementsException();
    }

	@Override
    public E firstOrNull() {
	    return null;
    }
	
	@Override
    public E firstOrNull(Predicate<? super E> predicate) {
	    return null;
    }

	@Override
    public E single() throws EmptyElementsException, TooManyElementsException {
	    throw new EmptyElementsException();
    }

	@Override
    public E singleOrNull() throws TooManyElementsException {
	    return null;
    }

	@Override
    public Object[] toArray() {
	    return Arrays2.EMPTY_OBJECT_ARRAY;
    }
	
	@Override
    public E[] toArray(Class<E> type) {
	    return (E[])Arrays2.EMPTY_OBJECT_ARRAY;
    }

	@Override
    public List<E> toList() {
	    return new ArrayList<E>(1);
    }

	@Override
    public Set<E> toSet() {
	    return new LinkedHashSet<E>(1);
    }
}

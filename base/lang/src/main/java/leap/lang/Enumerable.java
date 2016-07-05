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
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import leap.lang.collection.ListEnumerable;
import leap.lang.exception.EmptyElementsException;
import leap.lang.exception.TooManyElementsException;

/**
 * a {@link Iterable} sub interface provides some extra useful methods.
 */
public interface Enumerable<E> extends Iterable<E>, Emptiable {

	/**
	 * @see Collection#size()
	 */
	int size();
	
	/**
	 * @see List#get(int)
	 */
	E get(int index) throws IndexOutOfBoundsException;
	
	/**
	 * @see Collection#contains(Object)
	 */
	boolean contains(Object element);
	
	/**
	 * Returns the first element in this enumerable if element(s) exists.
	 * 
	 * <p/>
	 * 
	 * if no element(s) exists, throw {@link EmptyElementsException}
	 */
	E first() throws EmptyElementsException;
	
	/**
	 * Returns the first element in this enumerable if element(s) exists.
	 * 
	 * <p/>
	 * 
	 * if no element(s) exists, an null will be returned. 
	 */
	E firstOrNull();
	
	/**
	 * Returns the first element in this enumerable that satisfies the given predicate, if such an element exists.
	 * 
	 * <p/>
	 * 
	 * If no such element is found, an null will be returned.
	 */
	E firstOrNull(Predicate<? super E> predicate);
	
	/**
	 * Returns the first element in this enumerable if only one element exists.
	 * 
	 * <p/>
	 * 
	 * if no element(s) exists, throw {@link EmptyElementsException}.
	 * 
	 * <p/>
	 * 
	 * if two or more elements exists, throw {@link TooManyElementsException}
	 */
	E single() throws EmptyElementsException,TooManyElementsException;
	
	/**
	 * Returns the first element in this enumerable if one element exists.
	 * 
	 * <p/>
	 * 
	 * if no element(s) exists, return <code>null</code>
	 * 
	 * <p/>
	 * 
	 * if two or more elements exists, throw {@link TooManyElementsException}
	 */
	E singleOrNull() throws TooManyElementsException;
	
    /**
     * Returns a sequential {@code Stream} with this enumerable as its source.
     *
     * <p>
     * The default implementation creates a sequential {@code Stream} from the
     * collection's {@code Spliterator}.
     *
     * @return a sequential {@code Stream} over the elements in this enumerable
     */
    default Stream<E> stream() {
        return StreamSupport.stream(spliterator(), false);
    }
    
    /**
     * Returns a possibly parallel {@code Stream} with this enumerable as its
     * source.  It is allowable for this method to return a sequential stream.
     *
     * <p>
     * The default implementation creates a parallel {@code Stream} from the
     * enumerable's {@code Spliterator}.
     *
     * @return a possibly parallel {@code Stream} over the elements in this
     * enumerable
     */
    default Stream<E> parallelStream() {
        return StreamSupport.stream(spliterator(), true);
    }
	
    /**
     * Combines this enumerable and other enumerable and returns a new enumerable object contains all elements.
     */
    default Enumerable<E> concat(Enumerable<E> other) {
    	List<E> list = toList();
    	if(null != other){
    		list.addAll(other.toList());	
    	}
    	return new ListEnumerable<E>(list);
    }
    
	/**
	 * converts this {@link Enumerable} to a new array containing all of the elements.
	 */
	Object[] toArray();
	
	/**
	 * converts this {@link Enumerable} to a new array containing all of the elements,
	 */
	E[] toArray(Class<E> type);
	
	/**
	 * converts this {@link Enumerable} to a new created list containing all of the elements.
	 */
	List<E> toList();
	
	/**
	 * converts this {@link Enumerable} to a new created set containing all of the elements.
	 */
	Set<E> toSet();
}
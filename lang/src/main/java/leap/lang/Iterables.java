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
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import leap.lang.annotation.Nullable;


//some codes copy from google guava library 15.0-SNAPSHOT,under Apache License 2.0
public class Iterables {
	
    /**
     * Returns a sequential {@code Stream} with this iterable as its source.
     *
     * <p>
     * The default implementation creates a sequential {@code Stream} from the
     * collection's {@code Spliterator}.
     *
     * @return a sequential {@code Stream} over the elements in this iterable
     */
	public static <T> Stream<T> stream(Iterable<T> iterable) {
		if(null == iterable) {
			return Stream.empty();
		}else{
			return StreamSupport.stream(iterable.spliterator(), false);
		}
	}
	
    /**
     * Returns a possibly parallel {@code Stream} with this iterable as its
     * source.  It is allowable for this method to return a sequential stream.
     *
     * <p>
     * The default implementation creates a parallel {@code Stream} from the
     * enumerable's {@code Spliterator}.
     *
     * @return a possibly parallel {@code Stream} over the elements in this
     * iterable
     */
    public static <T> Stream<T> parallelStream(Iterable<T> iterable) {
		if(null == iterable) {
			return Stream.empty();
		}else{
			return StreamSupport.stream(iterable.spliterator(), true);
		}
    }
	
	/**
	 * Returns the element at the specified position in an iterable.
	 * 
	 * @param position position of the element to return
	 * @return the element at the specified position in {@code iterable}
	 * @throws IndexOutOfBoundsException if {@code position} is negative or greater than or equal to the size of
	 *             {@code iterable}
	 */
	public static <T> T get(Iterable<T> iterable, int position) {
		Args.notNull(iterable);
		return (iterable instanceof List) ? ((List<T>) iterable).get(position) : Iterators.get(iterable.iterator(), position);
	}

	/**
	 * Returns the number of elements in {@code iterable}.
	 */
	public static int size(Iterable<?> iterable) {
		return (iterable instanceof Collection) ? ((Collection<?>) iterable).size() : Iterators.size(iterable.iterator());
	}	
	
	/**
	 * Returns {@code true} if {@code iterable} contains any object for which {@code equals(element)} is true.
	 */
	public static boolean contains(Iterable<?> iterable, @Nullable Object element) {
		if (iterable instanceof Collection) {
			Collection<?> collection = (Collection<?>) iterable;
			return Collections2.contains(collection, element);
		}
		return Iterators.contains(iterable.iterator(), element);
	}
	
	/**
	 * Returns {@code true} if any element in {@code iterable} satisfies the predicate.
	 */
	public static <T> boolean any(Iterable<T> iterable, Predicate<? super T> predicate) {
		return Iterators.any(iterable.iterator(), predicate);
	}	
	
	/**
	 * Returns {@code true} if every element in {@code iterable} satisfies the predicate. If {@code iterable} is empty,
	 * {@code true} is returned.
	 */
	public static <T> boolean all(Iterable<T> iterable, Predicate<? super T> predicate) {
		return Iterators.all(iterable.iterator(), predicate);
	}
	
	public static <T> T firstOrNull(Iterable<T> iterable){
		return Iterators.firstOrNull(iterable.iterator());
	}
	
	/**
	 * Returns the first element in {@code iterable} that satisfies the given predicate,
	 * if such an element exists.If no such element is found, an null will be returned.
	 * 
	 */
	public static <T> T firstOrNull(Iterable<T> iterable, Predicate<? super T> predicate) {
		return Iterators.firstOrNull(iterable.iterator(), predicate);
	}
	
	/**
	 * Converts an iterable into a list. If the iterable is already a list, it is returned. 
	 * Otherwise, an {@link java.util.ArrayList} is created with the contents of the iterable in the same iteration order.
	 */	
	public static <T> List<T> toList(Iterable<T> iterable){
		return iterable instanceof List ? (List<T>)iterable : New.arrayList(iterable);
	}
	
	/**
	 * Copies an iterable's elements into an array.
	 * 
	 * @param iterable the iterable to copy
	 * @return a newly-allocated array into which all the elements of the iterable have been copied
	 */
	public static Object[] toArray(Iterable<?> iterable) {
		return toCollection(iterable).toArray();
	}
	
	/**
	 * Copies an iterable's elements into an array.
	 * 
	 * @param iterable the iterable to copy
	 * @param type the type of the elements
	 * @return a newly-allocated array into which all the elements of the iterable have been copied
	 */
	public static <T> T[] toArray(Iterable<? extends T> iterable, Class<T> type) {
		Collection<? extends T> collection = toCollection(iterable);
		T[] array = New.array(type, collection.size());
		return collection.toArray(array);
	}
	
	/**
	 * Converts an iterable into a collection. If the iterable is already a collection, it is returned. Otherwise, an
	 * {@link java.util.ArrayList} is created with the contents of the iterable in the same iteration order.
	 */
	static <E> Collection<E> toCollection(Iterable<E> iterable) {
		return (iterable instanceof Collection) ? (Collection<E>) iterable : New.arrayList(iterable.iterator());
	}
	
	protected Iterables(){
		
	}
}
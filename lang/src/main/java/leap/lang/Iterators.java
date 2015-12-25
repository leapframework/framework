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

import java.util.Iterator;
import java.util.function.Predicate;

import leap.lang.annotation.Nullable;

//some codes copy from google guava library 15.0-SNAPSHOT,under Apache License 2.0
public class Iterators {
	
	/**
	 * Advances {@code iterator} {@code position + 1} times, returning the element at the {@code position}th position.
	 * 
	 * @param position position of the element to return
	 * @return the element at the specified position in {@code iterator}
	 * @throws IndexOutOfBoundsException if {@code position} is negative or greater than or equal to the number of
	 *             elements remaining in {@code iterator}
	 */
	public static <T> T get(Iterator<T> iterator, int position) {
		checkNonnegative(position);
		int skipped = advance(iterator, position);
		if (!iterator.hasNext()) {
			throw new IndexOutOfBoundsException("position (" + position + ") must be less than the number of elements that remained (" + skipped + ")");
		}
		return iterator.next();
	}	
	
	/**
	 * Returns the number of elements remaining in {@code iterator}. The iterator will be left exhausted: its
	 * {@code hasNext()} method will return {@code false}.
	 */
	public static int size(Iterator<?> iterator) {
		int count = 0;
		while (iterator.hasNext()) {
			iterator.next();
			count++;
		}
		return count;
	}
	
	/**
	 * Returns {@code true} if {@code iterator} contains {@code element}.
	 */
	public static boolean contains(Iterator<?> iterator, @Nullable Object element) {
		return any(iterator, Predicates.equalTo(element));
	}
	
	/**
	 * Returns {@code true} if one or more elements returned by {@code iterator} satisfy the given predicate.
	 */
	public static <T> boolean any(Iterator<T> iterator, Predicate<? super T> predicate) {
		return indexOf(iterator, predicate) != -1;
	}
	
	/**
	 * Returns {@code true} if every element returned by {@code iterator} satisfies the given predicate. If
	 * {@code iterator} is empty, {@code true} is returned.
	 */
	public static <T> boolean all(Iterator<T> iterator, Predicate<? super T> predicate) {
		Args.notNull(predicate);
		while (iterator.hasNext()) {
			T element = iterator.next();
			if (!predicate.test(element)) {
				return false;
			}
		}
		return true;
	}
	
	public static <T> T firstOrNull(Iterator<T> iterator){
		Args.notNull(iterator);
		if(iterator.hasNext()){
			return iterator.next();
		}
		return null;
	}
	
	/**
	 * Returns the first element in {@code iterator} that satisfies the given predicate,if such an element exists. 
	 * If no such element is found, an null will be returned from this method and the iterator will be left exhausted: 
	 * its {@code hasNext()} method will return {@code false}.
	 */
	public static <T> T firstOrNull(Iterator<T> iterator, Predicate<? super T> predicate) {
		Args.notNull(iterator);
		Args.notNull(predicate);
		
		while(iterator.hasNext()){
			T element = iterator.next();
			if(predicate.test(element)){
				return element;
			}
		}
		
		return null;
	}
	
	/**
	 * Returns the index in {@code iterator} of the first element that satisfies the provided {@code predicate}, or
	 * {@code -1} if the Iterator has no such elements.
	 * 
	 * <p>
	 * More formally, returns the lowest index {@code i} such that {@code predicate.apply(Iterators.get(iterator, i))}
	 * returns {@code true}, or {@code -1} if there is no such index.
	 * 
	 * <p>
	 * If -1 is returned, the iterator will be left exhausted: its {@code hasNext()} method will return {@code false}.
	 * Otherwise, the iterator will be set to the element which satisfies the {@code predicate}.
	 */
	public static <T> int indexOf(Iterator<T> iterator, Predicate<? super T> predicate) {
		Args.notNull(predicate, "predicate");
		for (int i = 0; iterator.hasNext(); i++) {
			T current = iterator.next();
			if (predicate.test(current)) {
				return i;
			}
		}
		return -1;
	}
	
	static int advance(Iterator<?> iterator, int numberToAdvance) {
		Args.notNull(iterator);
		Args.assertTrue(numberToAdvance >= 0, "numberToAdvance");

		int i;
		for (i = 0; i < numberToAdvance && iterator.hasNext(); i++) {
			iterator.next();
		}
		return i;
	}

	static void checkNonnegative(int position) {
		if (position < 0) {
			throw new IndexOutOfBoundsException("position (" + position + ") must not be negative");
		}
	}	
	
	protected Iterators(){
		
	}
}

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

import java.lang.reflect.Array;
import java.util.Iterator;

import leap.lang.Args;
import leap.lang.Arrays2;
import leap.lang.Enumerable;
import leap.lang.Immutable;

@SuppressWarnings("unchecked")
public class ArrayObjectIterable<E> extends AbstractEnumerable<E> implements Iterable<E>,Immutable,Enumerable<E> {
	
	public static <E> ArrayObjectIterable<E> of(Object array){
		return new ArrayObjectIterable<E>(array);
	}

	protected final Object array;
	protected final int    length;

	public ArrayObjectIterable(Object array) {
		Args.notNull(array,"array object");
		this.array  = array;
		this.length = Array.getLength(array);
	}

	public Iterator<E> iterator() {
		return new ArrayObjectIterator<E>(array,length);
	}
	
	public int size() {
	    return length;
    }
	
	public boolean isEmpty() {
	    return length <= 0;
    }

    @Override
    public E get(int index) {
	    return (E)Array.get(array, index);
    }

	@Override
    public boolean contains(Object element) {
	    return Arrays2.containsInObjectArray(array, element);
    }
}
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

import java.util.Iterator;

import leap.lang.Arrays2;
import leap.lang.Enumerable;
import leap.lang.Immutable;

@SuppressWarnings("unchecked")
public class ArrayIterable<E> extends AbstractEnumerable<E> implements Iterable<E>,Immutable,Enumerable<E> {
	
	public static <E> ArrayIterable<E> of(E... values){
		return new ArrayIterable<E>(values);
	}

	protected final E[] values;

	public ArrayIterable(E... values) {
		this.values = values;
	}

	public Iterator<E> iterator() {
		return new ArrayIterator<E>(values);
	}
	
	public int size() {
	    return values.length;
    }
	
	public boolean isEmpty() {
	    return values.length == 0;
    }

	@Override
    public E get(int index) {
	    return values[index];
    }

	@Override
    public boolean contains(Object element) {
	    return Arrays2.contains(values, element);
    }

	@Override
    public Object[] toArray() {
	    return Arrays2.copyOf(values);
    }

	@Override
    public E[] toArray(Class<E> type) {
	    return Arrays2.copyOf(values);
    }
}
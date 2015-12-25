/*
 * Copyright 2012 the original author or authors.
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
import java.util.NoSuchElementException;

public class ArrayIterator<E> extends UnmodifiableIterator<E> implements Iterator<E> {

	protected final E[] values;
	protected int current = -1;

	public ArrayIterator(E[] values) {
		this.values = values;
	}

	public boolean hasNext() {
		return current < (values.length - 1);
	}

	public E next() {
		try {
			return values[++current];
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new NoSuchElementException();
		}
	}
}
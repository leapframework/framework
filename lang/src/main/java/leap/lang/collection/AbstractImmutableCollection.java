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

import java.util.Collection;

import leap.lang.Immutable;
import leap.lang.exception.ReadonlyException;

public abstract class AbstractImmutableCollection<E> implements Collection<E>,Immutable {
	
	public boolean add(E o) {
		throw readonlyException();
    }

	public boolean addAll(Collection<? extends E> c) {
		throw readonlyException();
    }

	public void clear() {
		throw readonlyException();
    }

	public boolean remove(Object o) {
		throw readonlyException();
    }

	public boolean removeAll(Collection<?> c) {
		throw readonlyException();
    }
	
	public boolean retainAll(Collection<?> c) {
		throw readonlyException();
    }

	protected static ReadonlyException readonlyException() {
		return new ReadonlyException("unsupported operation, this collection is immutable");
	}
}